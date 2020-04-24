package io.axoniq.demo.bikerental.config.tracing

import io.axoniq.demo.bikerental.Describable
import io.axoniq.demo.bikerental.config.ExpectedException
import io.opentracing.Span
import io.opentracing.SpanContext
import io.opentracing.Tracer
import io.opentracing.propagation.Format
import io.opentracing.tag.Tags
import org.apache.logging.log4j.core.util.Throwables
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ListenerInvocationErrorHandler
import org.axonframework.extensions.tracing.MapExtractor
import org.axonframework.extensions.tracing.MapInjector
import org.axonframework.extensions.tracing.OpenTraceDispatchInterceptor
import org.axonframework.extensions.tracing.OpenTraceHandlerInterceptor
import org.axonframework.extensions.tracing.SpanUtils
import org.axonframework.extensions.tracing.TracingProperties
import org.axonframework.extensions.tracing.TracingProvider
import org.axonframework.extensions.tracing.autoconfig.TracingAutoConfiguration
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.annotation.MessageHandlerInvocationException
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction

@Configuration
@EnableAutoConfiguration(exclude = [TracingAutoConfiguration::class])
@Import(TracingAutoConfiguration.PropertiesConfiguration::class)
class TracingAxonConfig {

    companion object {
        private val log = LoggerFactory.getLogger(TracingAxonConfig::class.java)
    }

    @Configuration
    class Instrumentation {

        companion object {
            const val EVENT_UOW_SPAN_KEY = "span"
        }

        @Bean
        @Order(0)
        fun traceDispatchInterceptor(tracer: Tracer): MessageDispatchInterceptor<Message<*>> =
                object : OpenTraceDispatchInterceptor(tracer) {
                    override fun handle(messages: MutableList<out Message<*>>?)
                            : BiFunction<Int, Message<*>, Message<*>> {
                        return BiFunction { _, message ->
                            val payloadName = message.payloadType.simpleName
                            val span = if (message is EventMessage<*>) {
                                val operationName = try {
                                    (message.payload as Describable).description()
                                } catch (e: ClassCastException) {
                                    log.error("$payloadName does not implement ${Describable::class.simpleName}")
                                    "Publish $payloadName"
                                }
                                //todo check up on https://github.com/AxonFramework/extension-tracing/issues/43
                                SpanUtils.withMessageTags(tracer.buildSpan(operationName), message)
                                        .withTag(Tags.SPAN_KIND.key, Tags.SPAN_KIND_CLIENT)
                                        .start()
                            } else {
                                //todo check up on https://github.com/AxonFramework/extension-tracing/issues/46
                                //parent must be present since it triggered the dispatching process
                                tracer.activeSpan()!!//.setOperationName("Send $payloadName")
                            }


                            span.setTag("axon.message.payload", message.payload.toString())

                            if (message is EventMessage<*>) {
                                CurrentUnitOfWork.get().onCleanup { span.finish() }
                                return@BiFunction tracer.activateSpan(span).use { message.andSpanMetaData(tracer)!! }
                            }

                            message.andSpanMetaData(tracer)!!
                        }
                    }
                }

        @Bean
        @Order(0)
        fun traceHandlerInterceptor(tracer: Tracer, tracingProperties: TracingProperties): MessageHandlerInterceptor<Message<*>> =
                object : OpenTraceHandlerInterceptor(tracer, tracingProperties) {
                    override fun handle(
                            unitOfWork: UnitOfWork<out Message<*>>,
                            interceptorChain: InterceptorChain
                    ): Any? {
                        val operationNamePrefix =
                                when (unitOfWork.message) {
                                    is CommandMessage<*> -> tracingProperties.handle.operationNamePrefix.command
                                    is QueryMessage<*, *> -> tracingProperties.handle.operationNamePrefix.query
                                    //fixme what prefix to use here?
                                    else -> tracingProperties.handle.operationNamePrefix.command
                                }
                        val operationName = operationNamePrefix + SpanUtils.messageName(unitOfWork.message)

                        //span context must be present in the message metadata for handlers
                        val parentSpan = tracer.extractContextFrom(unitOfWork.message)!!

                        val span = tracer.buildSpan(operationName)
                                .asChildOf(parentSpan)
                                .withTag(Tags.SPAN_KIND.key, Tags.SPAN_KIND_SERVER)
                                .start()
                        val scope = tracer.activateSpan(span)
                        //for queries this would be printed at the end, not very useful
                        if (unitOfWork.message !is QueryMessage<*, *>) {
                            unitOfWork.onPrepareCommit { span.log("finished handling") }
                            if (unitOfWork.message is EventMessage<*>) {
                                /* If we simply embed this Span into this Message
                                 * we would only be able to extract it into SpanContext
                                 * which doesn't allow to change the Span operation name.
                                 * see TracingEventHandlerAspect
                                 */
                                unitOfWork.resources()[EVENT_UOW_SPAN_KEY] = span
                            }
                        }
                        unitOfWork.onCleanup {
                            scope.close()
                            span.finish()
                        }
                        return interceptorChain.proceed()
                    }
                }

        @Bean
        fun tracingProvider(tracer: Tracer): CorrelationDataProvider = TracingProvider(tracer)
    }

    @Configuration
    class Logging {

        @Configuration
        class ErrorLoggingConfig {

            companion object {
                const val TAG_ERROR_EXPECTED = "error.expected"
                const val TAG_ERROR_UNEXPECTED = "error.unexpected"
            }

            @Bean
            @Order(1)
            fun commandExceptionLogger(tracer: Tracer): MessageHandlerInterceptor<CommandMessage<*>> {
                return MessageHandlerInterceptor { unitOfWork, chain ->
                    try {
                        return@MessageHandlerInterceptor chain.proceed()
                    } catch (exception: ExpectedException) {
                        val message = exception.message
                        tracer.markActiveSpanFailed()
                        tracer.log(TAG_ERROR_EXPECTED to message)
                        log.debug("[${tracer.id()}]: ${unitOfWork.messageName()} -- $message")
                        throw exception
                    } catch (exception: Exception) {
                        tracer.markActiveSpanFailed()
                        var msgForTracer = Throwables.getRootCause(exception)
                                .let { it?.message ?: it::class.simpleName }
                        if (exception is MessageHandlerInvocationException) {
                            msgForTracer = "${exception.message}: $msgForTracer"
                        }
                        tracer.log(TAG_ERROR_UNEXPECTED to msgForTracer)
                        log.error("[${tracer.id()}]: ${unitOfWork.messageName()} unexpected error:", exception)
                        throw exception
                    }
                }
            }

            /**
             * By default point-to-point queries do not have exception handling.
             */
            @Bean
            @Order(1)
            fun queryExceptionLogger(tracer: Tracer): MessageHandlerInterceptor<QueryMessage<*, *>> {
                return MessageHandlerInterceptor { unitOfWork, chain ->
                    try {
                        return@MessageHandlerInterceptor chain.proceed()
                    } catch (exception: Exception) {
                        tracer.markActiveSpanFailed()
                        tracer.log(TAG_ERROR_UNEXPECTED to Throwables.getRootCause(exception)
                                .let { it?.message ?: it::class.simpleName }
                        )
                        log.error("[${tracer.id()}]: ${unitOfWork.messageName()} unexpected error:", exception)
                        throw exception
                    }
                }
            }

            @Bean
            @Order(1)
            fun eventExceptionLogger(tracer: Tracer): ListenerInvocationErrorHandler {
                return ListenerInvocationErrorHandler { exception, event, eventHandler ->
                    tracer.markActiveSpanFailed()
                    tracer.log(TAG_ERROR_UNEXPECTED to Throwables.getRootCause(exception)
                            .let { it?.message ?: it::class.simpleName }
                    )
                    log.error("[${tracer.id()}]: " +
                            "Event listener in ${eventHandler.targetType.simpleName
                                    .replace("\\\$\\\$EnhancerBySpring.*".toRegex(), "")} " +
                            "failed to handle ${event.name()}. Continuing processing with next listener", exception)
                }
            }
        }

        @Bean
        fun eventLogger(tracer: Tracer): MessageDispatchInterceptor<EventMessage<*>> {
            return MessageDispatchInterceptor {
                BiFunction { _, event ->
                    try {
                        val description = (event.payload as Describable).description()
                        log.info("[${tracer.id()}]: $description")
                        if (log.isDebugEnabled) {
                            log.debug("[${tracer.id()}]: ${event.payload}")
                        }
                    } catch (e: ClassCastException) {
                        log.error("[${tracer.id()}]: ${event.name()} " +
                                "does not implement ${Describable::class.simpleName}")
                        log.info("[${tracer.id()}]: ${event.payload}")
                    }
                    event
                }
            }
        }

        @Bean
        fun commandLogger(tracer: Tracer): MessageDispatchInterceptor<CommandMessage<*>> {
            return MessageDispatchInterceptor {
                BiFunction { _, command ->
                    if (log.isDebugEnabled) {
                        log.debug("[${tracer.id()}]: ${command.payload}")
                    }
                    command
                }
            }
        }

        @Bean
        fun queryLogger(tracer: Tracer): MessageDispatchInterceptor<QueryMessage<*, *>> {
            return MessageDispatchInterceptor {
                BiFunction { _, query ->
                    if (log.isTraceEnabled) {
                        val payload = query.payload.toString()
                        log.trace("[${tracer.id()}]: $payload")
                    }
                    query
                }
            }
        }

        @Bean
        fun queryLoggerReturnValue(tracer: Tracer): MessageHandlerInterceptor<QueryMessage<*, *>> {
            return MessageHandlerInterceptor { unitOfWork, chain ->
                if (!log.isTraceEnabled) {
                    return@MessageHandlerInterceptor chain.proceed()
                }
                val span = tracer.activeSpan()!!
                val traceId = span.context().toTraceId()
                val logAction: (Any?) -> Unit = {
                    //fixme sometimes we can't see payload in the ui, maybe span is finished earlier then the future?
                    span.setTag("axon.return.message.payload", it.toString())
                    log.trace("[$traceId]: ${unitOfWork.message.payloadType.simpleName} returned: $it")
                }
                val returnValue: Any? = chain.proceed()
                if (returnValue == null) {
                    logAction(null)
                    return@MessageHandlerInterceptor returnValue
                }
                if (returnValue is CompletableFuture<*>) {
                    returnValue.thenAccept(logAction)
                } else {
                    logAction(returnValue)
                }
                return@MessageHandlerInterceptor returnValue
            }
        }
    }
}

fun Tracer.extractContextFrom(message: Message<*>): SpanContext? {
    return extract(
            Format.Builtin.TEXT_MAP,
            MapExtractor(message.metaData)
    )
}

/**
 * returns `null` if there is no active span
 */
@Suppress("UNCHECKED_CAST")
fun <T : Message<*>> T.andSpanMetaData(tracer: Tracer): T? {
    val activeSpan = tracer.activeSpan() ?: return null
    val injector = MapInjector()
    tracer.inject(activeSpan.context(), Format.Builtin.TEXT_MAP, injector)
    return andMetaData(injector.metaData) as T
}

private fun Tracer.markActiveSpanFailed(): Span? = activeSpan()?.setTag(Tags.ERROR, true)
private fun Tracer.tag(key: String, value: String): Span? = activeSpan()?.setTag(key, value)
private fun Tracer.log(event: String): Span? = activeSpan()?.log(event)
private fun <V : Any?> Tracer.log(vararg pairs: Pair<String, V>): Span? = activeSpan()?.log(pairs.toMap())
private fun Tracer.id(): String? = activeSpan()?.context()?.toTraceId()
private fun UnitOfWork<*>.messageName(): String = message.payloadType.simpleName
private fun Message<*>.name(): String = payloadType.simpleName
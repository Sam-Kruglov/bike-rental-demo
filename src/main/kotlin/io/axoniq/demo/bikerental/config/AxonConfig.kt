package io.axoniq.demo.bikerental.config

import io.opentracing.Tracer
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.axonframework.config.EventProcessingModule
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ListenerInvocationErrorHandler
import org.axonframework.extensions.tracing.TracingCommandGateway
import org.axonframework.extensions.tracing.TracingQueryGateway
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.queryhandling.DefaultQueryGateway
import org.axonframework.queryhandling.QueryBus
import org.axonframework.queryhandling.QueryMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//todo check up on https://github.com/AxonFramework/AxonFramework/issues/1422
@Configuration
class AxonConfig {

    @Bean
    fun commandGateway(
            commandBus: CommandBus,
            dispatchInterceptors: List<MessageDispatchInterceptor<in CommandMessage<*>>>,
            handlerInterceptors: List<MessageHandlerInterceptor<in CommandMessage<*>>>,
            tracer: Tracer
    ): TracingCommandGateway {
        handlerInterceptors.forEach { commandBus.registerHandlerInterceptor(it) }
        val customDefaultCommandGateway = CustomDefaultCommandGateway(DefaultCommandGateway
                .builder()
                .commandBus(commandBus)
                .dispatchInterceptors(dispatchInterceptors))
        return TracingCommandGateway.builder()
                .delegateCommandGateway(customDefaultCommandGateway)
                .tracer(tracer)
                .build()
    }

    @Bean
    fun queryGateway(
            queryBus: QueryBus,
            dispatchInterceptors: List<MessageDispatchInterceptor<in QueryMessage<*, *>>>,
            handlerInterceptors: List<MessageHandlerInterceptor<in QueryMessage<*, *>>>,
            tracer: Tracer
    ): TracingQueryGateway {
        handlerInterceptors.forEach { queryBus.registerHandlerInterceptor(it) }
        val defaultQueryGateway = DefaultQueryGateway.builder()
                .queryBus(queryBus)
                .dispatchInterceptors(dispatchInterceptors)
                .build()
        return TracingQueryGateway.builder()
                .tracer(tracer)
                .delegateQueryGateway(defaultQueryGateway)
                .build()
    }

    @Autowired
    fun eventInterceptorsRegistration(
            @Autowired(required = false) dispatchInterceptors: List<MessageDispatchInterceptor<in EventMessage<*>>>?,
            @Autowired(required = false) handlerInterceptors: List<MessageHandlerInterceptor<in EventMessage<*>>>?,
            @Autowired(required = false) defaultErrorHandler: ListenerInvocationErrorHandler?,
            configurer: EventProcessingModule,
            eventBus: EventBus
    ) {
        configurer.usingSubscribingEventProcessors()
        if (defaultErrorHandler != null) {
            configurer.registerDefaultListenerInvocationErrorHandler { defaultErrorHandler }
        }

        handlerInterceptors?.forEach {
            configurer.registerDefaultHandlerInterceptor { _, _ -> it }
        }

        dispatchInterceptors?.forEach { eventBus.registerDispatchInterceptor(it) }
    }
}
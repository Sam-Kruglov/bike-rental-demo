package io.axoniq.demo.bikerental.config.tracing

import io.opentracing.Span
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork
import org.springframework.stereotype.Service

@Aspect
@Service
class TracingEventHandlerAspect {

    companion object {
        private const val READ_MODEL_PACKAGE = "io.axoniq.demo.bikerental.read"
    }

    @Around("projectionServiceMethods() && @annotation(org.axonframework.eventhandling.EventHandler)")
    fun logStuff(p: ProceedingJoinPoint): Any? {
        val packageName = p.signature.declaringType.`package`.name

        //capitalized package after "read"
        val projectionName = packageName
                .replace(READ_MODEL_PACKAGE, "")
                .split(".")
                .joinToString(" ") { it.capitalize() }
                .trim()
        CurrentUnitOfWork.get()
                .getResource<Span>(TracingAxonConfig.Instrumentation.EVENT_UOW_SPAN_KEY)
                .setOperationName("Updating $projectionName Projection")
        return p.proceed()
    }

    @Pointcut("execution(* $READ_MODEL_PACKAGE..*ViewService.*(..))")
    fun projectionServiceMethods() = Unit
}
package io.axoniq.demo.bikerental.config

import org.axonframework.commandhandling.GenericCommandResultMessage
import org.axonframework.commandhandling.callbacks.FutureCallback
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import java.util.concurrent.CompletableFuture

/**
 * Does not log errors.
 */
open class CustomDefaultCommandGateway(builder: Builder) : DefaultCommandGateway(builder) {

    //copied from org.axonframework.commandhandling.gateway.DefaultCommandGateway.send(java.lang.Object)
    //does not include error logging callback
    override fun <R> send(command: Any): CompletableFuture<R> {
        val callback = FutureCallback<Any, R>()
        send(command, callback)
        val result = CompletableFuture<R>()
        callback.exceptionally { GenericCommandResultMessage.asCommandResultMessage(it) }
                .thenAccept {
                    try {
                        if (it.isExceptional) {
                            result.completeExceptionally(it.exceptionResult())
                        } else {
                            result.complete(it.payload)
                        }
                    } catch (e: Exception) {
                        result.completeExceptionally(e)
                    }
                }
        return result
    }
}
package io.axoniq.demo.bikerental.config

import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {

    companion object {
        private val log = LoggerFactory.getLogger(AxonConfig::class.java)
    }

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }
}
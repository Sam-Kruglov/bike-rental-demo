package io.axoniq.demo.bikerental.config

import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy
import org.axonframework.commandhandling.distributed.UnresolvedRoutingKeyPolicy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {

    @Bean
    fun routingStrategy() = AnnotationRoutingStrategy(UnresolvedRoutingKeyPolicy.RANDOM_KEY)
}
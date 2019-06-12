package io.axoniq.demo.bikerental.service.register

import io.axoniq.demo.bikerental.Bike
import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRepo
import io.axoniq.demo.bikerental.RegisterBikeRequest
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("register")
@Service
class BikeRegistrationService(
        private val eventBus: EventGateway,
        private val repo: BikeRepo
) {

    companion object {
        private val log = LoggerFactory.getLogger(BikeRegistrationService::class.java)
    }

    @CommandHandler
    fun register(request: RegisterBikeRequest): String {
        log.info(request.toString())
        val bike = repo.save(Bike(request.bikeId, request.location))
        eventBus.publish(BikeRegisteredEvent(bike.id, bike.location))
        return bike.id
    }

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }
}

package io.axoniq.demo.bikerental.service.return_

import io.axoniq.demo.bikerental.BikeRepo
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.ReturnBikeRequest
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Profile("return")
@Service
class BikeReturnService(
        private val eventBus: EventGateway,
        private val repo: BikeRepo
) {

    companion object {
        private val log = LoggerFactory.getLogger(BikeReturnService::class.java)
    }

    @Transactional
    @CommandHandler
    fun returnAt(request: ReturnBikeRequest) {
        log.info(request.toString())
        val bike = repo.findById(request.bikeId)
                .orElseThrow { IllegalArgumentException("bike #${request.bikeId} not found") }
        bike.returnAt(request.location)
        eventBus.publish(BikeReturnedEvent(bike.id, bike.location))
    }

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }
}

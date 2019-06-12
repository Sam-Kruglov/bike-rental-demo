package io.axoniq.demo.bikerental.service.rental

import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeRepo
import io.axoniq.demo.bikerental.RentBikeRequest
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Profile("rental")
@Service
class BikeRentalService(
        private val eventBus: EventGateway,
        private val repo: BikeRepo
) {

    companion object {
        private val log = LoggerFactory.getLogger(BikeRentalService::class.java)
    }

    @Transactional
    @CommandHandler
    fun rent(request: RentBikeRequest) {
        log.info(request.toString())
        val bike = repo.findById(request.bikeId)
                .orElseThrow { IllegalArgumentException("bike #${request.bikeId} not found") }
        bike.rent(request.renter)
        eventBus.publish(BikeRentedEvent(bike.id, bike.renter!!))
    }

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }
}

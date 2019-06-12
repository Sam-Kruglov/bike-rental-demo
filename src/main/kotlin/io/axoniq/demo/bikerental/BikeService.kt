package io.axoniq.demo.bikerental

import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class BikeService(
        private val eventBus: EventGateway,
        private val repo: BikeRepo
) {

    companion object {
        private val log = LoggerFactory.getLogger(BikeService::class.java)
    }

    fun register(id: String, location: String): String {
        val bike = repo.save(Bike(id, location))
        eventBus.publish(BikeRegisteredEvent(bike.id, bike.location))
        return bike.id
    }

    @Transactional
    fun rent(id: String, renter: String) {
        val bike = this.findByIfOrThrow(id)
        bike.rent(renter)
        eventBus.publish(BikeRentedEvent(bike.id, bike.renter!!))
    }

    @Transactional
    fun returnAt(id: String, location: String) {
        val bike = this.findByIfOrThrow(id)
        bike.returnAt(location)
        eventBus.publish(BikeReturnedEvent(bike.id, bike.location))
    }

    fun findAll(): Iterable<Bike> = repo.findAll()

    fun findById(id: String): Bike? = repo.findById(id).orElse(null)

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }

    private fun findByIfOrThrow(id: String): Bike =
            repo.findById(id).orElseThrow { IllegalArgumentException("bike #$id not found") }
}

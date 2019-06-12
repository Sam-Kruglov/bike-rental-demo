package io.axoniq.demo.bikerental

import org.axonframework.commandhandling.CommandHandler
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

    @CommandHandler
    fun register(request: RegisterBikeRequest): String {
        val bike = repo.save(Bike(request.bikeId, request.location))
        eventBus.publish(BikeRegisteredEvent(bike.id, bike.location))
        return bike.id
    }

    @Transactional
    @CommandHandler
    fun rent(request: RentBikeRequest) {
        val bike = this.findByIfOrThrow(request.bikeId)
        bike.rent(request.renter)
        eventBus.publish(BikeRentedEvent(bike.id, bike.renter!!))
    }

    @Transactional
    @CommandHandler
    fun returnAt(request: ReturnBikeRequest) {
        val bike = this.findByIfOrThrow(request.bikeId)
        bike.returnAt(request.location)
        eventBus.publish(BikeReturnedEvent(bike.id, bike.location))
    }

    @CommandHandler
    fun findAll(request: GetAllBikesRequest): Iterable<Bike> = repo.findAll()

    @CommandHandler
    fun findById(request: GetBikeByIdRequest): Bike? = repo.findById(request.bikeId).orElse(null)

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }

    private fun findByIfOrThrow(id: String): Bike =
            repo.findById(id).orElseThrow { IllegalArgumentException("bike #$id not found") }
}

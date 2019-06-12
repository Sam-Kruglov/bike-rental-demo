package io.axoniq.demo.bikerental

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class BikeService(
        private val eventGateway: EventGateway,
        private val repo: BikeRepo
) {

    companion object {
        private val log = LoggerFactory.getLogger(BikeService::class.java)
    }

    @CommandHandler
    fun register(command: RegisterBikeCommand): String {
        val bike = repo.save(Bike(command.bikeId, command.location))
        eventGateway.publish(BikeRegisteredEvent(bike.id, bike.location))
        return bike.id
    }

    @Transactional
    @CommandHandler
    fun rent(command: RentBikeCommand) {
        val bike = this.findByIfOrThrow(command.bikeId)
        bike.rent(command.renter)
        eventGateway.publish(BikeRentedEvent(bike.id, bike.renter!!))
    }

    @Transactional
    @CommandHandler
    fun returnAt(command: ReturnBikeCommand) {
        val bike = this.findByIfOrThrow(command.bikeId)
        bike.returnAt(command.location)
        eventGateway.publish(BikeReturnedEvent(bike.id, bike.location))
    }

    @QueryHandler
    fun findAll(query: GetAllBikesQuery): Iterable<Bike> = repo.findAll()

    @QueryHandler
    fun findById(query: GetBikeByIdQuery): Bike? = repo.findById(query.bikeId).orElse(null)

    @EventHandler
    fun anyEventListener(event: Any) {
        log.info(event.toString())
    }

    private fun findByIfOrThrow(id: String): Bike =
            repo.findById(id).orElseThrow { IllegalArgumentException("bike #$id not found") }
}

package io.axoniq.demo.bikerental.write

import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.RegisterBikeCommand
import io.axoniq.demo.bikerental.RentBikeCommand
import io.axoniq.demo.bikerental.ReturnBikeCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class BikeWriteService(
        private val eventGateway: EventGateway,
        private val repo: BikeRepo
) {

    @CommandHandler
    fun register(command: RegisterBikeCommand): String {
        val bike = repo.save(Bike(command.bikeId))
        eventGateway.publish(BikeRegisteredEvent(bike.id, command.location))
        return bike.id
    }

    @Transactional
    @CommandHandler
    fun rent(command: RentBikeCommand) {
        val bike = this.findByIfOrThrow(command.bikeId)
        bike.rent()
        eventGateway.publish(BikeRentedEvent(bike.id, command.renter))
    }

    @Transactional
    @CommandHandler
    fun returnAt(command: ReturnBikeCommand) {
        val bike = this.findByIfOrThrow(command.bikeId)
        bike.returnAt()
        eventGateway.publish(BikeReturnedEvent(bike.id, command.location))
    }

    private fun findByIfOrThrow(id: String): Bike =
            repo.findById(id).orElseThrow { IllegalArgumentException("bike #$id not found") }
}

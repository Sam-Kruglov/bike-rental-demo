package io.axoniq.demo.bikerental.write.service

import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.RegisterBikeCommand
import io.axoniq.demo.bikerental.RentBikeCommand
import io.axoniq.demo.bikerental.ReturnBikeCommand
import io.axoniq.demo.bikerental.write.Bike
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.gateway.EventGateway
import org.axonframework.modelling.command.Repository
import org.springframework.stereotype.Service

@Service
class BikeWriteService(
        private val eventGateway: EventGateway,
        private val repo: Repository<Bike>
) {

    @CommandHandler
    fun register(command: RegisterBikeCommand): String {
        val bike = repo.newInstance { Bike(command.bikeId) }
        eventGateway.publish(BikeRegisteredEvent(bike.invoke { it.id }, command.location))
        return bike.invoke { it.id }
    }

    @CommandHandler
    fun rent(command: RentBikeCommand) {
        repo.load(command.bikeId).execute {
            it.rent()
            eventGateway.publish(BikeRentedEvent(it.id, command.renter))
        }
    }

    @CommandHandler
    fun returnAt(command: ReturnBikeCommand) {
        repo.load(command.bikeId).execute {
            it.returnAt()
            eventGateway.publish(BikeReturnedEvent(it.id, command.location))
        }
    }
}

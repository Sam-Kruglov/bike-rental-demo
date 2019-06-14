package io.axoniq.demo.bikerental.write

import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.RegisterBikeCommand
import io.axoniq.demo.bikerental.RentBikeCommand
import io.axoniq.demo.bikerental.ReturnBikeCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Aggregate
class Bike {

    @Id
    val id: String

    var available: Boolean
        protected set

    @Suppress("ConvertSecondaryConstructorToPrimary", "LeakingThis")
    @CommandHandler
    constructor(command: RegisterBikeCommand) {
        this.id = command.bikeId
        this.available = true
        AggregateLifecycle.apply(BikeRegisteredEvent(id, command.location))
    }

    @CommandHandler
    fun rent(command: RentBikeCommand) {
        if (!available) {
            throw IllegalArgumentException("Bike is already rented")
        }
        available = false
        AggregateLifecycle.apply(BikeRentedEvent(id, command.renter))
    }

    @CommandHandler
    fun returnAt(command: ReturnBikeCommand) {
        if (available) {
            throw IllegalArgumentException("Bike is already returned")
        }
        available = true
        AggregateLifecycle.apply(BikeReturnedEvent(id, command.location))
    }
}

package io.axoniq.demo.bikerental.write

import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.RegisterBikeCommand
import io.axoniq.demo.bikerental.RentBikeCommand
import io.axoniq.demo.bikerental.ReturnBikeCommand
import io.axoniq.demo.bikerental.config.ErrorType.ALREADY_RENTED
import io.axoniq.demo.bikerental.config.ErrorType.ALREADY_RETURNED
import io.axoniq.demo.bikerental.config.ExpectedException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Bike {

    @AggregateIdentifier
    lateinit var id: String

    var available: Boolean = true
        protected set

    @Suppress("ConvertSecondaryConstructorToPrimary", "LeakingThis")
    @CommandHandler
    constructor(command: RegisterBikeCommand) {
        AggregateLifecycle.apply(BikeRegisteredEvent(command.bikeId, command.location))
    }

    @CommandHandler
    fun rent(command: RentBikeCommand) {
        if (!available) {
            throw ExpectedException(ALREADY_RENTED, "#$id")
        }
        AggregateLifecycle.apply(BikeRentedEvent(command.bikeId, command.renter))
    }

    @CommandHandler
    fun returnAt(command: ReturnBikeCommand) {
        if (available) {
            throw ExpectedException(ALREADY_RETURNED, "#$id")
        }
        AggregateLifecycle.apply(BikeReturnedEvent(command.bikeId, command.location))
    }

    @EventSourcingHandler
    fun registered(event: BikeRegisteredEvent) {
        this.id = event.bikeId
        this.available = true
    }

    @EventSourcingHandler
    fun rented(event: BikeRentedEvent) {
        available = false
    }

    @EventSourcingHandler
    fun returned(event: BikeReturnedEvent) {
        available = true
    }
}

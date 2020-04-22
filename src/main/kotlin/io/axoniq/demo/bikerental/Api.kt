package io.axoniq.demo.bikerental

import org.axonframework.modelling.command.TargetAggregateIdentifier

interface Describable {
    fun description(): String
}

data class RegisterBikeCommand(@TargetAggregateIdentifier val bikeId: String, val location: String)
data class RentBikeCommand(@TargetAggregateIdentifier val bikeId: String, val renter: String)
data class ReturnBikeCommand(@TargetAggregateIdentifier val bikeId: String, val location: String)

data class GetBikeHistoryByIdQuery(val bikeId: String)
data class GetBikeByIdQuery(val bikeId: String)
class GetAllBikesQuery

data class BikeRegisteredEvent(val bikeId: String, val location: String) : Describable {
    override fun description() = "Bike #$bikeId: registered in $location"
}

data class BikeRentedEvent(val bikeId: String, val renter: String) : Describable {
    override fun description() = "Bike #$bikeId: rented to $renter"
}

data class BikeReturnedEvent(val bikeId: String, val location: String) : Describable {
    override fun description() = "Bike #$bikeId: returned in $location"
}
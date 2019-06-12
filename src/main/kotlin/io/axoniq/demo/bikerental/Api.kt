package io.axoniq.demo.bikerental

data class RegisterBikeCommand(val bikeId: String, val location: String)
data class RentBikeCommand(val bikeId: String, val renter: String)
data class ReturnBikeCommand(val bikeId: String, val location: String)
data class GetBikeByIdQuery(val bikeId: String)
class GetAllBikesQuery

data class BikeRegisteredEvent(val bikeId: String, val location: String)
data class BikeRentedEvent(val bikeId: String, val renter: String)
data class BikeReturnedEvent(val bikeId: String, val location: String)
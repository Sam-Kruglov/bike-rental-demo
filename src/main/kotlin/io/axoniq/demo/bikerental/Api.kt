package io.axoniq.demo.bikerental

data class RegisterBikeRequest(val bikeId: String, val location: String)
data class RentBikeRequest(val bikeId: String, val renter: String)
data class ReturnBikeRequest(val bikeId: String, val location: String)
data class GetBikeByIdRequest(val bikeId: String)
class GetAllBikesRequest

data class BikeRegisteredEvent(val bikeId: String, val location: String)
data class BikeRentedEvent(val bikeId: String, val renter: String)
data class BikeReturnedEvent(val bikeId: String, val location: String)
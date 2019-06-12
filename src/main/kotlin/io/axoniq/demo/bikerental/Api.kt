package io.axoniq.demo.bikerental

data class BikeRegisteredEvent(val bikeId: String, val location: String)
data class BikeRentedEvent(val bikeId: String, val renter: String)
data class BikeReturnedEvent(val bikeId: String, val location: String)
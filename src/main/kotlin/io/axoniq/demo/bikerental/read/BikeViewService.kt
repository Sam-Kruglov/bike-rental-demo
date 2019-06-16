package io.axoniq.demo.bikerental.read

import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.GetAllBikesQuery
import io.axoniq.demo.bikerental.GetBikeByIdQuery
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class BikeViewService(
        private val repo: BikeViewRepo
) {

    @EventHandler
    fun registered(event: BikeRegisteredEvent) {
        repo.save(BikeView(event.bikeId, event.location))
    }

    @Transactional
    @EventHandler
    fun rented(event: BikeRentedEvent) {
        repo.findById(event.bikeId).ifPresent {
            it.renter = event.renter
        }
    }

    @Transactional
    @EventHandler
    fun returned(event: BikeReturnedEvent) {
        repo.findById(event.bikeId).ifPresent {
            it.renter = null
            it.location = event.location
        }
    }

    @QueryHandler
    fun findAll(query: GetAllBikesQuery): Iterable<BikeView> = repo.findAll()

    @QueryHandler
    fun findById(query: GetBikeByIdQuery): BikeView? = repo.findById(query.bikeId).orElse(null)
}

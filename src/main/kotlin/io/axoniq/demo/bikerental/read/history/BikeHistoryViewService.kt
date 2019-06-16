package io.axoniq.demo.bikerental.read.history

import io.axoniq.demo.bikerental.BikeRegisteredEvent
import io.axoniq.demo.bikerental.BikeRentedEvent
import io.axoniq.demo.bikerental.BikeReturnedEvent
import io.axoniq.demo.bikerental.GetBikeHistoryByIdQuery
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class BikeHistoryViewService(
        private val repo: BikeHistoryViewRepo
) {

    @EventHandler
    fun registered(event: BikeRegisteredEvent, @Timestamp instant: Instant) {
        repo.save(BikeHistoryView(
                event.bikeId,
                instant.toLocalDateTime(),
                "Bike registered in ${event.location}"
        ))
    }

    @EventHandler
    fun rented(event: BikeRentedEvent, @Timestamp instant: Instant) {
        repo.save(BikeHistoryView(
                event.bikeId,
                instant.toLocalDateTime(),
                "Bike rented to ${event.renter}"
        ))
    }

    @EventHandler
    fun returned(event: BikeReturnedEvent, @Timestamp instant: Instant) {
        repo.save(BikeHistoryView(
                event.bikeId,
                instant.toLocalDateTime(),
                "Bike returned in ${event.location}"
        ))
    }

    @QueryHandler
    fun findAllById(query: GetBikeHistoryByIdQuery): Iterable<BikeHistoryView> = repo.findAllByBikeId(query.bikeId)

    private fun Instant.toLocalDateTime() = LocalDateTime.ofInstant(this, ZoneOffset.systemDefault())
}

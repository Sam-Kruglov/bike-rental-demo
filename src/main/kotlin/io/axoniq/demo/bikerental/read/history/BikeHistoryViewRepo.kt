package io.axoniq.demo.bikerental.read.history

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BikeHistoryViewRepo : CrudRepository<BikeHistoryView, String> {
    fun findAllByBikeId(id: String): Iterable<BikeHistoryView>
}

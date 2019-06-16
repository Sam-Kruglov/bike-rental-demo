package io.axoniq.demo.bikerental.read.history

import org.hibernate.annotations.Immutable
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Immutable
@Entity
class BikeHistoryView {

    @Id
    @GeneratedValue
    var id: Long? = null
        protected set

    val bikeId: String
    val timestamp: LocalDateTime
    val description: String

    @Suppress("ConvertSecondaryConstructorToPrimary", "LeakingThis")
    constructor(bikeId: String, timestamp: LocalDateTime, description: String) {
        this.bikeId = bikeId
        this.timestamp = timestamp
        this.description = description
    }
}


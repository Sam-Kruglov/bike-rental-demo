package io.axoniq.demo.bikerental.read.main

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class BikeView {

    @Id
    val id: String
    var location: String
    var renter: String? = null

    @Suppress("ConvertSecondaryConstructorToPrimary", "LeakingThis")
    constructor(id: String, location: String) {
        this.id = id
        this.location = location
    }

    override fun toString(): String {
        return "BikeView(id='$id', location='$location', renter=$renter)"
    }
}

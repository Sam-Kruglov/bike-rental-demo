package io.axoniq.demo.bikerental

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Bike {

    @Id
    val id: String

    var location: String
        protected set

    var renter: String? = null
        protected set

    @Suppress("ConvertSecondaryConstructorToPrimary", "LeakingThis")
    constructor(id: String, location: String) {
        this.id = id
        this.location = location
    }

    fun rent(renter: String) {
        if (this.renter != null) {
            throw IllegalArgumentException("Bike is already rented")
        }
        this.renter = renter
    }

    fun returnAt(location: String) {
        if (this.renter == null) {
            throw IllegalArgumentException("Bike is already returned")
        }
        this.renter = null
        this.location = location
    }
}

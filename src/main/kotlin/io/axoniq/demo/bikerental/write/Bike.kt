package io.axoniq.demo.bikerental.write

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Bike {

    @Id
    val id: String

    var available: Boolean
        protected set

    @Suppress("ConvertSecondaryConstructorToPrimary", "LeakingThis")
    constructor(id: String) {
        this.id = id
        this.available = true
    }

    fun rent() {
        if (!available) {
            throw IllegalArgumentException("Bike is already rented")
        }
        available = false
    }

    fun returnAt() {
        if (available) {
            throw IllegalArgumentException("Bike is already returned")
        }
        available = true
    }
}

package io.axoniq.demo.bikerental

import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class BikeService(
        private val repo: BikeRepo
) {

    fun register(id: String, location: String): String {
        val bike = repo.save(Bike(id, location))
        return bike.id
    }

    @Transactional
    fun rent(id: String, renter: String) {
        val bike = this.findByIfOrThrow(id)
        bike.rent(renter)
    }

    @Transactional
    fun returnAt(id: String, location: String) {
        val bike = this.findByIfOrThrow(id)
        bike.returnAt(location)
    }

    fun findAll(): Iterable<Bike> = repo.findAll()

    fun findById(id: String): Bike? = repo.findById(id).orElse(null)

    private fun findByIfOrThrow(id: String): Bike =
            repo.findById(id).orElseThrow { IllegalArgumentException("bike #$id not found") }

}

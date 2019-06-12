package io.axoniq.demo.bikerental

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bikes")
class RentalController(
        private val service: BikeService
) {

    @PostMapping("{id}")
    fun register(
            @PathVariable id: String,
            @RequestParam location: String
    ): String {
        return service.register(id, location)
    }

    @PutMapping("/{id}/rent")
    fun rent(
            @PathVariable id: String,
            @RequestParam renter: String
    ): String {
        service.rent(id, renter)
        return "Bike rented to $renter"
    }

    @PutMapping("/{id}/return")
    fun returnBike(
            @PathVariable id: String,
            @RequestParam location: String
    ): String {
        service.returnAt(id, location)
        return "Bike returned in $location"
    }

    @GetMapping
    fun findAll(): Iterable<Bike> {
        return service.findAll()
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable id: String): Bike? {
        return service.findById(id)
    }

    @ExceptionHandler
    fun handleEverything(e: Exception) =
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
}

package io.axoniq.demo.bikerental

import org.axonframework.commandhandling.gateway.CommandGateway
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
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/bikes")
class RentalController(
        private val requestBus: CommandGateway
) {

    @PostMapping("{id}")
    fun register(
            @PathVariable id: String,
            @RequestParam location: String
    ): CompletableFuture<String> {
        return requestBus.send(RegisterBikeRequest(id, location))
    }

    @PutMapping("/{id}/rent")
    fun rent(
            @PathVariable id: String,
            @RequestParam renter: String
    ): CompletableFuture<String> {
        return requestBus.send<Void>(RentBikeRequest(id, renter))
                .thenApply { "Bike rented to $renter" }
    }

    @PutMapping("/{id}/return")
    fun returnBike(
            @PathVariable id: String,
            @RequestParam location: String
    ): CompletableFuture<String> {
        return requestBus.send<Void>(ReturnBikeRequest(id, location))
                .thenApply { "Bike returned in $location" }
    }

    @GetMapping
    fun findAll(): CompletableFuture<Iterable<Bike>> {
        return requestBus.send<Iterable<Bike>>(GetAllBikesRequest())
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable id: String): CompletableFuture<Bike?> {
        return requestBus.send<Bike>(GetBikeByIdRequest(id))
    }

    @ExceptionHandler
    fun handleEverything(e: Exception) =
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
}

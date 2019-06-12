package io.axoniq.demo.bikerental

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
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
        private val queryGateway: QueryGateway,
        private val commandGateway: CommandGateway
) {

    @PostMapping("{id}")
    fun register(
            @PathVariable id: String,
            @RequestParam location: String
    ): CompletableFuture<String> {
        return commandGateway.send(RegisterBikeCommand(id, location))
    }

    @PutMapping("/{id}/rent")
    fun rent(
            @PathVariable id: String,
            @RequestParam renter: String
    ): CompletableFuture<String> {
        return commandGateway.send<Void>(RentBikeCommand(id, renter))
                .thenApply { "Bike rented to $renter" }
    }

    @PutMapping("/{id}/return")
    fun returnBike(
            @PathVariable id: String,
            @RequestParam location: String
    ): CompletableFuture<String> {
        return commandGateway.send<Void>(ReturnBikeCommand(id, location))
                .thenApply { "Bike returned in $location" }
    }

    @GetMapping
    fun findAll(): CompletableFuture<List<Bike>> {
        return queryGateway.query(GetAllBikesQuery(), ResponseTypes.multipleInstancesOf(Bike::class.java))
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable id: String): CompletableFuture<Bike?> {
        return queryGateway.query(GetBikeByIdQuery(id), Bike::class.java)
    }

    @ExceptionHandler
    fun handleEverything(e: Exception) =
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
}

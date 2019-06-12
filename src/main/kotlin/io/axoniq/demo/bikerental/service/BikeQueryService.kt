package io.axoniq.demo.bikerental.service

import io.axoniq.demo.bikerental.Bike
import io.axoniq.demo.bikerental.BikeRepo
import io.axoniq.demo.bikerental.GetAllBikesRequest
import io.axoniq.demo.bikerental.GetBikeByIdRequest
import org.axonframework.commandhandling.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!gateway")
@Service
class BikeQueryService(
        private val repo: BikeRepo
) {

    companion object {
        private val log = LoggerFactory.getLogger(BikeQueryService::class.java)
    }

    @CommandHandler
    fun findAll(request: GetAllBikesRequest): Iterable<Bike> {
        log.info(request.toString())
        return repo.findAll()
    }

    @CommandHandler
    fun findById(request: GetBikeByIdRequest): Bike? {
        log.info(request.toString())
        return repo.findById(request.bikeId).orElse(null)
    }
}

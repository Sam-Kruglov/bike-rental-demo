package io.axoniq.demo.bikerental.write

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BikeRepo : CrudRepository<Bike, String>

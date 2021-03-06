package io.axoniq.demo.bikerental.read.main

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BikeViewRepo : CrudRepository<BikeView, String>

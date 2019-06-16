package io.axoniq.demo.bikerental

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
class ApiIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    val id = "1234"

    @Test
    @Order(1)
    fun `find all bikes -- empty`() {
        mockMvc.perform(
                get("/bikes")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    @Order(2)
    fun `register in Vilnius -- ok`() {
        mockMvc.perform(
                post("/bikes/{id}", id).param("location", "Vilnius")
        ).async()
                .andExpect(status().isOk)
                .andExpect(content().string(id))
    }

    @Test
    @Order(3)
    fun `find the bike -- found, no renter, is in Vilnius`() {
        //wait for the view to synchronize
        TimeUnit.MILLISECONDS.sleep(100)
        mockMvc.perform(
                get("/bikes/{id}", id)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("location").value("Vilnius"))
                .andExpect(jsonPath("renter").isEmpty)
    }

    @Test
    @Order(4)
    fun `rent to Allard -- ok`() {
        mockMvc.perform(
                put("/bikes/{id}/rent", id).param("renter", "Allard")
        ).async()
                .andExpect(status().isOk)
                .andExpect(content().string("Bike rented to Allard"))
    }

    @Test
    @Order(5)
    fun `find the bike -- found, renter is Allard, is in Vilnius`() {
        //wait for the view to synchronize
        TimeUnit.MILLISECONDS.sleep(100)
        mockMvc.perform(
                get("/bikes/{id}", id)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("location").value("Vilnius"))
                .andExpect(jsonPath("renter").value("Allard"))
    }

    @Test
    @Order(6)
    fun `rent to Steven -- already rented`() {
        val exception = mockMvc.perform(
                put("/bikes/{id}/rent", id).param("renter", "Steven")
        ).async()
                .andExpect(status().is5xxServerError)
                .andReturn().resolvedException
        assertThat(exception).hasMessage("Bike is already rented")
    }

    @Test
    @Order(7)
    fun `find the bike -- found, renter is still Allard, is in Vilnius`() {
        //wait for the view to synchronize
        TimeUnit.MILLISECONDS.sleep(100)
        mockMvc.perform(
                get("/bikes/{id}", id)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("location").value("Vilnius"))
                .andExpect(jsonPath("renter").value("Allard"))
    }

    @Test
    @Order(8)
    fun `return in Barcelona -- ok`() {
        mockMvc.perform(
                put("/bikes/{id}/return", id).param("location", "Barcelona")
        ).async()
                .andExpect(status().isOk)
                .andExpect(content().string("Bike returned in Barcelona"))
    }

    @Test
    @Order(9)
    fun `find the bike -- found, no renter, is in Barcelona`() {
        //wait for the view to synchronize
        TimeUnit.MILLISECONDS.sleep(100)
        mockMvc.perform(
                get("/bikes/{id}", id)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("location").value("Barcelona"))
                .andExpect(jsonPath("renter").isEmpty)
    }

    @Test
    @Order(10)
    fun `return in Vilnius -- already returned`() {
        val exception = mockMvc.perform(
                put("/bikes/{id}/return", id).param("location", "Vilnius")
        ).async()
                .andExpect(status().is5xxServerError)
                .andReturn().resolvedException
        assertThat(exception).hasMessage("Bike is already returned")
    }

    @Test
    @Order(11)
    fun `find the bike -- found, no renter, still is in Barcelona`() {
        //wait for the view to synchronize
        TimeUnit.MILLISECONDS.sleep(100)
        mockMvc.perform(
                get("/bikes/{id}", id)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("location").value("Barcelona"))
                .andExpect(jsonPath("renter").isEmpty)
    }

    @Test
    @Order(12)
    fun `find the bike history -- registered in Vilnius, rented to Allard, returned in Barcelona`() {
        //wait for the view to synchronize
        TimeUnit.MILLISECONDS.sleep(100)
        mockMvc.perform(
                get("/bikes/{id}/history", id)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        ).async()
                .andExpect(status().isOk)
                .andExpect(jsonPath("[0].description").value("Bike registered in Vilnius"))
                .andExpect(jsonPath("[1].description").value("Bike rented to Allard"))
                .andExpect(jsonPath("[2].description").value("Bike returned in Barcelona"))
    }

    private fun ResultActions.async() = mockMvc.perform(asyncDispatch(this.andReturn()))
}
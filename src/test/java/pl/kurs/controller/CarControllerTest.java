package pl.kurs.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.Main;
import pl.kurs.model.Car;
import pl.kurs.model.Garage;
import pl.kurs.model.command.CreateCarCommand;
import pl.kurs.model.command.EditCarCommand;
import pl.kurs.repository.CarRepository;
import pl.kurs.repository.GarageRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
class CarControllerTest {

    @Autowired
    private MockMvc postman;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private GarageRepository garageRepository;

    @Test
    public void shouldReturnSingleCar() throws Exception {
        Garage garage = garageRepository.findAll().get(0);
        int id = carRepository.saveAndFlush(new Car("BMW", "M5", "g",garage)).getId();
        postman.perform(get("/api/v1/cars/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.model").value("M5"))
                .andExpect(jsonPath("$.fuelType").value("g"));
    }

    @Test
    public void shouldAddCar() throws Exception {
        Garage garage = garageRepository.findAll().get(0);
        CreateCarCommand command = new CreateCarCommand("car", "CAR", "C", garage.getId());
        String json = objectMapper.writeValueAsString(command);

        String responseJson = postman.perform(post("/api/v1/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.brand").value("car"))
                .andExpect(jsonPath("$.model").value("CAR"))
                .andExpect(jsonPath("$.fuelType").value("C"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Car saved = objectMapper.readValue(responseJson, Car.class);

        Car recentlyAdded = carRepository.findById(saved.getId()).get();

        Assertions.assertEquals("car", recentlyAdded.getBrand());
        Assertions.assertEquals("CAR", recentlyAdded.getModel());
        Assertions.assertEquals("C", recentlyAdded.getFuelType());

    }

    @Test
    public void shouldDeleteCar() throws Exception {
        Garage garage = garageRepository.findAll().get(0);
        int carId = carRepository.saveAndFlush(new Car("car", "CAR", "C", garage)).getId();

        postman.perform(delete("/api/v1/cars/" + carId))
                .andExpect(status().isNoContent());

        postman.perform(get("/api/v1/cars/" + carId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldEditBook() throws Exception {
        Garage garage = garageRepository.findAll().get(0);
        int carId = carRepository.saveAndFlush(new Car("car", "CAR", "C", garage)).getId();

        EditCarCommand command = new EditCarCommand("new", "NEW", "N");
        String json = objectMapper.writeValueAsString(command);

        postman.perform(put("/api/v1/cars/{id}", carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId))
                .andExpect(jsonPath("$.brand").value("new"))
                .andExpect(jsonPath("$.model").value("NEW"))
                .andExpect(jsonPath("$.fuelType").value("N"));

        Car editCar = carRepository.findById(carId).get();
        assertNotNull(editCar);
        assertEquals("new", editCar.getBrand());
        assertEquals("NEW", editCar.getModel());
        assertEquals("N", editCar.getFuelType());
    }

    @Test
    public void shouldEditBookPartially() throws Exception {
        Garage garage = garageRepository.findAll().get(0);
        int carId = carRepository.saveAndFlush(new Car("car", "CAR", "C", garage)).getId();

        EditCarCommand command = new EditCarCommand("new", null, "N");
        String json = objectMapper.writeValueAsString(command);

        postman.perform(patch("/api/v1/cars/" + carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Car editCar = carRepository.findById(carId).get();

        assertEquals("new", editCar.getBrand());
        assertEquals("CAR", editCar.getModel());
        assertEquals("N", editCar.getFuelType());
    }
}
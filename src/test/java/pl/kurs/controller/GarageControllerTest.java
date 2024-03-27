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
import pl.kurs.model.Garage;
import pl.kurs.model.command.CreateGarageCommand;
import pl.kurs.model.command.EditGarageCommand;
import pl.kurs.repository.GarageRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
class GarageControllerTest {

    @Autowired
    private MockMvc postman;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GarageRepository garageRepository;

    @Test
    public void shouldReturnSingleGarage() throws Exception {
        int id = garageRepository.saveAndFlush(new Garage(2, "address", false)).getId();
        postman.perform(get("/api/v1/garages/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.places").value(2))
                .andExpect(jsonPath("$.address").value("address"))
                .andExpect(jsonPath("$.lpgAllowed").value(false));
    }

    @Test
    public void shouldAddGarage() throws Exception {

        CreateGarageCommand command = new CreateGarageCommand(2, "address", true);
        String json = objectMapper.writeValueAsString(command);

        String responseJson = postman.perform(post("/api/v1/garages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.places").value(2))
                .andExpect(jsonPath("$.address").value("address"))
                .andExpect(jsonPath("$.lpgAllowed").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Garage saved = objectMapper.readValue(responseJson, Garage.class);

        Garage recentlyAdded = garageRepository.findById(saved.getId()).get();

        Assertions.assertEquals(2, recentlyAdded.getPlaces());
        Assertions.assertEquals("address", recentlyAdded.getAddress());
        Assertions.assertTrue(recentlyAdded.isLpgAllowed());
    }

    @Test
    public void shouldDelteGarage() throws Exception {
        int garageId = garageRepository.saveAndFlush(new Garage(2, "address", false)).getId();

        postman.perform(delete("/api/v1/garages/" + garageId))
                .andExpect(status().isNoContent());

        postman.perform(get("/api/v1/garages/" + garageId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldEditGarage() throws Exception {
        int garageId = garageRepository.saveAndFlush(new Garage(2, "address", false)).getId();

        EditGarageCommand command = new EditGarageCommand(3, "new", true);
        String json = objectMapper.writeValueAsString(command);

        postman.perform(put("/api/v1/garages/{id}", garageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.places").value(3))
                .andExpect(jsonPath("$.address").value("new"))
                .andExpect(jsonPath("$.lpgAllowed").value(true));

        Garage editGarage = garageRepository.findById(garageId).get();
        assertNotNull(editGarage);
        assertEquals(3, editGarage.getPlaces());
        assertEquals("new", editGarage.getAddress());
        assertTrue(editGarage.isLpgAllowed());
    }

    @Test
    public void shouldEditGaragePartially() throws Exception {

        int garageId = garageRepository.saveAndFlush(new Garage(2, "address", true)).getId();

        EditGarageCommand command = new EditGarageCommand(5, null, false);
        String json = objectMapper.writeValueAsString(command);

        postman.perform(patch("/api/v1/garages/" + garageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Garage newGarage = garageRepository.findById(garageId).get();

        assertEquals(5, newGarage.getPlaces());
        assertEquals("address", newGarage.getAddress());
        assertFalse(newGarage.isLpgAllowed());
    }
}
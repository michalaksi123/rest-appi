package pl.kurs.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.exceptions.GarageNotFoundException;
import pl.kurs.model.Garage;
import pl.kurs.model.command.CreateGarageCommand;
import pl.kurs.model.command.EditGarageCommand;
import pl.kurs.repository.GarageRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/garages")
@Slf4j
@RequiredArgsConstructor
public class GarageController {

    private final GarageRepository garageRepository;

    @PostConstruct
    public void init() {
        garageRepository.saveAndFlush(new Garage(10, "address1", true));
        garageRepository.saveAndFlush(new Garage(20, "address2", false));
    }

    @GetMapping
    public ResponseEntity<List<Garage>> findAll() {
        log.info("findAll");
        return ResponseEntity.ok(garageRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Garage> addGarage(@RequestBody CreateGarageCommand command) {
        Garage garage = new Garage(command.getPlaces(), command.getAddress(), command.getLpgAllowed());
        return ResponseEntity.status(HttpStatus.CREATED).body(garageRepository.saveAndFlush(garage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Garage> findGarage(@PathVariable int id) {
        return ResponseEntity.ok(garageRepository.findById(id).orElseThrow(GarageNotFoundException::new));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Garage> deleteGarage(@PathVariable int id) {
        garageRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Garage> editGarage(@PathVariable int id, @RequestBody EditGarageCommand command) {
        Garage garage = garageRepository.findById(id).orElseThrow(GarageNotFoundException::new);
        garage.setAddress(command.getAddress());
        garage.setPlaces(command.getPlaces());
        garage.setLpgAllowed(command.getLpgAllowed());
        return ResponseEntity.status(HttpStatus.OK).body(garageRepository.saveAndFlush(garage));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Garage> editGaragePartially(@PathVariable int id, @RequestBody EditGarageCommand command) {
        Garage garage = garageRepository.findById(id).orElseThrow(GarageNotFoundException::new);
        Optional.ofNullable(command.getAddress()).ifPresent(garage::setAddress);
        Optional.ofNullable(command.getPlaces()).ifPresent(garage::setPlaces);
        Optional.ofNullable(command.getLpgAllowed()).ifPresent(garage::setLpgAllowed);
        return ResponseEntity.status(HttpStatus.OK).body(garageRepository.saveAndFlush(garage));
    }
}


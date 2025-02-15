package pl.kurs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int places;
    private String address;
    private boolean lpgAllowed;

    @OneToMany(mappedBy = "garage")
    private Set<Car> cars = new HashSet<>();

    public Garage(int places, String address, boolean lpgAllowed) {
        this.places = places;
        this.address = address;
        this.lpgAllowed = lpgAllowed;
    }

    public void addCar(Car car) {
        cars.add(car);
        car.setGarage(this);
    }
}
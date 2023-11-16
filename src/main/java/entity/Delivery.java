package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    private long id;
    private String city;
    private String street;
    private String house;
    private int entrance;
    private int apartment;


    public Delivery(String cityName, String streetName, String houseNumber, int entrance, int apartment) {
    }
}
package entity;


import java.math.BigDecimal;

import entity.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Product {
    private long id;
    private String name;
    private Type type;
    private BigDecimal size;
    private String color;
    private int amount;
    private BigDecimal actual_price;


    public Product() {

    }


}


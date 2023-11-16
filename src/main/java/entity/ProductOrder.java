package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductOrder {
    private Long productId;
    private BigDecimal price;
    private int amount;
    private String name;

}

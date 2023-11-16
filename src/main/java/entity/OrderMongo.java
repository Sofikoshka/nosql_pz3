package entity;

import entity.enums.OrderStatus;
import entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class OrderMongo {
    private long id;
    private LocalDateTime datetime;
    private OrderStatus status;
    private List<Product> products;
    private List<User>  users;
    private Delivery delivery;

    public OrderMongo() {
        products = new ArrayList<>();
        users = new ArrayList<>();
    }

    public OrderMongo(List<User> users, Delivery delivery, List<Product> products) {
        this.users = users;
        this.delivery = delivery;
        this.products = products;

    }

    public void addProduct(Product product) {
        products.add(product);
    }
    public void putUser(User user) {
        users.add(user);
    }



}


package org.example;

import DAO.*;

import entity.*;
import entity.enums.OrderStatus;
import entity.enums.Role;
import entity.enums.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        MongoDBDAOConfig mongoDBDAOConfig = new MongoDBDAOConfig();
        MongoDBConnectionManager.getInstance();

        MongoDBDAOFactory daoFactory = new MongoDBDAOFactory();

        UserDAO userDAO = daoFactory.getUserDAO();
        ProductDAO productDAO = daoFactory.getProductDAO();
        DeliveryDAO deliveryDAO = daoFactory.getDeliveryDAO();
        OrderMongoDAO orderDAO = daoFactory.getOrderDAO();

        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setName("User 1");
        user1.setLastname("LASTNAME");
        user1.setEmail("user1@example.com");
        user1.setPhone("43565f");
        user1.setPassword("dfkoreop54");


        users.add(user1);



        List<Product> products = new ArrayList<>();
        Product p1 = new Product();
        p1.setName("gggg");
        p1.setType(Type.valueOf("RING"));
        p1.setSize(BigDecimal.valueOf(7.5));
        p1.setColor("fff");
        p1.setAmount(20);
        p1.setActual_price(BigDecimal.valueOf(6.89));
        products.add(p1);

        List<Delivery> deliveries = new ArrayList<>();
        Delivery d1 = new Delivery();
        d1.setCity("gggwft");
        d1.setStreet("fperg");
        d1.setHouse("s3");
        d1.setEntrance(3);
        d1.setApartment(4);
        deliveries.add(d1);


        for (User user : users) {
            long userId = userDAO.createUser(user);
            System.out.println("Знайдений користувач: " + user);
        }


        long userIdToUpdate = user1.getId();
        String newEmail = "NEWMAIL.email@example.com";
        System.out.println("\nОновлення Email користувача:");
        User userBeforeUpdate = userDAO.findById(userIdToUpdate);
        System.out.println("Інформація про користувача до оновлення Email: " + userBeforeUpdate);
        userDAO.updateUser(userIdToUpdate, newEmail);
        User userAfterUpdate = userDAO.findById(userIdToUpdate);
        System.out.println("Інформація про користувача після оновлення Email: " + userAfterUpdate);


        for (Delivery delivery : deliveries) {
            long deliveryId = deliveryDAO.createDelivery(delivery);
            Delivery foundDelivery = deliveryDAO.findById(deliveryId);
            System.out.println("Знайдена доставка: " + foundDelivery);
        }

        System.out.println("\nОновлення вулиці в доставці:");

        // Оновлення вулиці в доставці
        long deliveryIdToUpdate = d1.getId();
        String newStreet = "Shevchenko";
        System.out.println("\nОновлення вулиці в доставці:");
        Delivery deliveryBeforeUpdate = deliveryDAO.findById(deliveryIdToUpdate);
        System.out.println("Інформація про доставку до оновлення вулиці: " + deliveryBeforeUpdate);
        deliveryDAO.updateStreet(deliveryIdToUpdate, newStreet);
        Delivery deliveryAfterUpdate = deliveryDAO.findById(deliveryIdToUpdate);
        System.out.println("Інформація про доставку після оновлення вулиці: " + deliveryAfterUpdate);


        for (Product product : products) {
            long productId = productDAO.createProduct(product);
            Product foundProduct = productDAO.getProductById(productId);
            System.out.println("Знайдений продукт: " + foundProduct);
        }

        System.out.println("\nОновлення кількості продуктів:");

// Оновлення кількості продуктів
        for (Product product : products) {
            long productIdToUpdate = p1.getId(); // Get the ID from the object
            System.out.println("\nОновлення кількості продукта (ID: " + productIdToUpdate + "):");
            Product productBeforeUpdate = productDAO.getProductById(productIdToUpdate);
            System.out.println("Інформація про продукт (ID: " + productIdToUpdate + ") до оновлення кількості: " + productBeforeUpdate);
            productDAO.updateProductAmount(productIdToUpdate, 40);
            Product productAfterUpdate = productDAO.getProductById(productIdToUpdate);
            System.out.println("Інформація про продукт (ID: " + productIdToUpdate + ") після оновлення кількості: " + productAfterUpdate);
        }

        System.out.println("\nОновлення ціни продуктів:");

// Оновлення ціни продуктів
        for (Product product : products) {
            long productIdToUpdatePrice = p1.getId(); // Get the ID from the object
            System.out.println("\nОновлення ціни продукта (ID: " + productIdToUpdatePrice + "):");
            Product productBeforeUpdatePrice = productDAO.getProductById(productIdToUpdatePrice);
            System.out.println("Інформація про продукт (ID: " + productIdToUpdatePrice + ") до оновлення ціни: " + productBeforeUpdatePrice);
            productDAO.updateProductPrice(productIdToUpdatePrice, BigDecimal.valueOf(100.99));
            Product productAfterUpdatePrice = productDAO.getProductById(productIdToUpdatePrice);
            System.out.println("Інформація про продукт (ID: " + productIdToUpdatePrice + ") після оновлення ціни: " + productAfterUpdatePrice);
        }





// Створення ордера
        OrderMongo order = createSampleOrder();
        long orderId = orderDAO.createOrder(order);
        System.out.println("\nOrder: " + order);

// Отримання ордера за ID
        OrderMongo retrievedOrder = orderDAO.getOrderById(orderId);


// Оновлення статусу ордера
        orderDAO.updateStatus(orderId, OrderStatus.ACCEPTED);
        System.out.println("\nOrder after update: " + retrievedOrder);







    }

    private static OrderMongo createSampleOrder () {
        Delivery delivery = new Delivery("City Name", "Street Name", "House Number", 1, 101);

        List<Product> Products = new ArrayList<>();
        Product product = new Product(1L, "PRODUCTNAME",Type.valueOf("RING"), new BigDecimal("7.5"), "fff", 20, new BigDecimal("6.89"));
        Products.add(product);

        List<User> Users = new ArrayList<>();
        User user = new User(2, "USERNAME", "LASTNAME", "user@example.com", "+1234567890", "hashed_password", Role.CLIENT);
        Users.add(user);

        return new OrderMongo(2, LocalDateTime.now(), OrderStatus.valueOf("ACCEPTED"), Products, Users, delivery);
    }

}


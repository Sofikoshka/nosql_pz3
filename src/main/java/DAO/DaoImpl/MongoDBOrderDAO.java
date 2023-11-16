package DAO.DaoImpl;

import DAO.MongoDBConnectionManager;
import DAO.MongoDBDAOConfig;
import DAO.OrderMongoDAO;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import entity.*;
import entity.enums.OrderStatus;
import entity.enums.Role;
import entity.enums.Type;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBOrderDAO implements OrderMongoDAO {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoDBOrderDAO() {
        mongoClient = MongoDBConnectionManager.getInstance();
        database = mongoClient.getDatabase(MongoDBDAOConfig.getDatabaseName());
        collection = database.getCollection("order");
    }

    @Override
    public long createOrder(OrderMongo order) {
        long orderId = 1;
        order.setId(orderId);

        Document orderDocument = new Document()
                .append("id", order.getId())
                .append("datetime", order.getDatetime())
                .append("status", order.getStatus().toString())
                .append("delivery", mapDelivery(order.getDelivery()))
                .append("products", mapProducts(order.getProducts()))
                .append("users", mapUsers(order.getUsers()));

        collection.insertOne(orderDocument);
        return order.getId();
    }


    private List<Document> mapUsers(List<User> users) {
        List<Document> userDocuments = new ArrayList<>();
        for (User user: users) {
            Document userDocument = new Document();
            userDocument.append("id", user.getId());
            userDocument.append("name", user.getName());
            userDocument.append("lastname", user.getLastname());
            userDocument.append("email", user.getEmail());
            userDocument.append("phone", user.getPhone());
            userDocument.append("password", user.getPassword());
            userDocument.append("role", user.getRole().name());
            userDocuments.add(userDocument);

        }

        return userDocuments;
    }



    private List<Document> mapProducts(List<Product> products) {
        List<Document> productDocuments = new ArrayList<>();
        for (Product product: products) {
            Document productDocument = new Document();
            productDocument.append("id", product.getId());
            productDocument.append("name", product.getName());
            productDocument.append("type", product.getType().name());
            productDocument.append("size", product.getSize());
            productDocument.append("color", product.getColor());
            productDocument.append("amount", product.getAmount());
            productDocument.append("actual_price", product.getActual_price());
            productDocuments.add(productDocument);

        }

        return productDocuments;
    }


    private Delivery mapDeliveryDocuments(Document deliveryDocument) {
        Delivery delivery = new Delivery();
        delivery.setCity(deliveryDocument.getString("city"));
        delivery.setStreet(deliveryDocument.getString("street"));
        delivery.setHouse(deliveryDocument.getString("house"));

        Integer entrance = deliveryDocument.getInteger("entrance");
        delivery.setEntrance(entrance != null ? entrance.intValue() : 0);

        Integer apartment = deliveryDocument.getInteger("apartment");
        delivery.setApartment(apartment != null ? apartment.intValue() : 0);

        return delivery;
    }



    private Document mapDelivery(Delivery delivery) {
        Document deliveryDocument = new Document();
        deliveryDocument.append("city", delivery.getCity());
        deliveryDocument.append("street", delivery.getStreet());
        deliveryDocument.append("house", delivery.getHouse());
        deliveryDocument.append("entrance", delivery.getEntrance());
        deliveryDocument.append("apartment", delivery.getApartment());
        return deliveryDocument;
    }

    private List<User> mapUserDocuments(List<Document> userDocuments) {
        List<User> users = new ArrayList<>();

        for (Document userDocument : userDocuments) {
            User user = new User();
            user.setId(userDocument.getLong("id"));
            user.setName(userDocument.getString("name"));
            user.setLastname(userDocument.getString("lastname"));
            user.setEmail(userDocument.getString("email"));
            user.setPhone(userDocument.getString("phone"));
            user.setPassword(userDocument.getString("password"));
            user.setRole(Enum.valueOf(Role.class, userDocument.getString("role")));
            users.add(user);
        }

        return users;
    }

    private List<Product> mapProductDocuments(List<Document> productDocuments) {
        List<Product> products = new ArrayList<>();

        for (Document productDocument : productDocuments) {
            Product product = new Product();
            product.setId(productDocument.getLong("id"));
            product.setName(productDocument.getString("name"));
            product.setType(Enum.valueOf(Type.class, productDocument.getString("type")));
            product.setSize(convertDecimal128ToBigDecimal(productDocument.get("size", Decimal128.class)));
            product.setColor(productDocument.getString("color"));
            product.setAmount(productDocument.getInteger("amount"));
            product.setActual_price(convertDecimal128ToBigDecimal(productDocument.get("actual_price", Decimal128.class)));
            products.add(product);
        }

        return products;
    }

    private BigDecimal convertDecimal128ToBigDecimal(Decimal128 decimal128) {
        return (decimal128 != null) ? decimal128.bigDecimalValue() : null;
    }


    private OrderMongo mapOrder(Document orderDocument) {
        OrderMongo order = new OrderMongo();
        order.setId(orderDocument.getLong("id"));

        Date date = orderDocument.getDate("datetime");
        if (date != null) {
            order.setDatetime(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else {

            order.setDatetime(LocalDateTime.now());
        }

        order.setStatus(OrderStatus.valueOf(orderDocument.getString("status")));
        order.setProducts(mapProductDocuments(orderDocument.getList("products", Document.class)));
        order.setUsers(mapUserDocuments(orderDocument.getList("users", Document.class)));
        order.setDelivery(mapDeliveryDocuments(orderDocument));

        return order;
    }


    private Document mapOrder(OrderMongo order) {
        Document orderDocument = new Document();

        orderDocument.append("id", order.getId());
        orderDocument.append("datetime", LocalDateTime.from(order.getDatetime().atZone(ZoneId.systemDefault()).toInstant()));
        orderDocument.append("status", order.getStatus().toString());
        orderDocument.append("products", mapProducts(order.getProducts()));
        orderDocument.append("users", mapUsers(order.getUsers()));
        orderDocument.append("delivery", mapDelivery(order.getDelivery()));

        return orderDocument;
    }


    @Override
    public OrderMongo getOrderById(long id) {
        Document filter = new Document("id", id);
        Document result = collection.find(filter).first();
        return (result != null) ? mapOrder(result) : null;
    }







    @Override
    public void updateStatus(Long orderId, OrderStatus status) {
        OrderMongo order = getOrderById(orderId);
        if (order != null) {
            order.setStatus(status);
            updateInMongoDB(order);
        }
    }

    private void updateInMongoDB(OrderMongo order) {
        Document orderDocument = convertOrderToDocument(order);
        collection.replaceOne(eq("id", order.getId()), orderDocument);
    }

    private Document createDeliveryDocument(Delivery delivery) {
        return new Document()
                .append("city", delivery.getCity())
                .append("street", delivery.getStreet())
                .append("house", delivery.getHouse())
                .append("entrance", delivery.getEntrance())
                .append("apartment", delivery.getApartment());
    }

    private Document createUserDocument(User user) {
        return new Document()
                .append("id", user.getId())
                .append("name", user.getName())
                .append("lastname", user.getLastname())
                .append("email", user.getEmail())
                .append("phone", user.getPhone())
                .append("password", user.getPassword())
                .append("role", user.getRole().name());
    }

    private Document createProductDocument(Product product) {
        return new Document()
                .append("id", product.getId())
                .append("name", product.getName())
                .append("size", product.getSize())
                .append("type", product.getType())
                .append("color", product.getColor())
                .append("amount", product.getAmount())
                .append("actual_price", product.getActual_price())
                .append("type", product.getType().name());
    }

    private List<Document> createProductDocuments(List<Product> products) {
        List<Document> productDocuments = new ArrayList<>();
        for (Product product : products) {
            productDocuments.add(createProductDocument(product));
        }
        return productDocuments;
    }

    private List<Document> createUserDocuments(List<User> users) {
        List<Document> userDocuments = new ArrayList<>();
        for (User user : users) {
            userDocuments.add(createUserDocument(user));
        }
        return userDocuments;
    }

    private Document convertOrderToDocument(OrderMongo order) {
        Document document = new Document();
        document.put("id", order.getId());
        document.put("date", order.getDatetime());
        document.put("status", order.getStatus().name());

        // Додавання інформації про доставку
        document.put("delivery", createDeliveryDocument(order.getDelivery()));

        // Додавання інформації про продукти
        document.put("products", createProductDocuments(order.getProducts()));

        // Додавання інформації про користувачів
        document.put("users", createUserDocuments(order.getUsers()));

        return document;
    }

    @Override
    public void deleteOrder(long id) {
        collection.deleteOne(eq("id", id));
    }
}

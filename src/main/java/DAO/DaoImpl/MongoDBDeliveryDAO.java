package DAO.DaoImpl;

import DAO.MongoDBConnectionManager;
import DAO.MongoDBDAOConfig;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import entity.Delivery;

import org.bson.Document;
import org.bson.types.ObjectId;
import DAO.DeliveryDAO;


import static com.mongodb.client.model.Filters.eq;

public class MongoDBDeliveryDAO implements DeliveryDAO {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoDBDeliveryDAO() {
        mongoClient = MongoDBConnectionManager.getInstance();
        database = mongoClient.getDatabase(MongoDBDAOConfig.getDatabaseName());
        collection = database.getCollection("delivery");
    }

    @Override
    public long createDelivery(Delivery delivery) {
        long deliveryId = 1;
        delivery.setId(deliveryId);

        Document deliveryDocument = new Document()
                .append("id", delivery.getId())
                .append("city", delivery.getCity())
                .append("street", delivery.getStreet())
                .append("house", delivery.getHouse()) // Виправлено назву поля
                .append("entrance", delivery.getEntrance())
                .append("apartment", delivery.getApartment());

        collection.insertOne(deliveryDocument);
        return delivery.getId();
    }

    @Override
    public Delivery findById(long id) {
        Document deliveryDocument = collection.find(eq("id", id)).first();
        if (deliveryDocument != null) {
            long deliveryId = deliveryDocument.getLong("id");
            String city = deliveryDocument.getString("city");
            String street = deliveryDocument.getString("street");
            String house = deliveryDocument.getString("house");
            Integer entrance = deliveryDocument.getInteger("entrance");
            Integer apartment = deliveryDocument.getInteger("apartment");

            Delivery delivery = new Delivery();
            delivery.setId(deliveryId);
            delivery.setCity(city);
            delivery.setStreet(street);
            delivery.setHouse(house);
            delivery.setEntrance(entrance != null ? entrance : 0);
            delivery.setApartment(apartment != null ? apartment : 0);

            return delivery;
        } else {
            return null;
        }
    }



    @Override
    public void updateStreet(long deliveryId, String newStreet) {
        Delivery delivery = findById(deliveryId);
        if (delivery != null) {
            delivery.setStreet(newStreet);
            updateInMongoDB(delivery);        }
    }


    private void updateInMongoDB(Delivery delivery) {
        Document deliveryDocument = convertDeliveryToDocument(delivery);
        collection.replaceOne(eq("id", delivery.getId()), deliveryDocument);
    }

    private Document convertDeliveryToDocument(Delivery delivery) {
        Document document = new Document();
        document.put("id", delivery.getId());
        document.put("city", delivery.getCity());
        document.put("street", delivery.getStreet());
        document.put("house", delivery.getHouse());
        document.put("entrance", delivery.getEntrance());
        document.put("apartment", delivery.getApartment());
        return document;
    }

    @Override
    public void deleteDelivery(long id) {
        collection.deleteOne(eq("_id", new ObjectId(String.valueOf(id))));
    }
}

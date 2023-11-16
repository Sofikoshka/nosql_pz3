package DAO;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import entity.Product;

public class MongoDBConnectionManager {
    private static MongoClient mongoClient;

    private MongoDBConnectionManager() {
    }

    public static MongoClient getInstance() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(MongoDBDAOConfig.getConnectionString());
        }
        return mongoClient;
    }
}

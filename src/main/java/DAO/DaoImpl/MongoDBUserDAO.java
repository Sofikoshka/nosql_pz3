package DAO.DaoImpl;

import DAO.MongoDBConnectionManager;
import DAO.MongoDBDAOConfig;
import DAO.UserDAO;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import entity.User;
import entity.enums.Role;
import org.bson.Document;


import static com.mongodb.client.model.Filters.eq;

public class MongoDBUserDAO implements UserDAO {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoDBUserDAO() {
        mongoClient = MongoDBConnectionManager.getInstance();
        database = mongoClient.getDatabase(MongoDBDAOConfig.getDatabaseName());
        collection = database.getCollection("user");
        collection.createIndex(Indexes.compoundIndex(Indexes.ascending("email"), Indexes.ascending("password")));

    }

    @Override
    public long createUser(User user) {
        long userId = 1;
        user.setId(userId);
        user.setRole(Role.valueOf("CLIENT"));
        Document userDocument = new Document()
                .append("id", user.getId())
                .append("name", user.getName())
                .append("lastname", user.getLastname())
                .append("email", user.getEmail())
                .append("phone", user.getPhone())
                .append("password", user.getPassword())
                .append("role", user.getRole().name());

        collection.insertOne(userDocument);
        return user.getId();
    }

    @Override
    public User login(String email, String password) {
        Document query = new Document("email", email).append("password", password);

        Document userDocument = collection.find(query).first();
        if (userDocument != null) {
            return convertDocumentToUser(userDocument);
        }

        return null;
    }

    private User convertDocumentToUser(Document userDocument) {
        User user = new User();
        user.setId(userDocument.getLong("id"));
        user.setName(userDocument.getString("name"));
        user.setEmail(userDocument.getString("email"));
        user.setPassword(userDocument.getString("password"));
        user.setRole(Role.valueOf(userDocument.getString("role")));
        return user;
    }

    @Override
    public User findById(long id) {
        Document userDocument = collection.find(eq("id", id)).first();
        if (userDocument != null) {
            long userId = userDocument.getLong("id");
            String name = userDocument.getString("name");
            String lastname = userDocument.getString("lastname");
            String email = userDocument.getString("email");
            String phone = userDocument.getString("phone");
            String password = userDocument.getString("password");
            String role = userDocument.getString("role");

            User user = new User();
            user.setId(userId);
            user.setName(name);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPassword(password);
            user.setRole(Role.valueOf(role));

            return user;
        } else {
            return null;
        }
    }



    @Override
    public void updateUser(long userId, String newEmail) {
        User user = findById(userId);
        if (user != null) {
            user.setEmail(newEmail);
            updateInMongoDB(user);
        }
    }

    private void updateInMongoDB(User user) {
        Document userDocument = convertUserToDocument(user);
        collection.replaceOne(eq("id", user.getId()), userDocument);
    }

    private Document convertUserToDocument(User user) {
        Document document = new Document();
        document.put("id", user.getId());
        document.put("name", user.getName());
        document.put("email", user.getEmail());
        document.put("password", user.getPassword());
        document.put("role", user.getRole().name());
        return document;
    }


    @Override
    public void deleteUser(long id) {
        collection.deleteOne(eq("id", id));
    }
}

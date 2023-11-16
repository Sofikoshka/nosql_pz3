package DAO.DaoImpl;

import DAO.MongoDBConnectionManager;
import DAO.MongoDBDAOConfig;
import DAO.ProductDAO;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import entity.Product;
import entity.User;
import entity.enums.Role;
import entity.enums.Type;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBProductDAO implements ProductDAO {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoDBProductDAO() {
        mongoClient = MongoDBConnectionManager.getInstance();
        database = mongoClient.getDatabase(MongoDBDAOConfig.getDatabaseName());
        collection = database.getCollection("product");    }

    @Override
    public long createProduct(Product product) {

        long productId = 1;

        product.setId(productId);

        Document productDocument = new Document()
                .append("id", product.getId())
                .append("name", product.getName())
                .append("size", product.getSize())
                .append("type", product.getType())
                .append("color", product.getColor())
                .append("amount", product.getAmount())
                .append("actual_price", product.getActual_price())
                .append("type", product.getType().name());

        collection.insertOne(productDocument);
        return product.getId();
    }

    @Override
    public Product getProductById(long id) {
        Document productDocument = collection.find(eq("id", id)).first();
        if (productDocument != null) {
            long productId = productDocument.getLong("id");
            String name = productDocument.getString("name");
            String type = productDocument.getString("type");
            Decimal128 size = productDocument.get("actual_price", Decimal128.class);
            String color = productDocument.getString("color");
            int amount = productDocument.getInteger("amount");
            Decimal128 actualPrice = productDocument.get("actual_price", Decimal128.class);

            Product product = new Product();
            product.setId(productId);
            product.setName(name);
            product.setType(Type.valueOf(type));
            product.setSize(size.bigDecimalValue());
            product.setColor(color);
            product.setAmount(amount);
            product.setActual_price(actualPrice.bigDecimalValue());

            return product;
        } else {
            return null;
        }
    }


    @Override
    public void updateProductAmount(long productId, int amount) {
        Product product = getProductById(productId);
        if (product != null) {
            product.setAmount(amount);
            updateInMongoDB(product);
        }
    }

    @Override
    public void updateProductPrice(long productId, BigDecimal actualPrice) {
        Product product = getProductById(productId);
        if (product != null) {
            product.setActual_price(actualPrice);
            updateInMongoDB(product);
        }
    }



    private void updateInMongoDB(Product product) {
        Document userDocument = convertProductToDocument(product);
        collection.replaceOne(eq("id", product.getId()), userDocument);
    }


    private Document convertProductToDocument(Product product) {
        Document document = new Document();
        document.put("id", product.getId());
        document.put("name", product.getName());
        document.put("type", product.getType().toString()); // Перетворення enum у строку
        document.put("size", product.getSize());
        document.put("color", product.getColor());
        document.put("amount", product.getAmount());
        document.put("actual_price", product.getActual_price());
        // Додайте інші поля, якщо вони є
        return document;
    }




    @Override
    public void deleteProduct(long id) {
        collection.deleteOne(eq("_id", new ObjectId(String.valueOf(id))));
    }
}


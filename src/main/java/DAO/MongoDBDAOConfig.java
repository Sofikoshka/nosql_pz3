package DAO;

public class MongoDBDAOConfig {
    private static final String DATABASE_NAME = "jewerly_shop";
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    public static String getConnectionString() {
        return CONNECTION_STRING;
    }
}

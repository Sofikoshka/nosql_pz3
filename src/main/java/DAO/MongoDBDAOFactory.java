package DAO;

import DAO.DaoImpl.MongoDBDeliveryDAO;
import DAO.DaoImpl.MongoDBOrderDAO;
import DAO.DaoImpl.MongoDBProductDAO;
import DAO.DaoImpl.MongoDBUserDAO;
import entity.OrderMongo;

public class MongoDBDAOFactory {

    public static UserDAO getUserDAO() {
        return new MongoDBUserDAO();
    }

    public static ProductDAO getProductDAO() {
        return new MongoDBProductDAO();
    }

    public static OrderMongoDAO getOrderDAO() {
        return new MongoDBOrderDAO();
    }
    public static DeliveryDAO getDeliveryDAO() {
        return new MongoDBDeliveryDAO();
    }


}

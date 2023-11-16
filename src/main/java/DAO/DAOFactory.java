package DAO;



import DAO.DaoImpl.MySQLDeliveryDAO;

import DAO.DaoImpl.MySQLOrderDAO;
import DAO.DaoImpl.MySQLProductDAO;
import DAO.DaoImpl.MySQLUserDAO;


public class DAOFactory {
    public UserDAO getUserDAOInstance(MySQLDAOConfig config) {
        return new MySQLUserDAO(config);
    }
    public ProductDAO getProductDAOInstance(MySQLDAOConfig config) {
        return new MySQLProductDAO(config);
    }
   public MySQLDeliveryDAO getDeliveryDAOInstance(MySQLDAOConfig config) {
        return new MySQLDeliveryDAO(config);
    }

    public MySQLOrderDAO getOrderDAOInstance(MySQLDAOConfig config) {

        return new MySQLOrderDAO(config);
    }

}



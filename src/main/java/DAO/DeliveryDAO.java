package DAO;


import entity.Delivery;


public interface DeliveryDAO {

    long createDelivery(Delivery delivery);

    Delivery findById(long id);


    void updateStreet(long orderId, String newStreet) ;

    void deleteDelivery(long id);
}

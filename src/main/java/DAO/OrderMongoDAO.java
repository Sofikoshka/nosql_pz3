package DAO;

import entity.OrderMongo;
import entity.enums.OrderStatus;

public interface OrderMongoDAO {

    long createOrder(OrderMongo order);


    OrderMongo getOrderById(long id);



    void updateStatus(Long orderId, OrderStatus status) ;


    void deleteOrder(long id);
}

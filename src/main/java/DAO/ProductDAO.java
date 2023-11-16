package DAO;

import entity.Product;

import java.math.BigDecimal;


public interface ProductDAO {

    long createProduct(Product product);


    Product getProductById(long id);


    void updateProductAmount(long productId, int amount);


    void updateProductPrice(long productId, BigDecimal actual_price);

    void deleteProduct(long id);
}


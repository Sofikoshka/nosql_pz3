package DAO.DaoImpl;

import DAO.MySQLConnectionManager;
import DAO.MySQLDAOConfig;
import DAO.ProductDAO;
import entity.Product;
import entity.enums.Type;

import java.math.BigDecimal;
import java.sql.*;


public class MySQLProductDAO implements ProductDAO {
    private final MySQLConnectionManager connectionManager;

    public MySQLProductDAO(MySQLDAOConfig config) {
        connectionManager = new MySQLConnectionManager(config);
    }

    private static final String ADD_PRODUCT = "INSERT INTO product (name, type, size, color, amount, actual_price) VALUES(?,?,?,?,?,?)";
    private static final String UPDATE_AMOUNT = "UPDATE product SET amount=? WHERE id=?";
    private static final String GET_PRODUCT_BY_ID = "SELECT * from product WHERE id=?";
    private static final String DELETE_PRODUCT = "DELETE FROM product WHERE id = ?";
    private static final String UPDATE_PRICE =
            "CALL UpdateProductPrice(?, ?)"
           ;

    @Override
    public long createProduct(Product product) {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(ADD_PRODUCT, Statement.RETURN_GENERATED_KEYS)) {
                int k = 0;
                ps.setString(++k, product.getName());
                ps.setString(++k, product.getType().getType());
                ps.setBigDecimal(++k, product.getSize());
                ps.setString(++k, product.getColor());
                ps.setInt(++k, product.getAmount());
                ps.setBigDecimal(++k, product.getActual_price());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setId(keys.getLong(1));
                    }
                }
            }
            return product.getId();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProductAmount(long productId, int amount) {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(UPDATE_AMOUNT)) {
                int k = 0;
                ps.setInt(++k, amount);
                ps.setLong(++k, productId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProductPrice(long productId, BigDecimal actual_price) {
        try (Connection con = connectionManager.getConnection()) {
            try (CallableStatement cs = con.prepareCall("{CALL UpdateProductPrice(?, ?)}")) {
                cs.setLong(1, productId);
                cs.setBigDecimal(2, actual_price);
                cs.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product price: " + e.getMessage(), e);
        }
    }


    @Override
    public Product getProductById(long id) {
        Product product = new Product();
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(GET_PRODUCT_BY_ID)) {
                int k = 0;
                ps.setLong(++k, id);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        product = mapProducts(resultSet);
                    }
                    return product;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private Product mapProducts(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setType(Type.valueOf(rs.getString("type").toUpperCase()));
        p.setSize(rs.getBigDecimal("size"));
        p.setColor(rs.getString("color"));
        p.setAmount(rs.getInt("amount"));
        p.setActual_price(rs.getBigDecimal("actual_price"));
        return p;
    }


    @Override
    public void deleteProduct(long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PRODUCT)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




}

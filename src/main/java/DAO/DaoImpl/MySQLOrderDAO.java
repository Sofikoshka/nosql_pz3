

package DAO.DaoImpl;



import DAO.MySQLConnectionManager;
import DAO.MySQLDAOConfig;
import DAO.OrderMongoDAO;
import entity.*;
import entity.enums.Role;
import entity.enums.OrderStatus;
import entity.enums.Type;

import java.sql.*;
import java.time.LocalDateTime;


public class MySQLOrderDAO implements OrderMongoDAO {

    private final MySQLConnectionManager connectionManager;

    public MySQLOrderDAO(MySQLDAOConfig config) {
        connectionManager = new MySQLConnectionManager(config);
    }

    private static final String ADD_ORDER = "INSERT INTO `order` (date, status, delivery) VALUES (?, DEFAULT, ?)";
    private static final String UPDATE_STATUS = "UPDATE `order` SET status=? WHERE id=?";
    private static final String ADD_DELIVERY = "INSERT INTO delivery (city, street, house, entrance, apartment) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_DELIVERY_BY_ID = "SELECT * from delivery WHERE id=?";
    private static final String GET_USER_BY_ID = "SELECT * from user WHERE id=?";
    private static final String GET_PRODUCT_BY_ID = "SELECT * from product WHERE id=?";
    private static final String INSERT_PRODUCTS_ORDER = "INSERT INTO `product_order` (product_id, order_id, amount, price) VALUES (?, ?, ?, ?)";
    private static final String INSERT_ORDER_USER = "INSERT INTO `user_order` (order_id, user_id, datetime) VALUES (?, ?, DEFAULT)";
    private static final String GET_ORDER_BY_ID = "SELECT * from `order` WHERE id=?";
    private static final String DELETE_ORDER = "DELETE FROM `order` WHERE id = ?";
    private static final String GET_PRODUCT_ORDER = "SELECT * FROM `product_order` WHERE order_id=?";
    private static final String GET_USER_ORDER = "SELECT * FROM `user_order` WHERE order_id=?";
    // private static final String GET_ROLE = "SELECT role FROM `user` WHERE id=?";
    private static final String DELETE_USER_ORDER_BY_ORDER_ID = "DELETE FROM user_order WHERE order_id = ?";


    @Override
    public long createOrder(OrderMongo order) {
        Connection con = null;
        PreparedStatement orderStatement = null;
        PreparedStatement productStatement = null;
        PreparedStatement userStatement = null;
        ResultSet generatedKeys = null;

        try {
            con = connectionManager.getConnection();
            con.setAutoCommit(false);

            // Check if the delivery already exists
            long deliveryId = order.getDelivery().getId();
            if (deliveryId == 0) {  // Assuming 0 represents a new delivery without an ID
                // If the delivery ID is not set, insert the delivery
                deliveryId = insertDelivery(con, order.getDelivery());
            }

            // Insert the order
            orderStatement = con.prepareStatement(ADD_ORDER, Statement.RETURN_GENERATED_KEYS);
            orderStatement.setObject(1, LocalDateTime.now());
            orderStatement.setLong(2, deliveryId);
            orderStatement.executeUpdate();

            // Get the order ID
            generatedKeys = orderStatement.getGeneratedKeys();
            long orderId;

            if (generatedKeys.next()) {
                orderId = generatedKeys.getLong(1);
                order.setId(orderId);

                // Insert products
                productStatement = con.prepareStatement(INSERT_PRODUCTS_ORDER);
                for (Product productOrder : order.getProducts()) {
                    productStatement.setLong(1, productOrder.getId());
                    productStatement.setLong(2, orderId);
                    productStatement.setInt(3, productOrder.getAmount());
                    productStatement.setBigDecimal(4, productOrder.getActual_price());
                    productStatement.addBatch();
                }
                productStatement.executeBatch();

                // Insert users
                userStatement = con.prepareStatement(INSERT_ORDER_USER);
                for (User user : order.getUsers()) {
                    userStatement.setLong(1, orderId);
                    userStatement.setLong(2, user.getId());
                    userStatement.addBatch();
                }
                userStatement.executeBatch();

                con.commit();
                return orderId;
            } else {
                con.rollback();
                throw new SQLException("Failed to retrieve Order ID");
            }
        } catch (SQLException ex) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error rolling back transaction", e);
            }
            throw new RuntimeException(ex);
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error setting auto-commit to true", e);
            }
            connectionManager.close(orderStatement, productStatement, userStatement, generatedKeys, con);
        }
    }

    private long insertDelivery(Connection con, Delivery delivery) throws SQLException {
        PreparedStatement deliveryStatement = null;
        ResultSet generatedKeys = null;

        try {
            // Insert delivery
            deliveryStatement = con.prepareStatement(ADD_DELIVERY, Statement.RETURN_GENERATED_KEYS);
            deliveryStatement.setString(1, delivery.getCity());
            deliveryStatement.setString(2, delivery.getStreet());
            deliveryStatement.setString(3, delivery.getHouse());
            deliveryStatement.setInt(4, delivery.getEntrance());
            deliveryStatement.setInt(5, delivery.getApartment());
            deliveryStatement.executeUpdate();

            // Get the delivery ID
            generatedKeys = deliveryStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("Failed to retrieve Delivery ID");
            }
        } finally {
            connectionManager.close(deliveryStatement, generatedKeys);
        }
    }




    @Override
    public OrderMongo getOrderById(long orderId) {
        OrderMongo order = new OrderMongo();
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(GET_ORDER_BY_ID)) {
                int k = 0;
                ps.setLong(++k, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        order = mapOrder(rs);
                        // Отримання інформації про користувачів
                        try (PreparedStatement userOrderPs = con.prepareStatement(GET_USER_ORDER)) {
                            int l = 0;
                            userOrderPs.setLong(++l, order.getId());
                            try (ResultSet userOrderRs = userOrderPs.executeQuery()) {
                                while (userOrderRs.next()) {
                                    UserOrder userOrder = mapUserOrder(userOrderRs);
                                    long userId = userOrder.getUserId();

                                    try (PreparedStatement getUserPs = con.prepareStatement(GET_USER_BY_ID)) {
                                        int m = 0;
                                        getUserPs.setLong(++m, userId);
                                        try (ResultSet userRs = getUserPs.executeQuery()) {
                                            while (userRs.next()) {
                                                User user = mapUsers(userRs);
                                                order.putUser(user);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Отримання інформації про продукти
                        try (PreparedStatement productOrderPs = con.prepareStatement(GET_PRODUCT_ORDER)) {
                            int l = 0;
                            productOrderPs.setLong(++l, order.getId());
                            try (ResultSet productOrderRs = productOrderPs.executeQuery()) {
                                while (productOrderRs.next()) {
                                    ProductOrder productOrder = mapProductOrder(productOrderRs);
                                    long productId = productOrder.getProductId();

                                    try (PreparedStatement getProductPs = con.prepareStatement(GET_PRODUCT_BY_ID)) {
                                        int m = 0;
                                        getProductPs.setLong(++m, productId);
                                        try (ResultSet productRs = getProductPs.executeQuery()) {
                                            while (productRs.next()) {
                                                Product product = mapProducts(productRs);
                                                order.addProduct(product);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Отримання інформації про доставку
                        try (PreparedStatement deliveryPs = con.prepareStatement(GET_DELIVERY_BY_ID)) {
                            int l = 0;
                            deliveryPs.setLong(++l, order.getDelivery().getId());
                            try (ResultSet deliveryRs = deliveryPs.executeQuery()) {
                                while (deliveryRs.next()) {
                                    Delivery delivery = mapDelivery(deliveryRs);
                                    order.setDelivery(delivery);
                                }
                            }
                        }
                    }
                }
                return order;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Delivery mapDelivery(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setId(rs.getLong("id"));
        d.setCity(rs.getString("city"));
        d.setStreet(rs.getString("street"));
        d.setHouse(rs.getString("house"));
        d.setEntrance(rs.getInt("entrance"));
        d.setApartment(rs.getInt("apartment"));

        return d;
    }


    private User mapUsers(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setLastname(rs.getString("lastname"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setRole(Role.valueOf(rs.getString("role").toUpperCase()));
        u.setPhone(rs.getString("phone"));
        return u;
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




    /*private Role getRole(Long id) {
        Role role = null;
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(GET_ROLE)) {
                int k = 0;
                ps.setLong(++k, id);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        role = Role.valueOf(resultSet.getString("role").toUpperCase());
                    }
                    return role;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
*/
    @Override
    public void updateStatus(Long orderId, OrderStatus status)  {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
                int k = 0;
                ps.setString(++k, String.valueOf(status));
                ps.setLong(++k, orderId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setProductOrder(long orderId, ProductOrder productOrder, PreparedStatement st) throws
            SQLException {
        int k = 0;
        st.setLong(++k, orderId);
        st.setLong(++k, productOrder.getProductId());
        st.setBigDecimal(++k, productOrder.getPrice());
        st.setInt(++k, productOrder.getAmount());
    }

   /* private static void setClient(OrderMongo order, PreparedStatement st) throws SQLException {
        st.setLong(1, order.getId());
        st.setLong(2, order.getUsers().get(Role.CLIENT).getUserId());
    }*/

    private OrderMongo mapOrder(ResultSet rs) throws SQLException {
        OrderMongo o = new OrderMongo();
        o.setId(rs.getInt("id"));
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        o.setDatetime(date);
        o.setStatus(OrderStatus.valueOf(rs.getString("status").toUpperCase()));
        entity.Delivery delivery = new entity.Delivery();
        delivery.setId(rs.getLong("id"));
        o.setDelivery(delivery);
        return o;
    }

    private ProductOrder mapProductOrder(ResultSet resultSet) throws SQLException {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductId(resultSet.getLong("product_id"));
        productOrder.setPrice(resultSet.getBigDecimal("price"));
        productOrder.setAmount(resultSet.getInt("amount"));
        return productOrder;
    }

    private UserOrder mapUserOrder(ResultSet rs) throws SQLException {
        UserOrder userOrder = new UserOrder();
        userOrder.setUserId(rs.getLong("user_id"));
        userOrder.setDateTime(rs.getTimestamp("datetime").toLocalDateTime());
        return userOrder;
    }

    @Override
    public void deleteOrder(long id) {
        try (Connection connection = connectionManager.getConnection()) {
            // Видаляємо пов'язані записи в таблиці user_order
            try (PreparedStatement userOrderStatement = connection.prepareStatement(DELETE_USER_ORDER_BY_ORDER_ID)) {
                userOrderStatement.setLong(1, id);
                userOrderStatement.executeUpdate();
            }


            try (PreparedStatement orderStatement = connection.prepareStatement(DELETE_ORDER)) {
                orderStatement.setLong(1, id);
                orderStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
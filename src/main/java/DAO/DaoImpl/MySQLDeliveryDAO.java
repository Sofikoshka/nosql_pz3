
package DAO.DaoImpl;



import DAO.DeliveryDAO;
import DAO.MySQLConnectionManager;
import DAO.MySQLDAOConfig;
import entity.Delivery;
import java.sql.*;


public class MySQLDeliveryDAO implements DeliveryDAO {

    private final MySQLConnectionManager connectionManager;

    public MySQLDeliveryDAO(MySQLDAOConfig config) {
        connectionManager = new MySQLConnectionManager(config);
    }

    private static final String ADD_DELIVERY = "INSERT INTO delivery (city, street, house, entrance, apartment) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_DELIVERY_BY_ID = "SELECT * from delivery WHERE id=?";
    private static final String UPDATE_STREET  = "UPDATE delivery SET street=? WHERE id=?";
    private static final String DELETE_DELIVERY = "DELETE FROM delivery WHERE id = ?";


    @Override
    public long createDelivery(Delivery delivery) {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(ADD_DELIVERY, Statement.RETURN_GENERATED_KEYS)) {
                int k = 0;
                ps.setString(++k, delivery.getCity());
                ps.setString(++k, delivery.getStreet());
                ps.setString(++k, delivery.getHouse());
                ps.setInt(++k, delivery.getEntrance());
                ps.setInt(++k, delivery.getApartment());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        delivery.setId(keys.getLong(1));
                    }
                }
            }
            return delivery.getId();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    public Delivery findById(long id) {
        Delivery Delivery = new Delivery();
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(GET_DELIVERY_BY_ID)) {
                int k = 0;
                ps.setLong(++k, id);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        Delivery = mapDeliveries(resultSet);
                    }
                    return Delivery;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Delivery mapDeliveries(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setId(rs.getLong("id"));
        d.setCity(rs.getString("city"));
        d.setStreet(rs.getString("street"));
        d.setHouse(rs.getString("house"));
        d.setEntrance(rs.getInt("entrance"));
        d.setApartment(rs.getInt("apartment"));

        return d;
    }

    @Override
    public void updateStreet(long orderId, String newStreet) {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(UPDATE_STREET)) {
                int k = 0;
                ps.setString(++k, newStreet);
                ps.setLong(++k, orderId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deleteDelivery(long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_DELIVERY)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}

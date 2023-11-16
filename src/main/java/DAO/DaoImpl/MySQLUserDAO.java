package DAO.DaoImpl;

import DAO.MySQLConnectionManager;
import DAO.MySQLDAOConfig;
import DAO.UserDAO;
import entity.User;
import entity.enums.Role;

import java.sql.*;

public class MySQLUserDAO implements UserDAO {
    private final MySQLConnectionManager connectionManager;

    public MySQLUserDAO(MySQLDAOConfig config) {
        connectionManager = new MySQLConnectionManager(config);
    }


    private static final String ADD_USER = "INSERT INTO user (name, lastname, email, phone, password) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_USER_BY_ID = "SELECT * from user WHERE id=?";
    private static final String UPDATE_EMAIL = "UPDATE user SET email=? WHERE id=?";
    private static final String DELETE_USER = "DELETE FROM user WHERE id = ?";
    private static final String LOGIN = "SELECT * from user WHERE email=? AND password=?";


    @Override
    public long createUser(User user) {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(ADD_USER, Statement.RETURN_GENERATED_KEYS)) {
                int k = 0;
                ps.setString(++k, user.getName());
                ps.setString(++k, user.getLastname());
                ps.setString(++k, user.getEmail());
                ps.setString(++k, user.getPhone());
                ps.setString(++k, user.getPassword());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getLong(1));
                    }
                }
            }
            return user.getId();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public User login(String email, String password) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(LOGIN)) {

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUsers(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public User findById(long id) {
        User user = new User();
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(GET_USER_BY_ID)) {
                int k = 0;
                ps.setLong(++k, id);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        user = mapUsers(resultSet);
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public void updateUser(long userId, String newEmail) {
        try (Connection con = connectionManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(UPDATE_EMAIL)) {
                int k = 0;
                ps.setString(++k, newEmail);
                ps.setLong(++k, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void deleteUser(long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




}



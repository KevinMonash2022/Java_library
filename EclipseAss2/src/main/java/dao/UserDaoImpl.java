package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.User;

public class UserDaoImpl implements UserDao {
    private final String TABLE_NAME = "users";
    private final String BOOK_TABLE_NAME = "books";


    public UserDaoImpl() {
    }

    @Override
    public void setup() throws SQLException {
        try (Connection connection = Database.getConnection();
                Statement stmt = connection.createStatement();) {
        
        	// Create users table if not exists
        	String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (username VARCHAR(10) NOT NULL,"
                    + "password VARCHAR(8) NOT NULL," 
                    + "firstName VARCHAR(50) NOT NULL,"
                    + "lastName VARCHAR(50) NOT NULL,"
                    + "PRIMARY KEY (username))";
            stmt.executeUpdate(sql);
            
         
            // Create books table if not exists
            String sqlBookTable = "CREATE TABLE IF NOT EXISTS " + BOOK_TABLE_NAME + " ("
                    + "title VARCHAR(100) NOT NULL,"
                    + "authors VARCHAR(100) NOT NULL,"
                    + "physicalCopies INTEGER NOT NULL,"
                    + "price DOUBLE NOT NULL,"
                    + "soldCopies INTEGER NOT NULL,"
                    + "PRIMARY KEY (title))";
            stmt.executeUpdate(sqlBookTable);
            
            // Create cart table if not exists
            String sqlCartTable = "CREATE TABLE IF NOT EXISTS cart ("
                    + "username VARCHAR(10) NOT NULL,"
                    + "title VARCHAR(100) NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "FOREIGN KEY (username) REFERENCES users(username),"
                    + "FOREIGN KEY (title) REFERENCES books(title))";
            stmt.executeUpdate(sqlCartTable);
            
            // Create order history table if not exists
            String sqlOrderHistoryTable = "CREATE TABLE IF NOT EXISTS order_history ("
                    + "orderNumber INTEGER NOT NULL,"
                    + "username VARCHAR(10) NOT NULL,"
                    + "title VARCHAR(100) NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "orderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (orderNumber, title),"
                    + "FOREIGN KEY (username) REFERENCES users(username),"
                    + "FOREIGN KEY (title) REFERENCES books(title))";
            stmt.executeUpdate(sqlOrderHistoryTable);
            
            
        } 
    }

    @Override
    public User getUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?";
        try (Connection connection = Database.getConnection(); 
                PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFirstName(rs.getString("firstName"));
                    user.setLastName(rs.getString("lastName"));
                    return user;
                }
                return null;
            } 
        }
    }

    @Override
    public User createUser(String username, String password, String firstName, String lastName) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (username, password, firstName, lastName) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);

            stmt.executeUpdate();
            return new User(username, password, firstName, lastName);
        } 
    }
    
    @Override
    public void updateUser(String username, String firstName, String lastName, String password) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET firstName = ?, lastName = ?, password = ? WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, password);
            stmt.setString(4, username);

            stmt.executeUpdate();
        }
    }
}

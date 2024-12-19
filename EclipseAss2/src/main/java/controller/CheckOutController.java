package controller;

import dao.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.User;
import java.util.Random;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CheckOutController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private Label totalAmountLabel;
    @FXML
    private TextField cardNumberField;
    @FXML
    private TextField expiryDateField;
    @FXML
    private TextField cvvField;
    @FXML
    private Button checkOutButton;
    @FXML
    private Button returnButton;
    @FXML
    private Label statusLabel;

    public CheckOutController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadTotalAmount();

        checkOutButton.setOnAction(event -> {
            if (validateCardInfo()) {
                completeCheckOut();
            }
        });

        returnButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    private void loadTotalAmount() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            String sql = "SELECT SUM(price * quantity) AS totalAmount FROM cart INNER JOIN books ON cart.title = books.title WHERE username = ?";
            try (Connection connection = Database.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, currentUser.getUsername());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double totalAmount = rs.getDouble("totalAmount");
                        totalAmountLabel.setText(String.format("$%.2f", totalAmount));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateCardInfo() {
        String cardNumber = cardNumberField.getText();
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            statusLabel.setText("Card number must be 16 digits.");
            return false;
        }

        try {
            YearMonth expiry = YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("MM/yy"));
            if (expiry.isBefore(YearMonth.now())) {
                statusLabel.setText("Expiry date must be in the future.");
                return false;
            }
        } catch (DateTimeParseException e) {
            statusLabel.setText("Invalid expiry date format. Use MM/YY.");
            return false;
        }

        if (cvv.length() != 3 || !cvv.matches("\\d+")) {
            statusLabel.setText("CVV must be 3 digits.");
            return false;
        }

        return true;
    }
    
    
    private int generateUniqueOrderNumber(Connection connection) throws SQLException {
        Random random = new Random();
        int orderNumber;
        boolean isUnique;

        do {
            orderNumber = 10000 + random.nextInt(90000); 
            String sqlCheckOrderNumber = "SELECT COUNT(*) AS count FROM order_history WHERE orderNumber = ?";
            try (PreparedStatement stmtCheck = connection.prepareStatement(sqlCheckOrderNumber)) {
                stmtCheck.setInt(1, orderNumber);
                try (ResultSet rs = stmtCheck.executeQuery()) {
                    rs.next();
                    isUnique = rs.getInt("count") == 0;
                }
            }
        } while (!isUnique);

        return orderNumber;
    }
    
    

    private void completeCheckOut() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getUsername();

            try (Connection connection = Database.getConnection()) {
                // Step 1: Generate a unique 5-digit order number
                int orderNumber = generateUniqueOrderNumber(connection);

                // Step 2: Insert each book in the cart into the order history table with the generated order number
                String sqlOrder = "INSERT INTO order_history (orderNumber, username, title, quantity, orderDate) "
                        + "SELECT ?, username, title, quantity, CURRENT_TIMESTAMP FROM cart WHERE username = ?";
                try (PreparedStatement stmtOrder = connection.prepareStatement(sqlOrder)) {
                    stmtOrder.setInt(1, orderNumber);
                    stmtOrder.setString(2, username);
                    int rowsInserted = stmtOrder.executeUpdate();
                    if (rowsInserted > 0) {
                        clearCart(username);
                        statusLabel.setText("Order placed successfully!");
                    } else {
                        statusLabel.setText("Failed to place order.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error occurred while placing order.");
            }
        }
    }


    private void clearCart(String username) {
        String sqlClear = "DELETE FROM cart WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmtClear = connection.prepareStatement(sqlClear)) {
            stmtClear.setString(1, username);
            stmtClear.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Check Out");
        stage.show();
    }
}

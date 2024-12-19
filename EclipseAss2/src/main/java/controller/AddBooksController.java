package controller;

import dao.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class AddBooksController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private ComboBox<String> bookComboBox;
    @FXML
    private TextField quantityTextField;
    @FXML
    private Button addToCartButton;
    @FXML
    private Button modifyCartButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextArea statusTextArea;  // TextArea to display multi-line status messages

    public AddBooksController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadBooks();

        addToCartButton.setOnAction(event -> addBookToCart());
        modifyCartButton.setOnAction(event -> openModifyCartPage());
        cancelButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    private void loadBooks() {
        String sql = "SELECT title FROM books";
        ObservableList<String> bookTitles = FXCollections.observableArrayList();
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                bookTitles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bookComboBox.setItems(bookTitles);
    }

    private void addBookToCart() {
        String selectedBook = bookComboBox.getValue();
        String quantityText = quantityTextField.getText();

        if (selectedBook != null && !quantityText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity > 0) {
                    User currentUser = model.getCurrentUser();
                    if (currentUser != null) {
                        String sql = "INSERT INTO cart (username, title, quantity) VALUES (?, ?, ?)";
                        try (Connection connection = Database.getConnection();
                             PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setString(1, currentUser.getUsername());
                            stmt.setString(2, selectedBook);
                            stmt.setInt(3, quantity);
                            stmt.executeUpdate();
                            appendStatusMessage("Added to cart: " + selectedBook + " (Quantity: " + quantity + ")");
                        }
                    }
                } else {
                    appendStatusMessage("Please enter a positive quantity.");
                }
            } catch (NumberFormatException e) {
                appendStatusMessage("Please enter a valid number for quantity.");
            } catch (SQLException e) {
                e.printStackTrace();
                appendStatusMessage("Error occurred while adding to cart.");
            }
        } else {
            appendStatusMessage("Please select a book and enter a quantity.");
        }
    }

    private void placeOrder() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getUsername();
            String sql = "INSERT INTO order_history (username, title, quantity) "
                    + "SELECT username, title, quantity FROM cart WHERE username = ?";
            try (Connection connection = Database.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    appendStatusMessage("Order placed successfully!");
                    clearCart(username);
                } else {
                    appendStatusMessage("No items in the cart to place an order.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                appendStatusMessage("Error occurred while placing order.");
            }
        }
    }

    private void clearCart(String username) {
        String sql = "DELETE FROM cart WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            appendStatusMessage("Cart cleared after placing order.");
        } catch (SQLException e) {
            e.printStackTrace();
            appendStatusMessage("Error occurred while clearing cart.");
        }
    }
    
    
    private void openModifyCartPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UpdateCartView.fxml"));
            UpdateCartController updateCartController = new UpdateCartController(stage, model);
            loader.setController(updateCartController);
            Pane root = loader.load();
            updateCartController.showStage(root);
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
            appendStatusMessage("Error occurred while opening the Modify Cart page.");
        }
    }

    private void appendStatusMessage(String message) {
        // Append new message to the existing content of the TextArea
        String existingMessages = statusTextArea.getText();
        statusTextArea.setText(existingMessages + (existingMessages.isEmpty() ? "" : "\n") + message);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Add Books to Cart");
        stage.show();
    }
}

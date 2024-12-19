package controller;

import dao.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.User;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateCartController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private ListView<String> cartListView;
    @FXML
    private ComboBox<String> bookComboBox;
    @FXML
    private TextField quantityTextField;
    @FXML
    private Button updateQuantityButton;
    @FXML
    private Button removeBookButton;
    @FXML
    private Button checkOutButton;
    @FXML
    private Button returnButton;

    public UpdateCartController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadCart();

        updateQuantityButton.setOnAction(event -> updateBookQuantity());
        removeBookButton.setOnAction(event -> removeBookFromCart());        
        returnButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
        
        checkOutButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CheckOutView.fxml"));
                CheckOutController checkOutController = new CheckOutController(stage, model);
                loader.setController(checkOutController);
                Pane root = loader.load();
                checkOutController.showStage(root);
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadCart() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            String sql = "SELECT title, quantity FROM cart WHERE username = ?";
            ObservableList<String> cartItems = FXCollections.observableArrayList();
            ObservableList<String> bookTitles = FXCollections.observableArrayList();
            try (Connection connection = Database.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, currentUser.getUsername());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String title = rs.getString("title");
                        int quantity = rs.getInt("quantity");
                        cartItems.add(title + " (Quantity: " + quantity + ")");
                        bookTitles.add(title);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            cartListView.setItems(cartItems);
            bookComboBox.setItems(bookTitles);
        }
    }

    private void updateBookQuantity() {
        String selectedBook = bookComboBox.getValue();
        String quantityText = quantityTextField.getText();

        if (selectedBook != null && !quantityText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity > 0) {
                    User currentUser = model.getCurrentUser();
                    if (currentUser != null) {
                        String sql = "UPDATE cart SET quantity = ? WHERE username = ? AND title = ?";
                        try (Connection connection = Database.getConnection();
                             PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setInt(1, quantity);
                            stmt.setString(2, currentUser.getUsername());
                            stmt.setString(3, selectedBook);
                            int rowsUpdated = stmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                loadCart();
                            }
                        }
                    }
                } else {
                    System.out.println("Please enter a positive quantity.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for quantity.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Please select a book and enter a quantity.");
        }
    }

    private void removeBookFromCart() {
        String selectedBook = bookComboBox.getValue();

        if (selectedBook != null) {
            User currentUser = model.getCurrentUser();
            if (currentUser != null) {
                String sql = "DELETE FROM cart WHERE username = ? AND title = ?";
                try (Connection connection = Database.getConnection();
                     PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, currentUser.getUsername());
                    stmt.setString(2, selectedBook);
                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        loadCart();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Please select a book to remove.");
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Update Shopping Cart");
        stage.show();
    }
}

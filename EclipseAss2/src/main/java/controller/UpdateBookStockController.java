package controller;

import dao.Database;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateBookStockController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private ComboBox<String> bookComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private Button updateButton;
    @FXML
    private Button returnButton;
    @FXML
    private Label statusLabel;

    public UpdateBookStockController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadBooks();

        updateButton.setOnAction(event -> updateStock());

        returnButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    private void loadBooks() {
        String sql = "SELECT title FROM books";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                bookComboBox.getItems().add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading books.");
            statusLabel.setTextFill(Color.RED);
        }
    }

    private void updateStock() {
        String selectedBook = bookComboBox.getValue();
        String quantityStr = quantityField.getText();

        if (selectedBook == null || quantityStr.isEmpty()) {
            statusLabel.setText("Please select a book and enter a quantity.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                statusLabel.setText("Quantity must be a non-negative number.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            String sql = "UPDATE books SET physicalCopies = physicalCopies + ? WHERE title = ?";
            try (Connection connection = Database.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setString(2, selectedBook);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    statusLabel.setText("Stock updated successfully for: " + selectedBook);
                    statusLabel.setTextFill(Color.GREEN);
                } else {
                    statusLabel.setText("Failed to update stock for: " + selectedBook);
                    statusLabel.setTextFill(Color.RED);
                }
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid quantity. Please enter a number.");
            statusLabel.setTextFill(Color.RED);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error occurred.");
            statusLabel.setTextFill(Color.RED);
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Update Book Stock");
        stage.show();
    }
}

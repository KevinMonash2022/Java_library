package controller;

import dao.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Model;
import model.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewAllOrdersController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private ListView<String> ordersListView;
    @FXML
    private Button exportButton;
    @FXML
    private Button returnButton;
    @FXML
    private Label exportStatusLabel;

    public ViewAllOrdersController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadOrders();

        returnButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });

        exportButton.setOnAction(event -> exportSelectedOrders());
    }

    private void loadOrders() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getUsername();
            String sql = "SELECT oh.orderNumber, oh.orderDate, SUM(b.price * oh.quantity) AS totalAmount, "
                       + "GROUP_CONCAT(b.title || ' (Qty: ' || oh.quantity || ')', '; ') AS booksPurchased "
                       + "FROM order_history oh "
                       + "INNER JOIN books b ON oh.title = b.title "
                       + "WHERE oh.username = ? "
                       + "GROUP BY oh.orderNumber, oh.orderDate "
                       + "ORDER BY oh.orderDate DESC";

            ObservableList<String> orders = FXCollections.observableArrayList();
            try (Connection connection = Database.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int orderNumber = rs.getInt("orderNumber");
                        String orderDate = rs.getString("orderDate");
                        double totalAmount = rs.getDouble("totalAmount");
                        String booksPurchased = rs.getString("booksPurchased");

                        String orderInfo = String.format("Order Number: %d\nDate: %s\nTotal Price: $%.2f\nBooks: %s\n",
                                orderNumber, orderDate, totalAmount, booksPurchased);
                        orders.add(orderInfo);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ordersListView.setItems(orders);
        }
    }

    private void exportSelectedOrders() {
        ObservableList<String> selectedOrders = ordersListView.getSelectionModel().getSelectedItems();
        if (selectedOrders.isEmpty()) {
            exportStatusLabel.setText("No orders selected for export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Orders");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Order Number,Date,Total Price,Books Purchased\n");
                for (String order : selectedOrders) {
                    String csvFormattedOrder = order.replaceAll("Order Number: |\nDate: |\nTotal Price: |\nBooks: |\n", "").replaceAll("\n", "");
                    writer.write(csvFormattedOrder + "\n");
                }
                writer.flush();
                exportStatusLabel.setText("Orders exported successfully to: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                exportStatusLabel.setText("Failed to export orders.");
            }
        } else {
            exportStatusLabel.setText("Export cancelled.");
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("View All Orders");
        stage.show();
    }
}
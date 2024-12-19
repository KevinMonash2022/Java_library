package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import javafx.fxml.FXMLLoader;


import java.io.IOException;

public class AdminDashboardController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button viewBooksButton;
    @FXML
    private Button updateStockButton;
    @FXML
    private Button logOutButton;

    public AdminDashboardController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + model.getCurrentUser().getFirstName() + "!");

        viewBooksButton.setOnAction(event -> {
            try {
                // Load the view to display all books
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ViewAllBooksView.fxml"));
                ViewAllBooksController viewAllBooksController = new ViewAllBooksController(stage, model);
                loader.setController(viewAllBooksController);
                Pane root = loader.load();
                viewAllBooksController.showStage(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        updateStockButton.setOnAction(event -> {
            try {
                // Load the view to update book stock
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UpdateBookStockView.fxml"));
                UpdateBookStockController updateBookStockController = new UpdateBookStockController(stage, model);
                loader.setController(updateBookStockController);
                Pane root = loader.load();
                updateBookStockController.showStage(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        logOutButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }
}

package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import dao.Database;
import model.Model;
import model.User;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class HomeController {
    private Model model;
    private Stage stage;
    private Stage parentStage;


    @FXML
    private MenuItem updateProfile;
    @FXML
    private MenuItem addBooks;
    @FXML
    private MenuItem cart;
    @FXML
    private MenuItem checkOut;
    @FXML
    private MenuItem viewAllBooks;
    
    
    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView<String> topBooksListView;
    @FXML
    private Button logOutButton;
    @FXML
    private Button refreshButton;

    public HomeController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
    	updateWelcomeMessage();

        loadTopBooks();

        logOutButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
        
        
        updateProfile.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UpdateProfileView.fxml"));
                UpdateProfileController updateProfileController = new UpdateProfileController(stage, model);
                loader.setController(updateProfileController);
                Pane root = loader.load();
                updateProfileController.showStage(root);
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        
        refreshButton.setOnAction(event -> {
            updateWelcomeMessage();
            loadTopBooks();
        });
        
        
        addBooks.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddBooksView.fxml"));
                AddBooksController addBooksController = new AddBooksController(stage, model);
                loader.setController(addBooksController);
                Pane root = loader.load();
                addBooksController.showStage(root);
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        
        cart.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UpdateCartView.fxml"));
                UpdateCartController updateCartController = new UpdateCartController(stage, model);
                loader.setController(updateCartController);
                Pane root = loader.load();
                updateCartController.showStage(root);
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        
        checkOut.setOnAction(event -> {
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
        
        
        viewAllBooks.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ViewAllOrdersView.fxml"));
                ViewAllOrdersController viewAllOrdersController = new ViewAllOrdersController(stage, model);
                loader.setController(viewAllOrdersController);
                Pane root = loader.load();
                viewAllOrdersController.showStage(root);
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        
    }
    
    private void updateWelcomeMessage() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome " + currentUser.getFirstName() + " " + currentUser.getLastName() + "!");
        }
    }

    private void loadTopBooks() {
        String sql = "SELECT title, authors, soldCopies FROM books ORDER BY soldCopies DESC LIMIT 5";
        ObservableList<String> topBooks = FXCollections.observableArrayList();
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String bookInfo = rs.getString("title") + " by " + rs.getString("authors") + " (Sold Copies: " + rs.getInt("soldCopies") + ")";
                topBooks.add(bookInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        topBooksListView.setItems(topBooks);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 350);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Home");
        stage.show();
    }
}

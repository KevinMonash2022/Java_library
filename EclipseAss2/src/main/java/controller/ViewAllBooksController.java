package controller;

import dao.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewAllBooksController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private ListView<String> booksListView;
    @FXML
    private Button returnButton;

    public ViewAllBooksController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        loadBooks();

        returnButton.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    private void loadBooks() {
        String sql = "SELECT title, authors, price, physicalCopies, soldCopies FROM books";

        ObservableList<String> books = FXCollections.observableArrayList();
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String bookInfo = String.format("Title: %s\nAuthors: %s\nPrice: $%.2f\nPhysical Copies: %d\nSold Copies: %d\n",
                        rs.getString("title"), rs.getString("authors"), rs.getDouble("price"),
                        rs.getInt("physicalCopies"), rs.getInt("soldCopies"));
                books.add(bookInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        booksListView.setItems(books);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("View All Books");
        stage.show();
    }
}

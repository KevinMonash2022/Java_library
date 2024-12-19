package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import dao.UserDao;
import model.Model;
import model.User;

import java.sql.SQLException;

public class UpdateProfileController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private TextField newFirstName;
    @FXML
    private TextField newLastName;
    @FXML
    private TextField newPassword;
    @FXML
    private Button updateProfile;
    @FXML
    private Button cancel;

    public UpdateProfileController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        User currentUser = model.getCurrentUser();
        if (currentUser != null) {
            newFirstName.setText(currentUser.getFirstName());
            newLastName.setText(currentUser.getLastName());
            newPassword.setText(currentUser.getPassword());
        }

        updateProfile.setOnAction(event -> {
            String updatedFirstName = newFirstName.getText();
            String updatedLastName = newLastName.getText();
            String updatedPassword = newPassword.getText();

            if (!updatedFirstName.isEmpty() && !updatedLastName.isEmpty() && !updatedPassword.isEmpty()) {
                try {
                    UserDao userDao = model.getUserDao();
                    userDao.updateUser(currentUser.getUsername(), updatedFirstName, updatedLastName, updatedPassword);
                    
                    // Update the current user in the model
                    currentUser.setFirstName(updatedFirstName);
                    currentUser.setLastName(updatedLastName);
                    currentUser.setPassword(updatedPassword);
                    model.setCurrentUser(currentUser);

                    // Close update profile window and go back to home
                    stage.close();
                    parentStage.show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        cancel.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Update Profile");
        stage.show();
    }
}

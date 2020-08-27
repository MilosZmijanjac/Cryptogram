package controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import models.MyCertificate;
import models.User;
import services.CertificateUtil;
import services.PasswordUtil;
import services.UserUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class LoginController {

    @ FXML private JFXTextField userField;
    @ FXML private JFXPasswordField passField;
    @ FXML private JFXButton loginBtn;
    public static User user=new User();
    double xOffset;
    double yOffset;

    public void onLoginClick(@SuppressWarnings("UnusedParameters")ActionEvent actionEvent) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        user.setUserName(userField.getText());
        if(user.getUserName().isEmpty()||passField.getText().isEmpty())
            errorWindow("Invalid username or password!!!");
        else{
            File userAccount = new File(user.getUserPath());
            if(userAccount.exists()){
                user=UserUtil.loadUserData(userAccount);
                if(user.isActive())
                    errorWindow("This user is already logged in!!!");
                else{
                    if(PasswordUtil.compareSaltedHashWithUserEnteredPwd(user.getSalt(),passField.getText(),user.getHash())){
                        user.setActivity(true);
                        UserUtil.storeUserData(user,new File(user.getUserPath()));
                        System.out.println("welcome "+user.getUserName());

                        if(validateCertificate(user.getUserName()))
                            showHome();
                        else
                            errorWindow("Invalid Certificate");

                    }
                    else{
                        errorWindow("Invalid username or password!!!");
                    }
                }
            }
            else{
                errorWindow("Invalid username or password!!!");
            }
        }
    }

    public void onExitClick(@SuppressWarnings("UnusedParameters")ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    private void showHome() throws IOException {
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("../views/HomeView.fxml"));
        root.setOnMousePressed(event ->{
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event ->{
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
        );
            stage.getIcons().clear();
            stage.setScene(new Scene(root));

            stage.show();
    }

    private boolean validateCertificate(String userName) {
        MyCertificate certDetails = CertificateUtil.getCertificateDetails("src" + File.separator + "resources" + File.separator + "accounts"+
                File.separator+userName+File.separator+userName+"-keystore.jks", "password");
        return certDetails.validateCertificate();
    }

    private void errorWindow(String message) {
        Platform.runLater(()-> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}

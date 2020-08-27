package controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.MyCertificate;
import models.User;
import services.CertificateUtil;
import services.RSAUtil;
import services.SteganographyUtil;
import services.SymmetricAlgorithms;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController implements Initializable {
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView iconView;
    @FXML
    private ImageView iconView1;
    @FXML
    private VBox chatBox;
    @FXML
    private JFXTextArea chatArea;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private JFXButton sendBtn;
    static User user;
    static User userToChat;
    boolean align=false;
    public static boolean inChat;
    private static List<String> list = new ArrayList<>();
    public static ObservableList<String> observableList = FXCollections.observableList(list);
    private SymmetricAlgorithms sa;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        user = new User(HomeController.user);
        userToChat = new User(HomeController.userToChat);

        SecretKey symmetricKey = HomeController.symmetricKey;
        try {
            sa=new SymmetricAlgorithms("DES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        sa.setSymmetricKey(symmetricKey);

        titleLabel.setText("You are now chatting with " + userToChat.getUserName());
        setIcons();
        scrollPane.vvalueProperty().bind(chatBox.heightProperty());
        initObservableList();

    }

    public void onClickSend(@SuppressWarnings("UnusedParameters")ActionEvent actionEvent) {
        String firstMessage = chatArea.getText();
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        String message =  "<from>"+user.getUserName()+"</from><time>"+timeStamp+"</time><content>"+firstMessage+"</content>";
        String finalMessage =makeMessageToWrite(message);
        align=true;
       // Platform.runLater(()-> observableList.add(finalMessage));
        showMessage(message);
        chatArea.clear();

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(userToChat.getInboxDirectoryPath() + File.separator + user.getUserName() + ".txt")));
            out.println(finalMessage);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void showMessage(String msg) {
        Pattern pattern = Pattern.compile("<content>(.+?)</content>");
        Matcher matcher = pattern.matcher(msg);
        Label label= new Label("");
        Tooltip tooltip = new Tooltip();
        tooltip.setText(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));

        if(matcher.find()) {
            label.setText(" " + matcher.group(1) + " ");
            label.setTooltip(tooltip);
        }
        VBox tile=new VBox();
        tile.setPrefHeight(30);
        tile.setPrefWidth(470);

        label.setPrefHeight(30);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setWrapText(true);
        label.setAlignment(Pos.BASELINE_CENTER);
        if(align)
            tile.setAlignment(Pos.BASELINE_RIGHT);
        else
            tile.setAlignment(Pos.BASELINE_LEFT);

        label.setBackground(new Background(new BackgroundFill(Paint.valueOf("#2196f3"), new CornerRadii(8), Insets.EMPTY)));
        label.setTextFill(Color.WHITE);
        tile.getChildren().add(label);
        chatBox.getChildren().add(tile);

    }

    private String makeMessageToWrite(String firstMessage) {
        String messageToWrite = "";
        try {
            System.out.println("in makeMessage ToWrite  "+firstMessage);
            //enkripcija poruke
            String encryptedMessage = sa.symmetricEncrypt(firstMessage);


            //hesiranje poruke
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] encryptedMessageDigest = md.digest(encryptedMessage.getBytes(StandardCharsets.UTF_8));

            //izvlacenje privatnog kljuca
            MyCertificate certificateDetails = CertificateUtil.getCertificateDetails(user.getKeyStorePath(), "password");
            PrivateKey privateKey=certificateDetails.getPrivateKey();

            //digitalno potpisivanje
            String digitalSignature = RSAUtil.encrypt(new String(encryptedMessageDigest), privateKey);

            messageToWrite += digitalSignature + "#terminate#" +encryptedMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return messageToWrite;
    }

    private String makeMessageToRead(String message) throws Exception{
        System.out.println(message);
        int index = message.indexOf("#terminate#");
        String digitalSignature = message.substring(0, index);
        String encryptedMessage = message.substring(index + 11);

        //izdvajanje javnog kljuca
        PublicKey publicKey = CertificateUtil.getPublicKey(user.getTrustStorePath(), "password", userToChat.getUserName() );

        //dekriptovanje digitalnog potpisa
        String firstDigest = RSAUtil.decrypt(digitalSignature, publicKey);


        //hesiranje enkriptovane prenesene poruke
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] secondDigestInBytes = md.digest(encryptedMessage.getBytes(StandardCharsets.UTF_8));
        String secondDigest = new String(secondDigestInBytes);


        if (!firstDigest.equals(secondDigest))
            throw new Exception("Error!!");

        //Return decrypted message
        return sa.symmetricDecrypt(encryptedMessage);
    }

    public void onExitClick(@SuppressWarnings("UnusedParameters")ActionEvent actionEvent) {
            if(inChat) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extensionFilter);
                File selectedFile = fileChooser.showOpenDialog(null);
                if (!selectedFile.exists())
                    return;
                try {
                    File newFile = new File(selectedFile.getParent() + File.separator + userToChat.getUserName() + "-END.png");
                    SteganographyUtil.encode(selectedFile, "END", newFile);
                    byte[] toWrite = ("<from>" + user.getUserName() + "</from><type>termination</type><content>" + newFile.toString() + "</content>").getBytes(StandardCharsets.UTF_8);
                    Files.write(new File(userToChat.getInboxDirectoryPath() + File.separator + user.getUserName() + ".txt").toPath(), toWrite);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        Stage stage = (Stage) sendBtn.getScene().getWindow();
        stage.close();
    }

    public static void endChat(){
        Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("CRYPTOGRAM");
                    alert.setHeaderText(null);
                    alert.setContentText("Chat session with "+userToChat.getUserName()+" ended");
                    alert.showAndWait();
                }
        );

    }

    private void setIcons() {
        try {
            iconView.setImage(new Image(new FileInputStream("src" + File.separator + "resources" + File.separator + "view_resources" + File.separator + "user_icons" + File.separator + user.getUserName() + ".png")));
            iconView1.setImage(new Image(new FileInputStream("src" + File.separator + "resources" + File.separator + "view_resources" + File.separator + "user_icons" + File.separator + userToChat.getUserName() + ".png")));
        }catch ( FileNotFoundException e){
            e.printStackTrace();
        }
    }

    private synchronized void initObservableList(){
        observableList.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if(change.wasAdded())
                {Platform.runLater(()->{
                    try {align=false;
                        showMessage(makeMessageToRead(observableList.get(observableList.size()-1)));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    });}
            }
        });
    }
}

package controllers;

import com.jfoenix.controls.JFXButton;

import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.MyCertificate;
import models.User;
import services.*;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HomeController implements Initializable {
   @FXML private JFXButton logoutBtn;
   @FXML private Label titleLabel;
   @FXML private ImageView iconView;
   @FXML private Image userIcon;
   @FXML private JFXListView<User> activeUsersList;
   public  ObservableList<User> listItems = FXCollections.observableArrayList();
   public  ArrayList<User> activeUsersArray= new ArrayList<>();

   public static User user;
   public static User userToChat;
   public static SecretKey symmetricKey;
   private boolean update;
   private static double xOffset = 0;
   private static double yOffset = 0;
   InboxMonitor im;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        update=true;
        user=new User(LoginController.user);
        titleLabel.setText("Welcome "+user.getUserName());
        setIcon();

        setCellFactory();
        activeUsersList.setItems(listItems);
        setUserActivityListener();


        im = new InboxMonitor(user.getInboxDirectoryPath());
        new Thread(im).start();

    }

    private void setUserActivityListener(){
       new Thread(()-> {
           try {
               while(update) {
                   initListView();
                   Thread.sleep(1000);
               }
           } catch ( InterruptedException e) {
               e.printStackTrace();
           }
       }).start();
    }

    private void setCellFactory(){
        activeUsersList.setCellFactory(cell -> new ListCell<>() {
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                if (u == null || empty)
                    setText(null);
                else
                    setText(u.getUserName());
            }
        });
    }

    public synchronized void initListView(){
        Platform.runLater(()-> {
            listItems.clear();
            try {
                activeUsersArray = getActiveUsers();
            } catch (IOException|ClassNotFoundException  e) {
                e.printStackTrace();
            }
            listItems.addAll(activeUsersArray);
            activeUsersList.refresh();
        });
    }

    private synchronized ArrayList<User> getActiveUsers() throws IOException, ClassNotFoundException {
        activeUsersArray.clear();
        User u;
        ArrayList<User> activeUsers = new ArrayList<>();
        File[] tmp = new File("src" + File.separator + "resources" + File.separator + "accounts").listFiles();

        assert tmp != null;
        for(File x : tmp){
            u=new User(UserUtil.loadUserData(new File(x.getPath()+File.separator+x.getName())));
            if(u.isActive()&&(!u.getUserName().equals(user.getUserName()))) {
                activeUsers.add(u);
                //System.out.println(u.getUserName());
            }
        }
        return activeUsers;
    }

    public void onExitClick(@SuppressWarnings("UnusedParameters")ActionEvent actionEvent) throws IOException {
        user.setActivity(false);
        UserUtil.storeUserData(user,new File(user.getUserPath()));
        update=false;
        Platform.exit();
        System.exit(0);
    }

    public void userChoose(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount()==2) {
            userToChat=new User(activeUsersList.getSelectionModel().getSelectedItem());
            Platform.runLater(()-> {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Do you want to start a chat with "+ userToChat.getUserName(),ButtonType.YES,ButtonType.NO);
                alert.setTitle("CRYPTOGRAM");
                alert.setHeaderText(null);
                alert.showAndWait()
                        .filter(response -> response == ButtonType.YES)
                        .ifPresent(response ->
                        {
                            try {
                                onClickYesBtn();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

            });}
    }

    private synchronized void onClickYesBtn() {
         FileChooser fileChooser = new FileChooser();
         FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
         fileChooser.getExtensionFilters().add(extensionFilter);
         File selectedFile = fileChooser.showOpenDialog(null);
         if (!selectedFile.exists())
             return ;
         try{
             File newFile = new File(selectedFile.getParent() + File.separator + userToChat.getUserName() + "-DE.png");
             SteganographyUtil.encode(selectedFile, getDigitalEnvelop(), newFile);
             byte[] toWrite = ("<from>" + user.getUserName() + "</from><type>request</type><content>" + newFile.toString() + "</content>").getBytes(StandardCharsets.UTF_8);
             Files.write(new File(userToChat.getInboxDirectoryPath() + File.separator + user.getUserName() + ".txt").toPath(), toWrite);
         }catch (Exception e){
             e.printStackTrace();
         }
    }

    public static synchronized void chatRequest(String message) {
        System.out.println("Chat requst enter");
        try{userToChat=new User();
        Pattern pattern = Pattern.compile("<from>(.+?)</from>");
        //Matching the compiled pattern in the String
        Matcher matcher = pattern.matcher(message);
        if(matcher.find())
        userToChat.setUserName(matcher.group(1));
        userToChat=new User(UserUtil.loadUserData(new File(userToChat.getUserPath())));
        HomeController.setSymmetricKey(message);
        Platform.runLater(()-> {Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Do you want to start a chat with "+ userToChat.getUserName(),ButtonType.YES,ButtonType.NO);
        alert.setTitle("CRYPTOGRAM");
        alert.setHeaderText(null);
        alert.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .ifPresent(response-> {


                        try {
                            replyYes();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                });
    });}catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("chat req end");
        }

    private static void replyYes() throws IOException {
        byte[]toWrite=("<from>"+user.getUserName() + "</from><type>reply</type><content>yes</content>").getBytes(StandardCharsets.UTF_8);
        Files.write(new File(userToChat.getInboxDirectoryPath() + File.separator + user.getUserName() + ".txt").toPath(),toWrite);
        showChat();
    }

    private void showLogin() throws IOException {
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("../views/LoginView.fxml"));
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

    public void onLogoutClick(@SuppressWarnings("UnusedParameters")ActionEvent actionEvent) throws IOException {
        user.setActivity(false);
        UserUtil.storeUserData(user,new File(user.getUserPath()));
        update=false;
        im.setRunning(false);
        showLogin();
    }

    public  static void showChat(){
        try {
            Parent root = FXMLLoader.load(HomeController.class.getResource("../views/ChatView.fxml"));
            Stage stage =new Stage();
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
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setIcon() {
        try {
            userIcon = new Image(new FileInputStream("src" + File.separator + "resources" + File.separator + "view_resources" + File.separator + "user_icons" + File.separator + user.getUserName() + ".png"));
            iconView.setImage(userIcon);
        }catch ( FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void setSymmetricKey(String message) throws IOException {
        String pathToImg = null;
        Pattern pattern = Pattern.compile("<content>(.+?)</content>");
        Matcher matcher = pattern.matcher(message);
        if(matcher.find())
            pathToImg=matcher.group(1);
        assert pathToImg != null;
        System.out.println(pathToImg);
        String digitalEnvelope = SteganographyUtil.decode(new File(pathToImg));

        MyCertificate cd= CertificateUtil.getCertificateDetails(user.getKeyStorePath(),"password");
        PrivateKey privateKey=cd.getPrivateKey();
        String keyInString="";
        try{
            keyInString= RSAUtil.decrypt(digitalEnvelope,privateKey);
        }catch (Exception e){
            e.printStackTrace();
        }
        byte[] decodedKey = Base64.getDecoder().decode(keyInString);
        symmetricKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");

    }

    private String getDigitalEnvelop() throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        SymmetricAlgorithms sa = new SymmetricAlgorithms("DES");
        symmetricKey=sa.getSymmetricKey();
        String key= Base64.getEncoder().encodeToString(sa.getSymmetricKey().getEncoded());
        PublicKey publicKey= CertificateUtil.getPublicKey(user.getTrustStorePath(),"password",userToChat.getUserName());
        return RSAUtil.encrypt(key,publicKey);
    }

}

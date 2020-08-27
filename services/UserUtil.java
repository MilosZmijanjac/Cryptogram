package services;

import models.User;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class UserUtil {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        for(int i=0;i<5;i++) {
            createUser("user"+(i+1),"pass"+(i+1));
            System.out.println("User"+(i+1)+" created");
        }

    }
    public static void createUser(String name, String password) throws NoSuchAlgorithmException, IOException {
        User user = new User();

        user.setUserName(name);
        user.setSalt(PasswordUtil.getSalt());

        user.setHash(PasswordUtil.hashPasswordPlusSalt(password,user.getSalt()));
        user.setActivity(false);

        File userAccountDirectory=new File(user.getUserAccountDirectory());
        File inboxDirectory=new File(user.getInboxDirectoryPath());
        userAccountDirectory.mkdirs();
        inboxDirectory.mkdirs();

        storeUserData(user,new File(user.getUserPath()));


    }
    public static void storeUserData(User user,File filename) throws IOException {
        FileOutputStream file = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(file);

        out.writeObject(user);
        out.close();
        file.close();


    }
    public static User loadUserData(File filename) throws IOException, ClassNotFoundException {

        FileInputStream file = new FileInputStream(filename);
        ObjectInputStream object = new ObjectInputStream(file);
        User user = null;
        user= (User) object.readObject();
        object.close();
        file.close();
        return user;

    }

}

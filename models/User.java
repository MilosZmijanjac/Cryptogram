package models;

import java.io.*;

public class User implements Serializable {
    private String userName;
    private static String rootPath="src"+File.separator+"resources"+File.separator+"accounts";
    private String hash;
    private String salt;
    private boolean activity;

    public User(){}

    public User(User u){
        this.userName=u.userName;
        this.activity=u.activity;
        this.salt=u.salt;
        this.hash=u.hash;
    }

    public boolean isActive() {
        return activity;
    }

    public void setActivity(boolean activity){ this.activity=activity;}

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUserName() { return this.userName; }

    public String getRootPath() { return rootPath; }

    public String getInboxDirectoryPath(){ return rootPath+File.separator+userName+File.separator+"inbox"; }

    public String getUserAccountDirectory() { return rootPath+File.separator+userName; }

    public String getUserPath(){return getUserAccountDirectory()+File.separator+userName;}

    public void setUserName(String userName){
        this.userName=userName;
    }

    public String getTrustStorePath(){ return getUserAccountDirectory()+File.separator+"truststore.ts";}

    public String getKeyStorePath() {return getUserAccountDirectory()+File.separator+this.userName+"-keystore.jks";}

    }

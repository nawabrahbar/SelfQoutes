package util;

import android.app.Application;

public class JournalApi extends Application {
    private String userName;
    private String userEmail;
    private String userMobile;
    private String userId;
    private static JournalApi instance;

    public static JournalApi getInstance() {
        if(instance==null)
            instance = new JournalApi();
        return instance;
    }

    public JournalApi(){}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

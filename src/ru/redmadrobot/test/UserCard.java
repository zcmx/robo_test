package ru.redmadrobot.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class UserCard {
    public String id;
    public String userName = "user name: ";
    public String fullName = "full name: ";
    public Bitmap userPic;

    public UserCard(String id,String userName, String fullName, String userPic) {
        this.id = id;
        this.userName += userName == null ? "" : userName;
        this.fullName += fullName == null ? "" : fullName;
        try {
            this.userPic = BitmapFactory.decodeStream(new URL(userPic).openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

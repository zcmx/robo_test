package ru.redmadrobot.test;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

public class UserPhoto {
    public boolean check;
    public int likes;
    public Bitmap photo;
    public UserPhoto(int likes, String urlPic) {
        this.likes = likes;
        try {
            this.photo = BitmapFactory.decodeStream(new URL(urlPic).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}

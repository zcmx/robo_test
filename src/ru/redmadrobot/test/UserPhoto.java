package ru.redmadrobot.test;


import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.net.URL;

public class UserPhoto implements Comparable<UserPhoto>{
    public boolean check;
    public int likes;
    public Drawable drawablePhoto;

    public UserPhoto(int likes, String urlPic) {
        this.likes = likes;
        try {
            this.drawablePhoto = new BitmapDrawable(null, BitmapFactory.decodeStream(new URL(urlPic).openConnection().getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(UserPhoto another) {
        if(this.likes > another.likes) return -1;
        else if(this.likes < another.likes) return 1;
        else return 0;
    }
}

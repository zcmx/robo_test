package ru.redmadrobot.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * User: zcmx
 * Date: 18.09.13
 * Time: 10:34
 */
public class ShareCollageBtn extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_collage_button, container, false);
    }
}

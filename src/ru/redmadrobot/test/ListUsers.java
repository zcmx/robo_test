package ru.redmadrobot.test;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;


public class ListUsers extends ListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<UserCard> userCards = new ArrayList<UserCard>();
        ArrayAdapter<UserCard> adapter = new ArrayAdapter<UserCard>(this, R.layout.list_users, userCards);
        setListAdapter(adapter);
    }
}

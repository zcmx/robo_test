package ru.redmadrobot.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StartActivity extends Activity {
    public static final String USER_NAME_REQUEST = "ru.redmadrobot.test.userName";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void comeOnCollageClick(View view){
        try {
            Intent intent = new Intent(this, ProcessResponse.class);
            intent.putExtra(USER_NAME_REQUEST, URLEncoder.encode(((EditText)findViewById(R.id.userName)).getText().toString(), "UTF-8"));
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            findViewById(R.id.userName).requestFocus();
            Toast.makeText(this, getString(R.string.correct_name), Toast.LENGTH_LONG).show();
        }
    }
}

package ru.redmadrobot.test;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessResponse extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instagram_response);
        Adapter adapter = new ArrayAdapter<UserCard>(this, R.layout.user_card);
        new Process().execute(InstargamServe.searchUsersByName(getIntent().getStringExtra(StartActivity.USER_NAME_REQUEST)));
    }

    private class Process extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... request) {
            StringBuilder instagramResponse = new StringBuilder();
            for(String req : request){
                try{
                    HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(req));
                    if(httpResponse.getStatusLine().getStatusCode() == InstargamServe.RESPONSE_OK){
                        BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                        String line;
                        while((line = reader.readLine()) != null){
                            instagramResponse.append(line);
                        }
                    }else{
                        Log.e("HTTP WORK", httpResponse.getEntity().getContent().toString());//FIXME
                    }
                }catch(Exception e){
                    Log.e("HTTP ERROR", e.toString());
                }
            }
            return instagramResponse.toString();
        }

        @Override
        protected void onPostExecute(String s) {
//            ((TextView)findViewById(R.id.response)).setText(s);
        }
    }

    public void selectUser(View view){

    }
}

package ru.redmadrobot.test;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ListUsers extends ListActivity {
    public static final String SELECT_USER_ID = "ru.redmadrobot.test.userId";
    public UserCardAdapter cardAdapter;
    private View header;
    private View noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardAdapter = new UserCardAdapter(getApplicationContext(), new ArrayList<UserCard>());
        header = getLayoutInflater().inflate(R.layout.progress_bar, null);
        noData = getLayoutInflater().inflate(R.layout.no_data, null);
        ((TextView)noData.findViewById(R.id.message)).setText(getString(R.string.no_data_users));
        getListView().addHeaderView(header);
        setListAdapter(cardAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), PhotoPicker.class);
                intent.putExtra(SELECT_USER_ID, ((UserCard)parent.getItemAtPosition(position)).id);
                startActivity(intent);
            }
        });

        new UsersProcess().execute(InstargamServe.searchUsersByName(getIntent().getStringExtra(StartActivity.USER_NAME_REQUEST)));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static class UserCardAdapter extends ArrayAdapter<UserCard>{

        public List<UserCard> cards;
        private final Context context;

        public UserCardAdapter(Context context, List<UserCard> cards) {
            super(context, R.layout.user_card, cards);
            this.cards = cards;
            this.context = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.user_card, parent, false);
            TextView userName = (TextView)view.findViewById(R.id.userName);
            TextView fullName = (TextView)view.findViewById(R.id.fullName);
            ImageView userPic = (ImageView)view.findViewById(R.id.userPic);
            userName.setText(cards.get(position).userName);
            fullName.setText(cards.get(position).fullName);
            userPic.setImageBitmap(cards.get(position).userPic);
            return view;
        }
    }

    private class UsersProcess extends AsyncTask<String, Void, Boolean> {
        private class JSONProcess extends AsyncTask<JSONObject, Void, UserCard>{
            @Override
            protected UserCard doInBackground(JSONObject... user) {
                try {
                    return new UserCard(user[0].getString(getString(R.string.id)), user[0].getString(getString(R.string.username)),
                            user[0].getString(getString(R.string.full_name)), user[0].getString(getString(R.string.profile_picture)));
                } catch (JSONException e) {
                    Log.e("JSONProcess -> doInBackground", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(UserCard userCards) {
                super.onPostExecute(userCards);
                cardAdapter.cards.add(userCards);
                cardAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected Boolean doInBackground(String... request) {
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
                        try{
                            JSONObject jsonObject = new JSONObject(instagramResponse.toString());
                            JSONArray data = jsonObject.getJSONArray(getString(R.string.data));
                            for(int i = 0; i < data.length(); i++){
                                JSONObject user = data.getJSONObject(i);
                                new JSONProcess().execute(user);
                            }
                            if(data.length() == 0) return true;
                            else return false;
                        }catch(JSONException e){
                            Log.e("UsersProcess -> doInBackground", e.toString());
                        }
                    }else{
                        Log.e("UsersProcess -> doInBackground", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                    }
                }catch(Exception e){
                    Log.e("UsersProcess -> doInBackground", e.toString());
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean checkData) {
            super.onPostExecute(checkData);
            getListView().removeHeaderView(header);
            if(checkData){
                getListView().setAdapter(null);
                getListView().addHeaderView(noData);
                getListView().setAdapter(cardAdapter);
            }
        }
    }


}

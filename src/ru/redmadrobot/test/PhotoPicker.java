package ru.redmadrobot.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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

/**
 * User: zcmx
 * Date: 16.09.13
 * Time: 14:15
 */
public class PhotoPicker extends Activity {
    private PhotoAdapter photoAdapter;
    private GridView gridView;
    private View progressBar;
    private View noData;
    private LinearLayout header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_pickers);
        this.photoAdapter = new PhotoAdapter(getApplicationContext(), new ArrayList<UserPhoto>());
        header = (LinearLayout)findViewById(R.id.header);
        progressBar = getLayoutInflater().inflate(R.layout.progress_bar, null);
        noData = getLayoutInflater().inflate(R.layout.no_data, null);
        ((TextView)noData.findViewById(R.id.message)).setText(getString(R.string.no_data_photo));
        gridView = (GridView)findViewById(R.id.gridViewPhoto);
        gridView.setAdapter(photoAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(((UserPhoto)parent.getItemAtPosition(position)).check)
                    ((UserPhoto)parent.getItemAtPosition(position)).setCheck(false);
                else
                    ((UserPhoto)parent.getItemAtPosition(position)).setCheck(true);

                photoAdapter.notifyDataSetChanged();
            }
        });
        new PhotoProcess().execute(InstargamServe.getUserPics(getIntent().getStringExtra(ListUsers.SELECT_USER_ID)));
    }

    public class PhotoAdapter extends ArrayAdapter<UserPhoto> {
        public List<UserPhoto> photos;
        private Context context;

        public PhotoAdapter(Context context, List<UserPhoto> photos) {
            super(context, R.layout.photo_pick, photos);
            this.photos = photos;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.photo_pick, parent, false);
            Drawable drawable = new BitmapDrawable(null, photos.get(position).photo);
            CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkBoxPhoto);
            checkbox.setBackgroundDrawable(drawable);
            checkbox.setChecked(photos.get(position).check);
            return view;
        }
    }

    private class PhotoProcess extends AsyncTask<String, Void, Boolean>{
        String type = "image";

        @Override
        protected void onPostExecute(Boolean checkData) {
            super.onPostExecute(checkData);
            photoAdapter.notifyDataSetChanged();
            header.removeAllViews();
            if(checkData){
                header.addView(noData);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            header.addView(progressBar);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            StringBuilder instagramResponse = new StringBuilder();
            for(String req : params){
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
                                new PhotoJSONProcess().execute(data.getJSONObject(i));
                            }
                            if(data.length() == 0) return true;
                            else return false;
                        }catch(JSONException e){
                            Log.e("PhotoProcess -> doInBackground", e.toString());
                        }
                    }else{
                        Log.e("PhotoProcess -> doInBackground", String.valueOf(httpResponse.getStatusLine().getStatusCode()));
                    }
                }catch(Exception e){
                    Log.e("PhotoProcess -> doInBackground", e.toString());
                }
            }
            return true;
        }

        private class PhotoJSONProcess extends AsyncTask<JSONObject, Void, UserPhoto>{

            @Override
            protected UserPhoto doInBackground(JSONObject... params) {
                try {
                    if(params[0].getString("type").equals(type)){
                        return new UserPhoto(params[0].getJSONObject("likes").getInt("count"), params[0].getJSONObject("images").getJSONObject("thumbnail").getString("url") );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(UserPhoto userPhoto) {
                super.onPostExecute(userPhoto);
                photoAdapter.photos.add(userPhoto);
                photoAdapter.notifyDataSetChanged();
            }
        }
    }
}

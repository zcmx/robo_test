package ru.redmadrobot.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PhotoPicker extends FragmentActivity {
    private PhotoAdapter photoAdapter;
    private View progressBar;
    private View noData;
    private GridView gridView;
    private LinearLayout header;
    private List<UserPhoto> photoCollage;
    private List<UserPhoto> _photos;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment lookAtBtn;
    private Fragment shareCollageBtn;
    private static String lookAtBtnTag = "look_at_button";
    private static String shareBtnTag = "share_btn_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_pickers);
        this.photoCollage = new ArrayList<UserPhoto>();
        this._photos = new ArrayList<UserPhoto>();
        this.photoAdapter = new PhotoAdapter(getApplicationContext(),R.layout.photo_pick, _photos);
        header = (LinearLayout)findViewById(R.id.header);
        progressBar = getLayoutInflater().inflate(R.layout.progress_bar, null);
        noData = getLayoutInflater().inflate(R.layout.no_data, null);
        ((TextView)noData.findViewById(R.id.message)).setText(getString(R.string.no_data_photo));
        gridView = (GridView) findViewById(R.id.gridViewPhoto);
        gridView.setAdapter(photoAdapter);
        new PhotoProcess().execute(InstargamServe.getUserPics(getIntent().getStringExtra(ListUsers.SELECT_USER_ID)));
        fragmentManager = getSupportFragmentManager();
        lookAtBtn = new LookAtBtn();
        shareCollageBtn = new ShareCollageBtn();
        if(savedInstanceState == null){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.operationPanel, lookAtBtn, lookAtBtnTag);
            fragmentTransaction.hide(lookAtBtn);
            fragmentTransaction.commit();
        }

    }

    public class PhotoAdapter extends ArrayAdapter<UserPhoto> {
        public List<UserPhoto> photos = new ArrayList<UserPhoto>();
        private Context context;
        int res;

        public PhotoAdapter(Context context, int resourceId, List<UserPhoto> photos) {
            super(context, resourceId, photos);
            this.photos = photos;
            this.res = resourceId;
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(res, parent, false);
            if(res == R.layout.photo_pick){
                CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkBoxPhoto);
                checkbox.setBackgroundDrawable(photos.get(position).drawablePhoto);
                checkbox.setChecked(photos.get(position).check);
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        photos.get(position).check = isChecked;
                        if(isChecked)
                            photoCollage.add(photos.get(position));
                        else
                            photoCollage.remove(photos.get(position));

                        if(photoCollage.size() > 0) showLookBtn();
                        else hideLookBtn();
                    }
                });
            }else {
                ImageView imageView = (ImageView)view.findViewById(R.id.photoView);
                imageView.setImageDrawable(photos.get(position).drawablePhoto);
                imageView.setDrawingCacheEnabled(true);
            }
            return view;
        }
    }

    private class PhotoProcess extends AsyncTask<String, Void, Boolean>{
        @Override
        protected void onPostExecute(Boolean checkData) {
            super.onPostExecute(checkData);
            Collections.sort(photoAdapter.photos);
            _photos = photoAdapter.photos;
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
            photoAdapter.photos.clear();
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
                                if(data.getJSONObject(i).getString(getString(R.string.type)).equals(getString(R.string.image))){
                                    photoAdapter.photos.add(new UserPhoto(data.getJSONObject(i).getJSONObject(getString(R.string.likes)).
                                            getInt(getString(R.string.count)),data.getJSONObject(i).getJSONObject(getString(R.string.images)).
                                            getJSONObject(getString(R.string.thumbnail)).getString(getString(R.string.url))));
                                }
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

    }

    public void lookAtCollage(View view){
        gridView.setAdapter(null);
        int countColumns = getCountColumns();
        gridView.setNumColumns(countColumns);
        photoAdapter = new PhotoAdapter(getApplicationContext(), R.layout.photo_img, photoCollage );
        gridView.setAdapter(photoAdapter);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.operationPanel, shareCollageBtn, shareBtnTag);
        fragmentTransaction.commit();
    }

    public void sendCollageByEmail(View view) {
        String pathToCollage = saveToFileCollage();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(getString(R.string.email_intent_type));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(getString(R.string.append_to_path_on_collage) + pathToCollage));
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    private String saveToFileCollage() {
        Bitmap bitmap = screenCollage();
        File collage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
        if(!collage.exists()) collage.mkdir();
        collage = new File(collage.toString() + File.separator + System.currentTimeMillis() + getString(R.string.jpg_suffix));
        OutputStream stream = null;
        try {
            collage.createNewFile();
            stream = new FileOutputStream(collage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            Log.e("PhotoPicker -> saveToFileCollage", e.toString());
        }
        return collage.toString();
    }

    private Bitmap screenCollage() {
        gridView.setDrawingCacheEnabled(true);
        gridView.buildDrawingCache(true);
        gridView.layout(0, 0, gridView.getMeasuredWidth(), gridView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(gridView.getDrawingCache());
        gridView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private int getCountColumns() {
        int countColumns;
        double sqrtSize = Math.sqrt(photoCollage.size());
        int floorSize = (int) Math.floor(sqrtSize);
        countColumns = floorSize <= 4 ? 4 : floorSize;
        return countColumns;
    }


    private void hideLookBtn(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(lookAtBtn);
        fragmentTransaction.commit();
    }

    private void showLookBtn(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.show(lookAtBtn);
        fragmentTransaction.commit();
    }
}

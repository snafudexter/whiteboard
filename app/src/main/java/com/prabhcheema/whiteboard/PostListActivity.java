package com.prabhcheema.whiteboard;


import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostListActivity extends AppCompatActivity
{
    List<Item> items;
    ExpandableHeightGridView gridView;
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            items = new ArrayList<Item>();
            String aResponse = msg.getData().getString("msg");
            try {

                JSONArray jsonArray = new JSONArray(aResponse);
                for(int i = 0; i < jsonArray.length(); i++)
                {
                    Item item = new Item();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    item.type=Integer.parseInt(jsonObject.getString("type"));
                    item.done=Integer.parseInt(jsonObject.getString("done"));
                    item.app_id=Integer.parseInt(jsonObject.getString("app_id"));
                    item.user_id=Integer.parseInt(jsonObject.getString("user"));
                    item.rating=Integer.parseInt(jsonObject.getString("rating"));
                    item.item_id=Integer.parseInt(jsonObject.getString("id"));

                    item.title = jsonObject.getString("title");
                    item.image = jsonObject.getString("image");
                    item.desc = jsonObject.getString("description");

                    items.add(item);

                }
                PostAdapter adapter = new PostAdapter(getApplication().getBaseContext(), items);
                gridView.invalidateViews();
                gridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();


            }catch (Exception e)
            {
                Log.e("error", e.getMessage());
            }
        }
    };

    String user_id, app_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        load_grid();

    }

    void load_grid()
    {
        String strJson = getIntent().getStringExtra("json");

        try
        {
            final JSONObject jsonObject = new JSONObject(strJson);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String data = URLEncoder.encode("app_id", "UTF-8") + "=" + URLEncoder.encode(jsonObject.getString("app_id"), "UTF-8");

                        user_id = jsonObject.getString("user_id");
                        app_id = jsonObject.getString("app_id");
                        String text = "";
                        BufferedReader reader = null;

                        URL url = new URL(getResources().getString(R.string.base_url) + "get_entry_list.php");
                        URLConnection connection = url.openConnection();
                        connection.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                        wr.write(data);
                        wr.flush();
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while((line = reader.readLine()) != null)
                        {

                            sb.append(line + "\n");
                        }


                        text = sb.toString();

                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("msg", text);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                    catch (Exception e)
                    {
                        Log.e("error", e.getMessage());
                    }

                }
            };

            Thread thread = new Thread(runnable);
            thread.start();

        }
        catch (Exception e)
        {
            Log.e("error", e.getMessage());
        }

        gridView = (ExpandableHeightGridView) findViewById(R.id.post_list_container);
        gridView.setExpanded(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.posts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.add_post:
                showAddPostWindow();
                break;

        }
        return true;
    }
    ImageView imageView;
    Bitmap selectedImage;
    String imageName;
    CheckBox chkBug;
    String isBug;

    EditText addTitle, addDesc;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Random random = new Random();
                        imageName = imageUri.getLastPathSegment() + String.valueOf(random.nextInt(5000))+ ".jpg";
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(Bitmap.createScaledBitmap(selectedImage, 200, 200, false));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

                break;
        }
    }

    void showAddPostWindow()
    {

        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View addCommentView = inflater.inflate(R.layout.add_post_layout, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(addCommentView);

        imageView = (ImageView)addCommentView.findViewById(R.id.img_SelectImage);
        addTitle = (EditText)addCommentView.findViewById(R.id.txt_addTitle);
        addDesc = (EditText)addCommentView.findViewById(R.id.txt_addDesc);
        chkBug = (CheckBox)addCommentView.findViewById(R.id.chk_isBug);



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        final TextView txt_rate = (TextView)addCommentView.findViewById(R.id.txt_rating);

        final SeekBar seekBar = (SeekBar)addCommentView.findViewById(R.id.r_seek);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_rate.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setCancelable(true);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        byte[] data = null;
                        try {
                            String url = getResources().getString(R.string.base_url)+ "insert_post.php";
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(url);
                            MultipartEntity entity = new MultipartEntity();

                            if(selectedImage!=null){
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                                data = bos.toByteArray();
                                entity.addPart("file_img", new ByteArrayBody(data,"image/jpeg",imageName ));
                            }
                            if(chkBug.isChecked())
                                isBug = "1";
                            else
                                isBug = "0";
                            // entity.addPart("category", new StringBody(categoryname,"text/plain",Charset.forName("UTF-8")));
                             entity.addPart("user_id", new StringBody(user_id,"text/plain",Charset.forName("UTF-8")));
                            entity.addPart("app_id", new  StringBody(app_id,"text/plain", Charset.forName("UTF-8")));
                            entity.addPart("rating", new StringBody(String.valueOf(seekBar.getProgress()) ,"text/plain",Charset.forName("UTF-8")));
                            entity.addPart("type", new StringBody(isBug,"text/plain",Charset.forName("UTF-8")));
                            entity.addPart("title", new StringBody(addTitle.getText().toString(),"text/plain",Charset.forName("UTF-8")));
                            entity.addPart("desc", new StringBody(addDesc.getText().toString(),"text/plain",Charset.forName("UTF-8")));

                            httppost.setEntity(entity);
                            HttpResponse resp = httpclient.execute(httppost);
                            HttpEntity resEntity = resp.getEntity();
                            String string= EntityUtils.toString(resEntity);
                            if(string.compareTo("1")==0)
                            {
                                dialog.dismiss();
                                load_grid();
                            }

                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();

            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

}

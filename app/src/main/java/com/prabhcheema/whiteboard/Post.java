package com.prabhcheema.whiteboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import java.io.InputStream;


public class Post extends AppCompatActivity {

    ImageView imageView;

    TextView desc;
    RatingBar ratingBar;

    Button btn_comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        imageView = (ImageView)findViewById(R.id.post_img);
        ratingBar = (RatingBar)findViewById(R.id.ratings);
        desc = (TextView)findViewById(R.id.post_desc);

        btn_comments = (Button)findViewById(R.id.btn_comments);

        btn_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), CommentsViewActivity.class);
                Bundle b = new Bundle();
                b.putInt("post_id", getIntent().getExtras().getInt("id"));
                i.putExtras(b);
                startActivity(i);
                
            }
        });

        desc.setText(getIntent().getExtras().getString("desc"));
        String url = getIntent().getExtras().getString("image");
        ratingBar.setProgress(getIntent().getExtras().getInt("rating"));

        if(url != "na")
            new DownloadImageTask(imageView).execute(getResources().getString(R.string.base_url) + url);

        setTitle(getIntent().getExtras().getString("title"));
    }



    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}

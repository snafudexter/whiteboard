package com.prabhcheema.whiteboard;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class CommentsViewActivity extends AppCompatActivity {
    Handler login_handler;

    List<CommentFragment> commentFragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_view);
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        commentFragmentList = new ArrayList<>();

        login_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String aResponse = msg.getData().getString("msg");
                Log.e("error", aResponse);

                try {
                    JSONArray jsonArray = new JSONArray(aResponse);
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        CommentFragment commentFragment = new CommentFragment();
                        Bundle b = new Bundle();
                        b.putString("user", jsonObject.getString("user"));
                        b.putString("comm", jsonObject.getString("comment"));
                        b.putString("id", jsonObject.getString("id"));
                        commentFragment.setArguments(b);

                        fragmentTransaction.add(R.id.comments_container_layout, commentFragment).commit();
                        commentFragmentList.add(commentFragment);
                    }
                }
                catch (Exception e)
                {
                    Log.e("error", e.getMessage());
                }
            }
        };

        load_comments();
    }

    void load_comments()
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run()
            {
                try {
                    String data = URLEncoder.encode("post_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(getIntent().getExtras().getInt("post_id")), "UTF-8");

                    String text = "";
                    BufferedReader reader = null;

                    URL url = new URL(getResources().getString(R.string.base_url) + "get_comments.php");
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

                    Message msg = login_handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("msg", text);
                    msg.setData(b);
                    login_handler.sendMessage(msg);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comments_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.add_comment:
                showAddCommentWindow();
                break;

        }
        return true;
    }
    SharedPreferences sharedPreferences;
    void showAddCommentWindow()
    {

        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View addCommentView = inflater.inflate(R.layout.addnew_comment_dialog_layout, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(addCommentView);

        final EditText txt_comment = (EditText)addCommentView.findViewById(R.id.txt_addComment);

        builder.setCancelable(true);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                            String data = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(sharedPreferences.getString("c_user", "user"), "UTF-8");
                            data += "&" + URLEncoder.encode("com", "UTF-8") + "=" + URLEncoder.encode(txt_comment.getText().toString(), "UTF-8");
                            data += "&" + URLEncoder.encode("post_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(getIntent().getExtras().getInt("post_id")), "UTF-8");

                            String text = "";
                            BufferedReader reader = null;

                            URL url = new URL(getResources().getString(R.string.base_url)+ "insert_comment.php");
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
                            if(text.compareTo("1\n") == 0) {
                                dialog.dismiss();

                                for(int i = 0; i < commentFragmentList.size(); i++) {
                                    FragmentManager fragmentManager = getFragmentManager();
                                    fragmentManager.beginTransaction().remove(commentFragmentList.get(i)).commit();
                                }

                                commentFragmentList.clear();

                                load_comments();
                            }

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

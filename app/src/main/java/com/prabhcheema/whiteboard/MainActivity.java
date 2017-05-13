package com.prabhcheema.whiteboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText email, password;
    Button login;

    Spinner spinner;

    Handler login_handler;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);


        email = (EditText)findViewById(R.id.txt_email);
        password = (EditText)findViewById(R.id.txt_pass);
        login = (Button)findViewById(R.id.btn_login);
        spinner = (Spinner)findViewById(R.id.app_selector);


        List<String> app_list = new ArrayList<String>();
        app_list.add("Tour & Travel Manager");
        app_list.add("Loan Manager");
        app_list.add("Routing Software");
        app_list.add("Content Management System");

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, app_list);

        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(listAdapter);

        login_handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                String aResponse = msg.getData().getString("msg");

                final Intent i = new Intent(getBaseContext(), PostListActivity.class);
                i.putExtra("json", aResponse);
                startActivity(i);
                finish();
            }
        };



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                login.setEnabled(false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("c_user", email.getText().toString());
                editor.commit();

                        Runnable runnable = new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                            String data = URLEncoder.encode("usern", "UTF-8") + "=" + URLEncoder.encode(email.getText().toString(), "UTF-8");
                            data += "&" + URLEncoder.encode("passw", "UTF-8") + "=" + URLEncoder.encode(password.getText().toString(), "UTF-8");
                            data += "&" + URLEncoder.encode("app_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(spinner.getSelectedItemId() + 1), "UTF-8");

                            String text = "";
                            BufferedReader reader = null;

                            URL url = new URL(getResources().getString(R.string.base_url));
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
        });

    }
}

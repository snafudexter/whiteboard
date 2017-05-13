package com.prabhcheema.whiteboard;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Prabh on 4/25/2017.
 */

public class CommentFragment extends Fragment {

    TextView user, comment;
    ImageButton del;

    SharedPreferences sharedPreferences;



    Handler login_handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comment_frag_layout, container, false);
        return view;
    }

    void remove()
    {
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        user = (TextView)view.findViewById(R.id.txt_user_name);
        comment = (TextView)view.findViewById(R.id.txt_com);
        del = (ImageButton)view.findViewById(R.id.btn_delete);
        user.setText(getArguments().getString("user"));
        comment.setText(getArguments().getString("comm"));

        if(sharedPreferences.getString("c_user","user").compareTo("admin") == 0) {
            del.setVisibility(View.VISIBLE);
        }

        login_handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                String aResponse = msg.getData().getString("msg");
                if(aResponse.compareTo("1\n") == 0)
                {
                   remove();
                }
            }
        };


        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run()
                    {

                        try {
                            String data = URLEncoder.encode("table", "UTF-8") + "=" + URLEncoder.encode("comments", "UTF-8");
                            data += "&" + URLEncoder.encode("wfield", "UTF-8") + "=" + URLEncoder.encode("id", "UTF-8");
                            data += "&" + URLEncoder.encode("wvalue", "UTF-8") + "=" + URLEncoder.encode(getArguments().getString("id"), "UTF-8");

                            String text = "";
                            BufferedReader reader = null;

                            URL url = new URL(getResources().getString(R.string.base_url)+"delete.php");
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

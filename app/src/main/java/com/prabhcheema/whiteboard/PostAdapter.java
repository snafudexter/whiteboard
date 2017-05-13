package com.prabhcheema.whiteboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Prabh on 4/14/2017.
 */

public class PostAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items;
    public PostAdapter(Context c, List<Item> list)
    {
        context = c;
        items = list;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageButton imageButton;
        final Item it = items.get(position);
        if (convertView == null) {
            imageButton = new ImageButton(context);
            imageButton.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            imageButton.setScaleType(ImageButton.ScaleType.CENTER_CROP);
            imageButton.setPadding(5, 5, 5, 5);
        } else {
            imageButton = (ImageButton) convertView;
        }

        if (it.type == 1) {
            imageButton.setImageDrawable(context.getDrawable(R.drawable.ic_bug));
        } else {
            imageButton.setImageDrawable(context.getDrawable(R.drawable.ic_system));
        }



        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, Post.class);
                Bundle b = new Bundle();
                b.putInt("id", it.item_id);
                b.putInt("app_id", it.app_id);
                b.putInt("done", it.done);
                b.putInt("rating", it.rating);
                b.putInt("user_id", it.user_id);
                b.putInt("type", it.type);
                b.putString("title", it.title);
                b.putString("desc", it.desc);
                b.putString("image", it.image);
                i.putExtras(b);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        return imageButton;
    }
}

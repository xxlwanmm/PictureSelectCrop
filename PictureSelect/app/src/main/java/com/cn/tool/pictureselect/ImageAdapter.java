package com.cn.tool.pictureselect;

import java.util.ArrayList;
import java.util.HashMap;

import com.bumptech.glide.Glide;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

/**
 * Created by admin on 2016/12/27.
 */

public class ImageAdapter extends BaseAdapter {
    private ArrayList<Uri> uriArray = new ArrayList<Uri>();//存放图片的uri数据
    private LayoutInflater mInflater;
    private Context context;
    public ImageAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return uriArray.size();
    }

    @Override
    public Object getItem(int position) {
        return uriArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item, parent, false);

            holder = new ViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.image_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Uri uri = uriArray.get(position);
        Glide.with(context).load(uri).into(holder.img);
        return convertView;
    }

    public ImageAdapter setUriArray(ArrayList<Uri> uriArray) {
        this.uriArray = uriArray;
        return this;
    }

    public class ViewHolder {
        ImageView img;
    }
}

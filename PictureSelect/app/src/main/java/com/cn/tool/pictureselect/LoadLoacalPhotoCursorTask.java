package com.cn.tool.pictureselect;

/**
 * Created by admin on 2016/12/27.
 */

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

/**
 * 获取本地图片的异步线程类
 */
public class LoadLoacalPhotoCursorTask extends AsyncTask<Object, Object, Object> {
    private Context mContext;
    private final ContentResolver mContentResolver;
    private boolean mExitTasksEarly = false;//退出任务线程的标志位
    private OnLoadPhotoCursor onLoadPhotoCursor;//定义回调接口，获取解析到的数据

    private ArrayList<Uri> uriArray = new ArrayList<Uri>();//存放图片URI
    private ArrayList<Long> origIdArray = new ArrayList<Long>();//存放图片ID

    public LoadLoacalPhotoCursorTask(Context mContext) {
        this.mContext = mContext;
        mContentResolver = mContext.getContentResolver();
    }

    @Override
    protected Object doInBackground(Object... params) {
        String[] projection = {
                MediaStore.Images.Media._ID
        };
        Uri ext_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String where = MediaStore.Images.Media.SIZE + ">=?";
        /**
         * 这个查询操作完成图片大小大于100K的图片的ID查询。
         * 大家可能疑惑为什么不查询得到图片DATA呢？
         * 这样是为了节省内存。通过图片的ID可以查询得到指定的图片
         * 如果这里就把图片数据查询得到，手机中的图片大量的情况下
         * 内存消耗严重。那么，什么时候查询图片呢？应该是在Adapter
         * 中完成指定的ID的图片的查询，并不一次性加载全部图片数据
         */
        Cursor c = MediaStore.Images.Media.query(
                mContentResolver,
                ext_uri,
                projection,
                where,
                new String[]{1 * 100 * 1024 + ""},
                MediaStore.Images.Media.DATE_ADDED+" desc");

        int columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

        int i = 0;
        while (c.moveToNext() && i < c.getCount() && !mExitTasksEarly) {   //移到指定的位置，遍历数据库
            long origId = c.getLong(columnIndex);
            uriArray.add(
                    Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            origId + "")
            );
            c.moveToPosition(i);
            i++;
        }
        c.close();//关闭数据库
        if (mExitTasksEarly) {
            uriArray = new ArrayList<Uri>();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        if (onLoadPhotoCursor != null && !mExitTasksEarly) {
            /**
             * 查询完成之后，设置回调接口中的数据，把数据传递到Activity中
             */
            onLoadPhotoCursor.onLoadPhotoSursorResult(uriArray);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mExitTasksEarly = true;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        this.mExitTasksEarly = exitTasksEarly;
    }

    public void setOnLoadPhotoCursor(OnLoadPhotoCursor onLoadPhotoCursor) {
        this.onLoadPhotoCursor = onLoadPhotoCursor;
    }

    public interface OnLoadPhotoCursor {
        public void onLoadPhotoSursorResult(ArrayList<Uri> uriArray);
    }
}

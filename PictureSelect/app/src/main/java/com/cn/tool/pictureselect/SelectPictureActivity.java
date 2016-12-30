package com.cn.tool.pictureselect;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * 大概流程：进入界面一个异步去拿所有本地图片的Uri,然后再在GridView中用Glide加载图片
 */
public class SelectPictureActivity extends FragmentActivity {
    private ArrayList<Uri> uriImageArray = new ArrayList<Uri>();//存放图片的uri数据
    public static final String UPLOAD_FILE_PATH = "Upload_file_Path";
    public static final String UPLOAD_FILE_NAME = "Upload_file_Name";
    private String mUploadfilePath;
    private String mUploadfileName;

    private ImageAdapter adapter;
    private GridView gridView;
    private View loadView;//进度条View
    private LoadLoacalPhotoCursorTask cursorTask;//获取本地图片数据的异步线程类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);
        Intent intent = getIntent();
        mUploadfilePath = intent.getStringExtra(UPLOAD_FILE_PATH);
        mUploadfileName = intent.getStringExtra(UPLOAD_FILE_NAME);
        createView();
        init();
    }

    private void createView() {
        gridView = (GridView) findViewById(R.id.sdcard);
        loadView = findViewById(R.id.load_layout);
    }

    /**
     * 初始化
     */
    private void init() {
        adapter = new ImageAdapter(this);
        gridView.setAdapter(adapter);
        loadData();
        onItemClick();
    }

    /**
     * 加载数据
     */
    private void loadData() {
        cursorTask = new LoadLoacalPhotoCursorTask(this);//获取本地图片的异步线程类
        /**
         * 回调接口,当完成本地图片数据的获取之后
         */
        cursorTask.setOnLoadPhotoCursor(new LoadLoacalPhotoCursorTask.OnLoadPhotoCursor() {
            @Override
            public void onLoadPhotoSursorResult(ArrayList<Uri> uriArray) {
                if (isNotNull(uriArray)) {
                    uriImageArray = uriArray;
                    loadView.setVisibility(View.GONE);
                    adapter.setUriArray(uriArray);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        cursorTask.execute();
    }

    /**
     * 点击每一项选择图片
     */
    private void onItemClick() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = uriImageArray.get(position);
                Intent i = new Intent(SelectPictureActivity.this, CropPictureActivity.class);
                i.putExtra(CropPictureActivity.PIC_URI, uri.toString());
                i.putExtra(CropPictureActivity.UPLOAD_FILE_PATH, mUploadfilePath);
                i.putExtra(CropPictureActivity.UPLOAD_FILE_NAME, mUploadfileName);
                i.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);   //a到b，b到c，c给a传数据
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * 判断list不为空
     */
    private static boolean isNotNull(ArrayList list) {
        return list != null && list.size() > 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursorTask.setExitTasksEarly(true);
    }
}

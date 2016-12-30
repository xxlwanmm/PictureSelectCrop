package com.cn.tool.pictureselect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private Button btSelect;
    private Button btPhoto;
    private ImageView iv;
    private String mUploadfilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String mUploadfileName = "cache_image.jpg";
    private static final int PHOTO = 101;
    private static final int CROP = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btSelect = (Button) findViewById(R.id.bt_select);
        btPhoto = (Button) findViewById(R.id.bt_photo);
        iv = (ImageView) findViewById(R.id.iv);
        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectPictureActivity.class);
                intent.putExtra(SelectPictureActivity.UPLOAD_FILE_PATH, mUploadfilePath);
                intent.putExtra(SelectPictureActivity.UPLOAD_FILE_NAME, mUploadfileName);
                startActivityForResult(intent, CROP);
            }
        });
        btPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri imageUri = Uri.fromFile(new File(mUploadfilePath, mUploadfileName));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//指定照片保存路径（SD卡）
                startActivityForResult(intent, PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //剪切后
                case CROP:
                    String photoPath = new File(mUploadfilePath, mUploadfileName).getAbsolutePath();
                    iv.setImageBitmap(getLoacalBitmap(photoPath));
                    break;
                //调用系统拍照
                case PHOTO:
                    Uri uri = Uri.fromFile(new File(mUploadfilePath, mUploadfileName));
                    Intent i = new Intent(this, CropPictureActivity.class);
                    i.putExtra(CropPictureActivity.PIC_URI, uri.toString());
                    i.putExtra(CropPictureActivity.UPLOAD_FILE_PATH, mUploadfilePath);
                    i.putExtra(CropPictureActivity.UPLOAD_FILE_NAME, mUploadfileName);
                    startActivityForResult(i, CROP);
                    break;
            }
        }
    }

    /**
     * 加载本地图片
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

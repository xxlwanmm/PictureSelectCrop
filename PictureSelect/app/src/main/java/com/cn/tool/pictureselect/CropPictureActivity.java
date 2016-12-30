package com.cn.tool.pictureselect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

/**
 * Created by wh on 2016/9/28.
 */

public class CropPictureActivity extends Activity {
    private Uri uri;
    private String mUploadfilePath;
    private String mUploadfileName;
    public static final String PIC_URI = "Pic_Uri";
    public static final String UPLOAD_FILE_PATH = "Upload_file_Path";
    public static final String UPLOAD_FILE_NAME = "Upload_file_Name";
    Button btSure;
    Button btCancel;
    ClipImageLayout mClipImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_picture);

        btSure = (Button)findViewById(R.id.sure);
        btCancel = (Button)findViewById(R.id.cancel);
        mClipImageLayout = (ClipImageLayout)findViewById(R.id.id_clipImageLayout);

        initPages();
    }
    public void initPages() {
        Intent intent = getIntent();
        uri = Uri.parse(intent.getStringExtra(PIC_URI));
        mUploadfilePath = intent.getStringExtra(UPLOAD_FILE_PATH);
        mUploadfileName = intent.getStringExtra(UPLOAD_FILE_NAME);

        mClipImageLayout.setZoomImageDrawable(scaleBitmap(getRealPathFromURI(uri),480,800));

        btSure.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bitmap bitmap = mClipImageLayout.clip();
                cacheFile(bitmap);
                setResult(RESULT_OK);
                CropPictureActivity.this.finish();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CropPictureActivity.this.finish();
            }
        });
    }

    /**由Uri获取真实路径*/
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

        if(cursor == null){  //证明是拍照，此时contentUri根本没有生成，直接就是真实路径
            return contentUri.getPath();
        }else{   //证明是选择照片,可以由Uri得到真实路径
            if(cursor.moveToFirst()){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
            }

            //加入未查询成功则通过此方式获取
            if(TextUtils.isEmpty(res)){
                res= UriTools.getImageAbsolutePath(CropPictureActivity.this,contentUri);
            }
            cursor.close();
        }
        return res;
    }


    /**图片压缩*/
    public Drawable scaleBitmap(String path, int width, int height) {
        if (width <= 0)
            width = 50;
        if (height <= 0)
            height = 50;
        // 加载图片的尺寸而不是图片本身
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path,
                bmpFactoryOptions);
        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / width);

        if (heightRatio > 1 && widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }
        bmpFactoryOptions.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path,
                bmpFactoryOptions);

        BitmapDrawable bd= new BitmapDrawable(getResources(), bmp);
        return bd;
    }

    /**
     * 一个缓存文件，用于上传图片
     */
    private void cacheFile(Bitmap bitmap) {
        File tempCacheFile = new File(mUploadfilePath,mUploadfileName);
        if (tempCacheFile.exists()) {
            tempCacheFile.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(tempCacheFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

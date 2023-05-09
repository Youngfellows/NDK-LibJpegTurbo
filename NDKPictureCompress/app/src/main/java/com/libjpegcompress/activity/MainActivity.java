package com.libjpegcompress.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.libjpegcompress.R;
import com.libjpegcompress.util.MPermissionsUtils;
import com.libjpegcompress.util.camera.CameraCore;
import com.libjpegcompress.util.camera.CameraProxy;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import me.xiaosai.imagecompress.ImageCompress;


/**
 * @author XiaoSai
 * @version V1.0.0
 * @Description TODO
 * @Class MainActivity
 * @Copyright: Copyright (c) 2016
 */
public class MainActivity extends AppCompatActivity implements CameraCore.CameraResult {

    private String TAG = this.getClass().getSimpleName();

    private Button choose_image, camera_image;
    private CameraProxy cameraProxy;
    private ImageView choose_bit;
    /**
     * SD卡根目录
     */
    private String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath() + "/picture/";
    //private final String externalStorageDirectory = getExternalCacheDir().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        externalStorageDirectory = getExternalCacheDir().getAbsolutePath();
        //压缩后保存临时文件目录
        File tempFile = new File(externalStorageDirectory);
        Log.i(TAG, "onCreate:: tempFile=" + tempFile.getAbsolutePath());
        if (!tempFile.exists()) {
            tempFile.mkdirs();
            Log.d(TAG, "onCreate:: make dirs,tempFile=" + tempFile.getAbsolutePath());
        }
        cameraProxy = new CameraProxy(this, MainActivity.this);
        choose_image = findViewById(R.id.choose_image);
        choose_image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                cameraProxy.getPhoto2Album();
            }
        });
        choose_bit = findViewById(R.id.choose_bit);
        camera_image = findViewById(R.id.camera_image);
        camera_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String cameraPath = externalStorageDirectory + System.currentTimeMillis() / 1000 + ".jpg";
                cameraProxy.getPhoto2Camera(cameraPath);
            }
        });
    }

    //拍照选图片成功回调
    @Override
    public void onCameraSuccess(final String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            ImageCompress.with(this)
                    .load(filePath)
                    .setTargetDir(externalStorageDirectory)
                    .ignoreBy(150)
                    .setOnCompressListener(new ImageCompress.OnCompressListener() {
                        @Override
                        public void onStart() {
                            Log.e("compress", "onStart");
                        }

                        @Override
                        public void onSuccess(String filePath) {
                            Log.e("compress", "onSuccess=" + filePath);
                            choose_bit.setImageBitmap(BitmapFactory.decodeFile(filePath));
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.e("compress", "onError");
                        }
                    }).launch();
        }
    }

    //拍照选图片失败回调
    @Override
    public void onCameraFail(String message) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MPermissionsUtils.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        cameraProxy.onResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cameraProxy.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        cameraProxy.onSaveInstanceState(outState);
    }

}
  

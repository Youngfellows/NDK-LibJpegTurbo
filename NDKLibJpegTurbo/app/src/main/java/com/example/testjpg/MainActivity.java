package com.example.testjpg;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import net.bither.util.NativeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testJpeg();
    }

    private void testJpeg() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    int quality = 90;
                    InputStream in = getResources().getAssets().open("test.jpg");
                    Bitmap bit = BitmapFactory.decodeStream(in);
                    File dirFile = getExternalCacheDir();
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File originalFile = new File(dirFile, "original.jpg");
                    FileOutputStream fileOutputStream = new FileOutputStream(originalFile);
                    bit.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                    File jpegTrueFile = new File(dirFile, "jpegtrue.jpg");
                    File jpegFalseFile = new File(dirFile, "jpegfalse.jpg");
                    NativeUtil.compressBitmap(bit, quality, jpegTrueFile.getAbsolutePath(), true);
                    NativeUtil.compressBitmap(bit, quality, jpegFalseFile.getAbsolutePath(), false);
                    Log.d(TAG, "run:: 压缩图片完成 ...");
                    Log.d(TAG, "run:: jpegTrueFile=" + jpegTrueFile.getAbsolutePath());
                    Log.d(TAG, "run:: jpegFalseFile=" + jpegFalseFile.getAbsolutePath());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
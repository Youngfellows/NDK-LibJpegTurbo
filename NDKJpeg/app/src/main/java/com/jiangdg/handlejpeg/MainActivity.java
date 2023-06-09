package com.jiangdg.handlejpeg;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jiangdg.natives.JPEGUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_IMAGE_OP = 11;
    private ImageView mIvAndrCompress;
    private ImageView mIvHaffCompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIvAndrCompress = findViewById(R.id.iv_android_compress);
        mIvHaffCompress = findViewById(R.id.iv_haffman_compress);
    }

    public void onBtnOpenPics(View view) {
        Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
        getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
        getImageByalbum.setType("image/jpeg");
        startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (requestCode == REQUEST_CODE_IMAGE_OP && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            final String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/compress_haffman.jpg";
            final String output1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/compress_android.jpg";
            Log.d(TAG, "onActivityResult: output=" + output);
            Log.d(TAG, "onActivityResult: output1=" + output1);
            File file = new File(output);
            if (file.exists()) {
                file.delete();
            }
            Log.d(TAG, "onActivityResult:: uri=" + uri.getPath());
            if (uri != null) {
                final String picPath = getPath(uri);
                Log.d(TAG, "onActivityResult:: picPath=" + picPath);
                if (!TextUtils.isEmpty(picPath)) {
                    // 使用自带压缩
                    File file1 = new File(output1);
                    Bitmap bitmap1 = BitmapFactory.decodeFile(picPath);
                    BufferedOutputStream bos = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(file1));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.d(TAG, "onActivityResult:: bos=" + bos);
                        if (bos != null) {
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 40, bos);
                            bos.flush();
                            bos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Glide.with(MainActivity.this).load(output1).into(mIvAndrCompress);
                    // 使用Haffman算法压缩
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run:: 1111");
                            Bitmap bitmap = BitmapFactory.decodeFile(picPath);
                            Log.d(TAG, "run:: bitmap=" + bitmap);
                            int ret = JPEGUtils.nativeCompressJPEG(bitmap, 40, output);
                            Log.d("dddddd", "ret=" + ret);
                            if (ret > 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(MainActivity.this).load(output).into(mIvHaffCompress);
                                    }
                                });
                            }
                        }
                    }).start();
                }
            }
        }
    }

    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "getPath:: uri=" + uri);
            if (DocumentsContract.isDocumentUri(this, uri)) {
                Log.d(TAG, "getPath:: isDocumentUri ...");
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    Log.d(TAG, "getPath:: isExternalStorageDocument ...");
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    Log.d(TAG, "getPath:: isDownloadsDocument ...");
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(this, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    Log.d(TAG, "getPath:: isMediaDocument ...");
                    final String docId = DocumentsContract.getDocumentId(uri);
                    Log.d(TAG, "getPath:: docId=" + docId);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    Log.d(TAG, "getPath:: contentUri=" + contentUri);
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(this, contentUri, selection, selectionArgs);
                }
            }
        }
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        String end = img_path.substring(img_path.length() - 4);
        if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
            return null;
        }
        return img_path;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            Log.d(TAG, "getDataColumn:: uri=" + uri);
            Log.d(TAG, "getDataColumn:: projection[0]=" + projection[0]);
            Log.d(TAG, "getDataColumn:: selection=" + selection);
            Log.d(TAG, "getDataColumn:: selectionArgs[0]=" + selectionArgs[0]);
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            Log.d(TAG, "getDataColumn:: cursor=" + cursor);
            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "getDataColumn:: ...");
                final int index = cursor.getColumnIndexOrThrow(column);
                Log.d(TAG, "getDataColumn:: index=" + index);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}

package com.example.solvemath.utilities;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.camera.core.ImageProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Helper {
    public static final String KEY_PREFERENCE_NAME = "SolveMath";
    public static  Uri getTempImageUri(Context context, Bitmap bitmap) {
        File cacheDir = context.getCacheDir();
        File tempFile = new File(cacheDir, "temp_image.jpg");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(tempFile);
    }

    public static Bitmap toBitmap(ImageProxy image) {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        Bitmap bitmap =  BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);

        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        return rotateBitmap(bitmap, rotationDegrees);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}

package wal.fennel.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Khawar on 18/10/2016.
 */
public class PhotoUtils {

    public static BitmapFactory.Options getDimens(String path)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        return options;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    || (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path) {

        BitmapFactory.Options options = getDimens(path);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, Constants.IMAGE_MAX_DIM, Constants.IMAGE_MAX_DIM);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static byte[] getByteArrayFromFile(File f) {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        byte[] byteArrayImage = new byte[(int)f.length()];
        try {
            is.read(byteArrayImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return byteArrayImage;
    }

    public static Bitmap getBitmapFromPath(String path) {
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = PhotoUtils.decodeSampledBitmapFromResource(path);
        ExifInterface ei = null;
        Bitmap imageBitmap = null;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
//                    imageBitmap = PhotoUtil.getInstance(context).rotateBitmap(bmp, 90);
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }
            imageBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageBitmap;
    }
}

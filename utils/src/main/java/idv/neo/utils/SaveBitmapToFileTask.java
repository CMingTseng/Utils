package idv.neo.utils;

/**
 * Created by Neo on 2017/3/23.
 */

import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveBitmapToFileTask extends AsyncTask<Object, Integer, String> {
    private static final String TAG = SaveBitmapToFileTask.class.getSimpleName();
    private OnTaskCompleted mListener;

    public interface OnTaskCompleted {
        void onTaskCompleted(String filepath);
    }

    public SaveBitmapToFileTask(OnTaskCompleted listener) {
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(Object... params) {
        final Bitmap bmp = (Bitmap) params[0];
        final File newphotofile = (File) params[1];
        final Location location = (Location) params[2];
        final Bitmap.CompressFormat format = (Bitmap.CompressFormat) params[3];
        final int quality = (int) params[4];
        if (newphotofile.exists()) {
            newphotofile.delete();
        }

        try {
            final FileOutputStream filestream = new FileOutputStream(newphotofile);
            bmp.compress(format, quality, filestream);
            filestream.flush();
            filestream.close();
            if (location != null) {
                final ExifInterface exif = new ExifInterface(newphotofile.getCanonicalPath());
                final double lat = location.getLatitude();
                final double lon = location.getLongitude();
                final String lats = LocationConvertUtils.makeLatLongToString(lat);
                final String longs = LocationConvertUtils.makeLatLongToString(lon);
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, lats);
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LocationConvertUtils.makeLatStringRef(lat));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longs);
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LocationConvertUtils.makeLonStringRef(lon));
                exif.saveAttributes();
            }
            if (newphotofile.exists()) {
                return newphotofile.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        Log.d(TAG, "onProgressUpdate Show  Progress  : " + progress[0]);
    }

    @Override
    protected void onPostExecute(String filepath) {
        if (mListener != null && filepath != null) {
            mListener.onTaskCompleted(filepath);
        }
    }
}

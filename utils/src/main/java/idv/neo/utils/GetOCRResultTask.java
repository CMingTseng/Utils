package idv.neo.utils;

/**
 * Created by Neo on 2017/3/23.
 */

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import static idv.neo.utils.FolderFileUtils.getSDPath;

public class GetOCRResultTask extends AsyncTask<Object, Integer, String> {
    private static final String TAG = GetOCRResultTask.class.getSimpleName();
    private OnTaskCompleted mListener;

    public interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }

    public GetOCRResultTask(OnTaskCompleted listener) {
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(Object... params) {
        Bitmap bitmap = (Bitmap) params[0];
        String language = (String) params[1];
        final TessBaseAPI baseApi = new TessBaseAPI(new TessBaseAPI.ProgressNotifier() {
            @Override
            public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
                //FIXME not work !!!!
                //http://stackoverflow.com/questions/30025912/progress-cancel-callback-in-tesseract-using-etext-desc
                Log.d(TAG, "Show  Progress : " + progressValues.getPercent());
                publishProgress(progressValues.getPercent());
            }
        });
        baseApi.init(getSDPath(), language);
        // 必須加此行，tess-two要求BMP必須為此配置
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);
        final String result = baseApi.getUTF8Text();
        baseApi.clear();
        baseApi.end();
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        Log.d(TAG, "onProgressUpdate Show  Progress  : " + progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        if (mListener != null) {
            mListener.onTaskCompleted(result);
        }
    }
}

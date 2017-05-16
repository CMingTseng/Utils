package idv.neo.utils;

import android.database.ContentObserver;

/**
 * Created by Neo on 2017/4/16.
 */

public interface ObservableInterface {
    ContentObservableCompat mGeneralObservable = new ContentObservableCompat();

    void registerContentObserver(ContentObserver observer);

    void unregisterContentObserver(ContentObserver observer);
}

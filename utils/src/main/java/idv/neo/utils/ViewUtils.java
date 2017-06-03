package idv.neo.utils;

import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Created by Neo on 2015/10/13.
 */

public class ViewUtils {
    private static final String TAG = ViewUtils.class.getSimpleName();

    //ref android.support.design.widget.Snackbar
    public static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    public static View getContentView(Activity activity) {
        final ViewGroup view = (ViewGroup) activity.getWindow().getDecorView();
        final FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);
        return content.getChildAt(0);
    }

    public static View getContentlayout(Activity activity) {
        final ViewGroup view = (ViewGroup) activity.getWindow().getDecorView();
        return view.findViewById(android.R.id.content);
    }

    public static FrameLayout getContentFrameLayout(Activity activity) {
        final ViewGroup content = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        return (FrameLayout) content.getChildAt(0);
    }

    public static View getAppContentView(Activity activity) {
        final ViewGroup content = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        return content.getChildAt(0);
    }

    public static void setViewEnableOrDisable(View view, boolean enable) {
        if (view != null && view instanceof ViewGroup) {
            final ViewGroup fallback = (ViewGroup) view;
            final int childcount = fallback.getChildCount();
            for (int i = 0; i < childcount; i++) {
                final View child = fallback.getChildAt(i);
                child.setEnabled(enable);
                if (child instanceof ViewGroup) {
                    setViewEnableOrDisable(child, enable);
                }
            }
        }
    }
}

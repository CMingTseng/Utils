package idv.neo.widget;

/**
 * Created by Neo on 2015/10/15.
 */

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MapLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = MapLayoutManager.class.getSimpleName();
    private static final boolean DEBUG = true;
    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

    /**
     * Many calculations are made depending on orientation. To keep it clean, this interface
     * helps {@link MapLayoutManager} make those decisions.
     * Based on {@link #mOrientation}, an implementation is lazily created in
     * {@link #ensureLayoutState} method.
     */
    OrientationHelper mOrientationHelper;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new RecyclerView.LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new RecyclerView.LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new RecyclerView.LayoutParams(lp);
        }
    }

    public boolean isSnappedToFirstVisibleItem() {
        final float floatPos = (float) mScrollingOffset / mScrollingUnit;
        final int intPos = (int) floatPos;
        return floatPos - intPos == 0;
    }

    public int findOptimalFirstVisibleItemPosition() {
        return (int) ((float) mScrollingOffset / mScrollingUnit + 0.5f);
    }

    private static final class ChildSpanInfo {
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right;
        int bottom;

        int view_left = 0;
        int view_right = 0;
        int view_top = 0;
        int view_bottom = 0;
        int view_width = 0;
        int view_height = 0;
    }

    private int mOrientation = HORIZONTAL;
    private int mRowCount;
    private int mColumnCount;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private ChildSpanInfo[] mChildSpanInfo;

    public MapLayoutManager(Context context, int orientation, int[][] map) {
        super();
        mOrientation = orientation;
        setChildMap(map);
    }

    /**
     * Returns the current orientaion of the layout.
     *
     * @return Current orientation.
     * @see #mOrientation
     * @see #setOrientation(int)
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * Sets the orientation of the layout. {@link android.support.v7.widget.LinearLayoutManager}
     * will do its best to keep scroll position.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        }
        assertNotInLayoutOrScroll(null);
        if (orientation == mOrientation) {
            return;
        }
        mOrientation = orientation;
        mOrientationHelper = null;
        updateOffscreenChildMapping();
        requestLayout();
    }

    public static final int MAP_NO_MAPPING = -2;

    public void setChildMap(int[][] map) {
        mRowCount = map.length;
        mColumnCount = map[0].length;
        int max = -1;
        for (int[] aMap : map)
            for (int anAMap : aMap)
                if (anAMap > max)
                    max = anAMap;
        mChildSpanInfo = new ChildSpanInfo[max + 3];
        for (int i = 0; i < mChildSpanInfo.length; ++i)
            mChildSpanInfo[i] = new ChildSpanInfo();
        for (int y = 0; y < map.length; ++y)
            for (int x = 0; x < map[y].length; ++x) {
                final int idx = map[y][x] + 1;
                if (idx > 0) {
                    final ChildSpanInfo info = mChildSpanInfo[idx];
                    if (info.left > x)
                        info.left = x;
                    if (info.right < x)
                        info.right = x;
                    if (info.top > y)
                        info.top = y;
                    if (info.bottom < y)
                        info.bottom = y;
                }
            }
        updateOffscreenChildMapping();
        recomputeScrollingRange();
        updateChildSpanInfoDimensions();
        requestLayout();

        if (DEBUG) {
            Log.d(TAG, "setChildMap(): " + mColumnCount + " x " + mRowCount);
            for (int i = 0; i < mChildSpanInfo.length; ++i) {
                Log.d(TAG, String.format("    mChildSpanInfo[%d] = { l = %d, t = %d, r = %d, b = %d },",
                        i, mChildSpanInfo[i].left, mChildSpanInfo[i].top, mChildSpanInfo[i].right, mChildSpanInfo[i].bottom));
            }
        }
    }

    private void updateChildSpanInfoDimensions() {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final float rowHeight = (mMeasuredHeight - paddingTop - getPaddingBottom()) / mRowCount;
        final float columnWidth = (mMeasuredWidth - paddingLeft - getPaddingRight()) / mColumnCount;

        for (ChildSpanInfo info : mChildSpanInfo) {
            info.view_left = (int) (paddingLeft + columnWidth * info.left + 0.5f);
            info.view_top = (int) (paddingTop + rowHeight * info.top + 0.5f);
            info.view_right = (int) (paddingLeft + columnWidth * (info.right + 1) + 0.5f);
            info.view_bottom = (int) (paddingTop + rowHeight * (info.bottom + 1) + 0.5f);
            info.view_width = info.view_right - info.view_left;
            info.view_height = info.view_bottom - info.view_top;
        }
    }

    private void updateOffscreenChildMapping() {
        if (mOrientation == HORIZONTAL) {
            final ChildSpanInfo first = mChildSpanInfo[0];
            final ChildSpanInfo last = mChildSpanInfo[mChildSpanInfo.length - 1];
            first.left = first.right = -1;
            first.top = first.bottom = mRowCount / 2;
            last.left = last.right = mColumnCount;
            last.top = last.bottom = (mRowCount + 1) / 2;
        } else { // if (mOrientation == VERTICAL)
            final ChildSpanInfo first = mChildSpanInfo[0];
            final ChildSpanInfo last = mChildSpanInfo[mChildSpanInfo.length - 1];
            first.left = first.right = mColumnCount / 2;
            first.top = first.bottom = -1;
            last.left = last.right = mRowCount;
            last.top = last.bottom = (mColumnCount + 1) / 2;
        }
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        Log.d(TAG, "onMeasure(): recycler = " + recycler + ", state = " + state + ", widthSpec = " + widthSpec + ", heightSpec = " + heightSpec);
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);
        int width, height;
        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
            case View.MeasureSpec.AT_MOST:
                width = widthSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                width = getMinimumWidth();
                break;
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
            case View.MeasureSpec.AT_MOST:
                height = heightSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
            default:
                height = getMinimumHeight();
                break;
        }

        setMeasuredDimension(width, height);
        updateChildSpanInfoDimensions();
        recomputeScrollingRange();
    }

    private int mScrollingUnit = 0;

    private void recomputeScrollingRange() {
        if (mOrientation == HORIZONTAL)
            mScrollingUnit = (mMeasuredWidth - getPaddingLeft() - getPaddingRight()) / mColumnCount;
        else // VERTICAL
            mScrollingUnit = (mMeasuredHeight - getPaddingTop() - getPaddingBottom()) / mRowCount;

        mScrollingRange = mScrollingUnit * (getItemCount() - mChildSpanInfo.length + 2);
    }

    List<View> mUnattachedChildren = new ArrayList<>();

    protected float getInterpolation(float input, float start, float end) {
        return input * (end - start) + start;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        ensureLayoutState();

        for (int i = 0; i < getChildCount(); ++i)
            mUnattachedChildren.add(getChildAt(i));

        detachAndScrapAttachedViews(recycler);

        final float floatPos = (float) mScrollingOffset / mScrollingUnit;
        final int intPos = (int) floatPos;
        final float tailPos = floatPos - intPos;

        for (int i = 1; i < mChildSpanInfo.length; ++i) {
            try {
                View view = recycler.getViewForPosition(intPos + i - 1);
                final ChildSpanInfo info0 = mChildSpanInfo[i - 1];
                final ChildSpanInfo info1 = mChildSpanInfo[i];

                addView(view);
                while (mUnattachedChildren.remove(view)) /* do nothing */ ;

                final int target_width = (int) getInterpolation(tailPos, info1.view_width, info0.view_width);
                final int target_height = (int) getInterpolation(tailPos, info1.view_height, info0.view_height);
                final int target_left = (int) getInterpolation(tailPos, info1.view_left, info0.view_left);
                final int target_right = (int) getInterpolation(tailPos, info1.view_right, info0.view_right);
                final int target_top = (int) getInterpolation(tailPos, info1.view_top, info0.view_top);
                final int target_bottom = (int) getInterpolation(tailPos, info1.view_bottom, info0.view_bottom);

                view.measure(
                        View.MeasureSpec.makeMeasureSpec(target_width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(target_height, View.MeasureSpec.EXACTLY)
                );
                layoutDecorated(
                        view,
                        target_left, target_top,
                        target_right, target_bottom
                );
            } catch (IndexOutOfBoundsException ignored) {
            }
        }

        for (View child : mUnattachedChildren)
            removeAndRecycleView(child, recycler);
        mUnattachedChildren.clear();
    }

    @Override
    public void measureChild(View child, int widthUsed, int heightUsed) {
        super.measureChild(child, widthUsed, heightUsed);
    }

    @Override
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        super.measureChildWithMargins(child, widthUsed, heightUsed);
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
                                    RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        if (mOrientation == HORIZONTAL) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    int mScrollingOffset = 0;
    int mScrollingRange = 0;

    private int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        final int mOldScrollingOffset = mScrollingOffset;
        mScrollingOffset += dy;
        if (mScrollingOffset < 0)
            mScrollingOffset = 0;
        else if (mScrollingOffset >= mScrollingRange)
            mScrollingOffset = mScrollingRange;
        onLayoutChildren(recycler, state);
        return mScrollingOffset - mOldScrollingOffset;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            /**
             * {@inheritDoc}
             */
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int
                    snapPreference) {
                return boxStart - viewStart + 1;
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return MapLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }
        };
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;
        if (mOrientation == HORIZONTAL) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return mMeasuredWidth / mColumnCount * (mChildSpanInfo.length - 2);
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return mOrientation == HORIZONTAL ? mScrollingOffset : 0;
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return mOrientation == HORIZONTAL ? (mMeasuredWidth / mColumnCount * getItemCount()) : 0;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return mMeasuredHeight / mRowCount * (mChildSpanInfo.length - 2);
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return mOrientation == VERTICAL ? mScrollingOffset : 0;
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return mOrientation == VERTICAL ? (mMeasuredHeight / mRowCount * getItemCount()) : 0;
    }

    @Override
    public void setMeasuredDimension(int widthSize, int heightSize) {
        mMeasuredWidth = widthSize;
        mMeasuredHeight = heightSize;
        super.setMeasuredDimension(widthSize, heightSize);
    }

    void ensureLayoutState() {
        if (mOrientationHelper == null)
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
    }
}
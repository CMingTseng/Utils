package idv.neo.widget;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Neo on 2015/10/15.
 */
public class AutoTextureVideoView extends TextureView implements TextureView.SurfaceTextureListener, MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = AutoTextureVideoView.class.getSimpleName();
    private Uri mUri;
    private Map<String, String> mHeaders;
    private static int mAspectRatio = 0;

    public class AspectRatio {
        public static final int normal = 0;
        public static final int HD = 1;
        public static final int trandition = 2;
    }

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private Vector<Pair<InputStream, MediaFormat>> mPendingSubtitleTracks;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private Surface mSurface = null;
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayer mMediaPlayer = null;
    private int mAudioSession;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaController mMediaController;
    private int mCurrentBufferPercentage;
    private boolean mMediaPlayerPrepared = false;
    private boolean mSurfaceTextureReady = false;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    //	private MediaExtractor extractorVideo = new MediaExtractor();
    private SurfaceView mSurfaceView;

    public AutoTextureVideoView(final Context context) {
        super(context);
        initVideoView();
    }

    public AutoTextureVideoView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initVideoView();
    }

    public AutoTextureVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView();
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        setSurfaceTextureListener(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mPendingSubtitleTracks = new Vector<Pair<InputStream, MediaFormat>>();
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    private void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        initVideo();
        requestLayout();
        invalidate();
    }

    public void setAspectRatio(int ratioType) {
        mAspectRatio = ratioType;
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
        release(true);
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        initVideo();
//		mMediaPlayer.reset();
//		mMediaPlayerPrepared = false;
    }

    public void stop() {
        release(false);
//		mMediaPlayer.stop();
    }

    private void initVideo() {
        if (mUri == null) {
            return;
        }
        release(false);
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            Log.d(TAG, "new MediaPlayer @initVideo _Uri : " + mUri);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mCurrentBufferPercentage = 0;
//            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);//FIXME ijkPlayer can not use
            mMediaPlayer.setDataSource(mUri.toString());//FIXME ijkPlayer use
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setScreenOnWhilePlaying(true);//FIXME
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachMediaController();
//            if (mAudioSession != 0) {
//                mMediaPlayer.setAudioSessionId(mAudioSession);
//            } else {
//                mAudioSession = mMediaPlayer.getAudioSessionId();
//            }
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } finally {
            mPendingSubtitleTracks.clear();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AutoTextureVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AutoTextureVideoView.class.getName());
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    public void stopPlayback() {
        release(true);
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }


    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(null);
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaPlayerPrepared = false;
            mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "MediaPlayer onPrepared  " + this + "VideoWidth: " + mediaPlayer.getVideoWidth() + "VideoHeight : " + mediaPlayer.getVideoHeight());
        mMediaPlayerPrepared = true;
        mCurrentState = STATE_PREPARED;
        mCanPause = mCanSeekBack = mCanSeekForward = true;
        if (mMediaController != null) {
            mMediaController.setEnabled(true);
        }
        int seekToPosition = mSeekWhenPrepared;
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
//			getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
            if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mTargetState == STATE_PLAYING) {
                    start();
                    if (mMediaController != null) {
                        mMediaController.show();
                    }
                } else if (!isPlaying() &&
                        (seekToPosition != 0 || getCurrentPosition() > 0)) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mMediaController.show(0);
                    }
                }
                requestLayout();
                invalidate();
            }
        } else {
            if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
        tryStartPlayback();
    }

    private void tryStartPlayback() {
        Log.d(TAG, "tryStartPlayback...MediaPlayerPrepared : " + mMediaPlayerPrepared + "_SurfaceTextureReady :" + mSurfaceTextureReady);
        if (mMediaPlayerPrepared && mSurfaceTextureReady) {
            if (mMediaPlayer != null && mSurface != null) {
                mMediaPlayer.setSurface(mSurface);
                mMediaPlayer.setVolume(0.0f, 0.0f);
                mMediaPlayer.start();
                start();
            }
        }
    }


    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        Log.d(TAG, "onVideoSizeChanged...MediaPlayer Width : " + mediaPlayer.getVideoWidth() + "_MediaPlayer Height : " + mediaPlayer.getVideoHeight() + "_Width : " + width + "_Height : " + height);
        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        //FIXME need reset BufferSize  ?? will crash!!
//		if (mVideoWidth != 0 && mVideoHeight != 0) {
//			getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
        requestLayout();
//		}
        invalidate();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        mCurrentBufferPercentage = percent;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mCurrentState == STATE_PLAYBACK_COMPLETED) {
            // sprylab bugfix: on some devices onCompletion is called twice
            return;
        }
        mCurrentState = STATE_PLAYBACK_COMPLETED;
        mTargetState = STATE_PLAYBACK_COMPLETED;
        if (mMediaController != null) {
            mMediaController.hide();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int framework_err, int impl_err) {
        if (framework_err == MediaPlayer.MEDIA_ERROR_IO) {
            Log.e(TAG, "onError... : File or network related operation errors." + "," + impl_err);
        } else if (framework_err == MediaPlayer.MEDIA_ERROR_MALFORMED) {
            Log.e(TAG, "onError... :  Bitstream is not conforming to the related coding standard or file spec." + "," + impl_err);
        } else if (framework_err == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.e(TAG, "onError... : Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one." + "," + impl_err);
        } else if (framework_err == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
            Log.e(TAG, "onError... :Some operation takes too long to complete, usually more than 3-5 seconds." + "," + impl_err);
        } else if (framework_err == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.e(TAG, "onError... : Unspecified media player error." + "," + impl_err);
        } else if (framework_err == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
            Log.e(TAG, "onError... : Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature." + "," + impl_err);
        } else if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            Log.e(TAG, "onError... :The video is streamed and its container is not valid for progressive playback i.e the video's index (e.g moov atom) is not at the start of the file." + "," + impl_err);
        } else {
            Log.d(TAG, "onError... :Unknown error " + framework_err + ", " + impl_err);
        }

        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;
        if (mMediaController != null) {
            mMediaController.hide();
        }
        // FIXME onError need show AlertDialog ??
//        if (getWindowToken() != null) {
//            Resources r = mContext.getResources();
//            int messageId;
//
//            if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                messageId = R.string.VideoView_error_text_invalid_progressive_playback;
//            } else {
//                messageId = R.string.VideoView_error_text_unknown;
//            }
//
//            new AlertDialog.Builder(mContext)
//                    .setMessage(messageId)
//                    .setPositiveButton(R.string.VideoView_error_button,
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int whichButton) {
//                                        /* If we get here, there is no onError listener, so
//                                         * at least inform them that the video is over.
//                                         */
////                                    if (mOnCompletionListener != null) {
////                                        mOnCompletionListener.onCompletion(mMediaPlayer);
////                                    }
//                                }
//                            })
//                    .setCancelable(false)
//                    .show();
//        }

        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {//FIXME Horizontal or Vertical
        Log.d(TAG, "adjustAspectRatio... videoWidth : " + videoWidth + "x videoHeight : " + videoHeight + "__View Width : " + this.getWidth() + "_Height : " + this.getHeight());
        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();
        if (videoWidth < viewWidth && videoHeight < viewHeight) {
            viewWidth = videoWidth;
            viewHeight = videoHeight;
        }
        double aspectRatio = (double) 9 / 16;
        switch (mAspectRatio) {
            case 2:
                aspectRatio = (double) 3 / 4;
                break;
            case 1:
                aspectRatio = (double) 9 / 16;
                break;
            case 0:
            default:
                if (videoHeight != 0 || videoWidth != 0) {
                    aspectRatio = (double) videoHeight / videoWidth;
                }
        }
        Log.d(TAG, "adjust  aspectRatio : " + aspectRatio);
        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {//FIXME Horizontal or Vertical
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        mVideoWidth = newWidth;
        mVideoHeight = newHeight;
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.d(TAG, "video=" + videoWidth + "x" + videoHeight + " view=" + viewWidth + "x" + viewHeight + " newView=" + newWidth + "x" + newHeight + " off=" + xoff + "," + yoff);
        final Matrix txform = new Matrix();
        this.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        this.setTransform(txform);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfacetexture, int width, int height) {
        Log.d(TAG, this + "_onSurfaceTextureAvailable...");
        mSurfaceTextureReady = true;
        mSurfaceTexture = surfacetexture;
        mSurface = new Surface(mSurfaceTexture);
        release(false);
        if (mUri != null) {//FIXME View move will call onSurfaceTextureDestroyed need renew MediaPlayer
            try {
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                }
                mMediaPlayer.setDataSource(mUri.toString());
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnVideoSizeChangedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnInfoListener(this);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mCurrentBufferPercentage = 0;
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "Unable to open content: " + mUri, e);
                onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            }
        }
//            if (mAudioSession != 0) {
//                mMediaPlayer.setAudioSessionId(mAudioSession);
//            } else {
//                mAudioSession = mMediaPlayer.getAudioSessionId();
//            }
        tryStartPlayback();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfacetexture, int width, int height) {
        Log.d(TAG, this + "_onSurfaceTextureSizeChanged() : View width : " + width + " x View height : " + height);
        boolean isValidState = (mTargetState == STATE_PLAYING);
        boolean hasValidSize = (mVideoWidth == width && mVideoHeight == height);
        if (mMediaPlayer != null && isValidState && hasValidSize) {
//			if (mSeekWhenPrepared != 0) {
//				seekTo(mSeekWhenPrepared);
//			}
            adjustAspectRatio(width, height);
            start();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfacetexture) {
        Log.d(TAG, this + "_onSurfaceTextureDestroyed...");
        Log.d(TAG, "TextureView @ onDetachedFromWindowInternal call destroySurface and then onSurfaceTextureDestroyed");
        surfacetexture.release();
        mSurface = null;
        mSurfaceTextureReady = false;
        if (mMediaController != null) {
            mMediaController.hide();
        }
        release(true);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfacetexture) {
    }
}


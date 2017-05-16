package idv.neo.widget;

import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import idv.neo.utils.Pyramid;

/**
 * Created by Neo on 2017/4/15.
 */

public class SensorRenderer implements GLSurfaceView.Renderer {
    final String TAG = SensorRenderer.class.getSimpleName();

    private Pyramid mPyramid;
    protected final float[] mRotationMatrix = new float[16];

    public SensorRenderer() {
        mPyramid = new Pyramid();
        // initialize the rotation matrix to identity
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;
    }

    public void updateRenderer(float[] values) {
        // convert the rotation-vector to a 4x4 matrix. the matrix
        // is interpreted by Open GL as the inverse of the
        // rotation-vector, which is what we want.
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, values);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // dither is enabled by default, we don't need it
        gl.glDisable(GL10.GL_DITHER);
        // clear screen in white
        gl.glClearColor(1, 1, 1, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // set view-port
        gl.glViewport(0, 0, width, height);
        // set projection matrix
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // clear screen
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        // set-up modelview matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -3.0f);
        gl.glMultMatrixf(mRotationMatrix, 0);

        // draw our object
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        mPyramid.draw(gl);
    }
}

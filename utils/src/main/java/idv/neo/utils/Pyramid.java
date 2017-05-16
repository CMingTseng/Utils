package idv.neo.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Neo on 2017/4/15.
 */

public class Pyramid {
    // initialize our pyramid
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;

    public Pyramid() {
        final float vertices[] = {
                -1, -1, -1, 1, -1, -1,
                1, 1, -1, -1, 1, -1,
                0, 0, 1,
        };

        final float colors[] = {
                0, 0, 0, 1, 1, 1, 0, 1,
                0, 0, 1, 1, 1, 1, 1, 1,
                0, 1, 1, 1,
        };

        final byte indices[] = {
                1, 4, 2, 2, 4, 3,
                3, 4, 0, 0, 4, 1,
                3, 0, 1, 3, 1, 2
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CW);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, 18, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
    }
}

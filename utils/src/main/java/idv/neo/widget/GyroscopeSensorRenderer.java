package idv.neo.widget;

import android.hardware.SensorManager;

/**
 * Created by Neo on 2017/4/15.
 */

public class GyroscopeSensorRenderer extends SensorRenderer {
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0f;
    private final float[] deltaRotationVector = new float[4];
    private long timestamp;

    @Override
    public void updateRenderer(float[] values) {
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        long eventTimeStamp = System.nanoTime();
        if (timestamp != 0) {
            final float dT = (eventTimeStamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = values[0];
            float axisY = values[1];
            float axisZ = values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = eventTimeStamp;
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    }
}


package idv.neo.utils;

/**
 * Created by Neo on 2017/4/15.
 */

/**
 * **********************************************************************
 * Compilation:  javac Matrix.java
 * Execution:    java Matrix
 * <p/>
 * A bare-bones collection of static methods for manipulating
 * matrices.
 * <p/>
 * ***********************************************************************
 */

public class Matrix {

    // return a random m-by-n matrix with values between 0 and 1
    public static float[][] random(int m, int n) {
        float[][] C = new float[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = (float) Math.random();
        return C;
    }

    // return n-by-n identity matrix I
    public static float[][] identity(int n) {
        float[][] I = new float[n][n];
        for (int i = 0; i < n; i++)
            I[i][i] = 1;
        return I;
    }

    // return x^T y
    public static float dot(float[] x, float[] y) {
        if (x.length != y.length) throw new RuntimeException("Illegal vector dimensions.");
        float sum = 0.0f;
        for (int i = 0; i < x.length; i++)
            sum += x[i] * y[i];
        return sum;
    }

    // return C = A^T
    public static float[][] transpose(float[][] A) {
        int m = A.length;
        int n = A[0].length;
        float[][] C = new float[n][m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[j][i] = A[i][j];
        return C;
    }

    // return C = A + B
    public static float[][] add(float[][] A, float[][] B) {
        int m = A.length;
        int n = A[0].length;
        float[][] C = new float[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];
        return C;
    }

    // return C = A - B
    public static float[][] subtract(float[][] A, float[][] B) {
        int m = A.length;
        int n = A[0].length;
        float[][] C = new float[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];
        return C;
    }

    // return C = A * B
    public static float[][] multiply(float[][] A, float[][] B) {
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = A[0].length;
        if (nA != mB) throw new RuntimeException("Illegal matrix dimensions.");
        float[][] C = new float[mA][nB];
        for (int i = 0; i < mA; i++)
            for (int j = 0; j < nB; j++)
                for (int k = 0; k < nA; k++)
                    C[i][j] += (A[i][k] * B[k][j]);
        return C;
    }

    // matrix-vector multiplication (y = A * x)
    public static float[] multiply(float[][] A, float[] x) {
        int m = A.length;
        int n = A[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        float[] y = new float[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += (A[i][j] * x[j]);
        return y;
    }


    // vector-matrix multiplication (y = x^T A)
    public static float[] multiply(float[] x, float[][] A) {
        int m = A.length;
        int n = A[0].length;
        if (x.length != m) throw new RuntimeException("Illegal matrix dimensions.");
        float[] y = new float[n];
        for (int j = 0; j < n; j++)
            for (int i = 0; i < m; i++)
                y[j] += (A[i][j] * x[i]);
        return y;
    }
}


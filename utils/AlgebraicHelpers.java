package utils;

public class AlgebraicHelpers {

    public static double[][] transpose(double[][] matrix) {
        double[][] matrixTransposed = new double[matrix[0].length][matrix.length];
        for ( int i = 0; i < matrix.length; i++ ) {
            for ( int j = 0; j < matrix[0].length; j++ ) {
                matrixTransposed[j][i] = matrix[i][j];
            }
        }
        return matrixTransposed;
    }

    public static double[][] matrixMultiplication(double[][] matrixA, double[][] matrixB) throws Exception {
        if ( matrixA[0].length != matrixB.length ) {
            throw new Exception("Matrix A must have the same number of columns as the rows of matrix B");
        }
        double[][] result = new double[matrixA.length][matrixB[0].length];
        for ( int i = 0; i < matrixA.length; i++ ) {
            for ( int j = 0; j < matrixB[0].length; j++ ) {
                for ( int k = 0; k < matrixB.length; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] matrixMultiplicationByNumber(double[][] matrix, double number) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for ( int i = 0; i < matrix.length; i++ ) {
            for ( int j = 0; j < matrix[0].length; j++ ) {
                result[i][j] = matrix[i][j] * number;
            }
        }
        return result;
    }

    public static double determinant(double[][] matrix) throws Exception {
        if ( matrix.length != matrix[0].length ) {
            throw new Exception("The matrix is not n x n");
        }
        return determinantRecursive(matrix);
    }

    private static double determinantRecursive(double[][] matrix) {
        if ( matrix.length == 1 ) {
            return matrix[0][0];
        }
        if ( matrix.length == 2 ) {
            return (matrix[0][0]*matrix[1][1]) - (matrix[0][1]*matrix[1][0]);
        }
        double result = 0.0;
        for ( int j = 0; j < matrix.length; j++ ) {
            result += matrix[0][j] * Math.pow(-1, (j + 2)) * determinantRecursive(getSubMatrix(matrix, 0, j));
        }
        return result;
    }

    private static double[][] getSubMatrix(double[][] matrix, int row, int column) {
        double[][] subMatrix = new double[matrix.length - 1][matrix.length - 1];
        for ( int i = 0, i2 = 0; i < matrix.length; i++ ) {
            if ( i == row ) {
                continue;
            }
            for ( int j = 0, j2 = 0; j < matrix.length; j++ ) {
                if ( j == column ) {
                    continue;
                }
                subMatrix[i2][j2] = matrix[i][j];
                j2++;
            }
            i2++;
        }
        return subMatrix;
    }

    public static double[][] adjugate(double[][] matrix) throws Exception {
        if ( matrix.length != matrix[0].length ) {
            throw new Exception("The matrix is not n x n");
        }
        double[][] matrixAdjugated = new double[matrix.length][matrix[0].length];
        if ( matrix.length == 1 ) {
            matrixAdjugated[0][0] = matrix[0][0];
            return matrixAdjugated;
        }
        for ( int i = 0; i < matrix.length; i++ ) {
            for ( int j = 0; j < matrix[0].length; j++ ) {
                matrixAdjugated[i][j] = Math.pow(-1, i + j + 2) * determinantRecursive(getSubMatrix(matrix, i, j));
            }
        }
        return matrixAdjugated;
    }

    public static double[][] inverse(double[][] matrix) throws Exception {
        if ( matrix.length != matrix[0].length ) {
            throw new Exception("The matrix is not n x n");
        }
        double determinant = determinantRecursive(matrix);
        if ( determinant == 0 ) {
            throw new Exception("The determinant of the matrix is 0");
        }
        if ( matrix.length == 1 ) {
            double[][] matrixInverse = new double[matrix.length][matrix[0].length];
            matrixInverse[0][0] = matrix[0][0];
            return matrixInverse;
        }
        return matrixMultiplicationByNumber(transpose(adjugate(matrix)), 1/determinant);
    }

    public static double[] getVectorOfTheMatrix(double[][] matrix, int column) throws Exception {
        if ( column < 0 || column >= matrix[0].length ) {
            throw new Exception("Column index (" + column + ") out of range (" + (matrix[0].length - 1) +")");
        }
        double[] vector = new double[matrix.length];
        for ( int i = 0; i < matrix.length; i++ ) {
            vector[i] = matrix[i][column];
        }
        return vector;
    }

    public static double getProductPoint(double[] vectorA, double[] vectorB) throws Exception {
        if ( vectorA.length != vectorB.length ) {
            throw new Exception("Vectors are not the same size (" + vectorA.length + " - " + vectorB.length + ")");
        }
        double result = 0;
        for ( int i = 0; i < vectorA.length; i++ ) {
            result += vectorA[i] * vectorB[i];
        }
        return result;
    }
}

package windows;

import models.AbstractModel;
import models.AbstractParams;
import models.Regression;
import utils.AlgebraicHelpers;
import utils.DataSet;
import utils.DataSetFunctions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PerceptronThread extends Regression implements Runnable {

    private DataSet dataSet;
    private AbstractParams params;
    private PerceptronWindow window;

    private Model model;

    public PerceptronThread()
    {

    }

//    public static Model generateModel(DataSet dataSet, Params params, PerceptronWindow window) throws Exception {
//        PerceptronThread perceptron = new PerceptronThread();
//        return perceptron.makeModel(dataSet, params, window);
//    }

    @Override
    protected Model makeModel(DataSet dataSet) throws Exception {
        throw new Exception("Not supported");
    }

    @Override
    protected AbstractModel<Double> makeModel(DataSet dataSet, AbstractParams params) throws Exception {
        throw new Exception("Not supported");
    }

    public void makeModel(DataSet dataSet, AbstractParams params, PerceptronWindow window) throws Exception {
        this.dataSet = dataSet;
        this.params = params;
        this.window = window;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            validateDataSetAndParams(dataSet, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int idxTarget = DataSetFunctions.getIndexAtAtributeFromDataSet(dataSet.getHeaders(), dataSet.getTarget());

        double[][] X;
        try {
            X = generateAnXMatrix(DataSetFunctions.generateNumericMatrix(dataSet, getColumns(dataSet, idxTarget)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        double[] Y;
        try {
            Y = DataSetFunctions.generateNumericVectorAsDouble(dataSet, idxTarget);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        double[] weights = ((Params) params).weights;
        double learningRate = ((Params) params).learningRate;
        int epochs = ((Params) params).epochs;
        int epoch = 0;
        boolean done = false;

        while ( !done && epoch < epochs ) {
            done = true;
            for ( int i = 0; i < X.length; i++ ) {
                double error;
                try {
                    error = Y[i] - getNetActivation(X[i], weights);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if ( error != 0.0 ) {
                    done = false;
                    for ( int j = 0; j < weights.length; j++ ) {
                        weights[j] = weights[j] + (learningRate * error * X[i][j]);
                    }
                    window.updateWeights(weights);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            epoch++;
            window.updateEpoch(epoch, done, false);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        window.updateEpoch(epoch, done, true);
        window.updateWeights(weights);
        window.setModel(new Model(dataSet, epochs, epoch, weights, done));
    }

    private static double getNetActivation(double[] instance, double[] weights) throws Exception {
        // Linear Variety
        return AlgebraicHelpers.getProductPoint(weights, instance) >= 0 ? 1 : 0;
    }

    private static double[][] generateAnXMatrix(double[][] matrix) {
        double[][] newMatrix = new double[matrix.length][matrix[0].length + 1];
        for ( int i = 0; i < matrix.length; i++ ) {
            newMatrix[i][0] = -1;
        }
        for ( int i = 1; i < newMatrix[0].length; i++ ) {
            for ( int j = 0; j < newMatrix.length; j++ ) {
                newMatrix[j][i] = matrix[j][i - 1];
            }
        }
        return newMatrix;
    }

    private static int[] getColumns(DataSet dataSet, int idxExcept) {
        int[] columns = new int[dataSet.getHeaders().size() - 1];
        for ( int i = 0, j = 0; i < dataSet.getHeaders().size(); i++ ) {
            if ( i == idxExcept ) {
                continue;
            }
            columns[j++] = i;
        }
        return columns;
    }

    private static void validateDataSetAndParams(DataSet dataSet, AbstractParams params) throws Exception {
        // DataSet
        DataSetFunctions.validateDataSet(dataSet);
        int idxTarget = DataSetFunctions.getIndexAtAtributeFromDataSet(dataSet.getHeaders(), dataSet.getTarget());
        if ( idxTarget == -1 ) {
            throw new Exception("Target not found");
        }
        for ( int i = 0; i < dataSet.getAttributeTypes().size(); i++ ) {
            if ( !dataSet.getAttributeTypes().get(i).equals(DataSet.NUMERIC_TYPE) ) {
                throw new Exception("The attribute " + dataSet.getHeaders().get(i) + " must be numeric");
            }
        }
        // Params
        if ( params == null ) {
            throw new Exception("Object params can not be null");
        }
        if ( !(params instanceof PerceptronThread.Params) ) {
            throw new Exception("Params object is invalid");
        }
        if ( ((Params) params).weights == null ) {
            throw new Exception("The weights of the parameters can not be null");
        }
        if ( ((Params) params).weights.length != dataSet.getHeaders().size() ) {
            throw new Exception("The weights indicated do not match those of the data set");
        }
        // Target
        Set<String> targetsFromDataSet = new HashSet<>();
        for ( int i = 0; i < dataSet.getInstances().size(); i++ ) {
            targetsFromDataSet.add( dataSet.getInstances().get(i).get(idxTarget) );
        }
        for ( String target : targetsFromDataSet ) {
            if ( !target.equals("1") && !target.equals("0") ) {
                throw new Exception("Class values are not allowed, only 0 and 1 are allowed");
            }
        }
    }

    public static class Params extends AbstractParams {

        private double learningRate;
        private int epochs;
        private double[] weights;

        public void setLearningRate(double learningRate) {
            this.learningRate = learningRate;
        }

        public void setEpochs(int epochs) {
            this.epochs = epochs;
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

    }

    public static class Model implements AbstractModel<Double> {

        private final DataSet dataSet;
        private final int epochs;
        private final int epoch;
        private final double[] weights;
        private final boolean done;

        public Model(DataSet dataSet, int epochs, int epoch, double[] weights, boolean done)
        {
            this.dataSet = dataSet;
            this.epochs = epochs;
            this.epoch = epoch;
            this.weights = weights;
            this.done = done;
        }

        @Override
        public Double predict(Object[] instance) throws Exception {
            if ( instance.length != dataSet.getHeaders().size() - 1 ) {
                throw new Exception("The instance is not the same size as the data set (" + instance.length + " - " + (dataSet.getHeaders().size() - 1) + ")");
            }
            for ( Object value : instance ) {
                if ( !(value instanceof Double) ) {
                    throw new Exception("Some instance value is not a numerical value");
                }
            }
            double[] newInstance = new double[instance.length];
            for ( int i = 0; i < instance.length; i++ ) {
                newInstance[i] = (double) instance[i];
            }
            double[] evaluate = new double[instance.length + 1];
            evaluate[0] = -1;
            System.arraycopy(newInstance, 0, evaluate, 1, instance.length);
            return getNetActivation(evaluate, weights);
        }

        @Override
        public void predict(DataSet dataSet, String classNameOut) throws Exception {
            throw new Exception("Not implemented yet");
        }

        @Override
        public String toString() {
            return "Model{" +
                    "epochs=" + epochs +
                    ", epoch=" + epoch +
                    ", weights=" + Arrays.toString(weights) +
                    ", done=" + done +
                    '}';
        }

    }

}

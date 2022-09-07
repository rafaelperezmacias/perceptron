package utils;

public class AritmeticHelpers {

    public interface SummationExpression {

        double result(int pos);

    }

    public static double summation(SummationExpression expression, double[][] vectors) throws Exception {
        if ( expression == null ) {
            throw new Exception("The object expression can not be null");
        }
        for (int i = 1; i < vectors.length; i++) {
            if (vectors[i].length != vectors[0].length) {
                throw new Exception("Some of the vectors do not have the same size");
            }
        }
        double result = 0;
        for (int i = 0; i < vectors[0].length; i++) {
            result += expression.result(i);
        }
        return result;
    }

}

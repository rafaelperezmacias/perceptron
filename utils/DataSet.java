package utils;

import java.util.ArrayList;
import java.util.Arrays;

public class DataSet {

    private ArrayList<ArrayList<String>> instances;

    private ArrayList<String> headers;

    private ArrayList<String> attributeTypes;

    private String target;

    private final int WIDTH_COLUMN = 16;

    public static final String NUMERIC_TYPE = "Numeric";
    public static final String CATEGORICAL_TYPE = "Categorical";

    private DataSet()
    {

    }

    public void addInstance(ArrayList<String> instance) throws Exception {
        if ( instance.size() != headers.size() ) {
            throw new Exception("Instance size (" + instance.size() + ") does not match the data set (" + headers.size() + ")");
        }
        instances.add(instance);
    }

    public ArrayList<ArrayList<String>> getInstances() {
        return instances;
    }

    public ArrayList<String> getHeaders() {
        return headers;
    }

    public ArrayList<String> getAttributeTypes() {
        return attributeTypes;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (headers != null && headers.size() > 0) {
            headers.forEach(header -> builder.append(textFormatter(header, WIDTH_COLUMN)));
            builder.append("\n");
        }
        if (attributeTypes != null && attributeTypes.size() > 0) {
            attributeTypes.forEach(header -> builder.append(textFormatter(header, WIDTH_COLUMN)));
            builder.append("\n");
        }
        if (instances != null && instances.size() > 0) {
            instances.forEach(instance -> {
                instance.forEach(value -> builder.append(textFormatter(value, WIDTH_COLUMN)));
                builder.append("\n");
            });
        }
        return builder.toString();
    }

    private static String textFormatter(String text, int length) {
        StringBuilder result = new StringBuilder();
        result.append(text);
        if (result.length() > length) {
            result.delete(length - 3, result.length());
            result.append(".").append(".").append(".");
        } else {
            while (result.length() < length) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    public static DataSet copyDataSet(DataSet copyDataSet) throws Exception {
        if ( copyDataSet == null ) {
            throw new Exception("The data set cannot be null");
        }
        ArrayList<String> headers = new ArrayList<>(copyDataSet.headers);
        ArrayList<ArrayList<String>> instances = new ArrayList<>();
        for (int i = 0; i < copyDataSet.getInstances().size(); i++) {
            instances.add(new ArrayList<>(copyDataSet.getInstances().get(i)));
        }
        ArrayList<String> atributeTypes = new ArrayList<>(copyDataSet.attributeTypes);
        DataSet newDataSet = new DataSet();
        newDataSet.headers = headers;
        newDataSet.attributeTypes = atributeTypes;
        newDataSet.instances = instances;
        newDataSet.target = copyDataSet.target;
        return newDataSet;
    }

    public static DataSet getEmptyDataSetWithHeaders(String[] headers, String[] attributeTypes, String target) throws Exception {
        if ( headers.length != attributeTypes.length || headers.length == 0 ) {
            throw new Exception("The size of the input vectors is invalid (headers or attributeTypes)");
        }
        for ( String attributeType : attributeTypes ) {
            if ( !attributeType.equalsIgnoreCase(CATEGORICAL_TYPE) && !attributeType.equalsIgnoreCase(NUMERIC_TYPE) ) {
                throw new Exception("The attribute type " + attributeType + " is invalid");
            }
        }
        DataSet dataSet = new DataSet();
        dataSet.headers = new ArrayList<>(Arrays.asList(headers));
        dataSet.attributeTypes = new ArrayList<>(Arrays.asList(attributeTypes));
        dataSet.instances = new ArrayList<>();
        int idxTarger = DataSetFunctions.getIndexAtAtributeFromDataSet(dataSet.headers, target);
        if ( idxTarger == -1 ) {
            throw new Exception("Target not found");
        }
        dataSet.target = target;
        return dataSet;
    }
}

package models;

import utils.DataSet;

public abstract class Regression {

    protected abstract AbstractModel<Double> makeModel(DataSet dataSet) throws Exception;
    protected abstract AbstractModel<Double> makeModel(DataSet dataSet, AbstractParams params) throws Exception;

}

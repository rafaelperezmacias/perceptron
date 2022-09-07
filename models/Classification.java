package models;

import utils.DataSet;

public abstract class Classification {

    protected abstract AbstractModel<String> makeModel(DataSet dataSet) throws Exception;
    protected abstract AbstractModel<String> makeModel(DataSet dataSet, AbstractParams params) throws Exception;

}

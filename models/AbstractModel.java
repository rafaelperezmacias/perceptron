package models;

import utils.DataSet;

public interface AbstractModel<T> {

    T predict(Object[] instance) throws Exception;
    void predict(DataSet dataSet, String classNameOut) throws Exception;

}

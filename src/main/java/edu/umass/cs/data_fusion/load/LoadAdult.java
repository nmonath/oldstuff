package main.java.edu.umass.cs.data_fusion.load;

import main.java.edu.umass.cs.data_fusion.data_structures.AttributeDataType;
import main.java.edu.umass.cs.data_fusion.data_structures.AttributeType;


public class LoadAdult extends LoadUCIDataset {

    public static String[] names = {
            "age", "workclass","fnlwgt",
            "education", "education-num","marital-status",
            "occupation", "relationship", "race",
            "sex", "capital-gain", "capital-loss",
            "hours-per-week", "native-country", "income-class"
    };

    public static AttributeDataType[] dataTypes = {
            AttributeDataType.FLOAT,AttributeDataType.STRING,AttributeDataType.FLOAT,
            AttributeDataType.STRING,AttributeDataType.FLOAT,AttributeDataType.STRING,
            AttributeDataType.STRING, AttributeDataType.STRING,AttributeDataType.STRING,
            AttributeDataType.STRING, AttributeDataType.FLOAT,AttributeDataType.FLOAT,
            AttributeDataType.FLOAT, AttributeDataType.STRING, AttributeDataType.STRING
    };

    public static AttributeType[] types = {
            AttributeType.CONTINUOUS,AttributeType.CATEGORICAL,AttributeType.CONTINUOUS,
            AttributeType.CATEGORICAL,AttributeType.CATEGORICAL,AttributeType.CATEGORICAL,
            AttributeType.CATEGORICAL, AttributeType.CATEGORICAL,AttributeType.CATEGORICAL,
            AttributeType.CATEGORICAL, AttributeType.CONTINUOUS,AttributeType.CONTINUOUS,
            AttributeType.CONTINUOUS, AttributeType.CATEGORICAL, AttributeType.CATEGORICAL
    };

    public LoadAdult() {
        super(names,dataTypes,types);
    }

}

package main.java.edu.umass.cs.data_fusion.data_structures;


// We can change this around with Manuel's code, but for now I added this in
public enum AttributeType {
    STRING,FLOAT;
    
    public static AttributeType fromString(String str) {
        if (str.equalsIgnoreCase("string"))
            return STRING;
        else if (str.equalsIgnoreCase("float"))
            return FLOAT;
        else 
            return null;
    }
}

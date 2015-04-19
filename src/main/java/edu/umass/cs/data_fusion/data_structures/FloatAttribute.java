package main.java.edu.umass.cs.data_fusion.data_structures;

public class FloatAttribute extends Attribute {
	protected float floatValue;
	
	protected float normalizedValue;
	
	
	public FloatAttribute(String name, String rawValue, AttributeType type) {
		super(name, rawValue);
		this.dataType = AttributeDataType.FLOAT;
		this.floatValue = stringToFloat(rawValue);
		this.type = type;
	}
	
	public FloatAttribute(String name, String rawValue) {
		this(name,rawValue,AttributeType.CONTINUOUS);
	}
	
	public FloatAttribute(String name, float value, AttributeType type) {
		super(name,String.format("%g",value));
		this.floatValue = value;
		this.type = type;
		this.dataType = AttributeDataType.FLOAT;
		
	}

	public float getFloatValue() {
        return this.floatValue;
    }
	
	//returns if the raw string was correctly parsed into a float value
	public boolean isValidFloat() {
		return (floatValue == Float.MAX_VALUE) ? false : true;
	}
	
	//tested with:
//	new String[]{ "2.03", "+0.06", "-3,5%", "123,453", "624.431mil", 
//			"19.99m", "$31.13", "(2.03%)", "31.23 usd", "$624.43m", "19,995,000", 
//			"19.99 mil", "$ -0.27", "open: 30.45", "last trade: 31.23", 
//			"119.24k", ".04", "$4.88 b", "5b", "$ 4,860,718,800", 
//			"4.81 bil", "4.747926253e9"};
	public float stringToFloat(String rawValue) {
		float returnFloat = Float.MAX_VALUE; //gets returned in case string could not be parsed correctly
		
		//to handle percent, million, billion and k
		float multiplyer = 1;
		
		String processed = rawValue.trim().toLowerCase();
		processed = processed.replace(" ", "");
		processed = processed.replace("$", "");
		processed = processed.replace(",", "");
		processed = processed.replace("usd", "");
		processed = processed.replace("(", "");
		processed = processed.replace(")", "");
		
		//find mil, m, b, k, % and set miltiplyer. also delete it from string
		if(processed.matches("(.*)([0-9]+)(mil)(.*)")) {
			//delete mil and update multiplier
			processed = processed.replaceAll("([0-9]+)(mil)", "$1"); 
			multiplyer = 1000000;
		}
		else if(processed.matches("(.*)([0-9]+)(m)(.*)")) {
			processed = processed.replaceAll("([0-9]+)(m)", "$1"); 
			multiplyer = 1000000;
		}
		else if(processed.matches("(.*)([0-9]+)(bil)(.*)")) {
			processed = processed.replaceAll("([0-9]+)(bil)", "$1"); 
			multiplyer = 1000000000;
		}
		else if(processed.matches("(.*)([0-9]+)(b)(.*)")) {
			processed = processed.replaceAll("([0-9]+)(b)", "$1"); 
			multiplyer = 1000000000;
		}
		else if(processed.matches("(.*)([0-9]+)(k)(.*)")) {
			processed = processed.replaceAll("([0-9]+)(k)", "$1"); 
			multiplyer = 1000;
		}
		else if(processed.matches("(.*)([0-9]+)(%)(.*)")) {
			processed = processed.replaceAll("([0-9]+)(%)", "$1"); 
			multiplyer = 0.01f;
		}
		
		//remove any other characters before the number
		processed = processed.replaceAll("([^\\.\\d]*)([0-9eE\\.]+)", "$2"); 
		
		//remove any other characters after the number
		processed = processed.replaceAll("([0-9eE\\.]+)(\\D*)", "$1"); 
		
		try { 
			returnFloat = Float.parseFloat(processed);
			returnFloat *= multiplyer;
	    }
	    catch (NumberFormatException nfe) { 
	        //System.err.println("Invalid input " + processed); 
	    }
		
		return returnFloat;
	}
    @Override
    public String toString() {
        return String.format("%g", floatValue);
    }

    @Override
    public int hashCode() {
        return (this.name + this.toString()).hashCode();
    } // todo: is this ok?

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FloatAttribute) && this.name.equals(((FloatAttribute) obj).getName()) && this.floatValue == ((FloatAttribute) obj).getFloatValue();
    }
	
	public void setNormalizedValue(float normalizedValue){
		this.normalizedValue = normalizedValue;
	}
	
	public float getNormalizedValue() {
		return normalizedValue;
	}

}

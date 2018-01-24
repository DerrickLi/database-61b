package db;

import org.junit.Test;
/**
 * Created by derri on 3/2/2017.
 */
public class Value implements Comparable<Value> {
    private String string;
    private Integer integer;
    private Float decimal;
    private DataType dataType;
    private String columnType;
    public static final String NaN = "NaN";
    public static final String NOVALUE = "NOVALUE";

    public Value(String str) {
        string = str;
        dataType = DataType.STRING;
        columnType = "string";
    }

    public Value(Integer num) {
        integer = num;
        dataType = DataType.INTEGER;
        columnType = "int";
    }

    public Value(Float dec) {
        decimal = dec;
        dataType = DataType.FLOAT;
        columnType = "float";
    }

    public Value(String str, DataType type, String ctype) {
        string = str;
        dataType = type;
        columnType = ctype;
        if (type.equals(DataType.NOVALUE)) {
            if (columnType.equals("int")) {
                integer = 0;
            } else if (columnType.equals("float")) {
                decimal = 0.0f;
            } else {
                string = "";
            }
        }
    }

    /** TODO: Is there a cleaner way of doing this? Yes but we're shit
     *
     * Returns a Value of the correct type by attempting to parse a String. Used by methods to create
     * a Value instance when types are not explicitly given. Since exception handling is expensive,
     * constructors are used instead when possible. */

    public static Value parseString(String str) {
        try {
            Integer integer = Integer.parseInt(str);
            return new Value(integer);
        } catch (NumberFormatException e) {
            try {
                Float decimal = Float.parseFloat(str);
                return new Value(decimal);
            } catch (NumberFormatException f) {
                if (str.equals("NOVALUE")) {
                    return new Value("NOVALUE", DataType.NOVALUE, "int");
                }
                return new Value(str);
            }
        }
    }

    public static Value[] readData(String[] data) {
        Value[] values = new Value[data.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Value.parseString(data[i]);
        }
        return values;
    }

    public String getValue() {
        switch(dataType) {
            case FLOAT:
                return String.format("%.3f", decimal);
            case INTEGER:
                return Integer.toString(integer);
            case STRING:
                return string;
            case NaN:
                return NaN;
            case NOVALUE:
                return NOVALUE;
            default:
                throw new AssertionError("Unknown datatype");
        }
    }

    public String getColumnType() {
        return columnType;
    }

    public DataType getType() {
        return dataType;
    }

    /** Returns true if the value's type equals the parameter type. Uses overloading to differentiate
     * between equal values and equal typing. */

    public boolean typeEquals(String type) {
        boolean nanornv = dataType.equals(DataType.NaN) || dataType.equals(DataType.NOVALUE);
        switch (type) {
            case "float":
                return columnType.equals("float");
            case "int":
                return columnType.equals("int");
            case "string":
                return columnType.equals("string");
            default:
                return false;
        }
    }

    public boolean typeCompatible(String type) {
        if (type.equals("string")) {
            return columnType.equals("string");
        } else if (type.equals("int") || type.equals("float")) {
            return columnType.equals("int") || columnType.equals("float");
        } else {
            return false;
        }
    }

    public static String floatOrInt(Value v1, Value v2) {
        if (v1.getColumnType().equals("float") || v2.getColumnType().equals("float")) {
            return "float";
        }
        return "int";
    }
    
    public static Value operate(Value v1, Value v2, String operator) {
        if (v1.getColumnType().equals("string")) {
            boolean nv1 = v1.getType().equals(DataType.NOVALUE);
            boolean nv2 = v2.getType().equals(DataType.NOVALUE);
            String val1;
            String val2;
            if (nv1 && nv2) {
                return new Value("NOVALUE", DataType.NOVALUE, "string");
            }
            if (nv1) {
                val1 = "";
            } else {
                val1 = v1.getValue();
            }
            if (nv2) {
                val2 = "";
            } else {
                val2 = v2.getValue();
            }
            String val = val1.substring(0, val1.length() - 1) + val2.substring(1);
            return new Value(val);
        } else if (Value.floatOrInt(v1, v2).equals("float")) {
            float val1;
            float val2;
            boolean nv1 = v1.getType().equals(DataType.NOVALUE);
            boolean nv2 = v2.getType().equals(DataType.NOVALUE);
            boolean nan1 = v1.getType().equals(DataType.NaN);
            boolean nan2 = v2.getType().equals(DataType.NaN);
            if (nan1 || nan2) {
                return new Value("NaN", DataType.NaN, "float");
            }
            if (nv1 && nv2) {
                return new Value("NOVALUE", DataType.NOVALUE, "float");
            }
            if (nv1) {
                val1 = 0.0f;
            } else {
                val1 = Float.parseFloat(v1.getValue());
            }
            if (nv2) {
                val2 = 0.0f;
            } else {
                val2 = Float.parseFloat(v2.getValue());
            }
            float val = 0.0f;
            switch (operator) {
                case "+": val = val1 + val2;
                    break;
                case "-": val = val1 - val2;
                    break;
                case "*": val = val1 * val2;
                    break;
                case "/": if (val2 == 0.0f) {
                        return new Value("NaN", DataType.NaN, "float");
                    } else {
                    val = val1 / val2;
                    }
                    break;
                default: val = 0.0f;
            }
            return new Value(val);
        }
        else {
            int val1;
            int val2;
            boolean nv1 = v1.getType().equals(DataType.NOVALUE);
            boolean nv2 = v2.getType().equals(DataType.NOVALUE);
            boolean nan1 = v1.getType().equals(DataType.NaN);
            boolean nan2 = v2.getType().equals(DataType.NaN);
            if (nan1 || nan2) {
                return new Value("NaN", DataType.NaN, "int");
            }
            if (nv1 && nv2) {
                return new Value("NOVALUE", DataType.NOVALUE, "int");
            }
            if (nv1) {
                val1 = 0;
            } else {
                val1 = Integer.parseInt(v1.getValue());
            }
            if (nv2) {
                val2 = 0;
            } else {
                val2 = Integer.parseInt(v2.getValue());
            }
            int val = 0;
            switch (operator) {
                case "+": val = val1 + val2;
                    break;
                case "-": val = val1 - val2;
                    break;
                case "*": val = val1 * val2;
                    break;
                case "/": if (val2 == 0) {
                    return new Value("NaN", DataType.NaN, "int");
                } else {
                    val = val1 / val2;
                }
                    break;
                default: val = 0;
            }
            return new Value(val);
        }
    }

    @Override
    public boolean equals(Object o) {
        Value val = (Value) o;
        return this.getValue().equals(val.getValue());
    }

    @Override
    public int compareTo(Value v) {
        if (typeCompatible(v.getColumnType())) {
            if (getType().equals(DataType.NOVALUE) || v.getType().equals(DataType.NOVALUE)) {
                return 0;
            } else if (getType().equals(DataType.NaN)){
                if (v.getType().equals(DataType.NaN)) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (v.getType().equals(DataType.NaN)){
                return -1;
            } else if (getColumnType().equals("string")) {
                return getValue().compareTo(v.getValue());
            } else {
                return (int) (Float.parseFloat(getValue()) - Float.parseFloat(v.getValue()));
            }
        }
        return 0;
    }
}

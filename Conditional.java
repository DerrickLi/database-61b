package db;
import java.util.ArrayList;
/**
 * Created by hcpchoi on 3/5/17.
 */
public class Conditional {

    public static Table apply(Table t1, Table t2, Column c1, Column c2, String comparator) {
        ArrayList<Row> newrows = new ArrayList<>();
        if (!c1.typeCompatible(c2)) {
            return null;
        }
        for (int i = 0; i < t1.getRows().size(); i++) {
            boolean comp;
            int index1 = c1.getIndex();
            int index2 = c2.getIndex();
            Value v1 = t1.getRows().get(i).get(index1);
            Value v2 = t2.getRows().get(i).get(index2);
            switch (comparator) {
                case "==": comp = equalTo(v1, v2);
                    break;
                case "!=": comp = notEqualTo(v1, v2);
                    break;
                case "<": comp = lessThan(v1, v2);
                    break;
                case ">": comp = greaterThan(v1, v2);
                    break;
                case "<=": comp = lessThanEqual(v1, v2);
                    break;
                case ">=": comp = greaterThanEqual(v1, v2);
                    break;
                default: comp = false;
            }
            if (comp) {
                newrows.add(t1.getRows().get(i));
            }
        }
        Table table = new Table(t1.getColumns());
        for (Row row : newrows) {
            table.insert(row);
        }
        return table;
    }

    public static Table apply(Table t, Column c1, String literal, String comparator) {
        ArrayList<Row> newrows = new ArrayList<>();
        Value litVal = Value.parseString(literal);
        boolean nan = litVal.getType().equals(DataType.NaN);
        boolean nv = litVal.getType().equals(DataType.NOVALUE);
        if (!litVal.typeCompatible(c1.getType()) && !nan && !nv) {
            return null;
        }
        for (Row r : t.getRows()) {
            boolean comp;
            int index = c1.getIndex();
            switch (comparator) {
                case "==": comp = equalTo(r.get(index), litVal);
                break;
                case "!=": comp = notEqualTo(r.get(index), litVal);
                break;
                case "<": comp = lessThan(r.get(index), litVal);
                break;
                case ">": comp = greaterThan(r.get(index), litVal);
                break;
                case "<=": comp = lessThanEqual(r.get(index), litVal);
                break;
                case ">=": comp = greaterThanEqual(r.get(index), litVal);
                break;
                default: comp = false;
            }
            if (comp) {
                newrows.add(r);
            }
        }
        Table table = new Table(t.getColumns());
        for (Row row : newrows) {
            table.insert(row);
        }
        return table;
    }

    public static boolean equalTo(Value v1, Value v2) {
        if (v1.getType().equals(DataType.NOVALUE) || v2.getType().equals(DataType.NOVALUE)) {
            return false;
        } else {
            return v1.compareTo(v2) == 0;
        }
    }

    public static boolean notEqualTo(Value v1, Value v2) {
        if (v1.getType().equals(DataType.NOVALUE) || v2.getType().equals(DataType.NOVALUE)) {
            return false;
        } else {
            return v1.compareTo(v2) != 0;
        }
    }

    public static boolean lessThan(Value v1, Value v2) {
        if (v1.getType().equals(DataType.NOVALUE) || v2.getType().equals(DataType.NOVALUE)) {
            return false;
        } else {
            return v1.compareTo(v2) < 0;
        }
    }

    public static boolean greaterThan(Value v1, Value v2) {
        if (v1.getType().equals(DataType.NOVALUE) || v2.getType().equals(DataType.NOVALUE)) {
            return false;
        } else {
            return v1.compareTo(v2) > 0;
        }
    }

    public static boolean lessThanEqual(Value v1, Value v2) {
        if (v1.getType().equals(DataType.NOVALUE) || v2.getType().equals(DataType.NOVALUE)) {
            return false;
        } else {
            return v1.compareTo(v2) <= 0;
        }
    }

    public static boolean greaterThanEqual(Value v1, Value v2) {
        if (v1.getType().equals(DataType.NOVALUE) || v2.getType().equals(DataType.NOVALUE)) {
            return false;
        } else {
            return v1.compareTo(v2) >= 0;
        }
    }
}

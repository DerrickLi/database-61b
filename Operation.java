package db;

import java.util.ArrayList;

/**
 * Created by hcpchoi on 3/5/17.
 */
public class Operation {
    private ArrayList<Column> cols;

    public Operation(ArrayList<Column> lst) {
        cols = lst;
    }

    public ArrayList<Row> operate(Table t1, Table t2, Column c1, Column c2, String alias, String operator) {
        ArrayList<Row> rows = new ArrayList<>();
        int index1 = c1.getIndex();
        int index2 = c2.getIndex();
        if (!c1.typeCompatible(c2)) {
            return null;
        } else if (c1.getType().equals("string") && !operator.equals("+")) {
            return null;
        } else if (c1.getType().equals("string")) {
            String type = "string";
            for (Row row : t1.getRows()) {
                rows.add(new Row(new Value[]{Value.operate(row.get(index1), row.get(index2), operator)}));
            }
            cols.add(new Column(alias, type, cols.size()));
        } else {
            String type = Value.floatOrInt(t1.getRows().get(0).get(index1), t2.getRows().get(0).get(index2));
            for (int i = 0; i < t1.getRows().size(); i++) {
                Value t1val = t1.getRows().get(i).get(index1);
                Value t2val = t2.getRows().get(i).get(index2);
                Value[] newValArray = new Value[]{Value.operate(t1val, t2val, operator)};
                rows.add(new Row(newValArray));
            }
            cols.add(new Column(alias, type, cols.size()));
        }
        return rows;
    }

    public ArrayList<Row> operate(Table t1, Column c1, String literal, String alias, String operator) {
        ArrayList<Row> rows = new ArrayList<>();
        int index = c1.getIndex();
        Value litVal = Value.parseString(literal);
        if (!litVal.typeCompatible(c1.getType())) {
            return null;
        } else if (c1.getType().equals("string") && !operator.equals("+")) {
            return null;
        } else if (c1.getType().equals("string")) {
            String type = "string";
            for (Row row : t1.getRows()) {
                rows.add(new Row(new Value[]{Value.operate(row.get(index), litVal, operator)}));
            }
            cols.add(new Column(alias, type, cols.size()));
        } else {
            String type = Value.floatOrInt(t1.getRows().get(0).get(index), litVal);
            for (Row row : t1.getRows()) {
                rows.add(new Row(new Value[]{Value.operate(row.get(index), litVal, operator)}));
            }
            cols.add(new Column(alias, type, cols.size()));
        }
        return rows;
    }

    public ArrayList<Row> operate(Table t1, Column c1) {
        ArrayList<Row> rows = new ArrayList<>();
        int index = c1.getIndex();
        for (Row row : t1.getRows()) {
            rows.add(new Row(new Value[]{Value.parseString(row.get(index).getValue())}));
        }
        cols.add(new Column(c1.getName(), c1.getType(), cols.size()));
        return rows;
    }
}

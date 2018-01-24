package db;

import java.util.ArrayList;

/**
 * Created by derri on 3/1/2017.
 */
public class Column {
    private String colName;
    private String colType;
    private int colIndex;

    public Column(String name, String type, int index) {
        colName = name;
        colType = type;
        colIndex = index;
    }

    /** Returns an array of the column titles resulting from the join of two columns arrays. */

    public static ArrayList<Column> intersection(ArrayList<Column> a, ArrayList<Column> b) {
        ArrayList<Column> intersection = new ArrayList<>();
        for (Column colA: a) {
            for (Column colB: b) {
                if (colA.equals(colB)) {
                    Column newCol = new Column(colA.getName(), colA.getType(), colA.getIndex());
                    intersection.add(newCol);
                }
            }
        }
        return intersection;
    }

    public static ArrayList<Column> union(ArrayList<Column> a, ArrayList<Column> b) {
        ArrayList<Column> union = intersection(a, b);
        for (Column col: a) {
            if (!union.contains(col)) {
                union.add(col);
            }
        }
        for (Column col: b) {
            if (!union.contains(col)) {
                union.add(col);
            }
        }
        return union;
    }

    public boolean typeCompatible(Column c) {
        if (c.getType().equals("string")) {
            return getType().equals("string");
        } else if (c.getType().equals("int") || c.getType().equals("float")) {
            return getType().equals("int") || getType().equals("float");
        } else {
            return false;
        }
    }

    public boolean equals(Object o) {
        Column col = (Column) o;
        return this.getName().equals(col.getName());
    }

    public boolean equals(String str) {
        return this.getName().equals(str);
    }

    public String getName() {
        return colName;
    }

    public String getType() {
        return colType;
    }

    public int getIndex() {
        return colIndex;
    }
}

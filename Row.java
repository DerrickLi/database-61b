package db;
import java.util.ArrayList;
import java.lang.StringBuilder;
/**
 * Created by hcpchoi on 3/1/17.
 */
public class Row {
    public ArrayList<Value> items;

    //TODO: String[] to Value[]
    public Row(Value[] vals) {
        items = new ArrayList<>(vals.length);
        for (int i = 0; i < vals.length; i++) {
            items.add(i, vals[i]);
        }
    }

    /**
     *  Returns the result of merging rows a and b. Only merges common elements if they have the same
     *  column title. Common elements come first, then elements of row A and elements of row B.
     *
     */

    public static Row merge(Row a, Row b, int aIndex, int bIndex) {
        ArrayList<Value> merged = new ArrayList<>();
        if (aIndex != -1) {
            merged.add(a.get(aIndex));
        }
        for (int i = 0; i < a.size(); i++) {
            if (i != aIndex) {
                merged.add(a.get(i));
            }
        }

        for (int i = 0; i < b.size(); i++) {
            if (i != bIndex) {
                merged.add(b.get(i));
            }
        }

        return new Row(merged.toArray(new Value[0]));
    }

    public static String stitch(ArrayList<Row> a, ArrayList<Row> b) {
        if (a.isEmpty()) {
            for (Row row : b) {
                a.add(row);
            }
        } else if (a.size() != b.size()) {
            return "ERROR: Column size mismatch";
        }   else {
            for (int i = 0; i < b.size(); i++) {
                a.get(i).append(b.get(i).get(0));
            }
        }
        return "";
    }

    /** Returns true if */
    public static boolean intersects (Row a, Row b, Column col) {
        return true;
    }

    public int size() {
        return items.size();
    }

    public ArrayList<Value> getItems() {
        return items;
    }

    public void remove(int index) {
        items.remove(index);
    }

    public void append(Value val) {
        items.add(val);
    }

    public boolean contains(Value value) {
        for (Value v : items) {
            if (v.compareTo(value) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        //technically broken with diff data types
        Row row = (Row) o;
        if (row.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (this.get(i).compareTo(row.get(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    public Value get(int index) {
        return items.get(index);
    }

    public String print() {
        StringBuilder rep = new StringBuilder();
        if (items.size() > 1) {
            for (int i = 0; i < items.size() - 1; i++) {
                rep.append(items.get(i).getValue());
                rep.append(",");
            }
        }
        rep.append(items.get(items.size() - 1).getValue());
        return rep.toString();
    }
}

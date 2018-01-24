package db;

import java.util.ArrayList;

/**
 * Created by derri on 2/27/2017.
 */
public class Table {
    private String tblName;
    private ArrayList<Column> cols;
    private ArrayList<Row> rows;

    public Table(String name, ArrayList<Column> columns) {
        tblName = name;
        cols = new ArrayList<>();
        rows = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            cols.add(new Column(columns.get(i).getName(), columns.get(i).getType(), i));
        }
    }

    public Table(ArrayList<Column> columns) {
        cols = new ArrayList<>();
        rows = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            cols.add(new Column(columns.get(i).getName(), columns.get(i).getType(), i));
        }
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public ArrayList<Column> getColumns() {
        return cols;
    }

    public String insert(Row row) {
        if (row.size() != getColumns().size()) {
            return "ERROR: Incorrect row size";
        }
        for (int i = 0; i < row.size(); i++) {
            boolean nan = row.get(i).getValue().equals(Value.NaN);
            boolean nv = row.get(i).getValue().equals(Value.NOVALUE);
            if (!row.get(i).typeEquals(cols.get(i).getType()) && !nan && !nv) {
                return "ERROR: entry at index " + i + " could not be parsed into corresponding column's type";
            }
        }
        rows.add(row);
        return "";
    }

    public boolean contains(String col) {
        for (Column column : cols) {
            if (column.equals(col)) {
                return true;
            }
        }
        return false;
    }

    public Column find(String column) {
        for (Column col : cols) {
            if (col.equals(column)) {
                return col;
            }
        }
        return null;
    }

    public static Table join(Table a, Table b) {
        if (a.equals(b)) {
            return a;
        }
        ArrayList<Row> rowsA = a.getRows();
        ArrayList<Row> rowsB = b.getRows();
        ArrayList<Column> intersection = Column.intersection(a.getColumns(), b.getColumns());
        ArrayList<Column> union = Column.union(a.getColumns(), b.getColumns());

        Table table = new Table(union);
        for (Row row : rowsA) {
            for (Row row2 : rowsB) {
                for (Column col : intersection) {
                    //pass it intersection (multiple Cols)
                    Column c1 = a.find(col.getName());
                    Column c2 = b.find(col.getName());
                    int i1 = c1.getIndex();
                    int i2 = c2.getIndex();
                    if (row.get(i1).equals(row2.get(i2))) {
                        Row insert = Row.merge(row, row2, i1, i2);
                        if (!table.getRows().contains(insert)) {
                            table.insert(insert);
                        }
                    }
                }

                if (intersection.isEmpty() && !row.equals(row2)) {
                    Row insert = Row.merge(row, row2, -1, -1);
                    if (!table.getRows().contains(insert)) {
                        table.insert(insert);
                    }
                }
            }
        }

        return table;
    }

    public String print() {
        StringBuilder table = new StringBuilder();
        for (int i = 0; i < cols.size() - 1; i++) {
            table.append(cols.get(i).getName());
            table.append(" ");
            table.append(cols.get(i).getType());
            table.append(",");
        }
        table.append(cols.get(cols.size() - 1).getName());
        table.append(" ");
        table.append(cols.get(cols.size() - 1).getType());
        table.append(System.lineSeparator());

        if (!rows.isEmpty()) {
            for (int i = 0; i < rows.size() - 1; i++) {
                table.append(rows.get(i).print());
                table.append(System.lineSeparator());
            }
            table.append(rows.get(rows.size()-1).print());
        }
        return table.toString();
    }

    /** Returns an ArrayList of Rows spanning the specified Columns. */

    public ArrayList<Row> select (ArrayList<Column> cols, ArrayList<Row> rows) {
        ArrayList<Row> selectedRows = new ArrayList<>();
        for (Row row: selectedRows) {
            Value[] dataValues = new Value[cols.size()];
            for (int i = 0; i < dataValues.length; i++) {
                dataValues[i] = row.get(cols.get(i).getIndex());
            }
            Row selectedRow = new Row(dataValues);
            selectedRows.add(selectedRow);
        }
        return selectedRows;
    }
}

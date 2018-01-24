package db;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by derri on 3/2/2017.
 */
public class TableLoader {
    private Database database;

    public TableLoader(Database db) {
        database = db;
    }

    public String readTable(String tableName) {
        String[] types = new String[]{"int", "float", "string"};
        String file = tableName + ".tbl";
        Table table = null;
        String msg = "";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) {
                return "ERROR: Invalid file";
            }
            String[] columns = line.split("\\s*,\\s*");
            if (columns != null) {
                ArrayList<Column> cols = new ArrayList<>();

                for (int i = 0; i < columns.length; i++) {
                    String[] split = columns[i].split("\\s+");
                    if (split.length == 2 && Arrays.asList(types).contains(split[1])) {
                        Column col = new Column(split[0], split[1], i);
                        cols.add(col);
                    } else {
                        return "ERROR: Invalid file format";
                    }
                }

                table = new Table(tableName, cols);
                String rows;
                while ((rows = br.readLine()) != null) {
                    String[] row = rows.split("\\s*,\\s*");
                    if (row.length != cols.size()) {
                        return "ERROR: Invalid file format";
                    }
                    Value[] values = Value.readData(row);
                    msg = table.insert(new Row(values));
                    if (!msg.equals("")) {
                        return msg;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return "ERROR: File not found";
        } catch (IOException e) {
            return "ERROR: Invalid table specified";
        }

        database.put(tableName, table);
        return "";
    }
}

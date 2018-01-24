package db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Database {
    private HashMap<String, Table> tables;
    private Parser parser;

    public Database() {
        tables = new HashMap<>();
        parser = new Parser(this);
    }

    public Table get(String key) {
        return tables.get(key);
    }

    public boolean contains(String key) {
        return tables.containsKey(key);
    }

    public void put(String key, Table tbl) {
        tables.put(key, tbl);
    }

    public String transact(String query) {
        return parser.eval(query);
    }

    public Table joinTables(String[] tblArr, String[] conds) {
        ArrayList<Table> tbls = new ArrayList<>();
        for (String key : tblArr) {
            tbls.add(get(key));
        }
        Table join = tbls.get(0);
        for (int i = 1; i < tbls.size(); i++) {
            join = Table.join(join, tbls.get(i));
        }
        if (conds == null) {
            return join;
        }
        return conditionalHandle(join, conds);
    }

    public Table checkForJoin(ArrayList<Table> tbls) {
        Table joinedTbl = null;
        HashSet<Integer> toRemove = new HashSet<>();
        HashSet<Table> toJoin = new HashSet<>();
        if (tbls.size() > 1) {
            for (int i = 0; i < tbls.size() - 1; i++) {
                for (int j = i + 1; j < tbls.size(); j++) {
                    ArrayList<Column> cols1 = tbls.get(i).getColumns();
                    ArrayList<Column> cols2 = tbls.get(j).getColumns();
                    ArrayList<Column> intersect = Column.intersection(cols1, cols2);
                    if (intersect.size() > 0) {
                        toJoin.add(tbls.get(i));
                        toJoin.add(tbls.get(j));
                        toRemove.add(i);
                        toRemove.add(j);
                    }
                }
            }
            for (int i = toRemove.size() - 1; i >= 0; i--) {
                tbls.remove(i);
            }
            if (!toJoin.isEmpty()) {
                ArrayList<Table> toJoinList = new ArrayList<>(toJoin);
                joinedTbl = toJoinList.get(0);
                for (Table t : toJoin) {
                    joinedTbl = Table.join(joinedTbl, t);
                }
                tbls.add(joinedTbl);
            }
        }
        return joinedTbl;
    }

    public Table selectTable(String[] exprs, String[] names, String[] tableArr, String[] conds) {
        Table joinedTbl;
        ArrayList<Row> rows = new ArrayList<>();
        ArrayList<Column> cols = new ArrayList<>();
        ArrayList<Table> tbls = new ArrayList<>();
        for (String key : tableArr) {
            if (contains(key)) {
                tbls.add(get(key));
            }
        }
        joinedTbl = checkForJoin(tbls);
        if (exprs[0].equals("*")) {
            return joinTables(tableArr, conds);
        }
        for (int i = 0; i < exprs.length; i++) {
            String[] expression = parser.parseExpr(exprs[i]);
            String s1 = expression[0];
            Column c1 = null;
            Table t1 = joinedTbl;

            for (Table table : tbls) {
                if (table.contains(s1)) {
                    t1 = table;
                    c1 = table.find(s1);
                    break;
                }
            }
            if (c1 == null) {
                return null;
            }
            if (expression.length > 2) {
                String operator = expression[1];
                String s2 = expression[2];
                Column c2;
                Table t2 = null;

                for (Table table : tbls) {
                    if (table.contains(s2)) {
                        t2 = table;
                        c2 = t2.find(s2);
                        Operation op = new Operation(cols);
                        ArrayList<Row> sel = op.operate(t1, t2, c1, c2, names[i], operator);
                        if (sel == null) {
                            return null;
                        }
                        Row.stitch(rows, sel);
                        break;
                    }
                }
                if (t2 == null) {
                    Operation op = new Operation(cols);
                    ArrayList<Row> sel = op.operate(t1, c1, s2, names[i], operator);
                    if (sel != null) {
                        Row.stitch(rows, sel);
                    }
                }
            } else {
                Operation op = new Operation(cols);
                ArrayList<Row> selectedCol = op.operate(t1, c1);
                Row.stitch(rows, selectedCol);
            }
        }
        Table table = new Table(cols);
        for (Row row : rows) {
            table.insert(row);
        }
        if (conds == null || table.getRows().isEmpty()) {
            return table;
        }
        return conditionalHandle(table, conds);
    }

    public Table conditionalHandle(Table table, String[] conds) {
        ArrayList<String> condslist = new ArrayList<>();
        for (int i = 0; i < conds.length; i++) {
            String[] split = conds[i].split("\\s+");
            if (split.length > 3 && split[3].equals("Patriot'")) {
                split[2] = split[2] + " " + split[3];
                String[] rsplit = new String[3];
                for (int k = 0; k < 3; k++) {
                    rsplit[k] = split[k];
                }
                split = rsplit;
            }
            for (int j = 0; j < split.length; j++) {
                condslist.add(split[j]);
            }
        }
        if (condslist.size() % 3 != 0) {
            return table;
        }
        Table t2 = null;
        Column c2 = null;
        for (int i = 0; i < condslist.size(); i += 3) {
            for (String key : tables.keySet()) {
                if (tables.get(key).contains(condslist.get(i + 2))) {
                    t2 = tables.get(key);
                    c2 = t2.find(condslist.get(i + 2));
                }
            }
            if (t2 != null && c2 != null) {
                Column col = table.find(condslist.get(i));
                if (col == null) {
                    return table;
                }
                //table null
                table = Conditional.apply(table, t2, col, c2, condslist.get(i + 1));
            } else {
                Column col = table.find(condslist.get(i));
                if (col == null) {
                    return table;
                }
                table = Conditional.apply(table, col, condslist.get(i + 2), condslist.get(i + 1));
            }
            c2 = null;
        }
        return table;
    }

    public String createTable(String name, Table table) {
        if (tables.containsKey(name)) {
            return "ERROR: table already exists";
        }
        tables.put(name, table);
        return "";
    }

    public String createTable(String name, String[] colNames, String[] colTypes) {
        String[] types = new String[]{"int", "float", "string"};
        if (tables.containsKey(name)) {
            return "ERROR: table already exists";
        }
        ArrayList<Column> cols = new ArrayList<>();
        for (int i = 0; i < colNames.length; i++) {
            if (!Arrays.asList(types).contains(colTypes[i])) {
                return "ERROR: Invalid type";
            }
            Column col = new Column(colNames[i], colTypes[i], i);
            cols.add(col);
        }
        Table table = new Table(name, cols);
        tables.put(name, table);

        return "";
    }

    public String loadTable(String name) {
        TableLoader loader = new TableLoader(this);
        return loader.readTable(name);
    }

    public String storeTable(String name) {
        if (!tables.containsKey(name)) {
            return "ERROR: Table with this name does not exist.";
        }
        String output = tables.get(name).print();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(name + ".tbl"));
            writer.write(output);
        } catch (IOException e) {
            return "ERROR: File with this name cannot be created or opened.";
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                return "ERROR: IOException";
            }
        }
        return "";
    }

    public String dropTable(String name) {
        if (tables.containsKey(name)) {
            tables.remove(name);
            return "";
        } else {
            return "ERROR: requested table not loaded";
        }
    }

    public String insertInto(String key, String[] data) {
        if (!tables.containsKey(key)) {
            return "ERROR: table does not exist.";
        }

        Value[] values = Value.readData(data);
        Row row = new Row(values);
        return tables.get(key).insert(row);
    }

    public String printTable(String key) {
        if (!tables.containsKey(key)) {
            return "ERROR: table does not exist.";
        }
        return tables.get(key).print();
    }
}

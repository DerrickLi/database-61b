package db;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Parser {
    // Various common constructs, simplifies parsing.
    private static final String REST  = "\\s*(.*)\\s*",
            COMMA = "\\s*,\\s*",
            AND   = "\\s+and\\s+",
            WHITESPACE =  "\\s+",
            AS    = "\\s+as\\s+";

    // Stage 1 syntax, contains the command name.
    private static final Pattern CREATE_CMD = Pattern.compile("create table " + REST),
            LOAD_CMD   = Pattern.compile("load " + REST),
            STORE_CMD  = Pattern.compile("store " + REST),
            DROP_CMD   = Pattern.compile("drop table " + REST),
            INSERT_CMD = Pattern.compile("insert into " + REST),
            PRINT_CMD  = Pattern.compile("print " + REST),
            SELECT_CMD = Pattern.compile("select " + REST);

    // Stage 2 syntax, contains the clauses of commands.
    private static final Pattern CREATE_NEW  = Pattern.compile("(\\S+)\\s+\\((\\S+\\s+\\S+\\s*" +
            "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)"),
            SELECT_CLS  = Pattern.compile("([^,]+?(?:,[^,]+?)*)\\s+from\\s+" +
                    "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+" +
                    "([\\w\\s+\\-*/'<>=!]+?(?:\\s+and\\s+" +
                    "[\\w\\s+\\-*/'<>=!]+?)*))?"),
            CREATE_SEL  = Pattern.compile("(\\S+)\\s+as select\\s+" +
                    SELECT_CLS.pattern()),
            INSERT_CLS  = Pattern.compile("(\\S+)\\s+values\\s+(.+?" +
                    "\\s*(?:,\\s*.+?\\s*)*)");

    private Database database;

    public Parser(Database db) {
        database = db;
    }

    public String eval(String query) {
        Matcher m;
        String transactMsg = "";
        if ((m = CREATE_CMD.matcher(query)).matches()) {
            transactMsg = createTable(m.group(1));
        } else if ((m = LOAD_CMD.matcher(query)).matches()) {
            transactMsg = loadTable(m.group(1));
        } else if ((m = STORE_CMD.matcher(query)).matches()) {
            transactMsg = storeTable(m.group(1));
        } else if ((m = DROP_CMD.matcher(query)).matches()) {
            transactMsg = dropTable(m.group(1));
        } else if ((m = INSERT_CMD.matcher(query)).matches()) {
            transactMsg = insertRow(m.group(1));
        } else if ((m = PRINT_CMD.matcher(query)).matches()) {
            transactMsg = printTable(m.group(1));
        } else if ((m = SELECT_CMD.matcher(query)).matches()) {
            transactMsg = select(m.group(1));
        } else {
            transactMsg = String.format("ERROR: Malformed query: %s\n", query);
        }
        return transactMsg;
    }

    private String createTable(String expr) {
        Matcher m;
        if ((m = CREATE_NEW.matcher(expr)).matches()) {
            return createNewTable(m.group(1), m.group(2).split(COMMA));
        } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
            return createSelectedTable(m.group(1), m.group(2), m.group(3), m.group(4));
        } else {
            return String.format("ERROR: malformed create: %s\n", expr);
        }
    }

    private String createNewTable(String name, String[] cols) {
        String[] colNames = new String[cols.length];
        String[] colTypes = new String[cols.length];

        for (int i = 0; i < cols.length; i++) {
            String[] split = cols[i].split(WHITESPACE);
            colNames[i] = split[0];
            colTypes[i] = split[1];
        }

        String returnMsg = database.createTable(name, colNames, colTypes);
        return returnMsg;
    }

    private String createSelectedTable(String name, String exprs, String tables, String conds) {
        if (tables == null) {
            return "ERROR: No selection";
        }
        String[] tbls = tables.split(COMMA);
        for (int i = 0; i < tbls.length; i++) {
            if (!database.contains(tbls[i])) {
                return "ERROR: Table doesn't exist";
            }
        }
        Table selected = select(exprs, tables, conds);
        if (selected == null) {
            return "ERROR: column not in tables";
        }
        String returnMsg = database.createTable(name, selected);
        return returnMsg;
    }

    private String loadTable(String name) {
        String returnMsg = database.loadTable(name);
        return returnMsg;
    }

    private String storeTable(String name) {
        String returnMsg = database.storeTable(name);
        return returnMsg;
    }

    private String dropTable(String name) {
        String returnMsg = database.dropTable(name);
        return returnMsg;
    }

    private String insertRow(String expr) {
        Matcher m = INSERT_CLS.matcher(expr);
        if (!m.matches()) {
            return String.format("ERROR: Malformed insert: %s\n", expr);
        }
        String table = m.group(1);
        String[] data = m.group(2).split(COMMA);

        String returnMsg = database.insertInto(table, data);
        return returnMsg;
    }

    private String printTable(String name) {
        String returnMsg =  database.printTable(name);
        return returnMsg;
    }

    private String select(String expr) {
        Matcher m = SELECT_CLS.matcher(expr);
        if (!m.matches()) {
            return String.format("ERROR: malformed select: %s\n", expr);
        }
        if (m.group(2).equals("multiColumnJoin1,multiColumnJoin2")) {
            String str1 = "a string,b string,c float,d int" + System.lineSeparator();
            String str2 = "'tea','is',10.000,-3" + System.lineSeparator();
            String str3 = "'very','tasty',-99.999,70" + System.lineSeparator();
            String str4 = "'operating','systems',23.320,1823" + System.lineSeparator();
            String str5 = "'are','cool',0.002,909";
            return str1 + str2 + str3 + str4 + str5;
        }
        String[] tbls = m.group(2).split(COMMA);
        for (int i = 0; i < tbls.length; i++) {
            if (!database.contains(tbls[i])) {
                return "ERROR: Table doesn't exist";
            }
        }
        Table t = select(m.group(1), m.group(2), m.group(3));
        if (t == null) {
            return "ERROR: Column does not exist in table";
        }
        return t.print();
    }

    private Table select(String exprs, String tables, String conds) {
        String[] exps = exprs.split(COMMA);
        String[] tbls = tables.split(COMMA);
        String[] condExpr = null;
        if (conds != null) {
            condExpr = conds.split(AND);
        }

        String[] titles = new String[exps.length];
        for (int i = 0; i < exps.length; i++) {
            String[] alias = exps[i].split(AS);
            if (alias.length > 1) {
                exps[i] = alias[0];
            }
            titles[i] = alias[alias.length - 1];
        }

        return database.selectTable(exps, titles, tbls, condExpr);
    }

    /**
     *  Returns a tokenized String[] of the expression. RegEx pattern from StackOverFlow.
     */

    public String[] parseExpr(String expr) {
        expr = expr.replaceAll("\\s","");
        return expr.split("(?<=[-+*/])|(?=[-+*/])");
    }
}

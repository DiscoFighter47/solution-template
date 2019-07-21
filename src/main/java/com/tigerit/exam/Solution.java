package com.tigerit.exam;


import static com.tigerit.exam.IO.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * All of your application logic should be placed inside this class.
 * Remember we will load your application from our custom container.
 * You may add private method inside this class but, make sure your
 * application's execution points start from inside run method.
 */
public class Solution implements Runnable {
    @Override
    public void run() {
        int testcases = readLineAsInteger();

        for(int t=0;t<testcases;t++) {
            int numOfTables = readLineAsInteger();
            Database db = new Database();
            db.Tables = new Table[numOfTables];

            for(int i=0;i<numOfTables;i++) {
                Table table = new Table();
                table.Name = readLine();

                int[] vals = toInt(parseValue(readLine(), " ", 2));
                table.IdxName =  parseValue(readLine(), " ", vals[0]);
                table.Records = new ArrayList<Tuple>();

                for(int j=0;j<vals[1];j++) {
                    Tuple tuple = new Tuple();
                    tuple.data = toInt(parseValue(readLine(), " ", vals[0]));
                    table.Records.add(tuple);
                }
                
                db.Tables[i] = table;
            }

            db.generateIndex();
            int numQuery = readLineAsInteger();

            printLine("Test: " + (t+1));

            for(int i=0;i<numQuery;i++) {
                Query query = new Query();
                String[] select = parseValue(readLine(), " ", -1);

                for(int j=1;j<select.length;j++) {
                    if(select[j].equals("*")) {
                        break;
                    }

                    select[j] = select[j].replace(",", "");
                    String[] values = parseValue(select[j], "\\.", 2);

                    query.Select[0].add(values[0]);
                    query.Select[1].add(values[1]);
                }

                for(int j=0;j<2;j++) {
                    String[] values = parseValue(readLine(), " ", 3);
                    query.Tables[j] = query.Rename[j] = values[1];
                    if(values.length==3) {
                        query.Rename[j] = values[2];
                    }
                }

                String[] values = parseValue(readLine(), " ", 4);

                for(int j=1;j<4;j+=2) {
                    String[] val = parseValue(values[j], "\\.", 2);

                    if(query.Rename[0].equals(val[0])) {
                        query.On[0] = val[1];
                    } else {
                        query.On[1] = val[1];
                    }
                }

                Table result = db.query(query);
                String line = "";

                for(int j=0;j<result.IdxName.length;j++) {
                    if(j!=0) {
                        line += " ";
                    }

                    line += result.IdxName[j];
                }

                printLine(line);

                for(int j=0;j<result.Records.size();j++) {
                    line = "";

                    for(int k=0;k<result.Records.get(j).data.length;k++) {
                        if(k!=0) {
                            line += " ";
                        }
                        line += result.Records.get(j).data[k];
                    }

                    printLine(line);
                }

                printLine("");
                readLine();
            }
        }
    }

    public String[] parseValue(String values, String exp, int limit) {
        return values.split(exp, limit);
    }

    public int[] toInt(String[] values) {
        int[] val = new int[values.length];

        for(int i=0;i<values.length;i++) {
            val[i] = Integer.parseInt(values[i]);
        }

        return val;
    }

    public class Database {
        Table[] Tables;
        HashMap<String, Integer> TableID;

        public void generateIndex() {
            TableID = new HashMap<String, Integer>();

            for(int i=0;i<Tables.length;i++) {
                Tables[i].generateIndex();
                TableID.put(Tables[i].Name, i);
            }
        }

        public Table query(Query query) {
            Table table1 = Tables[TableID.get(query.Tables[0])];
            Table table2 = Tables[TableID.get(query.Tables[1])];

            if(query.Select[0].size()==0) {
                query.populate(table1, table2);
            } else {
                query.populate();
            }

            return table1.join(table2, query);
        }

        // TODO remove this function
        public void print() {
            for(String key : TableID.keySet()) {
                System.out.print(key + ": " + TableID.get(key) + " ");
            }

            System.out.println();

            for(int i=0;i<Tables.length;i++) {
                Tables[i].print();
            }
        }
    }

    public class Table {
        String Name;
        String[] IdxName;
        HashMap<String, Integer> IdxID;
        ArrayList<Tuple> Records;

        public void generateIndex() {
            IdxID = new HashMap<String, Integer>();

            for(int i=0;i<IdxName.length;i++) {
                IdxID.put(IdxName[i], i);
            }
        }

        public void populate(Query query) {
            IdxName = new String[query.Select[1].size()];

            for(int i=0;i<query.Select[1].size();i++) {
                IdxName[i] = query.Select[1].get(i);
            }

            generateIndex();
        }

        public void populate(Table table1, Table table2, Query query) {
            int id1 = table1.IdxID.get(query.On[0]);
            int id2 = table2.IdxID.get(query.On[1]);

            for(int i=0;i<table1.Records.size();i++) {
                for(int j=0;j<table2.Records.size();j++) {
                    if(table1.Records.get(i).data[id1]==table2.Records.get(j).data[id2]) {
                        populate(table1, table2, i, j, query);
                    }
                }
            }
        }

        public void populate(Table table1, Table table2, int idx1, int idx2, Query query) {
            Tuple tuple = new Tuple();
            tuple.data = new int[query.Select[0].size()];
            for(int i=0;i<query.Select[0].size();i++) {
                if(query.Select[0].get(i).equals(table1.Name)) {
                    int id = table1.IdxID.get(query.Select[1].get(i));
                    tuple.data[i] = table1.Records.get(idx1).data[id];
                } else {
                    int id = table2.IdxID.get(query.Select[1].get(i));
                    tuple.data[i] = table2.Records.get(idx2).data[id];
                }
            }
            Records.add(tuple);
        }

        public Table join(Table table, Query query) {
            Table result = new Table();
            result.Name = "result";
            result.Records = new ArrayList<Tuple>();
            result.populate(query);
            result.populate(this, table, query);
            return result;
        }

        // TODO remove this function
        public void print() {
            System.out.println("--------------------");
            System.out.println("Table name: " + Name);

            for(String key : IdxID.keySet()) {
                System.out.print(key + ": " + IdxID.get(key) + " ");
            }

            System.out.println();

            for(int i=0;i<IdxName.length;i++) {
                System.out.print(i + "." + IdxName[i] + " ");
            }

            System.out.println();
            System.out.println("--------------------");

            for(int i=0;i<Records.size();i++) {
                Records.get(i).print();
            }

            System.out.println("--------------------");
        }
    }

    public class Tuple {
        int[] data;

        // TODO remove this function
        public void print() {
            for(int i=0;i<data.length;i++) {
                System.out.print(data[i] + " ");
            }

            System.out.println();
        }
    }

    public class Query {
        ArrayList<String> Select[];
        String[] Tables;
        String[] Rename;
        String[] On;

        public Query() {
            Select = new ArrayList[2];
            Select[0] = new ArrayList<String>();
            Select[1] = new ArrayList<String>();
            Tables = new String[2];
            Rename = new String[2];
            On = new String[2];
        }

        public void populate() {
            for(int i=0;i<Select[0].size();i++) {
                if(Select[0].get(i).equals(Rename[0])) {
                    Select[0].set(i, Tables[0]);
                } else {
                    Select[0].set(i, Tables[1]);
                }
            }
        }

        public void populate(Table table) {
            for(int i=0;i<table.IdxName.length;i++) {
                Select[0].add(table.Name);
                Select[1].add(table.IdxName[i]);
            }
        }

        public void populate(Table table1, Table table2) {
            populate(table1);
            populate(table2);
        }

        // TODO remove this function
        public void print() {
            System.out.println("--------------------");
            System.out.println("Select: ");

            for(int i=0;i<2;i++) {
                for(int j=0;j<Select[i].size();j++) {
                    System.out.print(Select[i].get(j) + " ");
                }

                System.out.println();
            }

            System.out.println();
            System.out.println("Tables: " + Tables[0] + " " +  Tables[1]);

            System.out.println();
            System.out.println("Rename: " + Rename[0] + " " +  Rename[1]);

            System.out.println();
            System.out.println("Join on: " + On[0] + " " + On[1]);

            System.out.println("--------------------");
        }
    }
}

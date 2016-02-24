package org.pgmongo.console;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.pgmongo.PgMongoClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConsoleApp {
    MongoDatabase db;
    MongoClient mongo;
    MongoCollection collection;
    BufferedReader in;
    boolean connectionSuccessful = false;

    // connect -url jdbc:postgresql://localhost:5432/postgres -u postgres -p postgres
// db.test_json.find({'review.votes': 2});

    void run(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        String tableName = "";

        while (true) {
            if (!connectionSuccessful) {
                switch (args[0]) {
                    case "help":
                        System.out.println("help connect: connect -u [user name] -p [password] -url [url_to_db] -jdata [json_data_name] -debug\n" +
                                "Options:\n" +
                                "   -u          user name\n" +
                                "   -p          password\n" +
                                "   -url        url to db\n" +
                                "   -jdata      json_data name (default: 'json_data')\n" +
                                "   -debug      debug mod on\n");
                        break;
                    case "connect":
                        if (args.length > 4) {
                            ini(args);
                        } else {
                            System.out.println("Bad input value1. Type 'help' for help.");
                        }
                        break;
                    case "exit":
                        return;
                    default:
                        System.out.println("Bad input value2. Type 'help' for help.");
                }
                args = in.readLine().split(" ");
            } else {
                String mongoQuery = in.readLine().substring(3);
                String collectionName = mongoQuery.substring(0, mongoQuery.indexOf('.')).trim();
                System.out.println(collectionName);

                String queryWithName = mongoQuery.substring(mongoQuery.indexOf('.') + 1).trim();
                System.out.println(queryWithName);

                this.collection = db.getCollection(collectionName);

                String queryName = queryWithName.substring(0, queryWithName.indexOf('('));
                System.out.println(queryName);

                String query = queryWithName.substring(queryName.length()+1, queryWithName.length() - 2).trim();
                System.out.println(query);

                switch (queryName) {
                    case "find":
                        ArrayList<String> resCut = cutQueryAndProj(query);
                        if (resCut.size() > 2) {
                            System.out.println(resCut.toString());
                            throw new RuntimeException("invalid request");
                        }

                        Document queryFind = Document.parse(resCut.get(0));
                        Document projectionFind = (resCut.size() == 1) ? new Document() : Document.parse(resCut.get(1));

                        System.out.println(queryFind.toJson());
                        FindIterable fi = collection.find(queryFind);
                        for (Object o : fi) {
                            System.out.println(o.toString());
                        }

                        break;
                    case "insert":


                    case "delete":
                    default:
                        throw new RuntimeException("Operation not supported.");
                }

            }
        }
    }

    private ArrayList<String> cutQueryAndProj(String s) {
        int balance = 0;
        int prevPos = 0;
        int pos = -1;

        ArrayList<String> res = new ArrayList<>();

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '{') balance++;
            if (s.charAt(i) == '}') balance--;

            if (balance == 0) {
                pos = i;
                res.add(s.substring(prevPos, pos + 1).trim());
                prevPos = pos;
            }
        }

        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).charAt(0) == ',') {
                res.set(i, res.get(i).substring(1).trim());
            }
        }


        return res;
    }

    public static void main(String[] args) throws IOException {
        new ConsoleApp().run(args);
    }

    void ini(String[] args) {
        String url_to_db = "";
        String user_name = "";
        String password = "";
        boolean debug = false;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "-u":
                    if (i < args.length - 1 && args[i + 1].charAt(0) != '-') {
                        user_name = args[i + 1];
                        i++;
                        break;
                    } else continue;
                case "-p":
                    if (i < args.length - 1 && args[i + 1].charAt(0) != '-') {
                        password = args[i + 1];
                        i++;
                        break;
                    } else continue;
                case "-url":
                    if (i < args.length - 1 && args[i + 1].charAt(0) != '-') {
                        url_to_db = args[i + 1];
                        i++;
                        break;
                    } else continue;
                case "-debug":
                    debug = true;
                    break;
                default:
                    throw new RuntimeException("Wrong input");
            }
        }

        try {
            this.mongo = new PgMongoClient(url_to_db, user_name, password, debug);
            this.db = mongo.getDatabase("");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }


        connectionSuccessful = true;
        System.out.println("Opened database successfully");
    }

    /*
    db.test_json.find({'review.votes': 2});


    * */
}

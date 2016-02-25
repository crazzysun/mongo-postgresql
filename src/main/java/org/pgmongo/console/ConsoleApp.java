package org.pgmongo.console;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.pgmongo.PgMongoClient;
import org.pgmongo.PgMongoCollection;
import org.pgmongo.PgMongoDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConsoleApp {
    PgMongoDatabase db;
    PgMongoClient mongo;
    PgMongoCollection collection;
    BufferedReader in;
    boolean connectionSuccessful = false;

    /*
        sample query:
        connect -url jdbc:postgresql://localhost:5432/postgres -u postgres -p postgres -debug
        db.test_json.insert([{'review.votes': 1234, '_id': 123456}]);
        db.test_json.find({_id: 123456});
        db.test_json.delete({_id: 123456});
        db.test_json.find({_id: 123456});
        db.test_json.insert({_id: "235"});
    */

    void run() throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String[] args = in.readLine().split(" ");
            if (!connectionSuccessful || !args[0].startsWith("db.")) {
                switch (args[0]) {
                    case "help":
                        System.out.println(
                                "   help connect: connect -u [user name] -p [password] -url [url_to_db] -debug\n" +
                                        "   Options:\n" +
                                        "       -u          user name\n" +
                                        "       -p          password\n" +
                                        "       -url        url to db\n" +
                                        "       -debug      debug mod on\n" +
                                        "\n" +
                                        "   help query: db.[collection_name].[query_name]([json]);\n" +
                                        "   Support query name:\n" +
                                        "       find    (with projection, support comparison and logical operation);\n" +
                                        "       insert  (support comparison and logical operation);\n" +
                                        "       delete  (support comparison and logical operation).\n\n" +
                                        "   Type 'help' for help, 'exit' for exit.");
                        break;
                    case "connect":
                        if (args.length > 4) {
                            ini(args);
                        } else {
                            System.out.println("Bad input value. Type 'help' for help.");
                        }
                        break;
                    case "exit":
                        return;
                    default:
                        System.out.println("Bad input value: \"" + args[0] + "\". Type 'help' for help.");
                }
            } else {
                String tmp = arrToString(args).trim();
                if (tmp.length() == 0) {
                    continue;
                }

                String mongoQuery = tmp.substring(3);
                String collectionName = mongoQuery.substring(0, mongoQuery.indexOf('.')).trim();
                String queryWithName = mongoQuery.substring(mongoQuery.indexOf('.') + 1).trim();
                String queryName = queryWithName.substring(0, queryWithName.indexOf('(')).trim();
                String query = queryWithName.substring(queryName.length() + 1, queryWithName.length() - 2).trim();
                this.collection = db.getCollection(collectionName);

                switch (queryName) {
                    case "find":
                        ArrayList<Document> resCut = cutDocsFromString(query);
                        if (resCut.size() > 2 || resCut.size() < 1) {
                            throw new RuntimeException("Find: invalid request: " + Arrays.toString(resCut.toArray()) + ".");
                        }

                        Document queryFind = resCut.get(0);
                        Document projectionFind = (resCut.size() == 1) ? new Document() : resCut.get(1);
                        FindIterable<Document> fi = collection.find(queryFind, projectionFind);

                        for (Document o : fi) {
                            System.out.println(o.toString());
                        }
                        break;
                    case "insert":
                        if (query.charAt(0) == '[') {
                            query = query.substring(1, query.length() - 1);
                        }

                        resCut = cutDocsFromString(query);
                        collection.insertMany(resCut);
                        break;
                    case "delete":
                        Document doc = Document.parse(query);
                        collection.deleteMany(doc);
                        break;
                    default:
                        throw new RuntimeException("Operation not supported.");
                }
            }
        }
    }

    private String arrToString(String[] arr) {
        StringBuilder res = new StringBuilder();
        for (String anArr : arr) {
            res.append(anArr).append(" ");
        }

        return res.toString().trim();
    }

    private ArrayList<Document> cutDocsFromString(String s) {
        int balance = 0;
        int prevPos = 0;
        int pos;

        ArrayList<String> tmp = new ArrayList<>();
        ArrayList<Document> res = new ArrayList<>();

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '{') balance++;
            if (s.charAt(i) == '}') balance--;

            if (balance == 0 && s.charAt(i) == '}') {
                pos = i;
                tmp.add(s.substring(prevPos, pos + 1).trim());
                prevPos = pos + 1;
            }
        }

        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i).charAt(0) == ',') {
                tmp.set(i, tmp.get(i).substring(1).trim());
            }
            res.add(Document.parse(tmp.get(i)));
        }

        return res;
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

    public static void main(String[] args) throws IOException {
        new ConsoleApp().run();
    }
}

package org.pgmongo.samples;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.pgmongo.PgMongoClient;

import java.sql.SQLException;

public class PgSolution {
   MongoDatabase db;
     MongoClient mongo;
    MongoCollection collection;

    //"jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres"
    public PgSolution(String url, String name, String password, boolean debug) throws SQLException, ClassNotFoundException {
        this.mongo = new PgMongoClient(url, name, password, debug);
        this.db = mongo.getDatabase("");
        this.collection = db.getCollection("test_json");
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        PgSolution pg = new PgSolution("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres", false);

            Document doc = Document.parse("{_id: 123456}");
            pg.collection.deleteMany(doc);

//        Document doc = Document.parse("{_id: 123456}");
//        pg.collection.insertOne(doc);
    }
}



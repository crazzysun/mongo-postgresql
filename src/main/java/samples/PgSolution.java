package samples;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import pgmongo.PgMongoClient;

import java.sql.SQLException;

public class PgSolution {
    MongoDatabase db;
    MongoClient mongo;
    MongoCollection collection;

    //"jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres"
    public PgSolution(String url, String name, String password) throws SQLException, ClassNotFoundException {
        this.mongo = new PgMongoClient(url, name, password);
        this.db = mongo.getDatabase("");
        this.collection = db.getCollection("zips");
    }
}



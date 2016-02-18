package samples;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;

public class MongoSolution {
    MongoDatabase db;
    public MongoClient mongo;
    public MongoCollection<Document> collection;

    public MongoSolution() {
        this.mongo = new MongoClient();
        this.db = mongo.getDatabase("zips");
        collection = db.getCollection("zips");
    }

    public void createCity(String zip, String name) {
        Document city = new Document();
        city.append("_id", zip);

        collection.find();

        if (collection.find(city).first() == null) {
            city.append("name", name);
            collection.insertOne(city);
        } else {
            System.err.println("city already in db");
        }
    }

    public void deleteCity(String zip) {
        Document city = new Document();
        city.append("_id", zip);
        collection.findOneAndDelete(city);
    }

    public Document findByName(String name) {
        Document docForSearch = new Document();
        docForSearch.append("name", name);
        return collection.find(docForSearch).first();
    }

    public Document findByZip(String zip) {
        Document docForSearch = new Document();
        docForSearch.append("_id", zip);

        collection.find(docForSearch).projection(new Document("loc", 1));
        return collection.find(docForSearch).first();
    }

    public ArrayList<Document> FItoArr(FindIterable<Document> fi) {
        ArrayList<Document> ans = new ArrayList<Document>();
        for (Document document : fi) {
            ans.add(document);
        }

        return ans;
    }
}



import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;

public class Solution {
    MongoDatabase db;
    MongoClient mongo;
    MongoCollection<Document> collection;

    public Solution() {
        this.mongo = new MongoClient();
        this.db = mongo.getDatabase("zips");
        collection = db.getCollection("zips");
    }

    public Document EmptyDocument() {
        Document empty = new Document();
        empty.append(null, null);
        return empty;
    }

    public void createCity(String zip) {
        Document city = new Document();
        city.append("_id", zip);

        if (collection.find(city).first() == null) {
            collection.insertOne(city);
        } else {
            System.err.println("city already");
        }
    }

    public void createCity(String zip, String name) {
        Document city = new Document();
        city.append("_id", zip);

        if (collection.find(city).first() == null) {
            city.append("name", name);
            collection.insertOne(city);
        } else {
            System.err.println("city already");
        }
    }

    public void createCity(String zip, String name, String state) {
        Document country = new Document();
        country.append("_id", zip);

        if (collection.find(country).first() == null) {
            country.append("name", name).append("state", state);
            collection.insertOne(country);
        } else {
            System.err.println("country already");
        }
    }

    public void TestMethod1(String zip) {
        Document city = new Document();
        city.append("_id", zip);


        if (collection.find(city).first() == null) {
            System.out.println("IN TESTM: NULL");
        } else {
            System.out.println("IN TESTM: NOT NULL");
        }
    }

    public void deleteCity(String zip) {
        Document city = new Document();
        city.append("_id", zip);
        Document tmp = collection.find(city).first();

//        if (tmp != null) {
//            System.out.println("!!!!!IN DELETE!!!!    " + tmp.get("_id"));
//        } else {
//            System.out.println("IN DELETE: NULL");
//        }

        collection.findOneAndDelete(city);

        tmp = collection.find(city).first();
    }

    public Document findByName(String name) {
        Document docForSearch = new Document();
        docForSearch.append("name", name);
        return collection.find(docForSearch).first();
    }

    public Document findByZip(String zip) {
        Document docForSearch = new Document();
        docForSearch.append("_id", zip);
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



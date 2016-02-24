import junit.framework.*;
import org.pgmongo.samples.MongoSolution;
import org.bson.Document;

public class MongoSolutionTest extends TestCase {
    MongoSolution cur;

    protected void setUp() {
        cur = new MongoSolution();
        cur.deleteCity("1234");
    }

    public void testCreateForName() {
        cur.createCity("1234", "qwe");
        Document ans = cur.findByName("qwe");
        assertEquals(ans.get("name"), "qwe");
        cur.deleteCity("1234");
    }

    public void testCreateForZip() {
        cur.createCity("123", "qwe");
        Document ans = cur.findByZip("123");
        assertNotNull(ans);
        assertEquals(ans.get("_id"), "123");
        cur.deleteCity("123");
    }

    public void testDeleteCity() {
        cur.createCity("1234567", "qwe");
        Document ans = cur.findByZip("1234567");
        assertNotNull(ans);

        cur.deleteCity("1234567");
        assertNull(cur.findByZip("1234567"));
    }

    public void testCount() {
        cur.deleteCity("1234");
        long curCnt = cur.collection.count();
        cur.createCity("1234", "qwe");
        assertEquals(cur.collection.count(), curCnt + 1);

        curCnt++;
        cur.createCity("1234", "qwe");
        assertEquals(cur.collection.count(), curCnt);
        cur.deleteCity("1234");
    }

    public void testUpdateOne() {
        Document doc = new Document("state", "CA");
        Document newDoc = new Document("$set", new Document("city", "SARATOV"));
        long cntCA = cur.FItoArr(cur.collection.find(doc)).size();
        cur.collection.updateOne(doc, newDoc);
        long newCntCA = cur.FItoArr(cur.collection.find(doc)).size();
        assertEquals(cntCA, newCntCA);

        Document doc1 = cur.collection.find(doc).first();
        Document doc2 = cur.collection.find(new Document("city", "SARATOV")).first();
        assertEquals(doc1.get("_id"), doc2.get("_id"));
    }
}


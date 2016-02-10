import junit.framework.*;
import org.bson.Document;
import translator.QueryTranslator;

public class QueryTranslatorTest extends TestCase {
    QueryTranslator qt;

    protected void setUp() {
        qt = new QueryTranslator();
    }

    public void testPrimitiveOpEq() {
        assertEquals(qt.find("instance", "{'name': 'Bob'}", ""), "select * from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': {$eq:'Alice'}}", ""), "select * from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': {$eq:'Trant'}}", ""), qt.find("instance", "{'name': 'Trant'}", ""));
    }

    public void testPrimitiveOp() {
        assertEquals(qt.find("instance", "{'price': {$gt: 10}}", ""), "select * from instance where price > ?;");
        assertEquals(qt.find("instance", "{'price': {$gte: 20}}", ""), "select * from instance where price >= ?;");
        assertEquals(qt.find("instance", "{'price': {$lt: 30}}", ""), "select * from instance where price < ?;");
        assertEquals(qt.find("instance", "{'price': {$lte: 40}}", ""), "select * from instance where price <= ?;");
        assertEquals(qt.find("instance", "{'price': {$ne: 50}}", ""), "select * from instance where price != ?;");
    }

    public void testPtimitiveOpLogAndDataType() {
        assertEquals(qt.find("instance", "{'item.name': 'Alice'}", ""), "select * from instance where item->name = ?;");

        assertEquals(qt.find("instance", "{'name': {$gt:'Bob'}}", ""), "select * from instance where name > ?;");
        assertEquals(qt.find("instance", "{'price': '10000'}", ""), "select * from instance where price = ?;");
        assertEquals(qt.find("instance", "{'price': {$ne: 100000000}}", ""), "select * from instance where price != ?;");
        assertEquals(qt.find("instance", "{'price': {$lt: 0.1}}", ""), "select * from instance where price < ?;");
    }

    public void testProjection() {
        assertEquals(qt.find("instance", "{'name': 'Alice'}", "{'name': 1, 'price': 1}"), "select _id, name, price from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': 'Bob'}", "{'item.name': 1, 'price': 1, '_id': 0}"), "select item->name, price from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': 'Carrol'}", "{'item.name': 1, 'item.price.max': 1, '_id': 0}"), "select item->name, item->price->max from instance where name = ?;");
    }
}

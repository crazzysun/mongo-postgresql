import junit.framework.*;
import org.bson.Document;
import translator.QueryTranslator;

public class QueryTranslatorTest extends TestCase {
    QueryTranslator qt;

    protected void setUp() {
        qt = new QueryTranslator();
    }

    public void testPrimitiveOpEq() {
        assertEquals(qt.find("instance", "{'name': 'Bob'}", "").getQuery(), "select * from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': {$eq:'Alice'}}", "").getQuery(), "select * from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': {$eq:'Trant'}}", "").getQuery(), qt.find("instance", "{'name': 'Trant'}", "").getQuery());
    }

    public void testPrimitiveOp() {
        assertEquals(qt.find("instance", "{'price': {$gt: 10}}", "").getQuery(), "select * from instance where price > ?;");
        assertEquals(qt.find("instance", "{'price': {$gte: 20}}", "").getQuery(), "select * from instance where price >= ?;");
        assertEquals(qt.find("instance", "{'price': {$lt: 30}}", "").getQuery(), "select * from instance where price < ?;");
        assertEquals(qt.find("instance", "{'price': {$lte: 40}}", "").getQuery(), "select * from instance where price <= ?;");
        assertEquals(qt.find("instance", "{'price': {$ne: 50}}", "").getQuery(), "select * from instance where price != ?;");
    }

    public void testPtimitiveOpLogAndDataType() {
        assertEquals(qt.find("instance", "{'item.name': 'Alice'}", "").getQuery(), "select * from instance where item->name = ?;");
        assertEquals(qt.find("instance", "{'name': {$gt:'Bob'}}", "").getQuery(), "select * from instance where name > ?;");
        assertEquals(qt.find("instance", "{'price': '10000'}", "").getQuery(), "select * from instance where price = ?;");
        assertEquals(qt.find("instance", "{'price': {$ne: 100000000}}", "").getQuery(), "select * from instance where price != ?;");
        assertEquals(qt.find("instance", "{'price': {$lt: 0.1}}", "").getQuery(), "select * from instance where price < ?;");
    }

    public void testProjection() {
        assertEquals(qt.find("instance", "{'name': 'Alice'}", "{'name': 1, 'price': 1}").getQuery(), "select _id, name, price from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': 'Bob'}", "{'item.name': 1, 'price': 1, '_id': 0}").getQuery(), "select item->name, price from instance where name = ?;");
        assertEquals(qt.find("instance", "{'name': 'Carrol'}", "{'item.name': 1, 'item.price.max': 1, '_id': 0}").getQuery(), "select item->name, item->price->max from instance where name = ?;");
    }
}

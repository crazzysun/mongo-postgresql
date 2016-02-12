import junit.framework.TestCase;
import translator.QueryTranslator;

import java.util.ArrayList;
import java.util.Arrays;

public class QueryTranslatorTest extends TestCase {
    QueryTranslator qt;

    protected void setUp() {
        qt = new QueryTranslator();
    }

    public void testPrimitiveOpEq() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'name': 'Bob'}", "").getQuery(), "select * from instance where name = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$eq:'Alice'}}", "").getQuery(), "select * from instance where name = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$eq:'Nikto ne chitaet testy'}}", "").getQuery(), qt.find("instance", "{'name': 'Trant'}", "").getQuery());
    }

    public void testPrimitiveOp() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 100, $gt: 10}}", "").getQuery(), "select * from instance where price < ? and price > ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$gt: 10}}", "").getQuery(), "select * from instance where price > ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$gte: 20}}", "").getQuery(), "select * from instance where price >= ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 30}}", "").getQuery(), "select * from instance where price < ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lte: 40}}", "").getQuery(), "select * from instance where price <= ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$ne: 50}}", "").getQuery(), "select * from instance where price != ?;");
    }

    public void testPtimitiveOpLogAndDataType() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'item.name': 'Alice'}", "").getQuery(), "select * from instance where item->name = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$gt:'Bob'}}", "").getQuery(), "select * from instance where name > ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': '10000'}", "").getQuery(), "select * from instance where price = ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$ne: 100000000}}", "").getQuery(), "select * from instance where price != ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 0.1}}", "").getQuery(), "select * from instance where price < ?;");
    }

    public void testProjection() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'name': 'Alice'}", "{'name': 1, 'price': 1}").getQuery(),
                "select _id, name, price from instance where name = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': 'Bob'}", "{'item.name': 1, 'price': 1, '_id': 0}").getQuery(),
                "select item->name, price from instance where name = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': 'Carrol'}", "{'item.name': 1, 'item.price.max': 1, '_id': 0}").getQuery(),
                "select item->name, item->price->max from instance where name = ?;");
    }

    public void testAndOr() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Alice'}, {'city': 'Saratov'}]}", "").getQuery(),
                "select * from instance where (name = ? AND city = ?);");
        TestCase.assertEquals(qt.find("instance", "{$or: [{'fruit': 'apple'}, {'price': 0.99}, {'fruit': 'cucumber'}]}", "").getQuery(),
                "select * from instance where (fruit = ? OR price = ? OR fruit = ?);");
    }

    public void testCombination() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Alice'}, {'city': 'Saratov'}]}", "").getQuery(),
                "select * from instance where (name = ? AND city = ?);");
        TestCase.assertEquals(qt.find("instance", "{'country': 'turkey', $or: [{'fruit': 'mandarin'}, {$and: [{'price': {$lt: 20}}, {'fruit':'cucumber'}]}], 'price': {$lt: 50}}", "").getQuery(),
                "select * from instance where country = ? AND (fruit = ? OR (price < ? AND fruit = ?)) AND price < ?;");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'fruit': 'banana'}, {'price': {$gte: 0}}, {'price': {$lte: 50}}]}", "{'_id': 1}").getQuery(),
                "select _id from instance where (fruit = ? AND price >= ? AND price <= ?);");
        TestCase.assertEquals(qt.find("instance", "{$or: [{$and: [{'fruit': 'mandarin'}, {'price': {$lt: 50}}]}, {'price': {$lt: 20}}, {'fruit':'cucumber'}], 'country': 'turkey'}", "").getQuery(),
                "select * from instance where ((fruit = ? AND price < ?) OR price < ? OR fruit = ?) AND country = ?;");
        TestCase.assertEquals(qt.find("instance", "{'country': 'turkey', $or: [{'fruit': 'mandarin'}, {$and: [{'price': {$lt: 20}}, {'fruit':'cucumber'}]}], 'price': {$lt: 50}}", "").getQuery(),
                "select * from instance where country = ? AND (fruit = ? OR (price < ? AND fruit = ?)) AND price < ?;");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'fruit': 'banana'}, {'price': {$gte: 0}}, {'price': {$lte: 50}}]}", "{'_id': 1}").getQuery(),
                "select _id from instance where (fruit = ? AND price >= ? AND price <= ?);");
    }

    public void testInNin() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$in: ['orange', 2, 'cucumber', 567, 'banana']}}", "").getQuery(),
                "select * from instance where fruit IN (?, ?, ?, ?, ?);");
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$nin: ['orange', 'cucumber', 'banana', 25364, 345]}}", "").getQuery(),
                "select * from instance where fruit NOT IN (?, ?, ?, ?, ?);");
    }

    public void testParam() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$in: ['orange', 2, 'cucumber', 567, 'banana']}}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("orange", 2, "cucumber", 567, "banana")));
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$nin: ['orange', 'cucumber', 'banana', 25364, 345]}}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("orange", "cucumber", "banana", 25364, 345)));
        TestCase.assertEquals(qt.find("instance", "{$or: [{$and: [{'fruit': 'mandarin'}, {'price': {$lt: 50}}]}, {'price': {$lt: 20}}, {'fruit':'cucumber'}], 'country': 'turkey'}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("mandarin", 50, 20, "cucumber", "turkey")));
    }

    public void testNotNor() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'price': {$not: {$eq: 100}}}", "").getQuery(), "select * from instance where NOT price = ?;");
        TestCase.assertEquals(qt.find("instance", "{$nor: [{'name': 'Yura'}, {'name': 'Dima'}]}", "").getQuery(), "select * from instance where NOT (name = ? OR name = ?);");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Yuri'}, {'city': 'Saratov'}, {'company': 'Grid Dynamics'}]}", "").getQuery(), "select * from instance where (name = ? AND city = ? AND company = ?);");
    }
}

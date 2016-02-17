import junit.framework.TestCase;
import translator.QueryTranslator;

import java.util.ArrayList;
import java.util.Arrays;

public class QueryTranslatorTest extends TestCase {
    QueryTranslator qt;

    protected void setUp() {
        qt = new QueryTranslator("json_data");
    }

    public void testPrimitiveOpEq() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'name': 'Bob'}", "").getQuery(), "select * from instance where json_data->'name' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$eq:'Alice'}}", "").getQuery(), "select * from instance where json_data->'name' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$eq:'Nikto ne chitaet testy'}}", "").getQuery(), qt.find("instance", "{'name': 'Trant'}", "").getQuery());
    }

    public void testPrimitiveOp() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 100, $gt: 10}}", "").getQuery(), "select * from instance where json_data->'price' < ? and json_data->'price' > ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$gt: 10}}", "").getQuery(), "select * from instance where json_data->'price' > ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$gte: 20}}", "").getQuery(), "select * from instance where json_data->'price' >= ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 30}}", "").getQuery(), "select * from instance where json_data->'price' < ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lte: 40}}", "").getQuery(), "select * from instance where json_data->'price' <= ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$ne: 50}}", "").getQuery(), "select * from instance where json_data->'price' != ?;");
    }

    public void testPtimitiveOpLogAndDataType() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'item.name': 'Alice'}", "").getQuery(), "select * from instance where json_data->'item'->'name' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$gt:'Bob'}}", "").getQuery(), "select * from instance where json_data->'name' > ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': '10000'}", "").getQuery(), "select * from instance where json_data->'price' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$ne: 100000000}}", "").getQuery(), "select * from instance where json_data->'price' != ?;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 0.1}}", "").getQuery(), "select * from instance where json_data->'price' < ?;");
    }

    public void testProjection() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'name': 'Alice'}", "{'name': 1, 'price': 1}").getQuery(),
                "select json_data->'_id' as '_id', json_data->'name' as 'name', json_data->'price' as 'price' from instance where json_data->'name' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': 'Bob'}", "{'item.name': 1, 'price': 1, '_id': 0}").getQuery(),
                "select json_data->'item'->'name' as 'item.name', json_data->'price' as 'price' from instance where json_data->'name' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'name': 'Carrol'}", "{'item.name': 1, 'item.price.max': 1, '_id': 0}").getQuery(),
                "select json_data->'item'->'name' as 'item.name', json_data->'item'->'price'->'max' as 'item.price.max' from instance where json_data->'name' = ?;");
        TestCase.assertEquals(qt.find("test_json", "{'review.votes': 2}", "{'product.title': 1, '_id': 0}").getQuery(), "select json_data->'product'->'title' as 'product.title' from test_json where json_data->'review'->'votes' = ?;");
    }

    public void testAndOr() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Alice'}, {'city': 'Saratov'}]}", "").getQuery(),
                "select * from instance where (json_data->'name' = ? AND json_data->'city' = ?);");
        TestCase.assertEquals(qt.find("instance", "{$or: [{'fruit': 'apple'}, {'price': 0.99}, {'fruit': 'cucumber'}]}", "").getQuery(),
                "select * from instance where (json_data->'fruit' = ? OR json_data->'price' = ? OR json_data->'fruit' = ?);");
    }

    public void testCombination() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Alice'}, {'city': 'Saratov'}]}", "").getQuery(),
                "select * from instance where (json_data->'name' = ? AND json_data->'city' = ?);");
        TestCase.assertEquals(qt.find("instance", "{'country': 'turkey', $or: [{'fruit': 'mandarin'}, {$and: [{'price': {$lt: 20}}, {'fruit':'cucumber'}]}], 'price': {$lt: 50}}", "").getQuery(),
                "select * from instance where json_data->'country' = ? AND (json_data->'fruit' = ? OR (json_data->'price' < ? AND json_data->'fruit' = ?)) AND json_data->'price' < ?;");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'fruit': 'banana'}, {'price': {$gte: 0}}, {'price': {$lte: 50}}]}", "{'_id': 1}").getQuery(),
                "select json_data->'_id' as '_id' from instance where (json_data->'fruit' = ? AND json_data->'price' >= ? AND json_data->'price' <= ?);");
        TestCase.assertEquals(qt.find("instance", "{$or: [{$and: [{'fruit': 'mandarin'}, {'price': {$lt: 50}}]}, {'price': {$lt: 20}}, {'fruit':'cucumber'}], 'country': 'turkey'}", "").getQuery(),
                "select * from instance where ((json_data->'fruit' = ? AND json_data->'price' < ?) OR json_data->'price' < ? OR json_data->'fruit' = ?) AND json_data->'country' = ?;");
        TestCase.assertEquals(qt.find("instance", "{'country': 'turkey', $or: [{'fruit': 'mandarin'}, {$and: [{'price': {$lt: 20}}, {'fruit':'cucumber'}]}], 'price': {$lt: 50}}", "").getQuery(),
                "select * from instance where json_data->'country' = ? AND (json_data->'fruit' = ? OR (json_data->'price' < ? AND json_data->'fruit' = ?)) AND json_data->'price' < ?;");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'fruit': 'banana'}, {'price': {$gte: 0}}, {'price': {$lte: 50}}]}", "{'_id': 1}").getQuery(),
                "select json_data->'_id' as '_id' from instance where (json_data->'fruit' = ? AND json_data->'price' >= ? AND json_data->'price' <= ?);");
    }

    public void testInNin() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$in: ['orange', 2, 'cucumber', 567, 'banana']}}", "").getQuery(),
                "select * from instance where json_data->'fruit' IN (?, ?, ?, ?, ?);");
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$nin: ['orange', 'cucumber', 'banana', 25364, 345]}}", "").getQuery(),
                "select * from instance where json_data->'fruit' NOT IN (?, ?, ?, ?, ?);");
    }

    public void testParam() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$in: ['orange', 2, 'cucumber', 567, 'banana']}}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("'orange'", "'2'", "'cucumber'", "'567'", "'banana'")));
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$nin: ['orange', 'cucumber', 'banana', 25364, 345]}}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("'orange'", "'cucumber'", "'banana'", "'25364'", "'345'")));
        TestCase.assertEquals(qt.find("instance", "{$or: [{$and: [{'fruit': 'mandarin'}, {'price': {$lt: 50}}]}, {'price': {$lt: 20}}, {'fruit':'cucumber'}], 'country': 'turkey'}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("'mandarin'", "'50'", "'20'", "'cucumber'", "'turkey'")));
    }

    public void testNotNor() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'price': {$not: {$eq: 100}}}", "").getQuery(), "select * from instance where NOT json_data->'price' = ?;");
        TestCase.assertEquals(qt.find("instance", "{$nor: [{'name': 'Yura'}, {'name': 'Dima'}]}", "").getQuery(), "select * from instance where NOT (json_data->'name' = ? OR json_data->'name' = ?);");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Yuri'}, {'city': 'Saratov'}, {'company': 'Grid Dynamics'}]}", "").getQuery(), "select * from instance where (json_data->'name' = ? AND json_data->'city' = ? AND json_data->'company' = ?);");
    }
}

import junit.framework.TestCase;
import org.pgmongo.QueryTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class QueryTranslatorTest extends TestCase {
    QueryTranslator qt;

    protected void setUp() {
        qt = new QueryTranslator("json_data");
    }

    public void testPrimitiveOpEq() throws Exception {
        TestCase.assertEquals(qt.find("test_json", "{'name': 'Bob'}", "").getQuery(), "select json_data from test_json where json_data->'name' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$eq:'Alice'}}", "").getQuery(), "select json_data from instance where json_data->'name' = ?::jsonb;");
        TestCase.assertEquals(qt.find("test_json", "{'review.rating' : {$lt: 10}}}", "").getQuery(), "select json_data from test_json where json_data->'review'->'rating' < ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$eq:'Nikto ne chitaet testy'}}", "").getQuery(), qt.find("instance", "{'name': 'Trant'}", "").getQuery());
    }

    public void testPrimitiveOp() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 100, $gt: 10}}", "").getQuery(), "select json_data from instance where json_data->'price' < ?::jsonb and json_data->'price' > ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$gt: 10}}", "").getQuery(), "select json_data from instance where json_data->'price' > ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$gte: 20}}", "").getQuery(), "select json_data from instance where json_data->'price' >= ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 30}}", "").getQuery(), "select json_data from instance where json_data->'price' < ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lte: 40}}", "").getQuery(), "select json_data from instance where json_data->'price' <= ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$ne: 50}}", "").getQuery(), "select json_data from instance where json_data->'price' != ?::jsonb;");
    }

    public void testPtimitiveOpLogAndDataType() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'item.name': 'Alice'}", "").getQuery(), "select json_data from instance where json_data->'item'->'name' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'name': {$gt:'Bob'}}", "").getQuery(), "select json_data from instance where json_data->'name' > ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': '10000'}", "").getQuery(), "select json_data from instance where json_data->'price' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$ne: 100000000}}", "").getQuery(), "select json_data from instance where json_data->'price' != ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'price': {$lt: 0.1}}", "").getQuery(), "select json_data from instance where json_data->'price' < ?::jsonb;");
    }

    public void testProjection() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'name': 'Alice'}", "{'name': 1, 'price': 1}").getQuery(),
                "select json_data->'_id' as \"_id\", json_data->'name' as \"name\", json_data->'price' as \"price\" from instance where json_data->'name' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'name': 'Bob'}", "{'item.name': 1, 'price': 1, '_id': 0}").getQuery(),
                "select json_data->'item'->'name' as \"item.name\", json_data->'price' as \"price\" from instance where json_data->'name' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'name': 'Carrol'}", "{'item.name': 1, 'item.price.max': 1, '_id': 0}").getQuery(),
                "select json_data->'item'->'name' as \"item.name\", json_data->'item'->'price'->'max' as \"item.price.max\" from instance where json_data->'name' = ?::jsonb;");
        TestCase.assertEquals(qt.find("test_json", "{'review.votes': 2}", "{'product.title': 1, '_id': 0}").getQuery(),
                "select json_data->'product'->'title' as \"product.title\" from test_json where json_data->'review'->'votes' = ?::jsonb;");
    }

    public void testAndOr() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Alice'}, {'city': 'Saratov'}]}", "").getQuery(),
                "select json_data from instance where (json_data->'name' = ?::jsonb AND json_data->'city' = ?::jsonb);");
        TestCase.assertEquals(qt.find("instance", "{$or: [{'fruit': 'apple'}, {'price': 0.99}, {'fruit': 'cucumber'}]}", "").getQuery(),
                "select json_data from instance where (json_data->'fruit' = ?::jsonb OR json_data->'price' = ?::jsonb OR json_data->'fruit' = ?::jsonb);");
    }

    public void testCombination() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Alice'}, {'city': 'Saratov'}]}", "").getQuery(),
                "select json_data from instance where (json_data->'name' = ?::jsonb AND json_data->'city' = ?::jsonb);");
        TestCase.assertEquals(qt.find("instance", "{'country': 'turkey', $or: [{'fruit': 'mandarin'}, {$and: [{'price': {$lt: 20}}, {'fruit':'cucumber'}]}], 'price': {$lt: 50}}", "").getQuery(),
                "select json_data from instance where json_data->'country' = ?::jsonb AND (json_data->'fruit' = ?::jsonb OR (json_data->'price' < ?::jsonb AND json_data->'fruit' = ?::jsonb)) AND json_data->'price' < ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'fruit': 'banana'}, {'price': {$gte: 0}}, {'price': {$lte: 50}}]}", "{'_id': 1}").getQuery(),
                "select json_data->'_id' as \"_id\" from instance where (json_data->'fruit' = ?::jsonb AND json_data->'price' >= ?::jsonb AND json_data->'price' <= ?::jsonb);");
        TestCase.assertEquals(qt.find("instance", "{$or: [{$and: [{'fruit': 'mandarin'}, {'price': {$lt: 50}}]}, {'price': {$lt: 20}}, {'fruit':'cucumber'}], 'country': 'turkey'}", "").getQuery(),
                "select json_data from instance where ((json_data->'fruit' = ?::jsonb AND json_data->'price' < ?::jsonb) OR json_data->'price' < ?::jsonb OR json_data->'fruit' = ?::jsonb) AND json_data->'country' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{'country': 'turkey', $or: [{'fruit': 'mandarin'}, {$and: [{'price': {$lt: 20}}, {'fruit':'cucumber'}]}], 'price': {$lt: 50}}", "").getQuery(),
                "select json_data from instance where json_data->'country' = ?::jsonb AND (json_data->'fruit' = ?::jsonb OR (json_data->'price' < ?::jsonb AND json_data->'fruit' = ?::jsonb)) AND json_data->'price' < ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'fruit': 'banana'}, {'price': {$gte: 0}}, {'price': {$lte: 50}}]}", "{'_id': 1}").getQuery(),
                "select json_data->'_id' as \"_id\" from instance where (json_data->'fruit' = ?::jsonb AND json_data->'price' >= ?::jsonb AND json_data->'price' <= ?::jsonb);");
    }

    public void testInNin() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$in: ['orange', 2, 'cucumber', 567, 'banana']}}", "").getQuery(),
                "select json_data from instance where json_data->'fruit' IN (?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb);");
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$nin: ['orange', 'cucumber', 'banana', 25364, 345]}}", "").getQuery(),
                "select json_data from instance where json_data->'fruit' NOT IN (?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb);");
    }

    public void testParam() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$in: ['orange', 2, 'cucumber', 567, 'banana']}}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("orange", "2", "cucumber", "567", "banana")));
        TestCase.assertEquals(qt.find("instance", "{'fruit': {$nin: ['orange', 'cucumber', 'banana', 25364, 345]}}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("orange", "cucumber", "banana", "25364", "345")));
        TestCase.assertEquals(qt.find("instance", "{$or: [{$and: [{'fruit': 'mandarin'}, {'price': {$lt: 50}}]}, {'price': {$lt: 20}}, {'fruit':'cucumber'}], 'country': 'turkey'}", "").getParameters(),
                new ArrayList<Object>(Arrays.asList("mandarin", "50", "20", "cucumber", "turkey")));
    }

    public void testNotNor() throws Exception {
        TestCase.assertEquals(qt.find("instance", "{'price': {$not: {$eq: 100}}}", "").getQuery(),
                "select json_data from instance where NOT json_data->'price' = ?::jsonb;");
        TestCase.assertEquals(qt.find("instance", "{$nor: [{'name': 'Yura'}, {'name': 'Dima'}]}", "").getQuery(),
                "select json_data from instance where NOT (json_data->'name' = ?::jsonb OR json_data->'name' = ?::jsonb);");
        TestCase.assertEquals(qt.find("instance", "{$and: [{'name': 'Yuri'}, {'city': 'Saratov'}, {'company': 'Grid Dynamics'}]}", "").getQuery(),
                "select json_data from instance where (json_data->'name' = ?::jsonb AND json_data->'city' = ?::jsonb AND json_data->'company' = ?::jsonb);");
    }

    public void testDelete() throws Exception {
        TestCase.assertEquals(qt.delete("test_json", "{'review.rating' : {$lt: 10}}").getQuery(),
                "delete from test_json where json_data->'review'->'rating' < ?::jsonb;");
        TestCase.assertEquals(qt.delete("test_json", "{'review.votes' : {$lte: 5}, 'product.group' : 'Book'}").getQuery(),
                "delete from test_json where json_data->'review'->'votes' <= ?::jsonb AND json_data->'product'->'group' = ?::jsonb;");
        TestCase.assertEquals(qt.delete("zips", "{'city': 'BARRE'}").getQuery(), "delete from zips where json_data->'city' = ?::jsonb;");
        TestCase.assertEquals(qt.delete("zips", "{'city': 'BARRE'}", 1).getQuery(), "delete from zips where json_data->'city' = ?::jsonb LIMIT 1;");
    }

    public void testInsert() throws Exception {
        TestCase.assertEquals(qt.insert("zips", new ArrayList<>(Collections.singletonList("{item: \"card\", _id: 15}")), "").getQuery(),
                "insert into zips (_id, json_data) values (?, ?::jsonb);");
        TestCase.assertEquals(qt.insert("zips", new ArrayList<>(Arrays.asList("{item: \"card\", _id: 15}", "{_id: 404, name: 'Nastya'}")), "").getQuery(),
                "insert into zips (_id, json_data) values (?, ?::jsonb), (?, ?::jsonb);");
    }
}

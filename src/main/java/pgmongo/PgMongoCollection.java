package pgmongo;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PgMongoCollection implements MongoCollection<Document> {
    private java.sql.Connection connection;
    private String tableName;
    private String columnWithJson;

    public PgMongoCollection(java.sql.Connection connection, String tableName, String columnWithJson) {
        this.connection = connection;
        this.tableName = tableName;
        this.columnWithJson = columnWithJson;
    }

    public void putColumnWithJsonName(String columnName) {
        columnWithJson = columnName;
    }

    @Override
    public MongoNamespace getNamespace() {
        return null;
    }

    @Override
    public Class<Document> getDocumentClass() {
        return null;
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return null;
    }

    @Override
    public ReadPreference getReadPreference() {
        return null;
    }

    @Override
    public WriteConcern getWriteConcern() {
        return null;
    }

    @Override
    public <NewDocument> MongoCollection<NewDocument> withDocumentClass(Class<NewDocument> aClass) {
        return null;
    }

    @Override
    public MongoCollection<Document> withCodecRegistry(CodecRegistry codecRegistry) {
        return null;
    }

    @Override
    public MongoCollection<Document> withReadPreference(ReadPreference readPreference) {
        return null;
    }

    @Override
    public MongoCollection<Document> withWriteConcern(WriteConcern writeConcern) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public long count(Bson bson) {
        return 0;
    }

    @Override
    public long count(Bson bson, CountOptions countOptions) {
        return 0;
    }

    @Override
    public <TResult> DistinctIterable<TResult> distinct(String s, Class<TResult> aClass) {
        return null;
    }

    @Override
    public FindIterable<Document> find() {
        return null;
    }

    @Override
    public <TResult> FindIterable<TResult> find(Class<TResult> aClass) {
        return null;
    }

    public FindIterable<Document> find(Bson query, Bson projection) {
        try {
            QueryTranslator qt = new QueryTranslator(columnWithJson);
            QueryTranslator.QueryResult result = qt.find(tableName, (Document) query, (Document) projection);
            PreparedStatement ps = connection.prepareStatement(result.getQuery());
            List<Object> param = result.getParameters();

            for (int i = 1; i <= param.size(); i++) {
                ps.setString(i, param.get(i - 1).toString());
            }

            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            ArrayList<Document> arr = new ArrayList<>();

            int cnt = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 0; i < cnt; i++) {
                    Document tmp = new Document();
                    tmp.put(metaData.getColumnLabel(i + 1), resultSet.getString(i + 1));
                    arr.add(tmp);
                }
            }

            Iterator it = arr.iterator();
            return new FindIterableStub<Document>() {
                @Override
                public MongoCursor<Document> iterator() {
                    return new MongoCursorStub<Document>() {
                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Document next() {
                            return (Document) it.next();
                        }
                    };
                }

                @Override
                public Document first() {
                    if (it.hasNext()) {
                        return (Document) it.next();
                    } else {
                        throw new ArrayIndexOutOfBoundsException();
                    }
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FindIterable<Document> find(Bson bson) {
        return find(bson, new Document());
    }

    @Override
    public <TResult> FindIterable<TResult> find(Bson bson, Class<TResult> aClass) {
        return null;
    }

    @Override
    public AggregateIterable<Document> aggregate(List<? extends Bson> list) {
        return null;
    }

    @Override
    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> list, Class<TResult> aClass) {
        return null;
    }

    @Override
    public MapReduceIterable<Document> mapReduce(String s, String s1) {
        return null;
    }

    @Override
    public <TResult> MapReduceIterable<TResult> mapReduce(String s, String s1, Class<TResult> aClass) {
        return null;
    }

    @Override
    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> list) {
        return null;
    }

    @Override
    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> list, BulkWriteOptions bulkWriteOptions) {
        return null;
    }

    @Override
    public void insertOne(Document Document) {
        ArrayList<Document> tmpArr = new ArrayList<>();
        tmpArr.add(Document);
        insertMany(tmpArr);
    }

    @Override
    public void insertMany(List<? extends Document> list) {
        QueryTranslator qt = new QueryTranslator(columnWithJson);
        try {
            QueryTranslator.QueryResult result = qt.insert(tableName, (List<Document>) list);
            PreparedStatement ps = connection.prepareStatement(result.getQuery());
            List<Object> param = result.getParameters();

            for (int i = 1; i <= param.size(); i++) {
                ps.setString(i, param.get(i - 1).toString());
            }

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertMany(List<? extends Document> list, InsertManyOptions insertManyOptions) {

    }

    @Override
    public DeleteResult deleteOne(Bson bson) {
        return null;
    }

    @Override
    public DeleteResult deleteMany(Bson bson) {
        QueryTranslator qt = new QueryTranslator(columnWithJson);
        try {
            QueryTranslator.QueryResult result = qt.delete(tableName, (Document) bson, 0);
            PreparedStatement ps = connection.prepareStatement(result.getQuery());
            List<Object> param = result.getParameters();

            for (int i = 1; i <= param.size(); i++) {
                ps.setString(i, "\"" + param.get(i - 1).toString() + "\"");
            }

            int res = ps.executeUpdate();

            return new DeleteResult() {
                @Override
                public boolean wasAcknowledged() {
                    return false;
                }

                @Override
                public long getDeletedCount() {
                    return res;
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UpdateResult replaceOne(Bson bson, Document Document) {
        return null;
    }

    @Override
    public UpdateResult replaceOne(Bson bson, Document Document, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateOne(Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public UpdateResult updateOne(Bson bson, Bson bson1, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public UpdateResult updateMany(Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public UpdateResult updateMany(Bson bson, Bson bson1, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public Document findOneAndDelete(Bson bson) {
        return null;
    }

    @Override
    public Document findOneAndDelete(Bson bson, FindOneAndDeleteOptions findOneAndDeleteOptions) {
        return null;
    }

    @Override
    public Document findOneAndReplace(Bson bson, Document Document) {
        return null;
    }

    @Override
    public Document findOneAndReplace(Bson bson, Document Document, FindOneAndReplaceOptions findOneAndReplaceOptions) {
        return null;
    }

    @Override
    public Document findOneAndUpdate(Bson bson, Bson bson1) {
        return null;
    }

    @Override
    public Document findOneAndUpdate(Bson bson, Bson bson1, FindOneAndUpdateOptions findOneAndUpdateOptions) {
        return null;
    }

    @Override
    public void drop() {

    }

    @Override
    public String createIndex(Bson bson) {
        return null;
    }

    @Override
    public String createIndex(Bson bson, IndexOptions indexOptions) {
        return null;
    }

    @Override
    public List<String> createIndexes(List<IndexModel> list) {
        return null;
    }

    @Override
    public ListIndexesIterable<Document> listIndexes() {
        return null;
    }

    @Override
    public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> aClass) {
        return null;
    }

    @Override
    public void dropIndex(String s) {

    }

    @Override
    public void dropIndex(Bson bson) {

    }

    @Override
    public void dropIndexes() {

    }

    @Override
    public void renameCollection(MongoNamespace mongoNamespace) {

    }

    @Override
    public void renameCollection(MongoNamespace mongoNamespace, RenameCollectionOptions renameCollectionOptions) {

    }
}

package pgmongo;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.sql.Connection;

public class PgMongoDatabase implements MongoDatabase {

    private Connection connection;
    private String columnWithJson;

    public PgMongoDatabase(Connection connection, String columnWithJson) {
        this.connection = connection;
        this.columnWithJson = columnWithJson;
    }

    @Override
    public String getName() {
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
    public MongoDatabase withCodecRegistry(CodecRegistry codecRegistry) {
        return null;
    }

    @Override
    public MongoDatabase withReadPreference(ReadPreference readPreference) {
        return null;
    }

    @Override
    public MongoDatabase withWriteConcern(WriteConcern writeConcern) {
        return null;
    }

    @Override
    public MongoCollection<Document> getCollection(String tableName) {
        return new PgMongoCollection(connection, tableName, columnWithJson);
    }

    @Override
    public <TDocument> MongoCollection<TDocument> getCollection(String s, Class<TDocument> aClass) {
        return null;
    }

    @Override
    public Document runCommand(Bson bson) {
        return null;
    }

    @Override
    public Document runCommand(Bson bson, ReadPreference readPreference) {
        return null;
    }

    @Override
    public <TResult> TResult runCommand(Bson bson, Class<TResult> aClass) {
        return null;
    }

    @Override
    public <TResult> TResult runCommand(Bson bson, ReadPreference readPreference, Class<TResult> aClass) {
        return null;
    }

    @Override
    public void drop() {

    }

    @Override
    public MongoIterable<String> listCollectionNames() {
        return null;
    }

    @Override
    public ListCollectionsIterable<Document> listCollections() {
        return null;
    }

    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(Class<TResult> aClass) {
        return null;
    }

    @Override
    public void createCollection(String s) {

    }

    @Override
    public void createCollection(String s, CreateCollectionOptions createCollectionOptions) {

    }
}

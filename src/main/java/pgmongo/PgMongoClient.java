package pgmongo;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class PgMongoClient<TDocument> extends MongoClient {
    private Connection connection = null;

    public PgMongoClient(String url, String user, String password) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public MongoDatabase getDatabase(String databaseName) {
        return new PgMongoDatabase(connection);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PgMongoClient(String host) {
        super(host);
    }

    public PgMongoClient(String host, MongoClientOptions options) {
        super(host, options);
    }

    public PgMongoClient(String host, int port) {
        super(host, port);
    }

    public PgMongoClient(ServerAddress addr) {
        super(addr);
    }

    public PgMongoClient(ServerAddress addr, List<MongoCredential> credentialsList) {
        super(addr, credentialsList);
    }

    public PgMongoClient(ServerAddress addr, MongoClientOptions options) {
        super(addr, options);
    }

    public PgMongoClient(ServerAddress addr, List<MongoCredential> credentialsList, MongoClientOptions options) {
        super(addr, credentialsList, options);
    }

    public PgMongoClient(List<ServerAddress> seeds) {
        super(seeds);
    }

    public PgMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList) {
        super(seeds, credentialsList);
    }

    public PgMongoClient(List<ServerAddress> seeds, MongoClientOptions options) {
        super(seeds, options);
    }

    public PgMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList, MongoClientOptions options) {
        super(seeds, credentialsList, options);
    }

    public PgMongoClient(MongoClientURI uri) {
        super(uri);
    }
}

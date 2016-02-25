package org.pgmongo;

import com.mongodb.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class PgMongoClient extends MongoClient {
    private Connection connection = null;
    private boolean debug;

    public PgMongoClient(String url, String user, String password, boolean debug) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        this.connection = DriverManager.getConnection(url, user, password);
        this.debug = debug;
    }

    @Override
    public PgMongoDatabase getDatabase(String databaseName) {
        return new PgMongoDatabase(connection, debug);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

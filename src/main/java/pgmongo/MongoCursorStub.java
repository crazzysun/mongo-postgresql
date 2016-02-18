package pgmongo;

import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.MongoCursor;

/**
 * Created by student on 2/18/16.
 */
public class MongoCursorStub<T> implements MongoCursor<T> {
    @Override
    public void close() {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        return null;
    }

    @Override
    public T tryNext() {
        return null;
    }

    @Override
    public ServerCursor getServerCursor() {
        return null;
    }

    @Override
    public ServerAddress getServerAddress() {
        return null;
    }
}

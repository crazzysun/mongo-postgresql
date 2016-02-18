package pgmongo;

import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by student on 2/18/16.
 */
public abstract class FindIterableStub<T> implements FindIterable<T> {

    @Override
    public FindIterable<T> filter(Bson bson) {
        return null;
    }

    @Override
    public FindIterable<T> limit(int i) {
        return null;
    }

    @Override
    public FindIterable<T> skip(int i) {
        return null;
    }

    @Override
    public FindIterable<T> maxTime(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public FindIterable<T> modifiers(Bson bson) {
        return null;
    }

    @Override
    public FindIterable<T> projection(Bson bson) {
       return null;
    }

    @Override
    public FindIterable<T> sort(Bson bson) {
        return null;
    }

    @Override
    public FindIterable<T> noCursorTimeout(boolean b) {
        return null;
    }

    @Override
    public FindIterable<T> oplogReplay(boolean b) {
        return null;
    }

    @Override
    public FindIterable<T> partial(boolean b) {
        return null;
    }

    @Override
    public FindIterable<T> cursorType(CursorType cursorType) {
        return null;
    }

    @Override
    public FindIterable<T> batchSize(int i) {
        return null;
    }

    @Override
    public MongoCursor<T> iterator() {
        return null;
    }

    @Override
    public T first() {
        return null;
    }

    @Override
    public <U> MongoIterable<U> map(Function<T, U> function) {
        return null;
    }

    @Override
    public void forEach(Block<? super T> block) {

    }

    @Override
    public <A extends Collection<? super T>> A into(A objects) {
        return null;
    }

    @Override
    public void forEach(Consumer<? super T> action) {

    }

    @Override
    public Spliterator<T> spliterator() {
        return null;
    }
}

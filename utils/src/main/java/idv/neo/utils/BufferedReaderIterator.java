package idv.neo.utils;

/**
 * Created by Neo on 2017/4/18.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * http://stackoverflow.com/questions/4677411/iterating-over-the-content-of-a-text-file-line-by-line-is-there-a-best-practic
 * Created by Neo on 2017/4/16.
 */

public class BufferedReaderIterator implements Iterable<String> {
    private static final String TAG = "BufferedReaderIterator";
    private BufferedReader r;

    public BufferedReaderIterator(BufferedReader r) {
        this.r = r;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                try {
                    r.mark(1);
                    if (r.read() < 0) {
                        return false;
                    }
                    r.reset();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public String next() {
                try {
                    return r.readLine();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

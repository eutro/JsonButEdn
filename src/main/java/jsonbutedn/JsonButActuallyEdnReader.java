package jsonbutedn;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JsonButActuallyEdnReader {

    private static final IFn EDN_TO_JSON = Clojure.var("jsonbutedn.core", "edn->json");

    @SuppressWarnings("unused") // ASM-ed
    public static Reader wrap(Reader in) {
        String s;
        try {
            s = IOUtils.toString(in);
        } catch (IOException e) {
            return new Reader() {
                private boolean thrown = false;

                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    throw e;
                }

                @Override
                public void close() {
                }
            };
        } catch (Throwable t) { // AssertionError from JsonTree etc.
            return in;
        }
        Object obj;
        try {
            obj = Clojure.read(s);
        } catch (RuntimeException e) {
            return new StringReader(s);
        }
        return new StringReader(EDN_TO_JSON.invoke(obj).toString());
    }

}

package cloud;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import java.io.IOException;
import java.io.InputStream;

public class Parser {
    public static String extractContentUsingParser(InputStream stream) {

        Tika tika = new Tika();
        String content = null;
        try {
            content = tika.parseToString(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
        return content;
    }
}

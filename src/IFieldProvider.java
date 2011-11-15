
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author gsmir
 */
public interface IFieldProvider {
    
    GameInfo init(InputStream in) throws IOException;
    IField getNextFieldState() throws IOException;
    
    boolean gameOver();
}

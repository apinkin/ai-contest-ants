
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Here is a collection of things that check if the behavior of one library or
 * another is really what I assume it to be. Just to stay on the safe side.
 */
public class TestUtils {
    @Test
    public void testRemoveFirst() {
        List<String> strings = new LinkedList<String>(Arrays.asList("first", "second"));
        Utils.removeFirst(strings);
        
        Assert.assertEquals(Arrays.asList("second"), strings);
        
    }
}

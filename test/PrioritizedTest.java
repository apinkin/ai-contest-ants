
import java.util.PriorityQueue;
import java.util.Queue;
import junit.framework.Assert;
import org.junit.Test;


public class PrioritizedTest {
    @Test
    public void testWithPriorityQueue() {
        Queue<Prioritized<Double, String>> queue = new PriorityQueue<Prioritized<Double, String>>();

        queue.add(new Prioritized<Double, String>(10.0, "last"));
        queue.add(new Prioritized<Double, String>(5.0, "second"));
        queue.add(new Prioritized<Double, String>(0.0, "first"));

        Assert.assertEquals("first", queue.poll().value);
        Assert.assertEquals("second", queue.poll().value);
        Assert.assertEquals("last", queue.poll().value);
    }
}

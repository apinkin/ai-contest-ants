
import junit.framework.Assert;
import org.junit.Test;

public class DirectionTest {
    @Test
    public void testCompute() {
        Cell from = new Cell(0, 0);
        Cell to = new Cell(0, 1);
        Assert.assertEquals(Direction.EAST, Direction.compute(from, to));
        
        from = new Cell(0, 0);
        to = new Cell(0, -1);
        Assert.assertEquals(Direction.WEST, Direction.compute(from, to));
        
        from = new Cell(10, 10);
        to = new Cell(9, 10);
        Assert.assertEquals(Direction.NORTH, Direction.compute(from, to));
        
        from = new Cell(100, 100);
        to = new Cell(101, 100);
        Assert.assertEquals(Direction.SOUTH, Direction.compute(from, to));
        
        from = new Cell(1234, 100);
        to = new Cell(101, 100);
        Assert.assertNull(Direction.compute(from, to));
    }
}

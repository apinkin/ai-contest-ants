
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class FieldTest {

    IMutableField field;

    // The field is like that:

    // 5: ..WWW
    // 4: ..W.W
    // 3: ..WWW
    // 2: ..W.W
    // 1: ..W.W
    // 0: ..W..
    //    01234
    @Before
    public void setUp() {
        GameInfo info = new GameInfo(0, 0, 6, 5, 0, 0, 0, 0, 42);
        field = new Field(info);

        field.setCell(0, 2, Cell.Type.WATER);
        field.setCell(1, 2, Cell.Type.WATER);
        field.setCell(2, 2, Cell.Type.WATER);
        field.setCell(3, 2, Cell.Type.WATER);
        field.setCell(4, 2, Cell.Type.WATER);
        field.setCell(5, 2, Cell.Type.WATER);

        field.setCell(1, 4, Cell.Type.WATER);
        field.setCell(2, 4, Cell.Type.WATER);
        field.setCell(3, 4, Cell.Type.WATER);
        field.setCell(4, 4, Cell.Type.WATER);
        field.setCell(5, 4, Cell.Type.WATER);

        field.setCell(3, 3, Cell.Type.WATER);
        field.setCell(5, 3, Cell.Type.WATER);
    }

    @Test
    @Ignore
    public void testSameCell() {
        Cell from = new Cell(0, 0);
        Cell to = new Cell(0, 0);

        List<PathNode> path = field.getPathFinder().computePath(from, to);

        Assert.assertEquals(Arrays.asList(new PathNode(to, null)), path);
    }

    @Test
    @Ignore
    public void testAdjacentCells() {
        Cell from = new Cell(0, 0);
        Cell to = new Cell(0, 1);

        List<PathNode> path = field.getPathFinder().computePath(from, to);

        Assert.assertEquals(Arrays.asList(
                new PathNode(from, Direction.EAST),
                new PathNode(to, null)
              ), path);
    }

    @Test
    @Ignore
    public void testWrapping() {
        Cell from = new Cell(0, 0);
        Cell to = new Cell(4, 0);

        List<PathNode> path = field.getPathFinder().computePath(from, to);

        Assert.assertEquals(Arrays.asList(
                new PathNode(from, Direction.NORTH),
                new PathNode(new Cell(5, 0), Direction.NORTH),
                new PathNode(to, null)), path);
    }

    @Test
    @Ignore
    public void testWithObstacles() {
        Cell from = new Cell(0, 0);
        Cell to = new Cell(2, 3);

        List<PathNode> path = field.getPathFinder().computePath(from, to);

        Assert.assertEquals(
                Arrays.asList(
                    new PathNode(from, Direction.WEST),
                    new PathNode(new Cell(0, 4), Direction.WEST),
                    new PathNode(new Cell(0, 3), Direction.SOUTH),
                    new PathNode(new Cell(1, 3), Direction.SOUTH),
                    new PathNode(to, null)
                ),path);
    }

    @Test
    @Ignore
    public void testWithNoPath() {
        Cell from = new Cell(0, 0);
        Cell to = new Cell(3, 4);

        List<PathNode> path = field.getPathFinder().computePath(from, to);

        Assert.assertTrue(path.isEmpty());
    }

    @Test
    public void testGetDistance() {
        Assert.assertEquals(0, field.getDistance(new Cell(0, 0), new Cell(0, 0)));

        Assert.assertEquals(1, field.getDistance(new Cell(0, 0), new Cell(1, 0)));
        Assert.assertEquals(1, field.getDistance(new Cell(0, 0), new Cell(0, 1)));

        Assert.assertEquals(1, field.getDistance(new Cell(0, 0), new Cell(5, 0)));
        Assert.assertEquals(1, field.getDistance(new Cell(0, 0), new Cell(0, 4)));

        Assert.assertEquals(2, field.getDistance(new Cell(0, 0), new Cell(1, 1)));
        Assert.assertEquals(2, field.getDistance(new Cell(0, 0), new Cell(5, 1)));

        Assert.assertEquals(2, field.getDistance(new Cell(0, 0), new Cell(1, 4)));
        Assert.assertEquals(2, field.getDistance(new Cell(0, 0), new Cell(5, 4)));

        Assert.assertEquals(8, field.getDistance(new Cell(0, 0), new Cell(2, 2)));
    }
}

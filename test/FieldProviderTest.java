
import java.io.IOException;
import java.io.FileInputStream;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class FieldProviderTest {
    
    FieldProvider instance;
    GameInfo info;
    
    @Before
    public void setUp() throws IOException {
        instance = new FieldProvider();
        info = instance.init(new FileInputStream("test/data/small.test.in"));
    }
    
    @Test
    public void testInit() throws Exception {
        GameInfo expResult = new GameInfo(3000, 1000, 20, 20, 500, 55, 5, 1, 42);
        
        Assert.assertEquals(expResult, info);
    }

    @Test
    public void testGetField() throws Exception {
        Field expResult = new Field(info);
        
        expResult.setCell(6, 5, Cell.Type.FOOD);
        expResult.setCell(7, 6, Cell.Type.WATER);
        
        expResult.setCell(7, 9, Cell.Type.ANT, 1);
        expResult.setCell(10, 8, Cell.Type.ANT, 0);
        expResult.setCell(10, 9, Cell.Type.ANT, 0);
        
        IField result = instance.getNextFieldState();
        
        Assert.assertEquals(expResult, result);
    }
}

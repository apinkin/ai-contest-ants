
import java.io.OutputStream;
import java.io.PrintWriter;


public class OrderManager implements IOrderManager{
    
    private static final String GO = "go";
    private final PrintWriter out;
    
    public OrderManager(GameInfo info, OutputStream out) {
        this.out = new PrintWriter(out);
    }

    @Override
    public boolean issueOrder(Order order) {
        out.println(order);
        return true;
    }
    
    public void onTurnEnded() {
        out.println(GO);
        out.flush();
    }

    void updateField(IField field) {
        //nothing for now
    }
    
}

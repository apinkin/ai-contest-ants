
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public abstract class AbstractHiveMind implements IHiveMind {

    public static final long PANIC_TIME = 100L;
    private int turnNumber;
    private long turnStart;
    private IField lastField;

    protected void log(Object message) {
        Utils.log(getTurnNumber() + "[" + Utils.toc(turnStart) + "/" + info.turnTime + "]: " + message);
    }

    public static class TimingOutException extends RuntimeException {

        private TimingOutException(long remaining) {
            super("would time out in " + remaining + ", stopping bot and commiting orders");
        }
    }

    protected GameInfo info;
    protected Random random;

    private IOrderManager orderManager;


    @Override
    public final void init(GameInfo info, IOrderManager orderManager) {
        this.info = info;
        this.orderManager = orderManager;
        this.random = new Random(info.seed);
        init();
    }

    protected void init() {}

    public boolean issueOrder(Order order) {
        //Utils.log("order: " + order);
        return orderManager.issueOrder(order);
    }

    public boolean issueOrder(int fromRow, int fromCol, Direction direction) {
        return issueOrder(new Order(fromRow, fromCol, direction));
    }

    private Set<Cell> targetCells = new HashSet<Cell>();

    public boolean issueOrder(Cell from, Direction direction) {
        Cell target = lastField.getDestination(from, direction);
        if(targetCells.contains(target)) {
            return false;
        } else {
            targetCells.add(target);
            return issueOrder(from.row, from.col, direction);
        }

    }


    @Override
    public void onNewTurn(IField field) {
        this.lastField = field;
        this.turnNumber = field.getTurnNumber();
        this.turnStart = Utils.tic();

        targetCells.clear();

        try {
            doTurn(field);
        } catch(TimingOutException e) {
            log(e.getMessage());
        }
    }

    protected abstract void doTurn(IField field);

    protected int getTurnNumber() {
        return turnNumber;
    }

    protected void saveFromTimingOut() {
        long remaining = info.turnTime - Utils.toc(turnStart);
        if(remaining < PANIC_TIME) {
            throw new TimingOutException(remaining);
        }
    }

}

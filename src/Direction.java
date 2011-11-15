import java.util.Random;

public enum Direction {
    NORTH(-1, 0, 'n'),
    EAST(0, 1, 'e'),
    SOUTH(1, 0, 's'),
    WEST(0, -1, 'w');

    private final int rowDelta;
    private final int colDelta;
    private final char symbol;
    
    private Direction(int rowDelta, int colDelta, char symbol) {
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
        this.symbol = symbol;
    }
    public int getRowDelta() {
        return rowDelta;
    }

    public int getColDelta() {
        return colDelta;
    }

    public char getSymbol() {
        return symbol;
    }
    
    private short code() {
        return (short) (rowDelta + 1 + (colDelta + 1) * 3);
    } 
    
    private static final Direction[] coded;

    private static final Random rand = new Random();
    
    static {
        coded = new Direction[9];
        for(Direction direction : values()) {
            coded[direction.code()] = direction;
        }
    }
    
    public static Direction compute(Cell from, Cell to) {
        int rowDelta = to.row - from.row;
        int colDelta = to.col - from.col;
        short code = (short) (rowDelta + 1 + (colDelta + 1) * 3);
        if(code < 0 || code >= coded.length) {
            return null;
        } else {
            return coded[code];
        }
    }

    public static Direction getRandom() {
        int rv = rand.nextInt(4);
        if (rv == 0) {
            return NORTH;
        }
        else if (rv == 1) {
            return EAST;
        }
        else if (rv == 2) {
            return SOUTH;
        }
        else
            return WEST;
    }
}

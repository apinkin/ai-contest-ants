public class Order {
    private final int row;
    
    private final int col;
    
    private final char direction;
    
    public Order(Cell cell, Direction direction) {
        this(cell.row, cell.col, direction);
    }
    
    public Order(int row, int col, Direction direction) {
        if(direction == null) {
            throw new NullPointerException("direction cannot be null");
        }
        this.row = row;
        this.col = col;
        this.direction = direction.getSymbol();
    }
    
    @Override
    public String toString() {
        return "o " + row + " " + col + " " + direction;
    }
}


public class Cell implements Comparable<Cell> {

    public static enum Type {
        WATER, FOOD, LAND, DEAD, ANT, HILL;

        public boolean isPassable() {
            return this == LAND || this == DEAD || this == HILL || this == FOOD;
        }
    }

    public final int row;
    public final int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public static Cell of(int row, int col) {
        return new Cell(row, col);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cell other = (Cell) obj;
        if (this.row != other.row) {
            return false;
        }
        if (this.col != other.col) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.row;
        hash = 67 * hash + this.col;
        return hash;
    }

    @Override
    public int compareTo(Cell other) {
        if (row > other.row) {
            return 1;
        } else if (row < other.row) {
            return -1;
        } else {
            if (col > other.col) {
                return 1;
            } else if (col < other.col) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }

}

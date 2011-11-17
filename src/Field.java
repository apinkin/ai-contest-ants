import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Field implements IMutableField {

    public final GameInfo info;

    private final Owned[][] map;
    private final Set<Cell> unexplored = new LinkedHashSet<Cell>();
    private final Map<Cell.Type, Map<Integer, Set<Cell>>> type2cells;
    private final IPathFinder pathFinder;

    {
        type2cells = new EnumMap<Cell.Type, Map<Integer, Set<Cell>>>(Cell.Type.class);
        for(Cell.Type type : Cell.Type.values()) {
            type2cells.put(type, new LinkedHashMap<Integer, Set<Cell>>());
        }
    }

    private int turnNumber = 0;

    public Field(GameInfo info) {
        map = new Owned[info.rows][info.cols];
        for(int row = 0; row < info.rows; row ++) {
            for(int col = 0; col < info.cols; col ++) {
                map[row][col] = new Owned(Cell.Type.LAND);
                unexplored.add(new Cell(row, col));
            }
        }

        this.info = info;
        this.pathFinder = new AStarPathFinder(this);
    }

    @Override
    public Owned get(int row, int col) {
        return map[row][col];
    }

    @Override
    public Owned get(Cell cell) {
        return get(cell.row, cell.col);
    }

    @Override
    public Cell getDestination(Cell from, Direction direction, int steps) {
        int row = (from.row + direction.getRowDelta() * steps) % info.rows;
        if (row < 0) {
            row += info.rows;
        }
        int col = (from.col + direction.getColDelta() * steps) % info.cols;
        if (col < 0) {
            col += info.cols;
        }
        return Cell.of(row, col);
    }

    @Override
    public Cell getDestination(Cell from, Direction direction) {
        return getDestination(from, direction, 1);
    }

    private void updateCache(Cell.Type type, int row, int col, int owner) {
        Cell.Type oldType = map[row][col].type;
        int oldOwner = map[row][col].owner;
        if(type != oldType || owner != oldOwner) {
            Map<Integer, Set<Cell>> mapping = this.getMappingFor(oldType);
            Set<Cell> cells = mapping.get(oldOwner);

            if(cells != null) {
                cells.remove(new Cell(row, col));
            }
        }


        Map<Integer, Set<Cell>> cells = this.type2cells.get(type);


        Set<Cell> ownedBy = cells.get(owner);
        if(ownedBy == null) {
            ownedBy = new LinkedHashSet<Cell>();
            cells.put(owner, ownedBy);
        }

        ownedBy.add(new Cell(row, col));
    }

    @Override
    public void setCell(int row, int col, Cell.Type type, int owner) {

        updateCache(type, row, col, owner);

        map[row][col].type = type;
        map[row][col].owner = owner;

    }

    @Override
    public void setCell(int row, int col, Cell.Type type) {
        setCell(row, col, type, map[row][col].owner);
    }

    @Override
    public void clear(Cell.Type... types) {
        for(Cell.Type type : types) {
            for(Set<Cell> cells : getMappingFor(type).values()) {
                for(Cell cell : cells) {
                    map[cell.row][cell.col].type = Cell.Type.LAND;
                    //Intentionally not changing the owner
                }
            }

            getMappingFor(type).clear();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Field other = (Field) obj;
        if (!Arrays.deepEquals(this.map, other.map)) {
            return false;
        }
        if (this.type2cells != other.type2cells && (this.type2cells == null || !this.type2cells.equals(other.type2cells))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Arrays.deepHashCode(this.map);
        hash = 13 * hash + (this.type2cells != null ? this.type2cells.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(int row = 0; row < info.rows; row ++) {
            for(int col = 0; col < info.cols; col ++) {
                builder.append(map[row][col].type.toString().charAt(0)).append(":").append(map[row][col].owner + "\t");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private Map<Integer, Set<Cell>> getMappingFor(Cell.Type type) {
        Map<Integer, Set<Cell>> map = this.type2cells.get(type);
        if(map == null) {
            map = new LinkedHashMap<Integer, Set<Cell>>();
            this.type2cells.put(type, map);
        }
        return map;
    }

    private Set<Cell> getCellsFor(Cell.Type type, int owner) {
        Set<Cell> cells = getMappingFor(type).get(owner);
        return cells == null ? Collections.<Cell>emptySet() : Collections.unmodifiableSet(cells);
    }

    @Override
    public Set<Cell> getMyAntPositions() {
        return getCellsFor(Cell.Type.ANT, Owned.OWNER_ME);
    }

    @Override
    public Set<Cell> getSeenFood() {
        return getCellsFor(Cell.Type.FOOD, Owned.OWNER_NOBODY);
    }

    @Override
    public void setTurnNumber(int turn) {
        this.turnNumber = turn;
    }

    @Override
    public int getTurnNumber() {
        return turnNumber;
    }

    @Override
    public Set<Cell> getEnemyHills() {
        Map<Integer, Set<Cell>> map = this.getMappingFor(Cell.Type.HILL);

        Set<Cell> result = new LinkedHashSet<Cell>();

        for(Entry<Integer, Set<Cell>> entry : map.entrySet()) {
            if(entry.getKey() != Owned.OWNER_ME && entry.getKey() != Owned.OWNER_NOBODY) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    @Override
    public Set<Cell> getMyHills() {
        Map<Integer, Set<Cell>> map = this.getMappingFor(Cell.Type.HILL);

        Set<Cell> result = new LinkedHashSet<Cell>();

        for(Entry<Integer, Set<Cell>> entry : map.entrySet()) {
            if(entry.getKey() == Owned.OWNER_ME) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    @Override
    public Set<Cell> getEnemyAnts() {
        Map<Integer, Set<Cell>> map = this.getMappingFor(Cell.Type.ANT);

        Set<Cell> result = new LinkedHashSet<Cell>();

        for(Entry<Integer, Set<Cell>> entry : map.entrySet()) {
            if(entry.getKey() != Owned.OWNER_ME && entry.getKey() != Owned.OWNER_NOBODY) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    @Override
    //works slowly in the beginning, but speeds up as more cells are explored
    public void updateExplored() {
        Iterator<Cell> iterator = unexplored.iterator();

        while(iterator.hasNext()) {
            Cell unexploredCell = iterator.next();
            if(isSeen(unexploredCell)) {
                iterator.remove();
                get(unexploredCell).explored = true;
            }
        }
    }

    @Override
    public Set<Cell> getUnexplored() {
        return Collections.unmodifiableSet(unexplored);
    }

    @Override
    public int cols() {
        return info.cols;
    }

    @Override
    public int rows() {
        return info.rows;
    }

    @Override
    public boolean isSeen(Cell cell) {
        for(Cell ant : getMyAntPositions()) {
            int distance = getDistance(cell, ant);
            if(distance <= info.viewRadiusSquared) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getDistance(Cell from, Cell to) {
        int rowDelta1 = Math.abs(to.row - from.row);
        int rowDelta2 = Math.abs((info.rows - to.row) + from.row);
        int rowDelta3 = Math.abs(to.row + (info.rows - from.row));

        int colDelta1 = Math.abs(to.col - from.col);
        int colDelta2 = Math.abs((info.cols - to.col) + from.col);
        int colDelta3 = Math.abs(to.col + (info.cols - from.col));

        int rowDelta = Utils.min(rowDelta1, rowDelta2, rowDelta3);
        int colDelta = Utils.min(colDelta1, colDelta2, colDelta3);

        return rowDelta * rowDelta + colDelta * colDelta;
    }

    @Override
    public IPathFinder getPathFinder() {
        //return pathFinder;
        return new AStarPathFinder(this);
    }

}

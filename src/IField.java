
import java.util.Set;

public interface IField  {

    int getTurnNumber();


    Owned get(int row, int col);
    Owned get(Cell cell);

    Cell getDestination(Cell from, Direction direction, int steps);
    Cell getDestination(Cell from, Direction direction);

    int getDistance(Cell from, Cell to);

    IPathFinder getPathFinder();

    Set<Cell> getEnemyHills();
    Set<Cell> getEnemyAnts();
    Set<Cell> getSeenFood();

    Set<Cell> getMyAntPositions();
    Set<Cell> getUnexplored();

    int cols();
    int rows();

    boolean isSeen(Cell cell);



}

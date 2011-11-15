import java.util.List;
import java.util.ArrayList;

public class PathFinder implements IPathFinder {

	private IField field;

	public PathFinder(IField field) {
		this.field = field;
	}	

	@Override
	public List<PathNode> computePath(Cell from, Cell to) {
        System.err.println("Finding path from " + from + " to " + to);
        PathNode pathSpots = new PathNode(from, Direction.EAST);
        List<PathNode> path = new ArrayList<PathNode>();
        path.add(pathSpots);
        return path;
	}
}

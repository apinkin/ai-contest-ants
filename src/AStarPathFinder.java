import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AStarPathFinder extends AStar<Cell> implements IPathFinder {

	private IField field;
    private Cell _goal;

	public AStarPathFinder(IField field) {
		this.field = field;
	}	

	@Override
	public List<PathNode> computePath(Cell from, Cell to) {
        setGoal(to);

        long begin = System.currentTimeMillis();
        List<Cell> nodes = compute(from);
        long end = System.currentTimeMillis();

        //System.err.println("Time = " + (end - begin) + " ms" );
        //System.err.println("Expanded = " + getExpandedCounter());
        if (nodes != null && !getCost().isInfinite())
            System.err.println("Path from " + from + " to " + to + "  cost : " + getCost());

//        if(nodes == null)
//            System.err.println("No path");
//        else{
//                System.err.print("Path = ");
//                for(Cell n : nodes)
//                        System.err.print(n);
//                System.err.println();
//        }

        if (nodes == null || getCost().isInfinite()) {
            return null;
        }

        Direction direction = Direction.compute(nodes.get(0), nodes.get(1));
        List<PathNode> path = new ArrayList<PathNode>();

        for (int i=0; i<(nodes.size() - 1); i++) {
            PathNode pathSpots = new PathNode(from, direction);
            path.add(pathSpots);
        }

        return path;
	}

    private void setGoal(Cell goal) {
        _goal = goal;
    }

    @Override
    protected boolean isGoal(Cell node) {
        return node.equals(_goal);
    }

    @Override
    protected Double g(Cell from, Cell to) {
        if (from.equals(to))
            return 0.0;

        //  && !field.getUnexplored().contains(to)
        if (field.get(to).type.isPassable() && !isMyHill(to) && !field.getUnexplored().contains(to))
            return 1.0;

        return Double.MAX_VALUE;
    }

    private boolean isMyHill(Cell cell) {
        Owned p = field.get(cell);
        return p.type == Cell.Type.HILL && p.isMine();
    }

    @Override
    protected Double h(Cell from, Cell to) {
        /* Use the Manhattan distance heuristic.  */
        return (double) Math.abs(_goal.row - to.row) + Math.abs(_goal.col - to.col);
    }

    @Override
    protected List<Cell> generateSuccessors(Cell node) {
        List<Cell> ret = new LinkedList<Cell>();
        for (Direction direction : Direction.values()) {
            Cell dest = field.getDestination(node, direction);
            if (field.get(dest).type.isPassable()) {
                ret.add(dest);
            }
        }

        return ret;
    }
}

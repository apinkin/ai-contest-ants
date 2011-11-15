import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CustomBot extends AbstractHiveMind {

    @Override
    protected void doTurn(IField field) {
        //System.err.println("Current turn: " + field.getTurnNumber());

        Set<Cell> ants = field.getMyAntPositions();
        Set<Cell> foods = field.getSeenFood();

        System.err.println("food phase...");

        Map<Cell, Cell> foodTargets = new HashMap<Cell, Cell>();
        Map<Cell, Direction> foodDirections = new HashMap<Cell, Direction>();
        Set<Cell> orderedAnts = new HashSet<Cell>();

        List<Route> foodRoutes = new ArrayList<Route>();
        TreeSet<Cell> sortedFood = new TreeSet<Cell>(foods);
        TreeSet<Cell> sortedAnts = new TreeSet<Cell>(ants);
        for (Cell foodLoc : sortedFood) {
            for (Cell antLoc : sortedAnts) {
                List<PathNode> path = field.getPathFinder().computePath(antLoc, foodLoc);
                if (path != null) {
                    int distance = path.size();
                    Route route = new Route(antLoc, foodLoc, distance, path.get(0).direction);
                    foodRoutes.add(route);
                }
            }
        }

        Collections.sort(foodRoutes);

        for (Route route : foodRoutes) {
            if (!foodTargets.containsKey(route.getEnd())
                    && !foodTargets.containsValue(route.getStart())) {
                foodTargets.put(route.getEnd(), route.getStart());
                foodDirections.put(route.getStart(), route.getDirection());
            }
        }

        for (Cell myAnt : foodTargets.values()) {
            Direction direction = foodDirections.get(myAnt);
            if (direction == null) {
                continue;
            }
            Cell dest = field.getDestination(myAnt, direction);
            if (field.get(dest).type.isPassable() && !isMyHill(field, dest)) {
                issueOrder(myAnt, direction);
                orderedAnts.add(myAnt);
            }
        }

        // attack hills
        int remainingAnts = ants.size() - orderedAnts.size();
        if (remainingAnts <= 0) {
            return;
        }
        if (remainingAnts > (field.getEnemyAnts().size() * 3) && remainingAnts >= 100) {
            System.err.println("attack phase... remaining ants: " + remainingAnts);
            List<Route> hillRoutes = new ArrayList<Route>();
            for (Cell hillLoc : field.getEnemyHills()) {
                for (Cell antLoc : sortedAnts) {
                    if (!orderedAnts.contains(antLoc)) {
                        int distance = Math.abs(hillLoc.row - antLoc.row) + Math.abs(hillLoc.col - antLoc.col);
                        Route route = new Route(antLoc, hillLoc, distance, null);
                        hillRoutes.add(route);
                    }
                }
            }
            Collections.sort(hillRoutes);
            for (Route route : hillRoutes) {
                List<PathNode> path = field.getPathFinder().computePath(route.getStart(), route.getEnd());
                if (path != null) {
                    issueOrder(route.getStart(), path.get(0).direction);
                    orderedAnts.add(route.getStart());
                    remainingAnts--;
                    if (remainingAnts <= 0) {
                        break;
                    }
                }
            }
        }

        // let's explore now
        remainingAnts = ants.size() - orderedAnts.size();
        if (remainingAnts <= 0) {
            return;
        }
        System.err.println("exploration phase... remaining ants: " + remainingAnts);
        List<Route> unseenRoutes = new ArrayList<Route>();
        for (Cell antLoc : ants) {
            if (!orderedAnts.contains(antLoc)) {
                for (Cell unseenLoc : field.getUnexplored()) {
                    int distance = Math.abs(unseenLoc.row - antLoc.row) + Math.abs(unseenLoc.col - antLoc.col);
                    Route route = new Route(antLoc, unseenLoc, distance, null);
                    unseenRoutes.add(route);
                }
            }
        }
        Collections.sort(unseenRoutes);
        int max_iterations = remainingAnts;
        int iter_count = 0;
        for (Route route : unseenRoutes) {
            List<PathNode> path = field.getPathFinder().computePath(route.getStart(), route.getEnd());
            if (path != null && path.get(0).direction != null) {
                issueOrder(route.getStart(), path.get(0).direction);
                orderedAnts.add(route.getStart());
                remainingAnts--;
            }
            iter_count++;
            if (remainingAnts <= 0 || iter_count >= max_iterations) {
                break;
            }
        }

        // if an ant still has nothing to do, just make a random move
        remainingAnts = ants.size() - orderedAnts.size();
        if (remainingAnts <= 0) {
            return;
        }
        System.err.println("random phase...");
        for (Cell ant : ants) {
            if (!orderedAnts.contains(ant)) {
                if (antHasOptions(field, ant)) {
                    Direction direction = getRandomDirection(field, ant);
                    if (direction != null) {
                        issueOrder(ant, direction);
                        orderedAnts.add(ant);
                    }
                }
            }
        }

    }

    private Direction getRandomDirection(IField field, Cell ant) {
        boolean passable = false;
        while (!passable) {
            Direction direction = Direction.getRandom();
            Cell dest = field.getDestination(ant, direction);
            if (field.get(dest).type.isPassable() && !isMyHill(field, dest)) {
                return direction;
            }
            passable = true;
        }
        return null;
    }

    private boolean isPassable(IField field, Cell cell) {
        return field.get(cell).type.isPassable() && !isMyHill(field, cell);

    }
    private boolean antHasOptions(IField field, Cell ant) {
        return isPassable(field, field.getDestination(ant, Direction.WEST)) || isPassable(field, field.getDestination(ant, Direction.NORTH))
                || isPassable(field, field.getDestination(ant, Direction.EAST)) || isPassable(field, field.getDestination(ant, Direction.SOUTH));
    }

    private boolean isMyHill(IField field, Cell cell) {
        Owned p = field.get(cell);
        return p.type == Cell.Type.HILL && p.isMine();
    }

}

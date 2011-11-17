import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CustomBot extends AbstractHiveMind {
    
    private static boolean LOGGING_ENABLED = true;

    private void log(String s) {
        if (LOGGING_ENABLED)
            System.err.println(s);
    }
    
    @Override
    protected void doTurn(IField field) {
        Utils.tic();
        //log("Current turn: " + field.getTurnNumber());

        //Set<Cell> myAnts = field.getMyAntPositions();
        //Set<Cell> orderedAnts = new HashSet<Cell>();

        //collectFood(field, myAnts, orderedAnts);

        diffuse(field);

        //attack(field, myAnts, orderedAnts);

        //explore(field, myAnts, orderedAnts);

        //randomMoves(field, myAnts, orderedAnts);

        log("Turn: " + field.getTurnNumber() + "    Time: " + Utils.toc());
    }

    private void diffuse(IField field) {
        double[][] diffExp = new double[info.rows][info.cols];
        boolean[][] diffusedExp = new boolean[info.rows][info.cols];
        Set<Cell> myHills = field.getMyHills();

        // seed the influence map
        for(int row = 0; row < info.rows; row ++) {
            for(int col = 0; col < info.cols; col ++) {
                Cell cell = new Cell(row, col);
                Owned o = field.get(row, col);
                if (o.type.equals(Cell.Type.FOOD) || !o.explored  || (o.type.equals(Cell.Type.HILL) && o.isEnemys())) {
                    diffExp[row][col] = Integer.MAX_VALUE;
                    diffusedExp[row][col] = true;
                }
                if (o.type.equals(Cell.Type.ANT) && o.isEnemys() && isCloseToMyHill(field, myHills, cell)) {
                    diffExp[row][col] = Integer.MAX_VALUE;
                    diffusedExp[row][col] = true;
                }
                if (o.type.equals(Cell.Type.WATER) || (o.type.equals(Cell.Type.HILL) && o.isMine())) {
                    diffExp[row][col] = 0;
                    diffusedExp[row][col] = true;
                }
            }
        }

        // iterate to diffuse the influence
        int iterations = 100;
        for (int i=0; i<iterations; i++) {
            for(int row = 0; row < info.rows; row ++) {
                for(int col = 0; col < info.cols; col ++) {
                    if (diffusedExp[row][col])
                        continue;
                    double up = diffExp[get_dest(row, -1, info.rows)][col];
                    double down = diffExp[get_dest(row, 1, info.rows)][col];
                    double left = diffExp[row][get_dest(col, -1, info.cols)];
                    double right = diffExp[row][get_dest(col, 1, info.cols)];

                    double divider = 4.0;
                    diffExp[row][col] = up/divider + down/divider + left/divider + right/divider;
                }
            }
        }
        //print(diffExp);

        // now make decisions where free ants can go
        for (Cell antLoc : field.getMyAntPositions()) {
            Direction direction = getDirectionHighestDiff(field, antLoc, diffExp);
            if (direction != null) {
                issueOrder(antLoc, direction);
                log("Sent ant: " + antLoc + "  to " + direction);
            }
            else {
                log("Ant stuck: " + antLoc);
            }
        }
    }

    private boolean isCloseToMyHill(IField field, Set<Cell> myHills, Cell cell) {
        if (myHills.size() == 0)
            return false;
        int min_distance = Integer.MAX_VALUE;
        for (Cell hill : myHills) {
            int distance = field.getDistance(cell, hill);
            min_distance = Math.min(min_distance, distance);
        }

        return (min_distance <= info.viewRadiusSquared);
    }

    private Direction getDirectionHighestDiff(IField field, Cell cell, double[][] diffExp) {
        Cell north = field.getDestination(cell, Direction.NORTH);
        Cell south = field.getDestination(cell, Direction.SOUTH);
        Cell west = field.getDestination(cell, Direction.WEST);
        Cell east = field.getDestination(cell, Direction.EAST);

        double diffNorth = field.get(north).type.isPassable() ? diffExp[north.row][north.col] : 0;
        double diffSouth = field.get(south).type.isPassable() ? diffExp[south.row][south.col] : 0;
        double diffWest = field.get(west).type.isPassable() ? diffExp[west.row][west.col] : 0;
        double diffEast = field.get(east).type.isPassable() ? diffExp[east.row][east.col] : 0;

        double maxDiff = Math.max(Math.max(Math.max(diffNorth, diffSouth), diffWest), diffEast);

        //log("Diffusion for " + cell + ":  " + diffNorth + " " + diffEast + " " + diffSouth + " " + diffWest);
        if (maxDiff == 0.0) {
            return null;
        }
        if (diffNorth == maxDiff) {
            return Direction.NORTH;
        }
        else if (diffSouth == maxDiff) {
            return Direction.SOUTH;
        }
        else if (diffWest == maxDiff) {
            return Direction.WEST;
        }
        return Direction.EAST;
    }

    private void print(int[][] diffExp) {
        for(int row = 0; row < diffExp.length; row ++) {
            for(int col = 0; col < diffExp[0].length; col ++) {
                System.err.print(diffExp[row][col] + " ");
            }
            log("");
        }
    }

    private int get_dest(int row, int direction, int rows) {
        int target_row = (row + direction) % rows;
        if (target_row < 0) {
            target_row += rows;
        }
        return target_row;
    }

    private void explore(IField field, Set<Cell> myAnts, Set<Cell> orderedAnts) {
        int remainingAnts = myAnts.size() - orderedAnts.size();
        if (remainingAnts <= 0) {
            return;
        }

        Set<Cell> unexplored = field.getUnexplored();
        log("exploration phase... remaining myAnts: " + remainingAnts);
        Set<Cell> exploredEdge = new HashSet<Cell>();
        for (Cell cell : unexplored) {
            for (Direction direction: Direction.values()) {
                Cell neighbour = field.getDestination(cell, direction);
                if (!unexplored.contains(neighbour) && field.get(neighbour).type.isPassable()) {
                    exploredEdge.add(neighbour);
                    break;
                }
            }
        }
        //log("explored edge: " + exploredEdge.size());


        List<Route> unseenRoutes = new ArrayList<Route>();
        for (Cell antLoc : myAnts) {
            if (!orderedAnts.contains(antLoc)) {
                for (Cell unseenLoc : exploredEdge) {
                    int distance = Math.abs(unseenLoc.row - antLoc.row) + Math.abs(unseenLoc.col - antLoc.col);
                    Route route = new Route(antLoc, unseenLoc, distance, null);
                    unseenRoutes.add(route);
                }
            }
        }
        Collections.sort(unseenRoutes);

        for (Route route : unseenRoutes) {
            List<PathNode> path = field.getPathFinder().computePath(route.getStart(), route.getEnd());
            if (path != null && path.get(0).direction != null && !orderedAnts.contains(route.getStart())) {
                issueOrder(route.getStart(), path.get(0).direction);
                orderedAnts.add(route.getStart());
                log("Sent ant to explore: " + route.getStart() + "  to " + route.getEnd());
                remainingAnts--;
            }
            if (remainingAnts <= 0) {
                break;
            }
        }
    }

    private void attack(IField field, Set<Cell> myAnts, Set<Cell> orderedAnts) {
        int remainingAnts = myAnts.size() - orderedAnts.size();
        if (remainingAnts <= 0) {
            return;
        }

        if (remainingAnts > (field.getEnemyAnts().size() * 3) /*&& remainingAnts >= 100*/) {
            log("attack phase... remaining myAnts: " + remainingAnts);
            List<Route> hillRoutes = new ArrayList<Route>();
            for (Cell hillLoc : field.getEnemyHills()) {
                for (Cell antLoc : myAnts) {
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
                    log("Sent ant after hill: " + route.getStart() + "  to " + route.getEnd());
                    remainingAnts--;
                    if (remainingAnts <= 0) {
                        break;
                    }
                }
            }
        }
    }

    private void collectFood(IField field, Set<Cell> ants, Set<Cell> orderedAnts) {
        log("food phase...");

        Set<Cell> foods = field.getSeenFood();
        Map<Cell, Cell> foodTargets = new HashMap<Cell, Cell>();
        Map<Cell, Direction> foodDirections = new HashMap<Cell, Direction>();
        Map<Cell, Cell> foodDestinations = new HashMap<Cell, Cell>();

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
                foodDestinations.put(route.getStart(), route.getEnd());
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
                log("Sent ant after food: " + myAnt + "  to " + foodDestinations.get(myAnt));
                orderedAnts.add(myAnt);
            }
        }
    }

    private void randomMoves(IField field, Set<Cell> ants, Set<Cell> orderedAnts) {
        int remainingAnts;
        remainingAnts = ants.size() - orderedAnts.size();
        if (remainingAnts <= 0) {
            return;
        }
        log("random phase...");
        for (Cell ant : ants) {
            if (!orderedAnts.contains(ant)) {
                if (antHasOptions(field, ant)) {
                    Direction direction = getRandomDirection(field, ant);
                    if (direction != null) {
                        issueOrder(ant, direction);
                        orderedAnts.add(ant);
                        log("Sent ant for random walk: " + ant + "  to " + field.getDestination(ant, direction));
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

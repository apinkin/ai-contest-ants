import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomBot extends AbstractHiveMind {

    public static enum Status {
        SAFE, KILL, DIE
    }

    private static boolean LOGGING_ENABLED = false;

    private static boolean LOGGING_VIS_ENABLED = false;
    private static boolean LOGGING_VIS_MAIN_INFLUENCE = false;
    private static boolean LOGGING_VIS_ANT_INFLUENCE = false;

    private static int INFLUENCE_MAX = Integer.MAX_VALUE;
    private static int INFLUENCE_FOOD = INFLUENCE_MAX;
    private static int INFLUENCE_ENEMY_HILL = INFLUENCE_MAX;
    private static int INFLUENCE_UNEXPLORED = INFLUENCE_MAX / 10;
    private static int INFLUENCE_UNSEEN = INFLUENCE_MAX / 100;
    private static int INFLUENCE_MY_HILL_DEFEND = INFLUENCE_MAX;
    private static int INFLUENCE_MY_ANT = 0;
    private static double DIFF = 1e-100;

    private static String ANT_COLOR_ENEMY = "255 0 0";
    private static String ANT_COLOR_MY = "0 255 0";

    private static Map<Cell, Integer> lastSeen = new HashMap<Cell, Integer>();
    private Set<Cell> myHills;
    IField myField;
    private int defenseRadiusSquared;

    @Override
    protected void doTurn(IField field) {
        Utils.tic();

        updateLastSeen(field);

        myField = field;
        myHills = field.getMyHills();
        int defenseRadius = (int) Math.ceil(Math.sqrt(info.attackRadiusSquared));
        defenseRadiusSquared = defenseRadius * defenseRadius;
        diffuse();

        log_err("Turn: " + field.getTurnNumber() + "    Time: " + Utils.toc());
    }

    private void diffuse() {
        double[][] diffExp = new double[info.rows][info.cols];
        boolean[][] diffusedExp = new boolean[info.rows][info.cols];

        // seed the main influence map
        for (int row = 0; row < info.rows; row++) {
            for (int col = 0; col < info.cols; col++) {
                Cell cell = new Cell(row, col);
                Owned o = myField.get(row, col);

                if (o.type.equals(Cell.Type.HILL) && o.isEnemys()) {
                    diffExp[row][col] = INFLUENCE_ENEMY_HILL;
                    diffusedExp[row][col] = true;
                } else if (o.type.equals(Cell.Type.FOOD)) {
                    diffExp[row][col] = INFLUENCE_FOOD;
                    diffusedExp[row][col] = true;
                } else if (o.type.equals(Cell.Type.ANT) && o.isEnemys() && isCloseToMyHill(myHills, cell)) {
                    diffExp[row][col] = INFLUENCE_MY_HILL_DEFEND;
                    diffusedExp[row][col] = true;
                } else if (o.type.equals(Cell.Type.ANT) && o.isMine()) {
                    diffExp[row][col] = INFLUENCE_MY_ANT;
                    diffusedExp[row][col] = true;
                } else if (o.type.equals(Cell.Type.WATER) || (o.type.equals(Cell.Type.HILL) && o.isMine())) {
                    diffExp[row][col] = 0;
                    diffusedExp[row][col] = true;
                } else if (!o.explored) {
                    diffExp[row][col] = INFLUENCE_UNEXPLORED;
                    diffusedExp[row][col] = true;
                } else if (o.explored && !myField.isSeen(cell)) {
                    Integer seenTurn = lastSeen.get(cell);
                    int seenTurnsAgo = myField.getTurnNumber() - seenTurn;
                    diffExp[row][col] = INFLUENCE_UNSEEN - (50 - (seenTurnsAgo % 50)) * INFLUENCE_UNSEEN / 50;
                    log_err("Last seen for : " + cell + " : " + seenTurn + "  diff: " + diffExp[row][col]);
                    diffusedExp[row][col] = true;
                }

            }
        }

        // iterate to diffuse the influence
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            for (int row = 0; row < info.rows; row++) {
                for (int col = 0; col < info.cols; col++) {
                    if (diffusedExp[row][col]) {
                        continue;
                    }
                    double up = diffExp[get_dest(row, -1, info.rows)][col];
                    double down = diffExp[get_dest(row, 1, info.rows)][col];
                    double left = diffExp[row][get_dest(col, -1, info.cols)];
                    double right = diffExp[row][get_dest(col, 1, info.cols)];

                    double divider = 4.0;
                    diffExp[row][col] = up / divider + down / divider + left / divider + right / divider;
                }
            }
        }

        // calculate my/enemy influence map
        int influenceRadius = (int) Math.ceil(Math.sqrt(info.attackRadiusSquared));

        Set<Cell> enemyAnts = myField.getEnemyAnts();
        int enemyPlayers = 0;
        for (Cell c : enemyAnts) {
            enemyPlayers = Math.max(enemyPlayers, myField.get(c).owner);
        }
        int playerCount = enemyPlayers + 1;

        int[][][] playerInfluence = new int[playerCount][info.rows][info.cols];
        boolean[][][] playerInfluenceEmitted = new boolean[playerCount][info.rows][info.cols];
        int[][] totalInfluence = new int[info.rows][info.cols];
        Set<Cell> ants = myField.getAnts();

        for (Cell ant : ants) {
            int player = myField.get(ant).owner;
            Set<Cell> neighbours = getNeighboursIncludingSelf(ant);
            Set<Cell> neighboursNew = new HashSet<Cell>();
            for (Cell n : neighbours) {
                if (!playerInfluenceEmitted[player][n.row][n.col]) {
                    neighboursNew.add(n);
                }
                playerInfluenceEmitted[player][n.row][n.col] = true;
            }
            Set<Cell> influencedCells = new HashSet<Cell>();
            for (Cell n : neighboursNew) {
                for (int row = -influenceRadius; row <= influenceRadius; row++) {
                    for (int col = -influenceRadius; col <= influenceRadius; col++) {
                        int crow = get_dest(n.row, row, info.rows);
                        int ccol = get_dest(n.col, col, info.cols);
                        Cell ccell = Cell.of(crow, ccol);
                        int distSquared = myField.getDistance(n, ccell);
                        if (distSquared <= info.attackRadiusSquared) {
                            influencedCells.add(ccell);
                        }
                    }
                }
            }
            for (Cell c : influencedCells) {
                totalInfluence[c.row][c.col] += 1;
                playerInfluence[player][c.row][c.col] += 1;
            }
        }

        // calculate fighting
        int[][][] fighting = new int[playerCount][info.rows][info.cols];
        for (int p = 0; p < playerCount; p++) {
            for (int row = 0; row < info.rows; row++) {
                for (int col = 0; col < info.cols; col++) {
                    fighting[p][row][col] = Integer.MAX_VALUE;
                }
            }
        }
        for (Cell ant : ants) {
            for (Cell position : getNeighboursIncludingSelf(ant)) {
                int antsFighting = totalInfluence[position.row][position.col] - playerInfluence[myField.get(ant).owner][position.row][position.col];
                // now store this in each tile in the fighting array within the combat zone of that tile if it is less than current value
                for (int row = -influenceRadius; row <= influenceRadius; row++) {
                    for (int col = -influenceRadius; col <= influenceRadius; col++) {
                        int crow = get_dest(position.row, row, info.rows);
                        int ccol = get_dest(position.col, col, info.cols);
                        int minDistSquared = myField.getDistance(position, Cell.of(crow, ccol));
                        if (minDistSquared <= info.attackRadiusSquared) {
                            fighting[myField.get(ant).owner][crow][ccol] = Math.min(fighting[myField.get(ant).owner][crow][ccol], antsFighting);
                        }
                    }
                }
            }
        }

        // now let's calculate SKD = Safe, Kill, Die
        Status[][] status = new Status[info.rows][info.cols];
        int[][] bestArr = new int[info.rows][info.cols];
        for (Cell myAnt : myField.getMyAntPositions()) {
            for (Cell position : getNeighboursIncludingSelf(myAnt)) {
                int enemy_count = totalInfluence[position.row][position.col] - playerInfluence[0][position.row][position.col];
                int best = Integer.MAX_VALUE;
                for (int p = 1; p < playerCount; p++) {
                    best = Math.min(best, fighting[p][position.row][position.col]);
                }
                bestArr[position.row][position.col] = best;
                if (best < enemy_count) {
                    status[position.row][position.col] = Status.DIE;
                } else if (best == enemy_count && enemy_count > 0) {
                    status[position.row][position.col] = Status.KILL;
                } else {
                    status[position.row][position.col] = Status.SAFE;
                }
            }
        }

        // now make decisions where free ants can go
        for (Cell antLoc : myField.getMyAntPositions()) {
            Direction direction = getDirectionHighestDiff(antLoc, diffExp, status);
            if (direction != null) {
                issueOrder(antLoc, direction);
                log_err("Sent ant: " + antLoc + "  to " + direction);
            } else {
                log_err("Ant stuck: " + antLoc);
            }
        }

        // visualize ant influence map
        if (LOGGING_VIS_ENABLED && LOGGING_VIS_ANT_INFLUENCE) {
            for (int row = 0; row < info.rows; row++) {
                for (int col = 0; col < info.cols; col++) {
                    if (playerInfluence[0][row][col] > 0) {
                        log_out("i " + row + " " + col + " my influence: " + playerInfluence[0][row][col]);
                        log_out("i " + row + " " + col + " total influence: " + totalInfluence[row][col]);
                        if (status[row][col] != null) {
                            log_out("i " + row + " " + col + " status: " + status[row][col].toString());
                            log_out("i " + row + " " + col + " fighting: " + fighting[0][row][col]);
                            int enemy_count = totalInfluence[row][col] - playerInfluence[0][row][col];
                            log_out("i " + row + " " + col + " enemy_count: " + enemy_count);
                            log_out("i " + row + " " + col + " best: " + bestArr[row][col]);
                        }
                    }
                }
            }
        }

        // visualize main influence map
        if (LOGGING_VIS_ENABLED && LOGGING_VIS_MAIN_INFLUENCE) {
            for (int row = 0; row < info.rows; row++) {
                for (int col = 0; col < info.cols; col++) {
                    log_out("v setFillColor 255 0 0 " + diffExp[row][col] / INFLUENCE_MAX);
                    log_out("v tile " + row + " " + col);
                    log_out("i " + row + " " + col + " " + diffExp[row][col] / INFLUENCE_MAX);
                }
            }
        }
    }

    private Set<Cell> getNeighboursIncludingSelf(Cell cell) {
        Set<Cell> r = getNeighbours(cell);
        r.add(cell);
        return r;
    }

    private Set<Cell> getNeighbours(Cell cell) {
        Set<Cell> r = new HashSet<Cell>();
        Cell north = myField.getDestination(cell, Direction.NORTH);
        Owned o = myField.get(north.row, north.col);
        if (!o.type.equals(Cell.Type.WATER)) {
            r.add(north);
        }
        Cell south = myField.getDestination(cell, Direction.SOUTH);
        o = myField.get(south.row, south.col);
        if (!o.type.equals(Cell.Type.WATER)) {
            r.add(south);
        }
        Cell west = myField.getDestination(cell, Direction.WEST);
        o = myField.get(west.row, west.col);
        if (!o.type.equals(Cell.Type.WATER)) {
            r.add(west);
        }
        Cell east = myField.getDestination(cell, Direction.EAST);
        o = myField.get(east.row, east.col);
        if (!o.type.equals(Cell.Type.WATER)) {
            r.add(east);
        }
        return r;
    }

    private boolean isCloseToMyHill(Set<Cell> myHills, Cell cell) {
        return isCloseToMyHill(myHills, cell, info.viewRadiusSquared);
    }

    private boolean isCloseToMyHill(Set<Cell> myHills, Cell cell, int dist) {
        if (myHills.size() == 0) {
            return false;
        }
        int min_distance = Integer.MAX_VALUE;
        for (Cell hill : myHills) {
            int distance = myField.getDistance(cell, hill);
            min_distance = Math.min(min_distance, distance);
        }

        return (min_distance <= dist);
    }

    private Direction getDirectionHighestDiff(Cell cell, double[][] diffExp, Status[][] status) {
        Cell north = myField.getDestination(cell, Direction.NORTH);
        Cell south = myField.getDestination(cell, Direction.SOUTH);
        Cell west = myField.getDestination(cell, Direction.WEST);
        Cell east = myField.getDestination(cell, Direction.EAST);

        double diffNorth = myField.get(north).type.isPassable() && isSafe(north, status) ? diffExp[north.row][north.col] : 0;
        double diffSouth = myField.get(south).type.isPassable() && isSafe(south, status) ? diffExp[south.row][south.col] : 0;
        double diffWest = myField.get(west).type.isPassable() && isSafe(west, status) ? diffExp[west.row][west.col] : 0;
        double diffEast = myField.get(east).type.isPassable() && isSafe(east, status) ? diffExp[east.row][east.col] : 0;

        double maxDiff = Math.max(Math.max(Math.max(diffNorth, diffSouth), diffWest), diffEast);

        //log_out("i " + cell.row + " " + cell.col + " Diffusion north:  " + diffNorth + "  east: " + diffEast + "  south: " + diffSouth + "  west: " + diffWest);

        if (maxDiff <= DIFF) {
            return null;
        }
        if (Math.abs(diffNorth - maxDiff) <= DIFF) {
            return Direction.NORTH;
        }
        if (Math.abs(diffSouth - maxDiff) <= DIFF) {
            return Direction.SOUTH;
        }
        if (Math.abs(diffWest - maxDiff) <= DIFF) {
            return Direction.WEST;
        }
        if (Math.abs(diffEast - maxDiff) <= DIFF) {
            return Direction.EAST;
        }
        return null;
    }

    private boolean isSafe(Cell cell, Status[][] status) {
        return (status[cell.row][cell.col] == Status.SAFE) || (isCloseToMyHill(myHills, cell, defenseRadiusSquared) && status[cell.row][cell.col] == Status.KILL);
    }

    private int get_dest(int row, int direction, int rows) {
        int target_row = (row + direction) % rows;
        if (target_row < 0) {
            target_row += rows;
        }
        return target_row;
    }

    private Set<Cell> getAllSeen() {
        Set<Cell> result = new HashSet<Cell>();

        for (int row = 0; row < info.rows; row++) {
            for (int col = 0; col < info.cols; col++) {
                Cell c = Cell.of(row, col);
                if (myField.isSeen(c)) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    private void updateLastSeen(IField field) {
        Set<Cell> currentSeen = getAllSeen();
        for (Cell c : currentSeen) {
            lastSeen.put(c, field.getTurnNumber());
        }
    }

    private void log_err(String s) {
        if (LOGGING_ENABLED) {
            System.err.println(s);
        }
    }

    private void log_out(String s) {
        if (LOGGING_VIS_ENABLED) {
            System.out.println(s);
        }
    }


}
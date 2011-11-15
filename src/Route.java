/**
 * Represents a route from one tile to another.
 */
public class Route implements Comparable<Route> {
    private final Cell start;

    private final Cell end;

    private final int distance;

    private final Direction direction;

    public Route(Cell start, Cell end, int distance, Direction direction) {
        this.start = start;
        this.end = end;
        this.distance = distance;
        this.direction = direction;
    }

    public Cell getStart() {
        return start;
    }

    public Cell getEnd() {
        return end;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Route route) {
        return distance - route.distance;
    }

    @Override
    public int hashCode() {
        return start.hashCode() * 1000 * 1000 + end.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Route) {
            Route route = (Route)o;
            result = start.equals(route.start) && end.equals(route.end);
        }
        return result;
    }

    public Direction getDirection() {
        return direction;
    }
}
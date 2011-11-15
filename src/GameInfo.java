
/**
 *
 * @author gsmir
 */
public class GameInfo {
    public final long loadTime;
    public final long turnTime;
    public final int rows;
    public final int cols;
    public final int turns;
    public final int viewRadiusSquared;
    public final int attackRadiusSquared;
    public final int harvestRadiusSquared;
    public final long seed;
    
    //sorry :(
    public GameInfo(long loadTime, long turnTime, int rows, int cols, int turns, int viewRadiusSquared, int attackRadiusSquared, int spawnRadiusSquared, long seed) {
        this.loadTime = loadTime;
        this.turnTime = turnTime;
        this.rows = rows;
        this.cols = cols;
        this.turns = turns;
        this.viewRadiusSquared = viewRadiusSquared;
        this.attackRadiusSquared = attackRadiusSquared;
        this.harvestRadiusSquared = spawnRadiusSquared;
        this.seed = seed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GameInfo other = (GameInfo) obj;
        if (this.loadTime != other.loadTime) {
            return false;
        }
        if (this.turnTime != other.turnTime) {
            return false;
        }
        if (this.rows != other.rows) {
            return false;
        }
        if (this.cols != other.cols) {
            return false;
        }
        if (this.turns != other.turns) {
            return false;
        }
        if (this.viewRadiusSquared != other.viewRadiusSquared) {
            return false;
        }
        if (this.attackRadiusSquared != other.attackRadiusSquared) {
            return false;
        }
        if (this.harvestRadiusSquared != other.harvestRadiusSquared) {
            return false;
        }
        if (this.seed != other.seed) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (this.loadTime ^ (this.loadTime >>> 32));
        hash = 89 * hash + (int) (this.turnTime ^ (this.turnTime >>> 32));
        hash = 89 * hash + this.rows;
        hash = 89 * hash + this.cols;
        hash = 89 * hash + this.turns;
        hash = 89 * hash + this.viewRadiusSquared;
        hash = 89 * hash + this.attackRadiusSquared;
        hash = 89 * hash + this.harvestRadiusSquared;
        hash = 89 * hash + (int) (this.seed ^ (this.seed >>> 32));
        return hash;
    }
    
    
}

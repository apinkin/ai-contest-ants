/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gsmir
 */
public class PathNode {
    public Cell cell;
    public Direction direction;

    public PathNode(Cell cell, Direction next) {
        this.cell = cell;
        this.direction = next;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathNode other = (PathNode) obj;
        if (this.cell != other.cell && (this.cell == null || !this.cell.equals(other.cell))) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.cell != null ? this.cell.hashCode() : 0);
        hash = 17 * hash + (this.direction != null ? this.direction.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "PathNode{" + "cell=" + cell + ", next=" + direction + '}';
    }
    
}


public class Owned {
    
    public static final int OWNER_ME = 0;
    public static final int OWNER_NOBODY = -1;
    
    public Cell.Type type;
    public int owner;
    public boolean explored;
    
    public Owned(Cell.Type type, int owner, boolean explored) {
        this.type = type;
        this.owner = owner;
        this.explored = explored;
    }
    
    public Owned(Cell.Type type, int owner) {
        this(type, owner, false);
    }
    
    public Owned(Cell.Type type, boolean explored) {
        this(type, OWNER_NOBODY, explored);
    }
    
    public Owned(Cell.Type type) {
        this(type, false);
    }
    
    public boolean isMine() {
        return owner == OWNER_ME;
    }
    
    public boolean isNobodys() {
        return owner <= OWNER_NOBODY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Owned other = (Owned) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.owner != other.owner) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + this.owner;
        return hash;
    }
    
    
    
}

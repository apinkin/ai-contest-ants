
public interface IMutableField extends IField {
    void setCell(int row, int col, Cell.Type type) ;
    void setCell(int row, int col, Cell.Type type, int owner) ;

    void clear(Cell.Type... types);
    
    void updateExplored();
    
    void setTurnNumber(int turn);
}

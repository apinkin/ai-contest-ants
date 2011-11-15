import java.util.Set;

public class SampleBot extends AbstractHiveMind {

    @Override
    protected void doTurn(IField field) {
        //System.err.println("Current turn: " + field.getTurnNumber());

        Set<Cell> ants = field.getMyAntPositions();
        for (Cell myAnt : ants) {
            for (Direction direction : Direction.values()) {
                if (field.get(field.getDestination(myAnt, direction)).type.isPassable()) {
                    issueOrder(myAnt, direction);
                    break;
                }
            }
        }
    }

}

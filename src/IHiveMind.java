
import java.util.Collection;


public interface IHiveMind {
    void init(GameInfo info, IOrderManager orderManager);
    void onNewTurn(IField field);
}

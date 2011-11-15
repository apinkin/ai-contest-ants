
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyBot {

    private static class Params {
        private InputStream in = System.in;
        private OutputStream out = System.out;
        private String algorithmName = "CustomBot";

        public Params(String[] args) throws FileNotFoundException {
            for(String arg : args) {
                if(arg.startsWith("-i=")) {
                    in = new FileInputStream(arg.substring(3));
                } else if(arg.startsWith("-o=")) {
                    out = new FileOutputStream(arg.substring(3));
                } else {
                    algorithmName = arg;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Params params = new Params(args);

        IFieldProvider fieldProvider = new FieldProvider();
        GameInfo info = fieldProvider.init(params.in);
        IHiveMind hiveMind;

        OrderManager orderManager = new OrderManager(info, params.out);

        try {
            hiveMind = createAlgorithm(params.algorithmName);
            hiveMind.init(info, orderManager);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit("what a pity what a mess".hashCode());
            return;
        }

        while(true) {
            orderManager.onTurnEnded();
            IField field = fieldProvider.getNextFieldState();
            if(fieldProvider.gameOver()) {
                break;
            } else {
                orderManager.updateField(field);
                hiveMind.onNewTurn(field);
            }
        }
    }

    private static IHiveMind createAlgorithm(String algorithmName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (IHiveMind) Class.forName(algorithmName).newInstance();
    }
}

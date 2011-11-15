
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;



public class Utils {

    private Utils() {}

    private static PrintWriter log = null;

    static {
        try {
            log = new PrintWriter("log.log");
        } catch (FileNotFoundException ex) {
            //pity
            System.err.println("Failed to initialize logger");
        }
    }

    public static void log(String s) {
        if(log != null) {
            log.println(s);
            log.flush();
        }
    }

    public static <T> void removeFirst(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        iterator.next();
        iterator.remove();
    }

    public static int min(int first, int... rest) {
        int min = first;
        for(int value : rest) {
            if(value < min) {
                min = value;
            }
        }
        return min;
    }

    public static String toCompactString(IField field) {
        StringBuilder builder = new StringBuilder();
        for(int row = 0; row < field.rows(); row ++) {
            for(int col = 0; col < field.cols(); col ++) {
                Owned cell = field.get(row, col);
                if(!cell.explored) {
                    builder.append("?");
                } else {
                    if(cell.type == Cell.Type.ANT) {
                        builder.append(cell.owner);
                    } else {
                        builder.append(cell.type.toString().charAt(0));
                    }
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private static long ticTime;

    public static long tic() {
        ticTime = System.currentTimeMillis();
        return ticTime;
    }

    public static long toc(long ticTime) {
        return System.currentTimeMillis() - ticTime;
    }

    public static long toc() {
        return toc(ticTime);
    }

}

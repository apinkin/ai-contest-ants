
import java.util.Comparator;


public class Prioritized<P extends Comparable<P>, T> implements Comparable<Prioritized<P, T>> {

    public P priority;
    public T value;
    private final Comparator<T> comparator;

    public Prioritized(P priority, T value, Comparator<T> comparator) {
        this.priority = priority;
        this.value = value;
        this.comparator = comparator;
    }

    public Prioritized(P priority, T value) {
        this(priority, value, new Comparator<T>() {

            @Override
            public int compare(T first, T second) {
                return 0;
            }

        });
    }

    @Override
    public int compareTo(Prioritized<P, T> other) {
        int result = priority.compareTo(other.priority);
        return result == 0 ? comparator.compare(value, other.value) : result;
    }

    @Override
    public String toString() {
        return value.toString();
    }



}

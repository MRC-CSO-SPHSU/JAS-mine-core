package microsim.collection;

import jamjam.Sum;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.collections4.Closure;

import java.util.stream.DoubleStream;

/**
 * A generic implementation of {@link Closure}, calculates some average.
 *
 * @param <T> A generic agent type.
 */
public abstract class AverageClosure<T> implements Closure<T> {

    @Getter
    final protected Sum.Accumulator accumulator = new Sum.Accumulator();

    @Getter
    protected long count = 0;

    public double getSum() {
        return accumulator.getSum();
    }

    /**
     * Calculates the average accumulated value.
     *
     * @return the average value.
     */
    public double getAverage() {
        return accumulator.getSum() / count;
    }

    /**
     * Adds a value to the total sum and increments the total number of values by {@code 1}.
     *
     * @param value The value to be added to the sum.
     */
    public void add(final double value) {
        accumulator.sum(value);
        count++;
    }

    /**
     * Adds together all values in the array and stores both the total sum and the total number of elements to date in
     * {@link #accumulator} and {@link #count}, respectively.
     *
     * @throws NullPointerException when {@code value} is {@code null}.
     * @see #add(double)
     */
    public void add(final double @NonNull [] value) {
        accumulator.sum(value);
        count += value.length;
    }

    /**
     * Adds all values from {@link DoubleStream} to the total sum and increments the total number by the corresponding
     * stream length.
     *
     * @throws NullPointerException when {@code value} is {@code null}.
     * @see #add(double)
     */
    public void add(final @NonNull DoubleStream value) {
        val scratch = value.toArray();
        accumulator.sum(scratch);
        count += scratch.length;
    }
}

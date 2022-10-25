package microsim.statistics.functions;

import jamjam.Sum;
import lombok.NonNull;
import lombok.val;
import microsim.statistics.DoubleArraySource;
import microsim.statistics.DoubleSource;
import microsim.statistics.IntArraySource;
import microsim.statistics.LongArraySource;

import java.util.Arrays;

/**
 * This class computes the average of the last given number of values in an array taken from a data source. The mean
 * function return always a double value, so it implements the {@link DoubleSource} interface and the standard
 * {@link DoubleSource} one.
 */
public class MovingAverageArrayFunction extends AbstractArrayFunction implements DoubleSource {

    protected double mean;
    protected int window;

    /**
     * Creates a count function on an integer array source.
     *
     * @param source The data source.
     * @throws NullPointerException     when {@code source} is {@code null}.
     * @throws IllegalArgumentException when {@code windowSize} is {@code < 1}.
     */
    public MovingAverageArrayFunction(final @NonNull IntArraySource source, final int windowSize) {
        super(source);
        if (windowSize < 1) throw new IllegalArgumentException("Unacceptable window size");
        this.window = windowSize;
    }

    /**
     * Creates a count function on a long array source.
     *
     * @param source The data source.
     * @throws NullPointerException     when {@code source} is {@code null}.
     * @throws IllegalArgumentException when {@code windowSize} is {@code < 1}.
     */
    public MovingAverageArrayFunction(final @NonNull LongArraySource source, final int windowSize) {
        super(source);
        if (windowSize < 1) throw new IllegalArgumentException("Unacceptable window size");
        this.window = windowSize;
    }

    /**
     * Create a count function on a double array source.
     *
     * @param source The data source.
     * @throws NullPointerException     when {@code source} is {@code null}.
     * @throws IllegalArgumentException when {@code windowSize} is {@code < 1}.
     */
    public MovingAverageArrayFunction(final @NonNull DoubleArraySource source, final int windowSize) {
        super(source);
        if (windowSize < 1) throw new IllegalArgumentException("Unacceptable window size");
        this.window = windowSize;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final double @NonNull [] data) {
        val firstElement = Math.max(data.length - window, 0);
        val vals = Math.min(data.length, window);
        mean = Sum.sum(Arrays.stream(data).skip(firstElement).limit(vals)) / vals; // todo check that it returns correct values
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final int @NonNull [] data) {
        val firstElement = Math.max(data.length - window, 0);
        val vals = Math.min(data.length, window);
        mean = Sum.sum(Arrays.stream(data).asDoubleStream().skip(firstElement).limit(vals)) / vals; // todo check that it returns correct values
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final long @NonNull [] data) {
        val firstElement = Math.max(data.length - window, 0);
        val vals = Math.min(data.length, window);
        mean = Sum.sum(Arrays.stream(data).asDoubleStream().skip(firstElement).limit(vals)) / vals; // todo check that it returns correct values
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public double getDoubleValue(final @NonNull Enum<?> variableID) {
        return mean;
    }
}

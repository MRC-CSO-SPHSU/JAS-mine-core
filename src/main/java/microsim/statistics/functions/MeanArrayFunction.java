package microsim.statistics.functions;

import lombok.NonNull;
import microsim.statistics.DoubleArraySource;
import microsim.statistics.DoubleSource;
import microsim.statistics.IntArraySource;
import microsim.statistics.LongArraySource;
import jamjam.Mean;

import java.util.Arrays;

/**
 * This class computes the average value of an array of values taken from a data source. The mean function return always
 * double values, so it implements only the {@link DoubleSource} interface.
 */
public class MeanArrayFunction extends AbstractArrayFunction implements DoubleSource {

    protected double mean;

    /**
     * Creates a mean function on an integer array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MeanArrayFunction(final @NonNull IntArraySource source) {
        super(source);
    }

    /**
     * Creates a mean function on a long array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MeanArrayFunction(final @NonNull LongArraySource source) {
        super(source);
    }

    /**
     * Creates a mean function on a double array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MeanArrayFunction(final @NonNull DoubleArraySource source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final double @NonNull [] data) {
        mean = Mean.mean(data);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final int @NonNull [] data) {
        mean = data.length != 0 ? (double) Arrays.stream(data).asLongStream().sum() / data.length : 0.;
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     *
     */
    public void apply(final long @NonNull [] data) {
        mean = data.length != 0 ? (double) Arrays.stream(data).sum() / data.length : 0.;
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public double getDoubleValue(final @NonNull Enum<?> variableID) {
        return mean;
    }
}

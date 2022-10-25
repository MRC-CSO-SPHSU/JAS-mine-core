package microsim.statistics.functions;

import lombok.NonNull;
import microsim.statistics.DoubleArraySource;
import microsim.statistics.DoubleSource;
import microsim.statistics.IntArraySource;
import microsim.statistics.LongArraySource;

import java.util.Arrays;

import static jamjam.Mean.mean;
import static jamjam.Variance.unweightedBiasedVariance;

/**
 * This class computes the average and variance value of an array of values taken from a data source. The mean function
 * always returns double values, so it implements only the {@link DoubleSource} interface. <BR>
 * In order to retrieve the mean pass the {@link MeanVarianceArrayFunction.Variables#MEAN} argument to the
 * {@link #getDoubleValue(Enum)}  function, while for the variance the
 * {@link MeanVarianceArrayFunction.Variables#VARIANCE} one.
 */
public class MeanVarianceArrayFunction extends AbstractArrayFunction implements DoubleSource {

    protected double mean, variance;

    /**
     * Creates a mean function on an integer array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MeanVarianceArrayFunction(final @NonNull IntArraySource source) {
        super(source);
    }

    /**
     * Creates a mean function on a long array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MeanVarianceArrayFunction(final @NonNull LongArraySource source) {
        super(source);
    }

    /**
     * Creates a mean function on a double array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MeanVarianceArrayFunction(final @NonNull DoubleArraySource source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final double @NonNull [] data) {
        mean = mean(data);
        variance = unweightedBiasedVariance(data, mean);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final int @NonNull [] data) {
        mean = (double) Arrays.stream(data).asLongStream().sum() / data.length;
        variance = unweightedBiasedVariance(data, mean);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public void apply(final long @NonNull [] data) {
        mean = (double) Arrays.stream(data).sum() / data.length;
        variance = unweightedBiasedVariance(data, mean);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException when {@code data} is {@code null}.
     */
    public double getDoubleValue(final @NonNull Enum<?> variableID) {
        return switch ((Variables) variableID) {
            case MEAN -> mean;
            case VARIANCE -> variance;
        };
    }

    public enum Variables {
        /**
         * Represents the mean function argument for the {@link #getDoubleValue(Enum)} method.
         */
        MEAN,
        /**
         * Represents the variance function argument for the {@link #getDoubleValue(Enum)} method.
         */
        VARIANCE
    }
}

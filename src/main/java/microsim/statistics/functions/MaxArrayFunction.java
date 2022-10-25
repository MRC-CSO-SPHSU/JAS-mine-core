package microsim.statistics.functions;

import lombok.NonNull;
import microsim.statistics.*;
import microsim.statistics.DoubleArraySource;
import microsim.statistics.DoubleSource;
import microsim.statistics.LongSource;

import java.util.Arrays;

/**
 * This class computes the maximum value in an array of source values. According to the source data type there are three
 * data-type oriented implementations. Each of them implements always the {@link DoubleSource} interface.
 */
public abstract class MaxArrayFunction extends AbstractArrayFunction implements DoubleSource {

    /**
     * Creates a maximum function on an integer array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MaxArrayFunction(final @NonNull IntArraySource source) {
        super(source);
    }

    /**
     * Creates a maximum function on a long array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MaxArrayFunction(final @NonNull LongArraySource source) {
        super(source);
    }

    /**
     * Create a maximum function on a double array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MaxArrayFunction(final @NonNull DoubleArraySource source) {
        super(source);
    }

    /**
     * MaxFunction operating on double source values.
     */
    public static class Double extends MaxArrayFunction implements DoubleSource {
        protected double dmax;

        /**
         * Creates a maximum function on a double array source.
         *
         * @param source The data source.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Double(final @NonNull DoubleArraySource source) {
            super(source);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code data} is {@code null}.
         */
        public void apply(final double @NonNull [] data) {
            dmax = java.lang.Double.MIN_VALUE;
            Arrays.stream(data).filter(datum -> dmax < datum).forEach(datum -> dmax = datum);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code id} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> id) {
            return dmax;
        }
    }

    /**
     * MaxFunction operating on long source values.
     */
    public static class Long extends MaxArrayFunction implements LongSource {
        protected long lmax;

        /**
         * Creates a maximum function on a long array source.
         *
         * @param source The data source.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Long(final @NonNull LongArraySource source) {
            super(source);
        }

        /**
         * Applies the {@code max} function to the provided array.
         *
         * @param data A source array of values.
         * @throws NullPointerException when {@code data} is {@code null}.
         */
        public void apply(final long @NonNull [] data) {
            lmax = java.lang.Long.MIN_VALUE;
            Arrays.stream(data).filter(datum -> lmax < datum).forEach(datum -> lmax = datum);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code id} is {@code null}.
         */
        public long getLongValue(final @NonNull Enum<?> id) {
            return lmax;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> variableID) {
            return lmax;
        }
    }

    /**
     * MaxFunction operating on integer source values.
     */
    public static class Integer extends MaxArrayFunction implements IntSource {
        protected int imax;

        /**
         * Creates a maximum function on an integer array source.
         *
         * @param source The data source.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Integer(final @NonNull IntArraySource source) {
            super(source);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code data} is {@code null}.
         */
        public void apply(final int @NonNull [] data) {
            imax = java.lang.Integer.MIN_VALUE;
            Arrays.stream(data).filter(datum -> imax < datum).forEach(datum -> imax = datum);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code id} is {@code null}.
         */
        public int getIntValue(final @NonNull Enum<?> id) {
            return imax;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> variableID) {
            return imax;
        }
    }
}

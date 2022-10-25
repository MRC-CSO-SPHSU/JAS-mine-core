package microsim.statistics.functions;

import lombok.NonNull;
import microsim.statistics.*;

import java.util.Arrays;

/**
 * This class computes the minimum value in an array of source values. According to the source data type there are three
 * data-type oriented implementations. Each of them implements always the {@link DoubleSource} interface.
 */
public abstract class MinArrayFunction extends AbstractArrayFunction implements DoubleSource {

    /**
     * Creates a minimum function on an int array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MinArrayFunction(final @NonNull IntArraySource source) {
        super(source);
    }

    /**
     * Creates a minimum function on a long array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MinArrayFunction(final @NonNull LongArraySource source) {
        super(source);
    }

    /**
     * Creates a minimum function on a double array source.
     *
     * @param source The data source.
     * @throws NullPointerException when {@code source} is {@code null}.
     */
    public MinArrayFunction(final @NonNull DoubleArraySource source) {
        super(source);
    }

    /**
     * MinFunction operating on double source values.
     */
    public static class Double extends MinArrayFunction implements DoubleSource {
        protected double min;

        /**
         * Creates a minimum function on a double array source.
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
            min = java.lang.Double.MAX_VALUE;
            Arrays.stream(data).filter(datum -> min > datum).forEach(datum -> min = datum);

        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> variableID) {
            return min;
        }
    }

    /**
     * MinFunction operating on long source values.
     */
    public static class Long extends MinArrayFunction implements LongSource {
        protected long lmin;

        /**
         * Creates a minimum function on a long array source.
         *
         * @param source The data source.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Long(final @NonNull LongArraySource source) {
            super(source);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code data} is {@code null}.
         */
        public void apply(final long @NonNull [] data) {
            lmin = java.lang.Long.MAX_VALUE;
            Arrays.stream(data).filter(datum -> lmin > datum).forEach(datum -> lmin = datum);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public long getLongValue(final @NonNull Enum<?> variableID) {
            return lmin;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> variableID) {
            return lmin;
        }
    }

    /**
     * MinFunction operating on integer source values.
     */
    public static class Integer extends MinArrayFunction implements IntSource {
        protected int imin;

        /**
         * Creates a minimum function on an integer array source.
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
            imin = java.lang.Integer.MAX_VALUE;
            Arrays.stream(data).filter(datum -> imin > datum).forEach(datum -> imin = datum);
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public int getIntValue(final @NonNull Enum<?> variableID) {
            return imin;
        }

        /**
         * {@inheritDoc}
         *
         * @throws NullPointerException when {@code variableID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> variableID) {
            return imin;
        }
    }
}

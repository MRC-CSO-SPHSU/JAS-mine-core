package microsim.statistics.functions;

import lombok.Getter;
import lombok.NonNull;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.exception.SimulationRuntimeException;
import microsim.statistics.DoubleSource;
import microsim.statistics.IntSource;
import microsim.statistics.LongSource;
import microsim.statistics.UpdatableSource;
import microsim.statistics.reflectors.DoubleInvoker;
import microsim.statistics.reflectors.IntegerInvoker;
import microsim.statistics.reflectors.LongInvoker;

/**
 * A MixFunction object is to collect data over time, computing some statistics on the fly, without storing the data in
 * memory. It is particularly useful when the user need to compute basic statistics on data sources, without affecting
 * the memory occupancy. The memoryless series computes automatically the statistics using accumulation variables and
 * counters.<br> This statistic computer should be used when possible, particularly when the simulation model has to run
 * for a long time, condition which implies the growth of the memory occupancy. Moreover, the Memoryless Series objects
 * are much faster than the Series one, because they pre-compute the statistics operation step by step. Trying to
 * compute a mean of a Series object, force the Mean function to sum all the values, every time series is updated.
 */
public abstract class MultiTraceFunction implements DoubleSource, UpdatableSource, EventListener {

    protected int count = 0;

    /**
     * Collects a value from the source.
     */
    public void updateSource() {
        count++;
    }

    public abstract double getMean();

    public abstract double getVariance();

    public int getCount() {
        return count;
    }

    /**
     * {@link microsim.event.EventListener} callback function. It supports only {@link CommonEventType#UPDATE} event.
     *
     * @param type The action id. Only {@link CommonEventType#UPDATE} is supported.
     * @throws SimulationRuntimeException If actionType is not supported.
     * @throws NullPointerException       when {@code type} is {@code null}.
     */
    public void onEvent(@NonNull Enum<?> type) {
        if (type.equals(CommonEventType.UPDATE)) updateSource();
        else throw new SimulationRuntimeException("SimpleStatistics object does not support " + type + " operation.");
    }

    /**
     * Computes one of the available statistical functions on the collected data.
     *
     * @throws NullPointerException          when {@code valueID} is {@code null}.
     * @throws UnsupportedOperationException when {@code valueID} is of not supported type.
     */
    public double getDoubleValue(final @NonNull Enum<?> valueID) {
        return switch ((Variables) valueID) {
            case MEAN -> getMean();
            case VARIANCE -> getVariance();
            case COUNT -> getCount();
            default -> throw new UnsupportedOperationException("The valueID " + valueID + " is not supported.");
        };
    }

    public enum Variables {
        /**
         * Return the last collected value.
         */
        LAST_VALUE,
        /**
         * Return the minimum collected value.
         */
        MIN,
        /**
         * Return the maximum collected value.
         */
        MAX,
        /**
         * Return the mean of the collected values.
         */
        MEAN,
        /**
         * Return the variance of the collected values.
         */
        VARIANCE,
        /**
         * Return the number of collected values.
         */
        COUNT,
        /**
         * Return the sum of collected values.
         */
        SUM
    }

    /**
     * An implementation of the Memoryless Series class, which manages long type data sources.
     */
    public static class Long extends MultiTraceFunction implements LongSource {
        private final Enum<?> valueID;
        @Getter
        protected long max = java.lang.Long.MIN_VALUE;
        @Getter
        protected long min = java.lang.Long.MAX_VALUE;
        @Getter
        protected long sum = 0;
        @Getter
        protected long sumSquare = 0;
        protected LongSource target;
        @Getter
        private long lastRead;

        /**
         * Creates a basic statistic probe on a {@link DoubleSource} object.
         *
         * @param source  The {@link LongSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Long(final @NonNull LongSource source, final @NonNull Enum<?> valueID) {
            target = source;
            this.valueID = valueID;
        }

        /**
         * Creates a basic statistic probe on a generic object.
         *
         * @param source        A generic source object.
         * @param valueName     The name of the field or the method returning the variable to be probed.
         * @param getFromMethod Specifies if valueName is a method or a property value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Long(final @NonNull Object source, final @NonNull String valueName, final boolean getFromMethod) {
            target = new LongInvoker(source, valueName, getFromMethod);
            valueID = LongSource.Variables.DEFAULT;
        }

        /**
         * Reads the source values and update statistics.
         */
        public void updateSource() {
            super.updateSource();
            if (target instanceof UpdatableSource) ((UpdatableSource) target).updateSource();
            lastRead = target.getLongValue(valueID);

            if (lastRead < min) min = lastRead;
            if (lastRead > max) max = lastRead;
            sum += lastRead;
            sumSquare += (lastRead * lastRead);
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MultiTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when {@code valueID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> valueID) {
            return switch ((MultiTraceFunction.Variables) valueID) {
                case LAST_VALUE -> (double) lastRead;
                case MAX -> (double) max;
                case MIN -> (double) min;
                case SUM -> (double) sum;
                default -> super.getDoubleValue(valueID);
            };
        }

        /**
         * Return the result of a given statistic.
         *
         * @param valueID One of the {@link MultiTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws UnsupportedOperationException If the given valueID is not supported.
         * @throws NullPointerException          when {valueID} is {@code null}.
         */
        public long getLongValue(@NonNull Enum<?> valueID) {
            if (valueID.equals(LongSource.Variables.DEFAULT))
                return lastRead;
            return switch ((MultiTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
                case MIN -> min;
                case COUNT -> count;
                case SUM -> sum;
                default -> throw new UnsupportedOperationException(valueID + " is not a defined function for " +
                    getClass() + ".");
            };
        }

        /**
         * The variance function.
         *
         * @return The variance value.
         */
        public double getVariance() {
            return count > 1 ? (double) (sumSquare - ((sum * sum) / count)) / (count - 1) : 0.0;
        }

        /**
         * The mean function.
         *
         * @return The mean value.
         */
        public double getMean() {
            return count > 0 ? (double) sum / count : 0.0;
        }

    }

    /**
     * An implementation of the Memoryless Series class, which manages double type data sources.
     */
    public static class Double extends MultiTraceFunction implements DoubleSource {
        private final Enum<?> valueID;
        @Getter
        protected double max = java.lang.Double.MIN_VALUE;
        @Getter
        protected double min = java.lang.Double.MAX_VALUE;
        @Getter
        protected double sum = 0, sumSquare = 0;
        protected DoubleSource target;
        @Getter
        private double lastRead;

        /**
         * Creates a basic statistic probe on a {@link DoubleSource} object.
         *
         * @param source  The {@link DoubleSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Double(final @NonNull DoubleSource source, final @NonNull Enum<?> valueID) {
            target = source;
            this.valueID = valueID;
        }

        /**
         * Creates a basic statistic probe on a generic object.
         *
         * @param source        A generic source object.
         * @param valueName     The name of the field or the method returning the variable to be probed.
         * @param getFromMethod Specifies if valueName is a method or a property value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Double(final @NonNull Object source, final @NonNull String valueName, final boolean getFromMethod) {
            target = new DoubleInvoker(source, valueName, getFromMethod);
            valueID = DoubleSource.Variables.DEFAULT;
        }

        /**
         * Reads the source values and update statistics.
         */
        public void updateSource() {
            super.updateSource();
            if (target instanceof UpdatableSource) ((UpdatableSource) target).updateSource();
            lastRead = target.getDoubleValue(valueID);

            if (lastRead < min) min = lastRead;
            if (lastRead > max) max = lastRead;
            sum += lastRead;
            sumSquare += (lastRead * lastRead);
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MultiTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> valueID) {
            if (valueID.equals(DoubleSource.Variables.DEFAULT))
                return lastRead;
            return switch ((MultiTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
                case MIN -> min;
                case SUM -> sum;
                default -> super.getDoubleValue(valueID);
            };
        }

        /**
         * The variance function.
         *
         * @return The variance value.
         */
        public double getVariance() {
            return count > 1 ? (sumSquare - ((sum * sum) / count)) / (count - 1) : 0.;
        }

        /**
         * The mean function.
         *
         * @return The mean value.
         */
        public double getMean() {
            return count > 0 ? sum / count : 0.;
        }

    }

    /**
     * An implementation of the Memoryless Series class, which manages integer type data sources.
     */
    public static class Integer extends MultiTraceFunction implements IntSource {
        private final Enum<?> valueID;
        @Getter
        protected int max = java.lang.Integer.MIN_VALUE;
        @Getter
        protected int min = java.lang.Integer.MAX_VALUE;
        @Getter
        protected int sum = 0;
        protected long sumSquare = 0;
        protected IntSource target;
        @Getter
        private int lastRead;

        /**
         * Creates a basic statistic probe on a {@link DoubleSource} object.
         *
         * @param source  The {@link DoubleSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Integer(final @NonNull IntSource source, final @NonNull Enum<?> valueID) {
            target = source;
            this.valueID = valueID;
        }

        /**
         * Creates a basic statistic probe on a generic object.
         *
         * @param source        A generic source object.
         * @param valueName     The name of the field or the method returning the variable to be probed.
         * @param getFromMethod Specifies if valueName is a method or a property value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Integer(final @NonNull Object source, final @NonNull String valueName, final boolean getFromMethod) {
            target = new IntegerInvoker(source, valueName, getFromMethod);
            valueID = IntSource.Variables.DEFAULT;
        }

        /**
         * Reads the source values and update statistics.
         */
        public void updateSource() {
            super.updateSource();
            if (target instanceof UpdatableSource) ((UpdatableSource) target).updateSource();
            lastRead = target.getIntValue(valueID);

            if (lastRead < min) min = lastRead;
            if (lastRead > max) max = lastRead;
            sum += lastRead;
            sumSquare += ((long) lastRead * lastRead);
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MultiTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when {@code valueID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> valueID) {
            return switch ((MultiTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
                case MIN -> min;
                case SUM -> sum;
                default -> super.getDoubleValue(valueID);
            };
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MultiTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws UnsupportedOperationException If the given {@code valueID} is not supported.
         * @throws NullPointerException when {@code valueID} is {@code null}.
         */
        public int getIntValue(final @NonNull Enum<?> valueID) {
            if (valueID.equals(IntSource.Variables.DEFAULT))
                return lastRead;
            return switch ((MultiTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
                case MIN -> min;
                case COUNT -> count;
                default -> throw new UnsupportedOperationException(valueID + " is not a defined function for " +
                    getClass() + ".");
            };
        }

        /**
         * The variance function.
         *
         * @return The variance value.
         */
        public double getVariance() {
            return count > 1 ? (double) (sumSquare - (((long) sum * sum) / count)) / (count - 1) : 0.0;
        }

        /**
         * The mean function.
         *
         * @return The mean value.
         */
        public double getMean() {
            return count > 0 ? (double) sum / count : 0.0;
        }
    }
}

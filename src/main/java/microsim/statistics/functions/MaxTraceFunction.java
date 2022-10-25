package microsim.statistics.functions;

import lombok.Getter;
import lombok.NonNull;
import microsim.event.CommonEventType;
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
public abstract class MaxTraceFunction extends AbstractFunction implements DoubleSource {

    protected int count = 0;

    /**
     * Collects a value from the source.
     */
    public void applyFunction() {
        count++;
    }

    /**
     * {@link microsim.event.EventListener} callback function. It supports only {@link CommonEventType#UPDATE} event.
     *
     * @param type The action id. Only {@link CommonEventType#UPDATE} is supported.
     * @throws SimulationRuntimeException If actionType is not supported.
     * @throws NullPointerException       when {@code type} is {@code null}.
     */
    @Override
    public void onEvent(final @NonNull Enum<?> type) {
        if (type.equals(CommonEventType.UPDATE)) updateSource();
        else throw new SimulationRuntimeException("The SimpleStatistics object does not support " + type +
            " operation.");
    }

    public enum Variables {
        LAST_VALUE,
        MAX
    }

    /**
     * An implementation of the Memoryless Series class, which manages long type data sources.
     */
    public static class Long extends MaxTraceFunction implements LongSource {
        private final Enum<?> valueID;
        @Getter
        protected long max = java.lang.Long.MIN_VALUE;
        protected LongSource target;
        @Getter
        private long lastRead;

        /**
         * Creates a basic statistic probe on a {@link LongSource} object.
         *
         * @param source  The {@link LongSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Long(final @NonNull LongSource source, final @NonNull Enum<?> valueID) {
            super();
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
            super();
            target = new LongInvoker(source, valueName, getFromMethod);
            valueID = LongSource.Variables.DEFAULT;
        }

        /**
         * Reads the source values and update statistics.
         */
        public void applyFunction() {
            super.applyFunction();
            if (target instanceof UpdatableSource) ((UpdatableSource) target).updateSource();
            lastRead = target.getLongValue(valueID);

            if (lastRead > max) max = lastRead;
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MaxTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when {@code valueId} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> valueID) {
            return switch ((MaxTraceFunction.Variables) valueID) {
                case LAST_VALUE -> (double) lastRead;
                case MAX -> (double) max;
            };
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MaxTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when {@code valueID} is {@code null}.
         */
        public long getLongValue(final @NonNull Enum<?> valueID) {
            return switch ((MaxTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
            };
        }
    }

    /**
     * An implementation of the Memoryless Series class, which manages double type data sources.
     */
    public static class Double extends MaxTraceFunction implements DoubleSource {
        private final Enum<?> valueID;
        @Getter
        protected double max = java.lang.Double.MIN_VALUE;
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
            super();
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
            super();
            target = new DoubleInvoker(source, valueName, getFromMethod);
            valueID = DoubleSource.Variables.DEFAULT;
        }

        /**
         * Reads the source values and update statistics.
         */
        public void applyFunction() {
            super.applyFunction();
            if (target instanceof UpdatableSource) ((UpdatableSource) target).updateSource();
            lastRead = target.getDoubleValue(valueID);
            if (lastRead > max) max = lastRead;
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MaxTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> valueID) {
            return switch ((MaxTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
            };
        }
    }

    /**
     * An implementation of the Memoryless Series class, which manages integer type data sources.
     */
    public static class Integer extends MaxTraceFunction implements IntSource {
        private final Enum<?> valueID;
        @Getter
        protected int max = java.lang.Integer.MIN_VALUE;
        protected IntSource target;
        @Getter
        private int lastRead;

        /**
         * Creates a basic statistic probe on a {@link IntSource} object.
         *
         * @param source  The {@link IntSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Integer(final @NonNull IntSource source, final @NonNull Enum<?> valueID) {
            super();
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
            super();
            target = new IntegerInvoker(source, valueName, getFromMethod);
            valueID = IntSource.Variables.DEFAULT;
        }

        /**
         * Reads the source values and update statistics.
         */
        public void applyFunction() {
            super.applyFunction();
            if (target instanceof UpdatableSource) ((UpdatableSource) target).updateSource();
            lastRead = target.getIntValue(valueID);
            if (lastRead > max) max = lastRead;
        }

        /**
         * Returns the result of a given statistic.
         *
         * @param valueID One of the {@link MaxTraceFunction.Variables} constants representing available statistics.
         * @return The computed value.
         * @throws NullPointerException when {@code valueID} is {@code null}.
         */
        public double getDoubleValue(final @NonNull Enum<?> valueID) {
            return switch ((MaxTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
            };
        }

        public int getIntValue(final @NonNull Enum<?> valueID) {
            return switch ((MaxTraceFunction.Variables) valueID) {
                case LAST_VALUE -> lastRead;
                case MAX -> max;
            };
        }
    }
}

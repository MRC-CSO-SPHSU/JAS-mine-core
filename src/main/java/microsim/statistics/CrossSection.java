package microsim.statistics;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.statistics.reflectors.DoubleInvoker;
import microsim.statistics.reflectors.IntegerInvoker;
import microsim.statistics.reflectors.LongInvoker;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

/**
 * A cross-section is a collection of values each of them representing the status of a given variable of an element of a
 * collection of agents.
 */
public abstract class CrossSection implements EventListener, UpdatableSource, SourceObjectArray {
    @Getter
    protected Object[] sourceArray;

    protected TimeChecker timeChecker = new TimeChecker();

    @Setter
    @Getter
    protected CollectionFilter filter;

    /**
     * A helper method, allows to build the string representation of an object.
     *
     * @param v    An object.
     * @param name The object's name/
     * @return a string.
     */
    private static @NonNull String builder(final Object v, final String name) {
        StringBuilder buf = new StringBuilder();
        buf.append("CrossSection.").append(name).append(" [");
        int size = Array.getLength(v) - 1;
        IntStream.range(0, size).forEachOrdered(i -> buf.append(Array.get(v, i)).append(" "));
        buf.append(Array.get(v, size)).append("]");
        return buf.toString();
    }

    public abstract void updateSource();

    /**
     * {@link EventListener} callback function. It supports only {@link CommonEventType#UPDATE} event.
     *
     * @param type The action id. Only {@link CommonEventType#UPDATE} is supported.
     * @throws UnsupportedOperationException If actionType is not supported.
     */
    public void onEvent(final @NonNull Enum<?> type) {
        if (type.equals(CommonEventType.UPDATE)) updateSource();
        else
            throw new UnsupportedOperationException("The SimpleStatistics object does not support " + type +
                " operation.");
    }

    /**
     * Returns the current status of the time checker. A time checker avoid the object to update more than one time per
     * simulation step. The default value is enabled (true).
     *
     * @return True if the computer is currently checking time before update cached data, false if disabled.
     */
    public boolean isCheckingTime() {
        return timeChecker.isEnabled();
    }

    /**
     * Sets the current status of the time checker. A time checker avoid the object to update more than one time per
     * simulation step. The default value is enabled (true).
     *
     * @param b True if the computer is currently checking time before update cached data, false if disabled.
     */
    public void setCheckingTime(final boolean b) {
        timeChecker.setEnabled(b);
    }

    public static class Double extends CrossSection implements DoubleArraySource {
        protected double[] valueList;

        protected DoubleInvoker invoker;
        protected Collection<?> target;
        protected Enum<?> valueID;

        /**
         * Creates a statistic probe on a collection of {@link DoubleSource} objects.
         *
         * @param source  The collection containing {@link DoubleSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Double(final @NonNull Collection<?> source, final @NonNull Enum<?> valueID) {
            target = source;
            this.valueID = valueID;
        }

        /**
         * Creates a statistic probe on a collection of {@link DoubleSource} objects. It uses the
         * {@link DoubleSource.Variables#DEFAULT} variable id.
         *
         * @param source The collection containing {@link DoubleSource} object.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Double(final @NonNull Collection<?> source) {
            target = source;
            this.valueID = DoubleSource.Variables.DEFAULT;
        }

        /**
         * Creates a basic statistic probe on a collection of objects.
         *
         * @param source        A collection of generic objects.
         * @param objectClass   The class of the objects contained by collection source.
         * @param valueName     The name of the field or the method returning the variable to be probed.
         * @param getFromMethod Specifies if valueName is a method or a property value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Double(final @NonNull Collection<?> source, final @NonNull Class<?> objectClass,
                      final @NonNull String valueName, final boolean getFromMethod) {
            target = source;
            this.valueID = DoubleSource.Variables.DEFAULT;
            invoker = new DoubleInvoker(objectClass, valueName, getFromMethod);
        }

        public double @NonNull [] getDoubleArray() {
            return valueList;
        }

        public @NonNull String toString() {
            return builder(valueList, "Double");
        }

        /**
         * Updates the state of the object when it's not up-to-date.
         */
        public void updateSource() {
            if (timeChecker.isUpToDate()) return;

            valueList = new double[target.size()];
            sourceArray = new Object[valueList.length];

            int i = 0;
            if (filter != null) {
                if (invoker != null)
                    for (Object obj : target) {
                        if (filter.isFiltered(obj)) {
                            valueList[i] = invoker.getDouble(obj);
                            sourceArray[i++] = obj;
                        }
                    }
                else
                    for (Object obj : target) {
                        if (filter.isFiltered(obj)) {
                            valueList[i] = ((DoubleSource) obj).getDoubleValue(valueID);
                            sourceArray[i++] = obj;
                        }
                    }
                valueList = cern.mateba.Arrays.trimToCapacity(valueList, i);
                sourceArray = cern.mateba.Arrays.trimToCapacity(sourceArray, i);
            } else if (invoker != null)
                for (Object o : target) {
                    valueList[i] = invoker.getDouble(o);
                    sourceArray[i++] = o;
                }
            else
                for (Object o : target) {
                    valueList[i] = ((DoubleSource) o).getDoubleValue(valueID);
                    sourceArray[i++] = o;
                }

        }

    }

    public static class Long extends CrossSection implements LongArraySource {
        protected long[] valueList;

        protected LongInvoker invoker;
        protected Collection<?> target;
        protected Enum<?> valueID;

        /**
         * Creates a statistic probe on a collection of {@link LongSource} objects.
         *
         * @param source  The collection containing {@link LongSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Long(final @NonNull Collection<?> source, final @NonNull Enum<?> valueID) {
            target = source;
            this.valueID = valueID;
        }

        /**
         * Creates a statistic probe on a collection of LongSource objects. It uses the
         * {@link LongSource.Variables#DEFAULT} variable id.
         *
         * @param source The collection containing {@link LongSource} object.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Long(final @NonNull Collection<?> source) {
            target = source;
            this.valueID = LongSource.Variables.DEFAULT;
        }

        /**
         * Creates a basic statistic probe on a collection of objects.
         *
         * @param source        A collection of generic objects.
         * @param objectClass   The class of the objects contained by collection source.
         * @param valueName     The name of the field or the method returning the variable to be probed.
         * @param getFromMethod Specifies if valueName is a method or a property value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Long(final @NonNull Collection<?> source, final @NonNull Class<?> objectClass,
                    final @NonNull String valueName, final boolean getFromMethod) {
            target = source;
            this.valueID = LongSource.Variables.DEFAULT;
            invoker = new LongInvoker(objectClass, valueName, getFromMethod);
        }

        public long @NonNull [] getLongArray() {
            return valueList;
        }

        public double @NonNull [] getDoubleArray() {
            return Arrays.stream(valueList).asDoubleStream().toArray();
        }

        public String toString() {
            return builder(valueList, "Long");
        }

        /**
         * Updates the state of the object when it's not up-to-date.
         */
        public void updateSource() {
            if (timeChecker.isUpToDate()) return;

            valueList = new long[target.size()];
            sourceArray = new Object[valueList.length];

            int i = 0;
            if (filter != null) {
                if (invoker != null)
                    for (Object obj : target) {
                        if (filter.isFiltered(obj)) {
                            valueList[i] = invoker.getLong(obj);
                            sourceArray[i++] = obj;
                        }
                    }
                else
                    for (Object obj : target) {
                        if (filter.isFiltered(obj)) {
                            valueList[i] = ((LongSource) obj).getLongValue(valueID);
                            sourceArray[i++] = obj;
                        }
                    }
                valueList = cern.mateba.Arrays.trimToCapacity(valueList, i);
                sourceArray = cern.mateba.Arrays.trimToCapacity(sourceArray, i);
            } else if (invoker != null)
                for (Object o : target) {
                    valueList[i] = invoker.getLong(o);
                    sourceArray[i++] = o;
                }
            else
                for (Object o : target) {
                    valueList[i] = ((LongSource) o).getLongValue(valueID);
                    sourceArray[i++] = o;
                }

        }
    }

    public static class Integer extends CrossSection implements IntArraySource {
        protected int[] valueList;

        protected IntegerInvoker invoker;
        protected Collection<?> target;
        protected Enum<?> valueID;

        /**
         * Creates a statistic probe on a collection of {@link IntSource} objects.
         *
         * @param source  The collection containing {@link IntSource} object.
         * @param valueID The value identifier defined by source object.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Integer(final @NonNull Collection<?> source, final @NonNull Enum<?> valueID) {
            target = source;
            this.valueID = valueID;
        }

        /**
         * Create a statistic probe on a collection of {@link IntSource} objects.
         * It uses the {@link IntSource.Variables#DEFAULT} variable id.
         *
         * @param source The collection containing {@link IntSource} object.
         * @throws NullPointerException when {@code source} is {@code null}.
         */
        public Integer(final @NonNull Collection<?> source) {
            target = source;
            this.valueID = IntSource.Variables.DEFAULT;
        }

        /**
         * Create a basic statistic probe on a collection of objects.
         *
         * @param source        A collection of generic objects.
         * @param objectClass   The class of the objects contained by collection source.
         * @param valueName     The name of the field or the method returning the variable to be probed.
         * @param getFromMethod Specifies if valueName is a method or a property value.
         * @throws NullPointerException when any of the input parameters is {@code null}.
         */
        public Integer(final @NonNull Collection<?> source, final @NonNull Class<?> objectClass,
                       final @NonNull String valueName, final boolean getFromMethod) {
            target = source;
            this.valueID = IntSource.Variables.DEFAULT;
            invoker = new IntegerInvoker(objectClass, valueName, getFromMethod);
        }

        public int @NonNull [] getIntArray() {
            return valueList;
        }

        public double @NonNull [] getDoubleArray() {
            return Arrays.stream(valueList).asDoubleStream().toArray();
        }

        public @NonNull String toString() {
            return builder(valueList, "Int");
        }

        /**
         * Updates the state of the object when it's not up-to-date.
         */
        public void updateSource() {
            if (timeChecker.isUpToDate()) return;

            valueList = new int[target.size()];
            sourceArray = new Object[valueList.length];

            int i = 0;
            if (filter != null) {
                if (invoker != null)
                    for (Object obj : target) {
                        if (filter.isFiltered(obj)) {
                            valueList[i] = invoker.getInt(obj);
                            sourceArray[i++] = obj;
                        }
                    }
                else
                    for (Object obj : target) {
                        if (filter.isFiltered(obj)) {
                            valueList[i] = ((IntSource) obj).getIntValue(valueID);
                            sourceArray[i++] = obj;
                        }
                    }
                valueList = cern.mateba.Arrays.trimToCapacity(valueList, i);
                sourceArray = cern.mateba.Arrays.trimToCapacity(sourceArray, i);
            } else if (invoker != null)
                for (Object o : target) {
                    valueList[i] = invoker.getInt(o);
                    sourceArray[i++] = o;
                }
            else
                for (Object o : target) {
                    valueList[i] = ((IntSource) o).getIntValue(valueID);
                    sourceArray[i++] = o;
                }
        }
    }
}

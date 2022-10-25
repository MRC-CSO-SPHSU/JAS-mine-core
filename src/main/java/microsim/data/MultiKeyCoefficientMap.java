package microsim.data;

import lombok.NonNull;
import lombok.val;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.AbstractHashedMap;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * This class is an implementation of a {@link Map} that has multiple keys and multiple values <em>at the same</em>
 * time.
 */
public class MultiKeyCoefficientMap extends MultiKeyMap {

    @Serial
    private static final long serialVersionUID = 5049597007431364596L;

    protected String[] keys;
    protected Map<String, Integer> valuesMap;

    /**
     * Creates an empty new {@link MultiKeyCoefficientMap} with the names of the keys and values categories specified by
     * {@link String}{@code []} keys and {@link String}{@code []} values arguments.
     *
     * @param keys   A {@link String} array listing the names of the categories of keys.
     * @param values A {@link String} array listing the names of the categories of values.
     * @throws NullPointerException when {@code keys} or any of its elements is {@code 0}.
     */
    public MultiKeyCoefficientMap(final @NonNull String @Nullable [] keys, final @Nullable String @Nullable [] values) {
        super();
        this.keys = keys;
        if (values != null) {
            valuesMap = new HashMap<>();
            IntStream.range(0, values.length).forEach(i -> valuesMap.put(values[i], i));
        }
    }

    /**
     * Creates a new {@link MultiKeyCoefficientMap} with values stored in map, and with the names of the keys and values
     * categories specified by {@link String}{@code []} keys and {@link String}{@code []} values arguments.
     *
     * @param map    Contains the values of the MultiKeyCoefficientMap.
     * @param keys   A {@link String} array listing the names of the categories of keys.
     * @param values A {@link String} array listing the names of the categories of values.
     * @throws NullPointerException when {@code map} is {@code null}.
     */
    public MultiKeyCoefficientMap(final @NonNull AbstractHashedMap map, final @NonNull String @Nullable [] keys,
                                  final @Nullable String @Nullable [] values) {
        super(map);
        this.keys = keys;
        if (values != null) {
            valuesMap = new HashMap<>();
            IntStream.range(0, values.length).forEach(i -> valuesMap.put(values[i], i));
        }
    }

    /**
     * Converts {@code value} to a {@link String}.
     *
     * @param value An object that can be {@link String}, {@link Double}, or {@link Boolean}.
     * @return the string representation of {@code value}.
     * @throws NullPointerException when {@code value} is {@code null}.
     */
    public static @NonNull String toStringKey(final @NonNull Object value) {
        if (value instanceof String s) {
            return s;
        } else if (value instanceof Double aDouble) {
            return aDouble.toString();
        } else if (value instanceof Boolean aBoolean) {
            return aBoolean.toString();
        }
        return value.toString();
    }

    /**
     * Gets an object from a vector using the provided {@code key}.
     *
     * @param key    A {@link String} representing a key.
     * @param vector An array of objects, the array itself or any of its elements can be {@code null}.
     * @return {@code null} when {@code vector} is {@code null} or when the value that corresponds to {@code key} is
     * {@code null}; an element of {@code vector} otherwise.
     * @throws NullPointerException when {@code key} is {@code null}.
     */
    private @Nullable Object extractValueFromVector(final @NonNull String key,
                                                    final @Nullable Object @Nullable [] vector) {
        return vector == null ? null : valuesMap.get(key) == null ? null : vector[valuesMap.get(key)];
    }

    /**
     * Assigns a value to a given element of a vector.
     *
     * @param key    A {@link String} representing a key.
     * @param vector A vector of objects to be updated, can be {@code null}.
     * @param value  An object to be passed to {@code vector}, can be {@code null}.
     * @throws NullPointerException when {@code key} is {@code null}.
     */
    private void putValueToVector(@NonNull String key, @Nullable Object[] vector, @Nullable Object value) {
        if (vector != null && valuesMap.get(key) != null) vector[valuesMap.get(key)] = value;
    }

    /**
     * Extracts the value that corresponds to {@code key} from the map.
     *
     * @param key A key or an array of keys.
     * @return the value that corresponds to {@code key}.
     * @throws NullPointerException     when {@code key} is {@code null} or when it's an array containing at least one
     *                                  {@code null} object.
     * @throws IllegalArgumentException when the total number of keys in {@code key} exceeds a certain threshold.
     */
    public @Nullable Object getValue(final @NonNull Object @NonNull ... key) {
        if (key.length == keys.length) {
            return switch (key.length) {
                case 1 -> super.get(key[0] instanceof MultiKey ? key[0] : new MultiKey<>(new Object[]{key[0]}));
                case 2 -> super.get(key[0], key[1]);
                case 3 -> super.get(key[0], key[1], key[2]);
                case 4 -> super.get(key[0], key[1], key[2], key[3]);
                case 5 -> super.get(key[0], key[1], key[2], key[3], key[4]);
                default -> throw new IllegalArgumentException("Wrong number of key parameters");
            };
        } else if (key.length == keys.length + 1) {
            Object[] value;
            switch (key.length) {
                case 2 -> {
                    if (key[0] instanceof MultiKey)
                        //Ross: If we don't do this check, a new MultiKey of a MultiKey is created unnecessarily,
                        // which then leads to a null pointer exception
                        // as the MultKeyCoefficientMap does not have a key entry of the type MultiKey(MultiKey()).
                        value = (Object[]) super.get(key[0]);
                    else value = (Object[]) super.get(new MultiKey<>(new Object[]{key[0]}));
                    return extractValueFromVector(toStringKey(key[1]), value);
                }
                case 3 -> {
                    value = (Object[]) super.get(key[0], key[1]);
                    return extractValueFromVector(toStringKey(key[2]), value);
                }
                case 4 -> {
                    value = (Object[]) super.get(key[0], key[1], key[2]);
                    return extractValueFromVector(toStringKey(key[3]), value);
                }
                case 5 -> {
                    value = (Object[]) super.get(key[0], key[1], key[2], key[3]);
                    return extractValueFromVector(toStringKey(key[4]), value);
                }
                case 6 -> {
                    value = (Object[]) super.get(key[0], key[1], key[2], key[3], key[4]);
                    return extractValueFromVector(toStringKey(key[5]), value);
                }
                default -> throw new IllegalArgumentException("Wrong number of key parameters");
            }
        } else
            throw new IllegalArgumentException("Wrong number of key parameters");
    }

    /**
     * Adds a value to the map.
     *
     * @param keyValues A single key or an array or keys.
     * @throws NullPointerException     when the input is {@code null} or contains {@code null}.
     * @throws IllegalArgumentException when the number of keys exceeds the limit.
     */
    public void putValue(final @NonNull Object @NonNull ... keyValues) {
        if (keyValues.length == keys.length + 1) {
            switch (keyValues.length) {
                case 2:
                    //Ross: If we don't do this check, a new MultiKey of a MultiKey is created unnecessarily,
                    // which then leads to a null pointer exception
                    // as the MultKeyCoefficientMap does not have a key entry of the type MultiKey(MultiKey()).
                    if (keyValues[0] instanceof MultiKey)
                        super.put((MultiKey<?>) keyValues[0], keyValues[1]);
                    else {
                        super.put(new MultiKey<>(new Object[]{keyValues[0]}), keyValues[1]);
                    }
                    break;
                case 3:
                    super.put(keyValues[0], keyValues[1], keyValues[2]);
                    break;
                case 4:
                    super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3]);
                    break;
                case 5:
                    super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4]);
                    break;
                case 6:
                    super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4], keyValues[5]);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong number of key parameters");
            }
        } else if (keyValues.length == keys.length + 2) {
            Object[] value;
            switch (keyValues.length) {
                case 3 -> {
                    value = (Object[]) super.get(keyValues[0]);
                    if (value == null)
                        value = new Object[valuesMap.size()];
                    putValueToVector((String) keyValues[1], value, keyValues[2]);
                    super.put(new MultiKey<>(new Object[]{keyValues[0]}), value);
                }
                case 4 -> {
                    value = (Object[]) super.get(keyValues[0], keyValues[1]);
                    if (value == null)
                        value = new Object[valuesMap.size()];
                    putValueToVector((String) keyValues[2], value, keyValues[3]);
                    super.put(keyValues[0], keyValues[1], value);
                }
                case 5 -> {
                    value = (Object[]) super.get(keyValues[0], keyValues[1], keyValues[2]);
                    if (value == null)
                        value = new Object[valuesMap.size()];
                    putValueToVector((String) keyValues[3], value, keyValues[4]);
                    super.put(keyValues[0], keyValues[1], keyValues[2], value);
                }
                case 6 -> {
                    value = (Object[]) super.get(keyValues[0], keyValues[1], keyValues[2], keyValues[3]);
                    if (value == null)
                        value = new Object[valuesMap.size()];
                    putValueToVector((String) keyValues[4], value, keyValues[5]);
                    super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], value);
                }
                case 7 -> {
                    value = (Object[]) super.get(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4]);
                    if (value == null)
                        value = new Object[valuesMap.size()];
                    putValueToVector((String) keyValues[5], value, keyValues[6]);
                    super.put(keyValues[0], keyValues[1], keyValues[2], keyValues[3], keyValues[4], value);
                }
                default -> throw new IllegalArgumentException("Wrong number of key parameters");
            }
        } else
            throw new IllegalArgumentException("Wrong number of key parameters");
    }

    /**
     * This method allows an instance of the {@link MultiKeyCoefficientMap} to provide a clone of the names of the keys.
     * This is especially useful for getting the name of the variables used as keys in the
     * {@link microsim.statistics.regression} classes.
     *
     * @return a {@link String} array clone of the names of the {@link MultiKeyCoefficientMap}'s keys.
     */
    public @NonNull String @Nullable [] getKeysNames() {
        val keysClone = new String[keys.length];
        System.arraycopy(keys, 0, keysClone, 0, keys.length);
        return keysClone;
    }

    /**
     * This method allows an instance of the {@link MultiKeyCoefficientMap} to provide a clone of the names of the
     * values.
     *
     * @return a {@link String} array clone of the names of the {@link MultiKeyCoefficientMap}'s values
     */
    public @NonNull String @Nullable [] getValuesNames() {
        val valuesClone = new String[valuesMap.size()];
        for (var name : valuesMap.keySet()) valuesClone[valuesMap.get(name)] = name;
        return valuesClone;
    }

    /**
     * @return a deep clone copy of the {@link MultiKeyCoefficientMap} object
     */
    @Override
    public MultiKeyCoefficientMap clone() {
        var mapClone = new HashedMap(this.decorated());
        return new MultiKeyCoefficientMap(mapClone, this.getKeysNames(), this.getValuesNames());
    }
}

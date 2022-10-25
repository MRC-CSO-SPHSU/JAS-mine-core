package microsim.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class MultiKeyHashMap extends HashMap<Integer, MultiKeyHashMap.EntryValue> {

    @Serial
    private static final long serialVersionUID = 4939180438185813582L;

    /**
     * Calculates the total sum of all hashcodes as if they were simple integers.
     *
     * @param keyArray An array of keys which are used for further calculations of hashcodes.
     * @return The total sum of all hashcodes.
     * @throws NullPointerException when the input array or any of its elements is {@code null}.
     */
    public int getHashKey(final @NonNull Object @NonNull [] keyArray) {
        return Arrays.stream(keyArray).mapToInt(Object::hashCode).sum();
    }

    /**
     * Searches through all the keys
     * @param keyArray
     * @return
     */
    public boolean containsKey(final @NonNull Object @NonNull ... keyArray) {
        return super.containsKey(getHashKey(keyArray));
    }

    /**
     * Adds
     * @param keyArray
     * @param valueArray
     * @return
     */
    public @NonNull Object[] put(final @NonNull Object[] keyArray, final @NonNull Object[] valueArray) {
        return Objects.requireNonNull(super.put(getHashKey(keyArray),
            new EntryValue(keyArray, valueArray))).getValueArray();
    }

    public @NonNull Object[] remove(final @NonNull Object... keyArray) {
        return super.remove(getHashKey(keyArray)).getValueArray();
    }

    public @NonNull Object[] get(final @NonNull Object... keyArray) {
        return super.get(getHashKey(keyArray)).getValueArray();
    }

    public static class EntryValue {

        @Setter
        @Getter
        private Object[] keyArray;
        @Setter
        @Getter
        private Object[] valueArray;

        public EntryValue(final @NonNull Object[] keyArray, final @NonNull Object[] valueArray) {
            super();
            this.keyArray = keyArray;
            this.valueArray = valueArray;
        }
    }
}

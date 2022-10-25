package microsim.statistics;

import lombok.NonNull;

/**
 * Used by statistical object to access array of long values.
 */
public interface LongArraySource {
    /**
     * Returns the currently cached array of long values.
     *
     * @return An array of long or a null pointer if the source is empty.
     */
    long @NonNull [] getLongArray();
}

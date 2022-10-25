package microsim.statistics;

import lombok.NonNull;

/**
 * Used by statistical object to access long data. Each variable must have a unique integer id.
 */
public interface LongSource {
    /**
     * Returns the long value corresponding to the given {@code variableID}
     *
     * @param variableID A unique identifier for a variable.
     * @return The current long value of the required variable.
     * @throws NullPointerException when {@code variableID} is {@code null}.
     */
    long getLongValue(final @NonNull Enum<?> variableID);

    enum Variables {
        DEFAULT
    }
}

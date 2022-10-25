package microsim.statistics;

import lombok.NonNull;

/**
 * Used by statistical object to access integer data. Each variable must have a unique integer id.
 */
public interface IntSource {
    /**
     * Returns the integer value corresponding to the given {@code variableID}
     *
     * @param variableID A unique identifier for a variable.
     * @return The current integer value of the required variable.
     * @throws NullPointerException when {@code variableID} is {@code null}.
     */
    int getIntValue(final @NonNull Enum<?> variableID);

    enum Variables {
        DEFAULT
    }
}

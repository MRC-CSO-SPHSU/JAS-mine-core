package microsim.alignment.outcome;

import lombok.NonNull;

/**
 * An interface to handle event-related attributes of agents.
 * @param <T> A generic type representing agents.
 */
public interface AlignmentOutcomeClosure<T> {

    /**
     * A method to extract the binary outcome value from an agent.
     * @param agent An agent.
     * @return a boolean value, {@code 0} or {@code 1}.
     * @throws NullPointerException when {@code agent} is {@code null}.
     */
    boolean getOutcome(final @NonNull T agent);

    /**
     * Attempts to resample (change) the binary outcome value of an agent.
     * @param agent An agent.
     * @throws NullPointerException when {@code agent} is {@code null}.
     */
    void resample(final @NonNull T agent);
}

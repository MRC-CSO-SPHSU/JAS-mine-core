package microsim.matching;

import lombok.NonNull;

public interface MatchingScoreClosure<T> {

    /**
     * Allows to get the matching score for a pair og agents.
     *
     * @param item1 An agent.
     * @param item2 A matching candidate.
     * @return the score.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    double getValue(final @NonNull T item1, final @NonNull T item2);

}

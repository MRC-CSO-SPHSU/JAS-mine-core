package microsim.matching;

import lombok.NonNull;

public interface MatchingClosure<T> {

    /**
     * Links two agents to form a matching pair.
     *
     * @param t1 An agent.
     * @param t2 The agent's partner.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    void match(final @NonNull T t1, final @NonNull T t2);

}

package microsim.matching;

import lombok.NonNull;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.math3.util.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public interface IterativeMatchingAlgorithm<T> {

    /**
     * Attempts to match people from two disjoint sets.
     *
     * @param collection1   A set of agents.
     * @param filter1       A filter for {@code collection1}.
     * @param collection2   Another set of agents to match to.
     * @param filter2       A filter for {@code collection2}.
     * @param doubleClosure An object implementing {@link MatchingScoreClosure} that allows scoring of pairs.
     * @param matching      An object implementing {@link MatchingClosure} to match pairs of agents.
     * @return all people with no pair to be passed to other matching methods.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    Pair<Set<T>, Set<T>> matching(final @NonNull Collection<T> collection1, final @NonNull Predicate<T> filter1,
                                  final @NonNull Collection<T> collection2, final @NonNull Predicate<T> filter2,
                                  final @NonNull MatchingScoreClosure<T> doubleClosure,
                                  final @NonNull MatchingClosure<T> matching);

}

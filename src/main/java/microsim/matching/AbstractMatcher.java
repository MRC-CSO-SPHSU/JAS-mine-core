package microsim.matching;

import lombok.NonNull;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract class for <em>all</em> matching procedures. Contains common methods only.
 *
 * @param <T> A generic object representing agents.
 */
public abstract class AbstractMatcher<T> {

    /**
     * A filtering method, allows selection of agents according to the provided {@code filter}. Does not change the
     * original collection.
     *
     * @param collection A collection of agents.
     * @param filter     A logical predicate for agent selection.
     * @return A list of filtered agents.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public @NonNull List<T> filterAgents(final @NonNull Collection<T> collection, final @Nullable Predicate<T> filter) {
        val filteredAgents = new ArrayList<T>();
        if (filter != null) CollectionUtils.select(collection, filter, filteredAgents);
        else filteredAgents.addAll(collection);
        return filteredAgents;
    }

    /**
     * A validation method, it ensures that two lists <em>do not</em> intersect.
     *
     * @param c1 A list of agents.
     * @param c2 Another list to check against.
     * @throws NullPointerException     when any of the input parameters is {@code null}.
     * @throws IllegalArgumentException when {@code c1} and {@code c2} have share at least one agent.
     */
    public void validateDisjointSets(final @NonNull List<T> c1, final @NonNull List<T> c2) {
        if (CollectionUtils.intersection(c1, c2).size() > 0)
            throw new IllegalArgumentException("Lists of people for matching must not intersect!");
    }
}

package microsim.alignment;

import lombok.NonNull;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A generic utility class with methods that are used across the {@link microsim.alignment} module.
 *
 * @param <T> A generic type describing an agent.
 */
public interface AlignmentUtils<T> {
    /**
     * Sorts out {@code agents} of type {@code <T>} according to the filter requirements.
     *
     * @param agents An unsorted collection of agents.
     * @param filter One predicate to filter them all or {@code null} if there is no need to filter the agents.
     * @return a filtered list of agents, or the same collection transformed to a list when the filter is {@code null};
     * always not {@code null}.
     * @throws NullPointerException when the  {@code agents} collection is null.
     */
    @NonNull
    default List<T> extractAgentList(final @NonNull Collection<T> agents,
                                     final @Nullable Predicate<T> filter) {
        val list = new ArrayList<T>();
        if (filter != null) CollectionUtils.select(agents, filter, list);
        else list.addAll(agents);
        return list;
    }

    /**
     * Validates provided input values.
     *
     * @param value A probability value.
     * @throws IllegalArgumentException if the value is out of {@code [0, 1]} range.
     */
    default void validateProbabilityValue(final double value) {
        if (value < 0. || value > 1.)
            throw new IllegalArgumentException("Probability value must lie in the closed range [0,1].");
    }
}

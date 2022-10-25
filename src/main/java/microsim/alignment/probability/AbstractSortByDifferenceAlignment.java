package microsim.alignment.probability;

import cern.jet.random.engine.RandomEngine;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;
import microsim.alignment.AlignmentUtils;
import microsim.engine.SimulationEngine;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

/**
 * An abstract class for all versions of the <em>sort-by-difference</em> algorithm.
 *
 * @param <T> A class usually representing an agent.
 * @see <a href="https://www.jasss.org/17/1/15.html">Jinjing Li and Cathal O'Donoghue, Evaluating Binary Alignment
 * Methods in Microsimulation Models, Journal of Artificial Societies and Social Simulation 17 (1) 15</a>
 */

@Log
abstract public class AbstractSortByDifferenceAlignment<T> implements AlignmentUtils<T> {// todo find the original research

    /**
     * The main alignment method that sorts by differences between predicted probability and a random number.
     *
     * @param agents            A collection of agents.
     * @param filter            A logical filter that allows filtering out certain agents.
     * @param closure           A closure that allows probability value manipulations, can be {@code null}.
     * @param targetProbability The resulting average probability that the method might converge to.
     * @throws NullPointerException when {@code agents}, or {@code closure}, or both are {@code null}.
     */
    public void align(final @NonNull Collection<T> agents, final @Nullable Predicate<T> filter,
                      final @NonNull AlignmentProbabilityClosure<T> closure,
                      final double targetProbability) {
        validateProbabilityValue(targetProbability);
        val list = extractAgentList(agents, filter);
        log.log(Level.INFO, "The total number of filtered individuals is %d".formatted(list.size()));

        Map<T, Double> map = new HashMap<>();
        val pValues = list.stream().mapToDouble(closure::getProbability).toArray();
        val rValues = new double[list.size()];

        for (int i = 0; i < list.size(); i++)
            rValues[i] = SimulationEngine.getRnd().nextDouble(RandomEngine.unitIntervalTypes.OPEN);

        val sortingVariable = generateSortingVariable(pValues, rValues);
        for (var i = 0; i < list.size(); i++) map.put(list.get(i), sortingVariable[i]);
        map = sortByComparator(map, false);

        int i = 0;

        for (T agent : map.keySet()) {
            closure.align(agent, i <= targetProbability * list.size() ? 1.0 : 0.0);
            i++;
        }
    }

    /**
     * Sorting of objects of type T (usually the agents) by an associated Double number. This method is used in the
     * {@link SBDAlignment} and in {@link SBDLAlignment} algorithms.
     *
     * @param unsortedMap    An unsorted map of objects of some type.
     * @param ascendingOrder If true, the method returns a map ordered by the Double value increasing, otherwise the map
     *                       will be ordered by the Double value decreasing.
     * @return a LinkedHashMap of type T objects that maintains the order of insertion, always not {@code null}.
     * @throws NullPointerException when {@code unsortedMap} is {@code null}.
     */
    protected @NonNull Map<T, Double> sortByComparator(final @NonNull Map<T, Double> unsortedMap,
                                                       final boolean ascendingOrder) {
        val list = new LinkedList<>(unsortedMap.entrySet());

        list.sort((o1, o2) -> o1.getValue().compareTo(ascendingOrder ? o2.getValue() : o1.getValue()));

        val sortedMap = new LinkedHashMap<T, Double>();
        for (var entry : list) sortedMap.put(entry.getKey(), entry.getValue());

        return sortedMap;
    }

    /**
     * Generates a sorting variable of a particular kind.
     *
     * @param probability Probability values.
     * @param random      Random values in {@code (0, 1)}.
     * @return an array of values, always not {@code null}.
     * @throws NullPointerException when {@code probability}, or {@code random}, or both are {@code null}.
     * @see <a href="https://www.jasss.org/17/1/15.html">Jinjing Li and Cathal O'Donoghue, Evaluating Binary Alignment
     * Methods in Microsimulation Models, Journal of Artificial Societies and Social Simulation 17 (1) 15</a>
     */
    abstract double @NonNull [] generateSortingVariable(final double @NonNull [] probability,
                                                        final double @NonNull [] random);
}

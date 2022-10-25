package microsim.alignment.probability;

import lombok.NonNull;
import lombok.val;
import microsim.alignment.AlignmentUtils;
import microsim.engine.SimulationEngine;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static jamjam.Sum.cumulativeSum;

/**
 * set individual probability to 1 if there is a change of the integer part of the cumulated probability, 0 otherwise
 *
 * @param <T>
 */
public class SidewalkAlignment<T> implements AlignmentUtils<T> {

    /**
     * The method calibrates the probabilities through nonlinear transformation in order to confine the probability
     * within the range of 0 and 1.
     *
     * @param agents            A collection of agents.
     * @param filter            A logical filter to filter out agents that don't match certain criteria, can be null.
     * @param closure           A closure to manipulate probabilities of the agents.
     * @param targetProbability The target probability value that the method converges to.
     * @throws NullPointerException when {@code agents}, or {@code closure}, or both are {@code null}.
     */
    public void align(final @NonNull Collection<T> agents, final @Nullable Predicate<T> filter,
                      final @NonNull AlignmentProbabilityClosure<T> closure, final double targetProbability) {
        if (targetProbability < 0. || targetProbability > 1.)
            throw new IllegalArgumentException("Target probability must lie in [0,1]");

        List<T> list = extractAgentList(agents, filter);
        if (list.size() == 0)
            return;

        Collections.shuffle(list, SimulationEngine.getRnd());

        val cumulativeProb = cumulativeSum(list.stream().mapToDouble(closure::getProbability).toArray());
        val cProbTruncated = IntStream.range(0, list.size()).map(i -> (int) cumulativeProb[i]).toArray();

        for (var i = 0; i < list.size(); i++)
            closure.align(list.get(i), cProbTruncated[i] != (i == 0 ? 0 : cProbTruncated[i - 1]) ? 1.0 : 0.0);
    }
}

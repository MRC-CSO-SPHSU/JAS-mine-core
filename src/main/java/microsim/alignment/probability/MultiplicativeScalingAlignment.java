package microsim.alignment.probability;

import cern.jet.random.engine.RandomEngine;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;
import microsim.alignment.AlignmentUtils;
import microsim.engine.SimulationEngine;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static jamjam.Mean.mean;

/**
 * A class for multiplicative scaling alignment method. All probabilities are extracted from the collection
 * {@code agents} using provided {@code filter}. Further steps involve calculation of the ration between the desired
 * transition rate and the actual transition to be used as a scaling factor for probabilities.
 *
 * @param <T> A class usually representing an agent.
 * @see <a href="https://www.jasss.org/17/1/15.html">Jinjing Li and Cathal O'Donoghue, Evaluating Binary Alignment
 * Methods in Microsimulation Models, Journal of Artificial Societies and Social Simulation 17 (1) 15</a>
 * @see <a href="https://www.jasss.org/17/1/15.html">Li, Jinjing and O'Donoghue, Cathal (2014) 'Evaluating Binary
 * Alignment Methods in Microsimulation Models' Journal of Artificial Societies and Social Simulation 17 (1) 15</a>
 */

@Log
public class MultiplicativeScalingAlignment<T> implements AlignmentUtils<T> {


    /**
     * The main alignment method. It involves scaling the existing probabilities by multiplying by a factor. Its value
     * is the target probability divided by the sum of all initial probabilities. The resulting values then are compared
     * to a random number from the {@code (0, 1)} range to decide if the transition happens.
     *
     * @param agents            An unfiltered collection of agents.
     * @param filter            A logical filter for agents, can be {@code null}.
     * @param closure           A closure to handle probabilities of agents.
     * @param targetProbability The target probability value.
     * @return The corrected set of outcomes.
     * @throws NullPointerException when {@code agents}, or {@code closure}, or both are {@code null}.
     */
    public double @NonNull [] align(final @NonNull Collection<T> agents, final @Nullable Predicate<T> filter,
                                    final @NonNull AlignmentProbabilityClosure<T> closure,
                                    final double targetProbability) {
        validateProbabilityValue(targetProbability);
        val agentList = extractAgentList(agents, filter);
        log.log(Level.INFO, "The total number of filtered individuals is %d".formatted(agentList.size()));
        val l = agentList.size();

        val probabilities = agentList.stream().mapToDouble(closure::getProbability).toArray();

        val averageRatio = targetProbability / mean(probabilities);

        IntStream.range(0, l).forEach(i -> probabilities[i] *= averageRatio);

        val randomValues = new double[l];
        for (int i = 0; i < l; i++)
            randomValues[i] = SimulationEngine.getRnd().nextDouble(RandomEngine.unitIntervalTypes.OPEN);

        return IntStream.range(0, l).mapToDouble(i -> randomValues[i] < probabilities[i] ? 1 : 0).toArray();
    }
}

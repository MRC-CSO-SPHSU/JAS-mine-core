package microsim.alignment.probability;

import lombok.NonNull;

/**
 * An auxiliary interface to get and align probabilities of an agent.
 *
 * @param <T> A generic type representing agents.
 */
public interface AlignmentProbabilityClosure<T> {

    /**
     * Returns the unaligned probability of a 'positive' outcome for the agent (the user should define what the positive
     * outcome is; it could be that something happens or does not happen).
     *
     * @param agent an agent-representing object.
     * @return the probability of a 'positive' outcome for the agent.
     * @throws NullPointerException when {@code agent} is {@code null}.
     */
    double getProbability(final @NonNull T agent);

    /**
     * Method specifying the sampling of the aligned probability to determine the outcome for the agent.
     *
     * @param agent              an agent-representing object.
     * @param alignedProbability the corrected probability of a 'positive' outcome
     * @throws NullPointerException when {@code agent} is {@code null}.
     */
    void align(final @NonNull T agent, final double alignedProbability);
}

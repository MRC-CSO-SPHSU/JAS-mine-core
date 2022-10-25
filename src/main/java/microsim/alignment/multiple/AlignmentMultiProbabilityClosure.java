package microsim.alignment.multiple;

import lombok.NonNull;

/**
 * A general interface for alignment procedures with multiple outcomes.
 *
 * @param <T> A generic type describing an agent.
 */

public interface AlignmentMultiProbabilityClosure<T> {

    /**
     * Returns a discrete probability distribution for a given agent.
     *
     * @param agent An agent object.
     * @return a linear array of {@code double} with probabilities, always not {@code null}.
     * @throws NullPointerException when {@code agent} is {@code null}.
     */
    double @NonNull [] getProbability(final @NonNull T agent);

    /**
     * Aligns (corrects) probabilities of a given agent.
     *
     * @param agent              An agent for correction.
     * @param alignedProbability Probabilities that replace old values.
     * @throws NullPointerException when {@code agent}, or {@code alignedProbability}, or both are {@code null}.
     */
    void align(final @NonNull T agent, final double @NonNull [] alignedProbability);
}

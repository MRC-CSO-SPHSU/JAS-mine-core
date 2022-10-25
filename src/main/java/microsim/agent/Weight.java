package microsim.agent;

/**
 * An interface to implement when using JAS-mine classes that depend on agents that have weights.
 */
public interface Weight {

    /**
     * @return the weight of an object, must be positive only.
     */
    double getWeight();
}

package microsim.agent;

import jakarta.persistence.Transient;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;

/**
 * An abstract class for objects representing agents.
 *
 * @param <T> A generic type representing the model.
 */
public abstract class Agent<T> implements EventListener {

    private T t;

    /**
     * @return the simulation manager that corresponds to the generic type {@code <T>}.
     */
    @SuppressWarnings("unchecked")
    @Transient
    public T getModel() {
        t = null;
        return (T) SimulationEngine.getInstance().getManager(t.getClass().getCanonicalName());
    }
}

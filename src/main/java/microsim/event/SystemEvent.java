package microsim.event;

import lombok.NonNull;
import microsim.engine.SimulationEngine;
import microsim.exception.SimulationException;

/**
 * System events are directly processed by the simulation engine. There are some special events that engine is able to
 * understand. For instance, you can schedule the end of simulation using a system event {@link SystemEventType#END} by
 * passing it to {@link EventQueue}.
 */
public class SystemEvent extends Event {

    SystemEventType type;
    SimulationEngine engine;

    /**
     * The default constructor.
     *
     * @param engine A {@link SimulationEngine} object.
     * @param type   The type of system event.
     * @throws NullPointerException when any of the input object is {@code null}.
     */
    public SystemEvent(final @NonNull SimulationEngine engine, final @NonNull SystemEventType type) {
        this.type = type;
        this.engine = engine;
    }

    /**
     * Triggers the event based on its type
     *
     * @throws SimulationException when the engine fails to progress current simulation.
     */
    public void fireEvent() throws SimulationException {
        switch (type) {
            case START -> engine.startSimulation();
            case RESTART -> engine.rebuildModels();
            case STOP -> engine.pause();
            case SHUTDOWN -> engine.quit();
            case BUILD -> engine.buildModels();
            case STEP -> engine.step();
            case END -> engine.end();
            case SETUP -> engine.setup();
        }
    }

    /**
     * Converts this system event to a string.
     *
     * @return the string representation of an event.
     */
    public @NonNull String toString() {
        return "SystemEvent(@" + getTime() + " " + type + ")";
    }
}

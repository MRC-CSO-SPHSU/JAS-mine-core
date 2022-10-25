package microsim.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import microsim.engine.SimulationEngine;
import microsim.exception.SimulationException;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * The {@link EventQueue} class manages a time ordered queue of events. It is based on a priority queue. At every
 * simulation step the head of the queue is taken and fired. This class extends a thread, because it runs independently
 * of other processes. When activated it runs the simulation.
 */
public class EventQueue {
    /**
     * The action type passed to step listeners. The {@code int} value is {@value}.
     */
    public static final int EVENT_LIST_STEP = 10000;

    /**
     * Constant: {@value}
     */
    @Setter
    @Getter
    private static double simulationTimeout = 100000;

    protected Queue<Event> eventQueue;

    @Getter
    double time;

    /**
     * Builds a new event queue of size 10.
     */
    public EventQueue() {
        eventQueue = new PriorityQueue<>(10);
        time = 0;
    }

    /**
     * Builds a new event queue inheriting parameters from another EventQueue.
     *
     * @param previousEventQueue An {@link EventQueue} to copy.
     * @throws NullPointerException when {@code previousEventQueue} is {@code null}.
     */
    public EventQueue(final @NonNull EventQueue previousEventQueue) {
        this();
        time = previousEventQueue.time;
    }

    /**
     * Empties this object and sets time to {@code 0} for a new simulation.
     */
    public void clear() {
        eventQueue.clear();
        time = 0;
    }

    /**
     * Makes one simulation step forward. Does nothing when the queue is empty.
     *
     * @throws SimulationException when fails to invoke an event.
     */
    public synchronized void step() throws SimulationException {
        if (eventQueue.isEmpty()) return;

        Event event = eventQueue.poll();

        time = event.getTime();

        event.fireEvent();
        if (event.getLoop() > 0) {
            event.setTimeAtNextLoop();
            scheduleEvent(event);
        }

    }

    /**
     * Runs an entire simulation. If model does not stop itself during the process of simulation, it will be stopped
     * automatically at timeout time.
     *
     * @throws SimulationException when {@link #step()} fails to progress.
     */
    public void simulate() throws SimulationException {
        while (eventQueue.size() > 0 && time < simulationTimeout)
            step();
    }

    /**
     * Adds an event to the queue.
     *
     * @param event An {@link Event} object.
     * @throws NullPointerException when {@code event} is {@code null}.
     */
    protected void scheduleEvent(final @NonNull Event event) {
        eventQueue.add(event);
    }

    /**
     * Schedules a generic event to occur at a given time.
     *
     * @param atTime       The time when event will be fired.
     * @param withOrdering The order that the event will be fired: for two events {@code e1} and {@code e2} scheduled to
     *                     occur at the same time {@code e1.time == e2.time}, if {@code e1.ordering < e2.ordering}, then
     *                     {@code e1} will be fired first. If {@code e1.time == e2.time AND e1.ordering == e2.ordering},
     *                     the first event that was scheduled (added to the {@link EventQueue}) will be fired first.
     * @throws NullPointerException when {@code event} is {@code null}.
     */
    public EventQueue scheduleOnce(final @NonNull Event event, final double atTime, final int withOrdering) {
        event.setTimeOrderingAndLoopPeriod(atTime, withOrdering, 0);
        scheduleEvent(event);

        return this;
    }

    /**
     * Schedules a generic looped event at a given time and ordering.
     *
     * @param atTime            The time when event will be fired for the first time.
     * @param withOrdering      The order that the event will be fired: for two events {@code e1} and {@code e2}
     *                          scheduled to occur at the same time {@code e1.time == e2.time}, if
     *                          {@code e1.ordering < e2.ordering}, then {@code e1} will be fired first. If
     *                          {@code e1.time == e2.time AND e1.ordering == e2.ordering}, the first event that was
     *                          scheduled (added to the {@link EventQueue}) will be fired first.
     * @param timeBetweenEvents The time period between repeated firing of the event. If this parameter is set to 0,
     *                          this event will not be fired more than once.
     * @throws NullPointerException when {@code event} is {@code null}.
     */
    public EventQueue scheduleRepeat(final @NonNull Event event, final double atTime, final int withOrdering,
                                     final double timeBetweenEvents) {
        event.setTimeOrderingAndLoopPeriod(atTime, withOrdering, timeBetweenEvents);
        scheduleEvent(event);

        return this;
    }

    /**
     * Removes a certain event from the queue.
     *
     * @param event An event to remove.
     * @throws NullPointerException when {@code event} is {@code null}.
     */
    public void unschedule(final @NonNull Event event) {
        eventQueue.remove(event);
    }

    /**
     * Schedules a looped system event.
     *
     * @param atTime       The time when event will be fired for the first time.
     * @param withOrdering The order that the event will be fired: for two events {@code e1} and {@code e2} scheduled to
     *                     occur at the same time {@code e1.time == e2.time}, if {@code e1.ordering < e2.ordering}, then
     *                     {@code e1} will be fired first. If {@code e1.time == e2.time AND e1.ordering == e2.ordering},
     *                     the first event that was scheduled (added to the {@link EventQueue}) will be fired first.
     * @param withLoop     The time period between repeated firing of the event. If this parameter is set to 0, this
     *                     event will not be fired more than once.
     * @param engine       A {@link SimulationEngine} object to use.
     * @param type         A {@link SystemEventType} to use.
     * @return a new scheduled {@link SystemEvent}.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public SystemEvent scheduleSystem(final double atTime, final int withOrdering, final double withLoop,
                                      final @NonNull SimulationEngine engine, final @NonNull SystemEventType type) {
        SystemEvent event = new SystemEvent(engine, type);
        event.setTimeOrderingAndLoopPeriod(atTime, withOrdering, withLoop);
        scheduleEvent(event);
        return event;
    }

    /**
     * Converts the event queue to an array.
     *
     * @return an {@link Event} array.
     */
    public @NonNull Event[] getEventArray() {
        return eventQueue.toArray(new Event[]{});
    }
}

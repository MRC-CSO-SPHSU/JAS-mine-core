package microsim.event;

import lombok.Getter;
import lombok.NonNull;
import microsim.engine.SimulationEngine;
import microsim.exception.SimulationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A special implementation of the {@link Event} family. It is not a real event, but a container for other events. When
 * fired this object automatically fires the events contained. Each contained event is fired at the group time, any
 * other time pointer is ignored.
 */
@SuppressWarnings("unused")
public class EventGroup extends Event {
    @Getter
    private final List<Event> actions;

    /**
     * Builds a new group event.
     */
    public EventGroup() {
        actions = new ArrayList<>();
    }

    /**
     * Empties the event list.
     */
    public void clear() {
        actions.clear();
    }

    /**
     * Adds an event to the list.
     *
     * @param newEvent An {@link Event} object.
     * @throws NullPointerException when {@code newEvent} is {@code null}.
     */
    public @NonNull EventGroup addEvent(final @NonNull Event newEvent) {
        actions.add(newEvent);
        return this;
    }

    /**
     * Creates a new {@link SingleTargetEvent} and adds the event to the list, using late binding method.
     *
     * @param object An {@link Object}.
     * @param method The method name.
     * @throws SimulationException  when creation of {@link SingleTargetEvent} fails.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     * @see CollectionTargetEvent
     */
    public @NonNull EventGroup addEvent(final @NonNull Object object,
                                        final @NonNull String method) throws SimulationException {
        actions.add(new SingleTargetEvent(object, method));
        return this;
    }

    /**
     * Creates a new {@link SingleTargetEvent} and adds the event to the list, using early binding method.
     *
     * @param object     An {@link Object}.
     * @param actionType An action to invoke.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public EventGroup addEvent(final @NonNull Object object, final @NonNull Enum<?> actionType) {
        actions.add(new SingleTargetEvent(object, actionType));
        return this;
    }

    /**
     * Creates a new {@link SystemEvent} and adds the event to the list.
     *
     * @param engine     A {@link SimulationEngine} object.
     * @param actionType An action to invoke.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public EventGroup addSystemEvent(final @NonNull SimulationEngine engine,
                                     final @NonNull SystemEventType actionType) {
        actions.add(new SystemEvent(engine, actionType));
        return this;
    }

    /**
     * {@code readOnly} defaults to {@code true}.
     *
     * @see #addCollectionEvent(Collection, Class, String, boolean)
     */
    public @NonNull EventGroup addCollectionEvent(final @NonNull Collection<?> elements,
                                                  final @NonNull Class<?> objectType,
                                                  final @NonNull String method) throws SimulationException {
        actions.add(new CollectionTargetEvent(elements, objectType, method, true));
        return this;
    }

    /**
     * Creates a new {@link CollectionTargetEvent} and adds the event to the list, using late binding method.
     *
     * @param elements   A collection of elements of some nature.
     * @param objectType The type of {@code elements}.
     * @param method     The method name.
     * @param readOnly   A boolean flag that makes {@code elements} an immutable object.
     * @throws SimulationException  when creation of {@link CollectionTargetEvent} fails.
     * @throws NullPointerException when any of the input object is {@code null}.
     * @see CollectionTargetEvent
     */
    public @NonNull EventGroup addCollectionEvent(final @NonNull Collection<?> elements,
                                                  final @NonNull Class<?> objectType,
                                                  final @NonNull String method,
                                                  final boolean readOnly) throws SimulationException {
        actions.add(new CollectionTargetEvent(elements, objectType, method, readOnly));
        return this;
    }

    /**
     * {@code readOnly} defaults to {@code true}.
     *
     * @see #addCollectionEvent(Collection, Enum, boolean)
     */
    public @NonNull EventGroup addCollectionEvent(final @NonNull Collection<?> elements,
                                                  final @NonNull Enum<?> actionType) {
        actions.add(new CollectionTargetEvent(elements, actionType, true));
        return this;
    }

    /**
     * Creates a new {@link CollectionTargetEvent} and adds the event to the list, using early binding method.
     *
     * @param elements   A collection of elements of some nature.
     * @param actionType An action to invoke.
     * @param readOnly   A boolean flag that makes {@code elements} an immutable object.
     * @throws NullPointerException when any of the input objects is {@code null}.
     * @see CollectionTargetEvent
     */
    public @NonNull EventGroup addCollectionEvent(final @NonNull Collection<?> elements,
                                                  final @NonNull Enum<?> actionType, final boolean readOnly) {
        actions.add(new CollectionTargetEvent(elements, actionType, readOnly));
        return this;
    }

    /**
     * Removes the given event from the list.
     *
     * @param event An {@link Event} object.
     * @throws NullPointerException when {@code event} is {@code null}.
     */
    public void removeEvent(final @NonNull Event event) {
        actions.remove(event);
    }

    /**
     * Fires each event into the list.
     *
     * @throws SimulationException when one of the events fails to do so.
     */
    public void fireEvent() throws SimulationException {
        for (var event : actions) event.fireEvent();
    }

    /**
     * Converts the list of actions to an array.
     *
     * @return an array of {@link Event} objects.
     */
    public @NonNull Event @NonNull [] eventsToArray() {
        return actions.toArray(Event[]::new);
    }
}

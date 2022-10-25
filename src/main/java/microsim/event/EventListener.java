package microsim.event;


import lombok.NonNull;

/**
 * An interface for objects to react to events.
 */
public interface EventListener {

    void onEvent(final @NonNull Enum<?> type);
}

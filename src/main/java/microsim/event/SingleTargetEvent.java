package microsim.event;

import lombok.NonNull;
import microsim.exception.SimulationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The simplest implementation of {@link Event} class. It represents an event to be passed to one specific object only.
 * It is often used in discrete event simulations, when an object schedule itself for a future event.
 */
// todo seems to be package-private as well.
public class SingleTargetEvent extends Event {

    protected Enum<?> eventType;
    protected Method methodInvoker;

    protected Object object;

    /**
     * Creates a new event using late binding.
     *
     * @param object An {@link Object}.
     * @param method The method name.
     * @throws SimulationException  when no such method exists.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public SingleTargetEvent(final @NonNull Object object, final @NonNull String method) throws SimulationException {
        this.object = object;
        eventType = null;

        Class<?> cl = object.getClass();
        while (cl != null)
            try {
                methodInvoker = cl.getDeclaredMethod(method, (Class<?>) null);
                return;
            } catch (NoSuchMethodException e) {
                cl = cl.getSuperclass();
            } catch (SecurityException e) {
                System.out.println("Method: " + method);
                System.out.println(this.getClass().getName() + " -> SecurityException: " + e.getMessage());
                printStackTrace(e);
            }

        if (methodInvoker == null)
            throw new SimulationException(this.getClass().getName() + " didn't find method " + method);
    }

    /**
     * Creates a new event using early binding.
     *
     * @param object     An {@link Object}.
     * @param actionType The type of action to invoke.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public SingleTargetEvent(final @NonNull Object object, final @NonNull Enum<?> actionType) {
        this.object = object;
        methodInvoker = null;
        eventType = actionType;
    }

    /**
     * Converts this event to a string.
     *
     * @return the string representation of an object.
     */
    public @NonNull String toString() {
        if (methodInvoker != null) return "[@" + getTime() + "->" + methodInvoker.toString() + "]";
        else return "[@" + getTime() + "->" + object.toString() + "." + eventType + "]";
    }

    /**
     * Fires the event by calling the target object.
     */
    public void fireEvent() {
        if (methodInvoker != null) {
            try {
                methodInvoker.invoke(object, (Object) null);
            } catch (InvocationTargetException e) {
                System.out.println("Object " + methodInvoker + " Method: " + methodInvoker.getName());
                System.out.println(this.getClass().getName() + ".fireEvent -> InvocationTargetException: "
                    + e.getTargetException().toString());
                printStackTrace(e);
            } catch (IllegalAccessException e) {
                System.out.println("Object " + methodInvoker + " Method: " + methodInvoker.getName());
                System.out.println(this.getClass().getName() + ".fireEvent -> IllegalAccessException: "
                    + e.getMessage());
                printStackTrace(e);
            }
        } else {
            EventListener evL = (EventListener) object;
            evL.onEvent(eventType);
        }
    }
}

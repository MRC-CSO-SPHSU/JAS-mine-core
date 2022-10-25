package microsim.event;

import lombok.NonNull;
import microsim.exception.SimulationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class informs all elements within a collection about {@link Event}.
 */
//todo the whole class seems to be package-private
public class CollectionTargetEvent extends Event {

    protected Enum<?> eventType;
    protected boolean readOnly;
    protected Collection<?> collection;
    private Method methodInvoker;

    private CollectionTargetEvent(final Collection<?> elements, final boolean readOnly) {
        collection = elements;
        this.readOnly = readOnly;
    }

    /**
     * Creates an object of {@link CollectionTargetEvent} type using late binding method call (type detection happens at
     * run-time).
     *
     * @param elements   A collection of elements of some nature.
     * @param objectType The type of {@code elements}.
     * @param method     The method name.
     * @param readOnly   A boolean flag that makes {@code elements} an immutable object.
     * @throws SimulationException  when there is no such method to invoke.
     * @throws NullPointerException when any of the objects passed to the constructor is {@code null}.
     */
    public CollectionTargetEvent(final @NonNull Collection<?> elements, final @NonNull Class<?> objectType,
                                 final @NonNull String method, final boolean readOnly) throws SimulationException {
        this(elements, readOnly);

        eventType = null;

        Class<?> cl = objectType;
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
     * Creates an object of {@link CollectionTargetEvent} type with the method
     * Creates an object of {@link CollectionTargetEvent} type using early binding method call (all types are set during
     * the process of compilation).
     *
     * @param elements   A collection of elements of some nature.
     * @param actionType An action to invoke.
     * @param readOnly   A boolean flag that makes {@code elements} an immutable object.
     * @throws NullPointerException when any of the objects passed to the constructor is {@code null}.
     */
    public CollectionTargetEvent(final @NonNull Collection<?> elements, final @NonNull Enum<?> actionType,
                                 final boolean readOnly) {
        this(elements, readOnly);

        eventType = actionType;
        methodInvoker = null;
    }

    /**
     * Fires a particular event, i.e., calls each element contained into the collection.
     */
    public void fireEvent() {
        // todo check the logic of the block right below
        Collection<?> localCollection = collection;
        if (!readOnly)
            localCollection = new ArrayList<Object>(collection);

        Iterator<?> itr = localCollection.iterator();

        if (methodInvoker != null) {
            while (itr.hasNext()) {
                try {
                    methodInvoker.invoke(itr.next(), (Object) null);
                } catch (InvocationTargetException e) {
                    System.out.println("Object " + methodInvoker + " Method: "
                        + methodInvoker.getName());
                    System.out
                        .println("SimCollectionEvent.fireEvent -> InvocationTargetException: "
                            + e.getTargetException().toString());
                    printStackTrace(e);
                } catch (IllegalAccessException e) {
                    System.out.println("Object " + methodInvoker + " Method: "
                        + methodInvoker.getName());
                    System.out
                        .println("SimCollectionEvent.fireEvent -> IllegalAccessException: "
                            + e.getMessage());
                    printStackTrace(e);
                }
            }
        } else {
            while (itr.hasNext()) {
                EventListener evL = (EventListener) itr.next();
                evL.onEvent(eventType);
            }
        }
    }
}

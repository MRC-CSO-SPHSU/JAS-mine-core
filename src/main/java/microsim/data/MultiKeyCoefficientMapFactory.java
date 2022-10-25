package microsim.data;

import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;
import microsim.annotation.CoefficientMapping;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * This class allows creation of {@link MultiKeyCoefficientMap} from an annotated list.
 */
@Log
public class MultiKeyCoefficientMapFactory {

    /**
     * Creates a {@link MultiKeyCoefficientMap} and adds values from the {@code list} to it according to the provided
     * {@link CoefficientMapping} class annotation.
     *
     * @param list A list of objects that are supposed to be annotated.
     * @return a {@link MultiKeyCoefficientMap}.
     * @throws IllegalArgumentException when the input list length is {@code 0}; when objects in the list are
     *                                  not annotated with {@link CoefficientMapping}; when the number of keys exceeds
     *                                  {@code 5}.
     * @throws NullPointerException     when {@code list} is {@code null}.
     */
    public static MultiKeyCoefficientMap createMapFromAnnotatedList(final @NonNull List<?> list) {
        if (list.size() == 0)
            throw new IllegalArgumentException("List must be not null and must contain at least one element");

        val clazz = list.get(0).getClass();
        if (!clazz.isAnnotationPresent(CoefficientMapping.class))
            throw new IllegalArgumentException("List must contain CoefficientMap annotated objects");

        val annotation = clazz.getAnnotation(CoefficientMapping.class);

        val keys = annotation.keys();
        val values = annotation.values();

        MultiKeyCoefficientMap map = new MultiKeyCoefficientMap(keys, values);

        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> field.setAccessible(true));

        for (Object object : list) {
            for (String value : values) {
                switch (keys.length) {
                    case 1 -> map.putValue(getValue(clazz, keys[0], object), value, getValue(clazz, value, object));
                    case 2 -> map.putValue(getValue(clazz, keys[0], object), getValue(clazz, keys[1], object), value,
                        getValue(clazz, value, object));
                    case 3 -> map.putValue(getValue(clazz, keys[0], object), getValue(clazz, keys[1], object),
                        getValue(clazz, keys[2], object), value, getValue(clazz, value, object));
                    case 4 -> map.putValue(getValue(clazz, keys[0], object), getValue(clazz, keys[1], object),
                        getValue(clazz, keys[2], object), getValue(clazz, keys[3], object), value,
                        getValue(clazz, value, object));
                    case 5 -> map.putValue(getValue(clazz, keys[0], object), getValue(clazz, keys[1], object),
                        getValue(clazz, keys[2], object), getValue(clazz, keys[3], object),
                        getValue(clazz, keys[4], object), value, getValue(clazz, value, object));
                    default -> throw new IllegalArgumentException("Unsupported number of keys");
                }
            }
        }
        return map;
    }

    /**
     * Extracts the value of {@code fieldName} {@link Field} from {@code object}.
     *
     * @param clazz     The object class.
     * @param fieldName The name of a field.
     * @param object    An object to get value from.
     * @return the value of an object.
     * @throws RuntimeException when the method fails to get access to.
     * @implSpec No checks of any kind in a private method.
     */
    private static Object getValue(final Class<?> clazz, final String fieldName, final Object object) {
        final Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            var message = "Failed to get access to " + fieldName + ", no such field exists.";
            log.log(Level.SEVERE, message, e);
            throw new RuntimeException(message, e);
        }
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            var message = "Failed to get access to " + object.toString() + ", no access.";
            log.log(Level.SEVERE, message, e);
            throw new RuntimeException(message, e);
        }
    }
}

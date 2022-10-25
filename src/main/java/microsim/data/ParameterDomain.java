package microsim.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.Nullable;

/**
 * This class is used as a map to associate arrays of values with certain names.
 */
public class ParameterDomain {

    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private Object[] values;

    /**
     * The default constructor, it only creates an instance of the class.
     */
    public ParameterDomain() {
    }

    /**
     * Creates an instance of the class with the given name and provided values.
     *
     * @param name   The name of the domain.
     * @param values The values of the domains, the array itself can be {@code null} or it can contain {@code null}
     *               values.
     * @throws NullPointerException when {@code name} is {@code null}.
     */
    public ParameterDomain(final @NonNull String name, final @Nullable Object @Nullable [] values) {
        this.name = name;
        this.values = values;
    }

    /**
     * Modifies the instance by adding a value to the list of existing values.
     *
     * @param value A value object.
     * @return the same object.
     */
    public @NonNull ParameterDomain addValue(final @Nullable Object value) {
        val scratchLength = 1 + (values != null ? values.length : 0);
        val scratchArray = new Object[scratchLength];
        if (values != null) System.arraycopy(values, 0, scratchArray, 0, values.length);
        scratchArray[scratchLength - 1] = value;
        values = scratchArray;
        return this;
    }
}

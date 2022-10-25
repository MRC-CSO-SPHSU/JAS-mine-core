package microsim.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import java.util.ArrayList;

/**
 * This class is an extension of {@link ParameterDomain} where values that are stored are elements of arithmetic
 * progressions of floating-point numbers generated from intervals.
 */
public class ParameterRangeDomain extends ParameterDomain {

    @Setter
    @Getter
    private Double min;

    @Setter
    @Getter
    private Double max;

    @Setter
    @Getter
    private Double step;

    /**
     * The default constructor, creates an instance only.
     */
    public ParameterRangeDomain() {
    }

    /**
     * Creates an instance of the class, sets the name and {@code min}, {@code max}, and {@code step} values.
     *
     * @param name The range name.
     * @param min  The left bound of the interval.
     * @param max  The right bound of the interval.
     * @param step The step of the progression.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public ParameterRangeDomain(final @NonNull String name, final @NonNull Double min, final @NonNull Double max,
                                final @NonNull Double step) {
        setName(name);
        this.max = max;
        this.min = min;
        this.step = step;
    }

    /**
     * Generates the actual arithmetic progression and returns all the values.
     *
     * @return an array of objects that are instances of {@link Double}, always not {@code null}.
     * @implNote The left bound is always included, but the right one might or might not be. That depends on the actual
     * parameters of the progression, various numerical errors also affect the result.
     */
    @Override
    public @NonNull Object[] getValues() {
        val array = new ArrayList<>();

        var currentValue = min;
        while (currentValue < max) { // improve this// fixme current jamjam deals with the number of intervals only, we need a separate version for step size
            array.add(currentValue);
            currentValue += step;
        }

        return array.toArray();
    }

    /**
     * This method is not supported.
     *
     * @throws UnsupportedOperationException when executed.
     */
    @Override
    public void setValues(Object[] values) {
        throw new UnsupportedOperationException("Range parameters cannot be set as list");
    }
}

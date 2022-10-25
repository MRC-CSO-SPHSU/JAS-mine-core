package microsim.event;

import lombok.Getter;

@Deprecated
public enum Order {
    BEFORE_ALL(Integer.MIN_VALUE),
    AFTER_ALL(Integer.MAX_VALUE);

    @Getter
    private final int ordering;

    Order(final int ordering) {
        this.ordering = ordering;
    }
}

package microsim.engine;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;

// todo rework using modern java.time, see https://stackoverflow.com/questions/36639154/convert-java-util-date-to-what-java-time-type
public class SimulationTimeManager {

    private final Calendar calendar;
    private final Date initOfTime;
    @Setter
    @Getter
    private int dayTickUnit;

    public SimulationTimeManager(final @NonNull Date initOfTime, final int dayTickUnit) {
        this.dayTickUnit = dayTickUnit;
        this.initOfTime = initOfTime;
        this.calendar = Calendar.getInstance();
    }

    public SimulationTimeManager(final int year, final int month, final int day, final int dayTickUnit) {
        this.dayTickUnit = dayTickUnit;
        this.calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        this.initOfTime = calendar.getTime();
    }

    public @NonNull Date getRealDate(final long simulatedTime) {
        calendar.setTime(initOfTime);
        final int days = (int) ((double) simulatedTime / (double) dayTickUnit);
        calendar.add(Calendar.DATE, days);

        return calendar.getTime();
    }
}

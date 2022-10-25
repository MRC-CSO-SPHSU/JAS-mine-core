package microsim.statistics;

import cern.mateba.list.tdouble.DoubleArrayList;
import cern.mateba.list.tint.IntArrayList;
import cern.mateba.list.tlong.LongArrayList;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;
import microsim.engine.SimulationEngine;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.reflection.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A container for multiple synchronized time series.
 */
@Log
public class TimeSeries implements EventListener, UpdatableSource {

    /**
     * The character used to separate data in the output file: {@value}
     */
    public static final char DEFAULT_SEPARATOR = ',';
    @Getter
    protected ArrayList<Series> series;
    protected DoubleArrayList absTimes;
    protected ArrayList<String> descTimes;
    @Setter
    @Getter
    private String fileName = "timeSeries.txt";
    private double lastTimeUpdate = -1.;

    /**
     * Creates a new time series container.
     */
    public TimeSeries() {
        series = new ArrayList<>();
        absTimes = new DoubleArrayList();
        descTimes = new ArrayList<>();
    }

    /**
     * Adds a new series to the existing one.
     *
     * @param aSeries An instance of {@link Series}.
     * @throws NullPointerException when {@code aSeries} is {@code null}.
     */
    public void addSeries(final @NonNull Series aSeries) {
        series.add(aSeries);
    }

    /**
     * Adds a new series to the existing one.
     *
     * @param source  A {@link DoubleSource} object.
     * @param valueID The value identifier defined by source object.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public void addSeries(final @NonNull DoubleSource source, final @NonNull Enum<?> valueID) {
        series.add(new Series.Double(source, valueID));
    }

    /**
     * Adds a new series to the existing one.
     *
     * @param source  The {@link IntSource} object.
     * @param valueID The value identifier defined by source object.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public void addSeries(final @NonNull IntSource source, final @NonNull Enum<?> valueID) {
        series.add(new Series.Integer(source, valueID));
    }

    /**
     * Adds a new series to the existing one.
     *
     * @param source  The LongSource object.
     * @param valueID The value identifier defined by source object.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public void addSeries(final @NonNull LongSource source, final @NonNull Enum<?> valueID) {
        series.add(new Series.Long(source, valueID));
    }

    /**
     * Adds a new series to the existing one.
     *
     * @param target        A generic source object.
     * @param variableName  The name of the field or the method returning the variable to
     *                      be probed.
     * @param getFromMethod Specifies if valueName is a method or a property value.
     * @throws NullPointerException when any of the input parameters is {@code null}.
     */
    public void addSeries(final @NonNull Object target, final @NonNull String variableName,
                          final boolean getFromMethod) {
        Series aSeries;// bloated
        if (ReflectionUtils.isDoubleSource(target.getClass(), variableName,
            getFromMethod))
            aSeries = new Series.Double(target, variableName, getFromMethod);
        else if (ReflectionUtils.isIntSource(target.getClass(), variableName,
            getFromMethod))
            aSeries = new Series.Integer(target, variableName, getFromMethod);
        else if (ReflectionUtils.isLongSource(target.getClass(), variableName,
            getFromMethod))
            aSeries = new Series.Long(target, variableName, getFromMethod);
        else
            throw new IllegalArgumentException("The passed argument is not a valid number source");

        series.add(aSeries);
    }

    /**
     * Updates all the contained time series and the current time.
     */
    public void updateSource() {
        if (SimulationEngine.getInstance().getEventQueue().getTime() == lastTimeUpdate)
            return;

        for (Series value : series) value.updateSource();

        absTimes.add(SimulationEngine.getInstance().getEventQueue().getTime());
        descTimes.add("" + SimulationEngine.getInstance().getEventQueue().getTime());

        lastTimeUpdate = SimulationEngine.getInstance().getEventQueue().getTime();
    }

    /**
     * Returns a series at the given index.
     *
     * @param seriesIndex The name of the series.
     * @return The asked series or {@code null} if series does not exist.
     * @throws IndexOutOfBoundsException If {@code seriesIndex} is out of bounds.
     */
    public Series getSeries(final int seriesIndex) {
        if (seriesIndex >= series.size())
            throw new IndexOutOfBoundsException(seriesIndex + " is out of max bound " + series.size());

        return series.get(seriesIndex);
    }

    /**
     * Returns the number of series.
     *
     * @return The number of series.
     */
    public int getSeriesCount() {
        return series.size();
    }

    /**
     * Stores the entire data content in the output file in the same directory.
     */
    public void saveToFile() {
        saveToFile("", fileName, true, DEFAULT_SEPARATOR);
    }

    /**
     * Stores the entire data content in the given output file.
     *
     * @param path     The optional path string.
     * @param fileName The name of the output file.
     */
    public void saveToFile(final @NonNull String path, final @NonNull String fileName) {
        saveToFile(path, fileName, true, DEFAULT_SEPARATOR);
    }

    /**
     * Stores the entire data content in the given output file.
     *
     * @param path      The optional path string. Passing an empty string it is ignored.
     * @param fileName  The name of the output file.
     * @param withTimes If {@code true} time description is saved. Only absolute time is saved if {@code false}.
     */
    public void saveToFile(final @NonNull String path, final @NonNull String fileName, final boolean withTimes) {
        saveToFile(path, fileName, withTimes, DEFAULT_SEPARATOR);
    }

    /**
     * Stores the entire data content in the given output file.
     *
     * @param path      The optional path string. Passing an empty string it is ignored.
     * @param fileName  The name of the output file.
     * @param withTimes If {@code true} time description is saved. Only absolute time is saved if {@code false}.
     * @param separator The character used to separate data.
     */
    public void saveToFile(@NonNull String path, @NonNull String fileName, final boolean withTimes,
                           final char separator) {

        if (SimulationEngine.getInstance().getCurrentRunNumber() != 0)
            fileName = getNumberedFile(fileName);

        path += File.separator + fileName;

        File file = new File(path);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            if (withTimes) // no need to check every iteration
                out.write("real time" + separator);
            out.write("time" + separator);

            for (var i = 0; i < absTimes.size() - 1; i++) {
                if (withTimes)
                    out.write(descTimes.get(i) + separator);
                out.write("" + absTimes.get(i) + separator);

                for (Series s : series) {
                    if (s instanceof Series.Double) {// bloated
                        DoubleArrayList dl;
                        dl = ((Series.Double) s).getDoubleArrayList();
                        out.write("" + dl.get(i) + separator);
                    } else if (s instanceof Series.Integer) {
                        IntArrayList dl;
                        dl = ((Series.Integer) s).getIntArrayList();
                        out.write("" + dl.get(i) + separator);
                    } else {
                        LongArrayList dl;
                        dl = ((Series.Long) s).getLongArrayList();
                        out.write("" + dl.get(i) + separator);
                    }
                    out.newLine();
                }
            }
            out.newLine();
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            log.log(Level.SEVERE, "Error saving " + path + ioe.getMessage());
        }
    }

    private String getNumberedFile(String fileName) {
        int index;
        if ((index = fileName.lastIndexOf(".")) == 0)
            return fileName + "_" + SimulationEngine.getInstance().getCurrentRunNumber();
        else {
            String name = fileName.substring(0, index);
            String ext = fileName.substring(index);
            return name + "_" + SimulationEngine.getInstance().getCurrentRunNumber() + ext;
        }
    }

    /**
     * Performs one of the defined actions.
     *
     * @param type a {@link CommonEventType} object.
     * @throws NullPointerException when {@code type} is {@code null}.
     */
    public void onEvent(final @NonNull Enum<?> type) {
        if (type instanceof CommonEventType) {
            switch ((CommonEventType) type) {
                case UPDATE -> updateSource();
                case SAVE -> saveToFile();
            }
        }
    }
}

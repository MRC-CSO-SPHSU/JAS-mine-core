package microsim.data;

import lombok.NonNull;
import microsim.data.db.DatabaseUtils;

import java.util.Collection;

/**
 * {@link DataExport} is a class that handles the exporting to data to an output database and/or {@code *.csv} files.
 * Note that only numbers, enums or strings are exported to {@code *.csv} files.
 */
public class DataExport {

    private final boolean toDatabase;
    private final boolean toCSV;
    private ExportToCSV csvExport;
    private Object targetObject;
    private Collection<?> collectionTarget;

    /**
     * Creates a {@link DataExport} object to handle the exporting of a collection of objects to an output database
     * and/or {@code *.csv} files. Note that only numbers, enums or strings are exported to {@code *.csv} files.
     *
     * @param targetCollection A collection of objects whose fields (including private and inherited) will be exported.
     * @param exportToDatabase Set to true if the user wants to export to an output database.
     * @param exportToCSVfile  Set to true if the user wants to export to {@code *.csv} files named after the class name
     *                         of the {@code targetCollection}.
     * @throws NullPointerException when {@code targetCollection} is {@code null}.
     */
    public DataExport(final @NonNull Collection<?> targetCollection, final boolean exportToDatabase,
                      final boolean exportToCSVfile) {
        collectionTarget = targetCollection;
        toDatabase = exportToDatabase;
        toCSV = exportToCSVfile;
        if (toCSV) csvExport = new ExportToCSV(collectionTarget);
    }

    /**
     * Create a {@link DataExport} object to handle the exporting of an object to an output database and/or
     * {@code *.csv} files. Note that only numbers, enums or strings are exported to {@code *.csv} files.
     *
     * @param targetSingleObject An object whose fields (including private and inherited) will be exported.
     * @param exportToDatabase   Set to true if the user wants to export to an output database.
     * @param exportToCSVfile    Set to true if the user wants to export to {@code *.csv} files named after the class
     *                           name of the {@code targetSingleObject}.
     * @throws NullPointerException when {@code targetSingleObject} is {@code null}.
     */
    public DataExport(final @NonNull Object targetSingleObject, final boolean exportToDatabase,
                      final boolean exportToCSVfile) {
        targetObject = targetSingleObject;
        toDatabase = exportToDatabase;
        toCSV = exportToCSVfile;
        if (toCSV) csvExport = new ExportToCSV(targetObject);
    }

    /**
     * This method exports field values of an object or a collection of objects to the corresponding database or
     * {@code *.csv} file.
     */
    public void export() {
        if (toCSV) csvExport.dumpToCSV();

        if (toDatabase) {
            try {
                if (collectionTarget != null) DatabaseUtils.snap(collectionTarget);
                else if (targetObject != null) DatabaseUtils.snap(targetObject);
                else throw new NullPointerException();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

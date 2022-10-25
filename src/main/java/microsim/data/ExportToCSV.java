package microsim.data;

import jakarta.persistence.Transient;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

import static java.io.File.separator;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;

/**
 * {@link ExportToCSV} class allows the exporting of data to {@code *.csv} files. This is a useful alternative to
 * exporting to an output database, as it is faster and produces separate files for each class of object. Note that only
 * numbers, enums or strings are exported to {@code *.csv} files.
 */
@Log
public class ExportToCSV {
    final static String NEW_LINE = "\n";
    final static String DELIMITER = ",";
    final static SimulationEngine simInstance = SimulationEngine.getInstance();
    final static String directory = simInstance.getCurrentExperiment().getOutputFolder() + separator + "csv";

    final static String FAIL_WRITING = ", no data is written.";

    Set<String> fieldsForExport;
    BufferedWriter bufferWriter;
    String idFieldName;

    Collection<?> targetCollection;

    Object targetObject;
    Field targetObjectIdField;

    /**
     * Allows the exporting of all fields (including private and inherited fields) of an object to a {@code *.csv} file
     * named after the object's class name. Note that only numbers, enums or strings are exported to {@code *.csv}
     * files. The {@code serialVersionUID} of a class will also not be exported.
     *
     * @param target The object whose fields will be exported to a {@code *.csv} file with a name equal to the object's
     *               class name. If the target is a {@link Collection} of objects, each member of the collection will
     *               have their individual fields exported to the {@code *.csv} file, labelled by their id.
     *               Can be {@code null}.
     * @implSpec No export happens when {@code target} is {@code null}, but the attempt is logged.
     */
    public ExportToCSV(final @Nullable Object target) {
        if (isNull(target)) {
            log.log(Level.SEVERE, "The object to save is null, no data is written.");
            return;
        }

        val collectionMode = target instanceof Collection<?>;

        if (collectionMode) targetCollection = (Collection<?>) target;
        else targetObject = target;

        if (collectionMode) {
            if (targetCollection.size() == 0) {
                log.log(Level.SEVERE, "The collection size is 0" + FAIL_WRITING);
                return;
            } else {
                try {
                    targetCollection.removeIf(Objects::isNull);
                } catch (java.lang.UnsupportedOperationException e) {
                    log.log(Level.INFO, e + ": Passed collection is immutable/fixed size, " +
                        "switching to the slow implementation.");
                    val scratch = new ArrayList<>();
                    for (Object nextObject : targetCollection) if (!isNull(nextObject)) scratch.add(nextObject);
                    if (scratch.size() == 0) {
                        log.log(Level.SEVERE, "All objects in the collection are null" + FAIL_WRITING);
                        return;
                    }
                    targetCollection = scratch;
                }
            }
            val iterator = targetCollection.iterator();
            val refObject = iterator.next();


            while (iterator.hasNext()) {
                val actualObject = iterator.next();
                if (!refObject.getClass().equals(actualObject.getClass())) {
                    log.log(Level.SEVERE, "Objects in the collection are of different type" + FAIL_WRITING);
                    return;
                }
            }
            if (refObject.getClass().getDeclaredFields().length == 0) {
                log.log(Level.SEVERE, "The collection has no usable fields" + FAIL_WRITING);
                return;
            }
        }

        val parsedTarget = collectionMode ? targetCollection.iterator().next() : targetObject;

        val objFields = parsedTarget.getClass().getDeclaredFields();
        Field idField = null;

        if (objFields.length != 0) {
            for (var fld : objFields) {
                if (fld.getType().equals(PanelEntityKey.class)) {
                    idField = fld;
                    idFieldName = fld.getName();
                    if (!collectionMode) {
                        targetObjectIdField = fld;
                        targetObjectIdField.setAccessible(true);
                    }
                    break;
                }
            }
        } else {
            log.log(Level.SEVERE, "The object has no fields" + FAIL_WRITING);
            return;
        }

        if (idField != null) idField.setAccessible(true);
        else {
            log.log(Level.SEVERE, "The object of type " + target.getClass() + " does not have a field of type" +
                " PanelEntityKey.class" + FAIL_WRITING);
            return;
        }

        val filename = generateFilename(parsedTarget, collectionMode, idField);
        if (isNull(filename)) return;

        val dir = new File(directory);
        val file = new File(directory + separator + filename + ".csv");
        boolean dirExist = dir.exists();
        boolean fileExists;
        boolean createdNow = false;

        try {
            fileExists = file.exists();
        } catch (SecurityException e) {
            log.log(Level.SEVERE, "Can't verify file/dir existence due to lack of access" + FAIL_WRITING);
            return;
        }

        if (file.isDirectory()) {
            log.log(Level.SEVERE, "The path is a directory, not a file" + FAIL_WRITING);
            return;
        }

        if (!fileExists) {
            if (!dirExist) {
                dirExist = dir.mkdirs();
                if (!dirExist) {
                    log.log(Level.SEVERE, "Failed to create a new directory" + FAIL_WRITING);
                    return;
                }
            }

            try {
                fileExists = file.createNewFile();
                createdNow = true;
            } catch (IOException e) {
                log.log(Level.SEVERE, "I/O error occurred" + FAIL_WRITING);
                return;
            }

            if (!fileExists) {
                log.log(Level.SEVERE, "Failed to create a new file" + FAIL_WRITING);
                return;
            }
        }

        try {
            bufferWriter = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to create a buffer writer" + FAIL_WRITING);
            return;
        }

        if (createdNow) {
            try {
                bufferWriter.append("run" + DELIMITER + "time" + DELIMITER + "id_").append(filename).append(DELIMITER);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to append to the buffer writer" + FAIL_WRITING);
                return;
            }
        }

        val allFields = getAllFields(parsedTarget.getClass());

        val nonTransientFieldNames = new TreeSet<String>();

        for (var field : allFields) {
            Transient transientAnnotation = field.getAnnotation(Transient.class);
            if (transientAnnotation == null) {
                val t = field.getType();
                if (t.isPrimitive() || t.isEnum() || Number.class.isAssignableFrom(field.getType())
                    || t.equals(String.class) || t.equals(Character.class) || t.equals(Boolean.class)) {

                    val name = field.getName();

                    if (!name.equals("serialVersionUID")) nonTransientFieldNames.add(name);
                }
            }
        }

        fieldsForExport = extractFieldNames(createdNow, nonTransientFieldNames);
    }

    /**
     * Collects all fields, including inherited ones via {@link ExportToCSV#getAllFields(Class)}, checks if any of them
     * matches the required field name.
     *
     * @param type      Class type to be searched through.
     * @param fieldName The name of the {@link Field}, can be {@code null}.
     * @return {@code null}, if there is no fields at all or no matches; the actual {@link Field} object if there is a
     * match.
     * @throws NullPointerException when {@code type} is {@code null}.
     */
    static @Nullable Field findUnderlyingField(final @NonNull Class<?> type, final @Nullable String fieldName) {
        if (fieldName == null) return null;

        if (fieldName.isBlank()) return null;

        val allFields = getAllFields(type);

        if (allFields.size() == 0) return null;

        for (Field field : allFields) if (field.getName().equals(fieldName)) return field;

        return null;
    }

    /**
     * Recursive method to get all fields of a class, including inherited ones. No field type filtering is done here.
     *
     * @param type The class to be analyzed, can be {@code null}.
     * @return A list of fields, always not {@code null}.
     */
    static @NonNull List<Field> getAllFields(final @Nullable Class<?> type) {
        if (type == null) return new ArrayList<>();

        val inherited = new ArrayList<>(getAllFields(type.getSuperclass()));
        val own = Arrays.stream(type.getDeclaredFields()).toList();
        inherited.addAll(own);

        return inherited.stream().sorted(Comparator.comparing(Field::getName)).toList();
    }

    /**
     * Goes over a set of field names, tries to add them to the buffer. Also copies them to the {@link LinkedHashSet}.
     *
     * @param createdNowFlag Shows if there is a need to create headers, but only if there was no file there.
     * @param fieldNameSet   A set of strings that correspond to field names.
     * @return {@code null} if can't add to the buffer, a {@link LinkedHashSet} otherwise.
     * @throws NullPointerException when {@code fieldNameSet} is {@code null}.
     */
    public @Nullable LinkedHashSet<String> extractFieldNames(final boolean createdNowFlag,
                                                             final @NonNull TreeSet<String> fieldNameSet) {
        if (createdNowFlag) {
            for (var fieldNames : fieldNameSet)
                try {
                    bufferWriter.append(fieldNames).append(DELIMITER);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failed to append headers to the buffer writer" + FAIL_WRITING);
                    return null;
                }
        }
        return new LinkedHashSet<>(fieldNameSet);
    }

    /**
     * Generates filenames based on the parsed object name, its type (collection or not), and {@link Field} id.
     *
     * @param parsedTargetObject The object containing data saved to the file.
     * @param collectionModeFlag True when the parsed object is {@link Collection}.
     * @param id                 Object {@link Field} id.
     * @return A {@link String} or {@code null} when some checks fail.
     * @throws NullPointerException when {@code parsedTargetObject}, or {@code id}, or both are {@code null}.
     */
    @Nullable String generateFilename(final @NonNull Object parsedTargetObject, final boolean collectionModeFlag,
                                      final @NonNull Field id) {
        val filename = new StringBuilder();
        filename.append(parsedTargetObject.getClass().getSimpleName());

        try {
            var appValue = (!collectionModeFlag) ? ((PanelEntityKey) id.get(targetObject)).getId() : "";
            filename.append(appValue);
            return filename.toString();
        } catch (IllegalAccessException e) {
            log.log(Level.SEVERE, "Failed to append to the filename due to no access" + FAIL_WRITING);
            return null;
        } catch (ClassCastException e) {
            log.log(Level.SEVERE, "Target object doesn't have fields of the PanelEntityKey type" + FAIL_WRITING);
            return null;
        } catch (NullPointerException e) {
            log.log(Level.SEVERE, "The field of the PanelEntityKey type is not initialized, i.e., null" +
                FAIL_WRITING);
            return null;
        }
    }

    /**
     * Export data to the {@code *.csv} files named after the class of the target object (or if a collection of objects,
     * the class of the collection's members).
     *
     * @implSpec Only numbers, enums or strings are exported to {@code *.csv} files.
     */
    public void dumpToCSV() {
        val run = valueOf(simInstance.getCurrentRunNumber());
        val time = valueOf(simInstance.getTime());

        if (targetCollection != null && targetCollection.size() != 0) {
            for (Object obj : targetCollection) {
                if (addSimParametersToBuffer(run, time)) return;

                Field idField;
                try {
                    idField = obj.getClass().getDeclaredField(idFieldName);
                } catch (NoSuchFieldException e) {
                    log.log(Level.SEVERE, "No such field to get access to" + FAIL_WRITING);
                    return;
                }
                idField.setAccessible(true);
                try {
                    bufferWriter.append(String.valueOf(((PanelEntityKey) idField.get(obj)).getId())).append(DELIMITER);
                } catch (IOException | IllegalAccessException e) {
                    log.log(Level.SEVERE, "Failed to append panel data to the buffer writer" + FAIL_WRITING);
                    return;
                }

                for (String fieldName : fieldsForExport) {
                    val thisField = findUnderlyingField(obj.getClass(), fieldName);
                    Objects.requireNonNull(thisField).setAccessible(true);
                    Object value;
                    try {
                        value = thisField.get(obj);
                    } catch (IllegalAccessException e) {
                        log.log(Level.SEVERE, "Failed to get the value of the field" + FAIL_WRITING);
                        return;
                    }
                    try {
                        bufferWriter.append(value == null ? "null" : value.toString()).append(DELIMITER);
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Failed to append the value to the buffer writer" + FAIL_WRITING);
                        return;
                    }
                }
            }
        } else if (targetObject != null) {
            if (addSimParametersToBuffer(run, time)) return;

            try {
                bufferWriter.append(String.valueOf(((PanelEntityKey) targetObjectIdField.get(targetObject)).getId()))
                    .append(DELIMITER);
            } catch (IOException | IllegalAccessException | NullPointerException e) {
                log.log(Level.SEVERE, "Failed to append run id/time/panel data to the buffer writer" + FAIL_WRITING);
                return;
            }

            for (String fieldName : fieldsForExport) {
                Field thisField;
                try {
                    thisField = targetObject.getClass().getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    log.log(Level.SEVERE, "Failed to get the field" + FAIL_WRITING);
                    return;
                }
                thisField.setAccessible(true);
                Object value;
                try {
                    value = thisField.get(targetObject).toString();
                } catch (IllegalAccessException e) {
                    log.log(Level.SEVERE, "Failed to get the value of a field, no access" + FAIL_WRITING);
                    return;
                }
                try {
                    bufferWriter.append(value == null ? "null" : value.toString()).append(DELIMITER);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failed to append a field value to the buffer" + FAIL_WRITING);
                    return;
                }
            }
        } else {
            log.log(Level.SEVERE, "ExportToCSV's targetCollection and targetObject fields are both" +
                " null! Cannot export to CSV.");
            return;
        }
        try {
            bufferWriter.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to flush the existing buffer.");
        }
    }

    /**
     * Adds basic parameters such as the run id and the time of simulation to the file buffer.
     *
     * @param runValue  The string representation of a run id.
     * @param timeValue The string representation of a simulation time.
     * @return {@code boolean}, {@code false} if there is no problems, {@code true} when fails to append to the buffer.
     * @throws NullPointerException when {@code runValue}, or {@code timeValue}, ot both are {@code null}.
     */
    boolean addSimParametersToBuffer(final @NonNull String runValue, final @NonNull String timeValue) {
        try {
            bufferWriter.append(NEW_LINE).append(runValue).append(DELIMITER).append(timeValue).append(DELIMITER);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to append run id/time to the buffer writer" + FAIL_WRITING);
            return true;
        }
        return false;
    }
}

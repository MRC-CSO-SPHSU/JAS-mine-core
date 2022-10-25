package microsim.data.excel;

import lombok.NonNull;
import microsim.data.MultiKeyCoefficientMap;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelAssistant {

    /**
     * Converts a {@link Cell} object to a suitable data type.
     *
     * @param cell A {@link Cell} object.
     * @return A {@link String}, a {@link Double}, a {@link Boolean}, or {@code null}.
     * @throws NullPointerException when {@code cell} is {@code null};
     */
    private static @Nullable Object getCellValue(final @NonNull Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> cell.getNumericCellValue() - ((Double) cell.getNumericCellValue()).intValue() == 0.0 ?
                cell.getNumericCellValue() : ((Double) cell.getNumericCellValue()).intValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> null;
        };
    }

    /**
     * Converts the {@link Cell} object value to its {@code String} representation.
     *
     * @param cell A {@link Cell} object.
     * @return a string.
     * @throws NullPointerException     when {@code cell} is {@code null}.
     * @throws IllegalArgumentException when {@code cell} value is not a {@link String}, a {@link Double}, or a
     *                                  {@link Boolean}.
     */
    private static @NonNull String getStringCellValue(final @NonNull Cell cell) {
        return MultiKeyCoefficientMap.toStringKey(switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> getCellValue(cell) + "";
            case BOOLEAN -> cell.getBooleanCellValue() + "";
            default -> throw new IllegalStateException("Unexpected value: " + cell.getCellType());
        });
    }

    /**
     * Loads {@link MultiKeyCoefficientMap} from Excel spreadsheet data, reading from the first line of the spreadsheet,
     * and automatically finds the last line of the spreadsheet.
     *
     * @param excelFileName An Excel workbook ({@code *.xls} or {@code *.xlsx}) that stores the data.
     * @param sheetName     An Excel worksheet name that stores the data.
     * @param keyColumns    The number of columns (stored to the left of the worksheet) that represent keys. This will
     *                      equal the number of keys of the {@link MultiKeyCoefficientMap} that is returned.
     * @param valueColumns  The number of columns (stored to the right of the keys in the worksheet) that represents
     *                      values, not keys. This will equal the size of the {@code values[]} array for each
     *                      {@link org.apache.commons.collections4.keyvalue.MultiKey} in the
     *                      {@link MultiKeyCoefficientMap}.
     * @return an instance of {@link MultiKeyCoefficientMap} with coefficients and their corresponding labels.
     * @throws NullPointerException when {@code excelFileName}, or {@code sheetName}, or both are {@code null}.
     * @implSpec Default {@code startLine} is first line of Excel spreadsheet. {@code endLine} will be found
     * automatically. Blank lines are not allowed.
     * @see #loadCoefficientMap(String, String, int, int, int, int)
     */
    public static @Nullable MultiKeyCoefficientMap loadCoefficientMap(final @NonNull String excelFileName,
                                                                      final @NonNull String sheetName,
                                                                      final int keyColumns, final int valueColumns) {
        return loadCoefficientMap(excelFileName, sheetName, keyColumns, valueColumns, 1, Integer.MAX_VALUE);
    }

    /**
     * Load {@link MultiKeyCoefficientMap} from Excel spreadsheet data, choosing which line to start reading from via
     * the {@code startLine} parameter.
     *
     * @param excelFileName An Excel workbook ({@code *.xls} or {@code *.xlsx}) that stores the data.
     * @param sheetName     An Excel worksheet name that stores the data.
     * @param keyColumns    The number of columns (stored to the left of the worksheet) that represent keys. This will
     *                      equal the number of keys of the {@link MultiKeyCoefficientMap} that is returned.
     * @param valueColumns  The number of columns (stored to the right of the keys in the worksheet) that represents
     *                      values, not keys. This will equal the size of the {@code values[]} array for each
     *                      {@link org.apache.commons.collections4.keyvalue.MultiKey} in the
     *                      {@link MultiKeyCoefficientMap}.
     * @param startLine     Parameter specifying the (physical, not logical) Excel row number at which to start reading
     *                      (1 is the first line)
     * @param endLine       Parameter specifying the (physical, not logical) Excel row number at which to finish
     *                      reading (1 is the first line)
     * @return an instance of {@link MultiKeyCoefficientMap} with coefficients and their respective labels.
     * @throws NullPointerException when {@code excelFileName}, or {@code sheetName}, or both are {@code null}.
     * @implSpec {@code startLine} and {@code endLine} are physical (not logical) rows, therefore they have to be
     * decremented by 1.
     */
    public static @Nullable MultiKeyCoefficientMap loadCoefficientMap(final @NonNull String excelFileName,
                                                                      final @NonNull String sheetName,
                                                                      final int keyColumns, final int valueColumns,
                                                                      final int startLine, final int endLine) {

        MultiKeyCoefficientMap map = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(excelFileName);
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            Sheet worksheet = workbook.getSheet(sheetName);

            Row headerRow = worksheet.getRow(startLine - 1);
            String[] keyVector = new String[keyColumns];
            for (int j = 0; j < keyColumns; j++) {
                Cell cell = headerRow.getCell((short) j, MissingCellPolicy.RETURN_BLANK_AS_NULL);
                keyVector[j] = getStringCellValue(cell);
            }
            String[] valueVector = new String[valueColumns];
            for (int j = keyColumns; j < valueColumns + keyColumns; j++) {
                Cell cell = headerRow.getCell((short) j, MissingCellPolicy.RETURN_BLANK_AS_NULL);
                valueVector[j - keyColumns] = getStringCellValue(cell);
            }

            map = new MultiKeyCoefficientMap(keyVector, valueVector);

            for (int i = startLine; i <= Math.min(worksheet.getLastRowNum(), endLine - 1); i++) {
                Row row = worksheet.getRow(i);
                if (row == null) continue;
                Object[] keyValueVector;
                if (valueColumns == 1) {
                    keyValueVector = new Object[keyColumns + valueColumns];
                    for (int j = 0; j < keyColumns; j++) {
                        Cell cell = row.getCell((short) j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        keyValueVector[j] = getCellValue(cell);
                    }
                    for (int j = keyColumns; j < keyColumns + valueColumns; j++) {
                        Cell cell = row.getCell((short) j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        keyValueVector[j] = getCellValue(cell);
                    }
                } else {
                    keyValueVector = new Object[keyColumns + 1];
                    for (int j = 0; j < keyColumns; j++) {
                        Cell cell = row.getCell((short) j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        keyValueVector[j] = getCellValue(cell);
                    }
                    Object[] values = new Object[valueColumns];
                    for (int j = 0; j < valueColumns; j++) {
                        Cell cell = row.getCell((short) (j + keyColumns), MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        values[j] = getCellValue(cell);
                    }
                    keyValueVector[keyValueVector.length - 1] = values;
                }
                map.putValue(keyValueVector);
            }
        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }

        return map;
    }

}

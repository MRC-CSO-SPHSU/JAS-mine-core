package microsim.statistics;

/**
 * Used by statistical object to access array of double values.
 */
public interface DoubleArraySource {
	/**
	 * Return the currently cached array of double values.
	 * @return An array of double or a null pointer if the source is empty.
	 */
	double[] getDoubleArray();
}
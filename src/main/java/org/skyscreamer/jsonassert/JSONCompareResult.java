package org.skyscreamer.jsonassert;

/**
 * Bean for holding results from JSONCompare.
 */
public class JSONCompareResult {
    private boolean _success;
    private StringBuilder _message;
    private String _field;
    private Object _expected;
    private Object _actual;

    /**
     * Default constructor.
     */
    public JSONCompareResult() {
        this(true, null);
    }

    private JSONCompareResult(boolean success, String message) {
        _success = success;
        _message = new StringBuilder(message == null ? "" : message);
    }

    /**
     * Did the comparison pass?
     * @return True if it passed
     */
    public boolean passed() {
        return _success;
    }

    /**
     * Did the comparison fail?
     * @return True if it failed
     */
    public boolean failed() {
        return !_success;
    }

    /**
     * Result message
     * @return String explaining why if the comparison failed
     */
    public String getMessage() {
        return _message.toString();
    }

    /**
     * Actual field value
     * 
     * @return a {@code JSONObject}, {@code JSONArray} or other {@code Object}
     *         instance, or {@code null} if the comparison did not fail on a
     *         particular field
     */
    public Object getActual() {
        return _actual;
    }
    
    /**
     * Expected field value
     * 
     * @return a {@code JSONObject}, {@code JSONArray} or other {@code Object}
     *         instance, or {@code null} if the comparison did not fail on a
     *         particular field
     */
    public Object getExpected() {
        return _expected;
    }
    
    /**
     * Check if comparison failed on a particular field
     */
    public boolean isFailureOnField() {
        return _field != null;
    }

    /**
     * Dot-separated path the the field that failed comparison
     * 
     * @return a {@code String} instance, or {@code null} if the comparison did
     *         not fail on a particular field
     */
    public String getField() {
        return _field;
    }
    
    public void fail(String message) {
        _success = false;
        if (_message.length() == 0) {
            _message.append(message);
        } else {
            _message.append(" ; ").append(message);
        }
    }

    /**
     * Identify that the comparison failed
     * @param field Which field failed
     * @param expected Expected result
     * @param actual Actual result
     */
    public void fail(String field, Object expected, Object actual) {
        this._field = field;
        this._expected = expected;
        this._actual = actual;
        fail(formatFailureMessage(field, expected, actual));
    }

    public String formatFailureMessage(String field, Object expected, Object actual) {
        return field + "\nExpected: " + expected + "\n     got: " + actual + "\n";
    }
}

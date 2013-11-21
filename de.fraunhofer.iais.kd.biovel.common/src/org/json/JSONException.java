package org.json;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 * <p>
 * Version 2x : JSONException is changed to be a RuntimeException.
 * <code>Test</code> runs the same as before. (Karl-Heinz Sylla)
 * 
 * @author JSON.org
 * @version 2x
 */
public class JSONException extends RuntimeException {

    private static final long serialVersionUID = 1;

    private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     * 
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
    }

    public JSONException(Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}

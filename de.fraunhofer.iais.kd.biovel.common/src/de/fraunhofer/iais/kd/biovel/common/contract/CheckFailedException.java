package de.fraunhofer.iais.kd.biovel.common.contract;

public class CheckFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CheckFailedException() {
    }

    public CheckFailedException(String message) {
        super(message);
    }

    public CheckFailedException(Throwable cause) {
        super(cause);
    }

    public CheckFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}

package de.fraunhofer.iais.kd.biovel.shim.util;

import de.fraunhofer.iais.kd.biovel.common.contract.ApplicationException;

public class ShimException extends ApplicationException{
    
    private static final long serialVersionUID = 1L;

    public ShimException() {
        super();
    }

    public ShimException(String msg) {
        super(msg);
    }

    public ShimException(Throwable throwable) {
        super(throwable);
    }

    public ShimException(String msg,Throwable throwable) {
        super(msg, throwable);
    }
}

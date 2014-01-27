package de.fraunhofer.iais.kd.biovel.util;

import de.fraunhofer.iais.kd.biovel.common.contract.ApplicationException;

public class WorkflowException extends ApplicationException{
    
    private static final long serialVersionUID = 1L;

    public WorkflowException() {
        super();
    }

    public WorkflowException(String msg) {
        super(msg);
    }

    public WorkflowException(Throwable throwable) {
        super(throwable);
    }

    public WorkflowException(String msg,Throwable throwable) {
        super(msg, throwable);
    }
}

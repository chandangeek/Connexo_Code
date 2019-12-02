package com.energyict.mdc.engine.offline.core.exception;

import java.io.PrintWriter;

/**
 * User: gde
 * Date: 6/05/13
 */
public class CompositeCommandBusinessException extends BusinessException {

    private String compositeCommandName;
    private BusinessException businessException;

    public CompositeCommandBusinessException(String commandName, BusinessException ex) {
        super(ex.getMessageId(), "");
        compositeCommandName = commandName;
        businessException = ex;
    }

    public String getCompositeCommandName() {
        return compositeCommandName;
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        businessException.printStackTrace(s);
    }
}

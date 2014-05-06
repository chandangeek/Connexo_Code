package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import org.json.JSONException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Models the exceptional situation where access to the underlying data source has failed.
 * The usual situations in which these failures will occur are
 * during development when the code is not yet complete or contains issues
 * that cause malformed sql statement.
 * The unusual situations in which these failures will occur during runtime
 * are typically so severe that it is impossible to recover from
 * the situation as a whole.
 * For this reason, these situation have been modeled
 * as a runtime exception to avoid exposing this exception
 * on all or almost all methods in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-16 (16:31)
 */
public class DataAccessException extends ComServerRuntimeException {

    public DataAccessException (String message) {
        super(exceptionCode(), message);
    }

    public DataAccessException (NotFoundException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    public DataAccessException (SQLException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    public DataAccessException (JSONException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    public DataAccessException (IOException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    public DataAccessException (InterruptedException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    public DataAccessException (ExecutionException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    public DataAccessException (TimeoutException cause) {
        super(cause, exceptionCode(), cause.getMessage());
    }

    private static ExceptionCode exceptionCode () {
        return new ExceptionCode(ComServerPersistenceReferenceScope.SINGLETON, ExceptionType.SQL, ComServerPersistenceExceptionReferences.UNEXPECTED_SQL_ERROR);
    }

}
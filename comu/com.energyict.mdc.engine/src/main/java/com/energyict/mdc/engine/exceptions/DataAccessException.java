/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;

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

    public DataAccessException(MessageSeed messageSeed) {
        super(messageSeed);
    }

    public DataAccessException(ClassNotFoundException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(ClassCastException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(SQLException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(JSONException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(IOException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(InterruptedException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(ExecutionException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

    public DataAccessException(TimeoutException cause, MessageSeed messageSeed) {
        super(cause, messageSeed);
    }

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlExceptionMapper implements ExceptionMapper<SQLException> {

    private static final Logger LOGGER = Logger.getLogger(SqlExceptionMapper.class.getName());
    private final Provider<SensitiveExceptionInfo> infoProvider;

    @Inject
    public SqlExceptionMapper(Provider<SensitiveExceptionInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(SQLException exception) {
        SensitiveExceptionInfo exceptionInfo = infoProvider.get();
        exceptionInfo = exceptionInfo.from(exception);
        LOGGER.log(Level.SEVERE, exceptionInfo.errorCode + " - " + exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionInfo).build();
    }

}


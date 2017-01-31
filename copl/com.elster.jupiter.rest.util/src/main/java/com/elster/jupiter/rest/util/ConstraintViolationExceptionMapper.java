/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Whenever a REST call results in a ConstraintViolationException, this mapper will convert the exception in a Result understood by our
 * front-end ExtJS application. The result looks like:
 * {
 *     "success" : false,
 *     "message" : <some message, e.g. "update failed">,
 *     "error"   : <detailing the error, localized message>,
 *     "errors"  : [
 *          {
 *              "id"  : <field id, eg. "name">,
 *              "msg" : <message detailing what is wrong with the field, eg MDC.CanNotBeNull, localized>
 *          },
 *          {
 *              ...
 *          }
 *      ]
 * }
 */
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>{


    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public ConstraintViolationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(infoProvider.get().from(exception)).build();
    }

}

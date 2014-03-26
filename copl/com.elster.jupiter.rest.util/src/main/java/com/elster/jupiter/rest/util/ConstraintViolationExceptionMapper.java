package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.NlsService;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>{

    private final NlsService nlsService;

    @Inject
    public ConstraintViolationExceptionMapper(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ConstraintViolationInfo(nlsService).from(exception)).build();
    }

}

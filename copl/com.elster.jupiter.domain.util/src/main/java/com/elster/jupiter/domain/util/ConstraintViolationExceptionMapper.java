package com.elster.jupiter.domain.util;

import com.elster.jupiter.nls.NlsService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.annotate.JsonProperty;

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
        return Response.status(Response.Status.BAD_REQUEST).entity(new ConstraintViolationInfo(exception)).build();
    }

    public class ConstraintViolationInfo {
        @JsonProperty("success")
        public final boolean success = false;
        @JsonProperty("message")
        public String message;
        @JsonProperty("error")
        public String error;
        @JsonProperty("errors")
        public List<FieldError> errors = new ArrayList<>();

        public ConstraintViolationInfo(ConstraintViolationException exception) {
            message=exception.getMessage();
            error=exception.getLocalizedMessage();

            for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
                errors.add(new FieldError(constraintViolation.getPropertyPath().toString(), nlsService.interpolate(constraintViolation)));
            }
        }

        class FieldError {
            @JsonProperty("id")
            public String fieldIdentifier;
            @JsonProperty("msg")
            public String fieldLevelMessage;

            FieldError(String fieldIdentifier, String fieldLevelMessage) {
                this.fieldIdentifier = fieldIdentifier;
                this.fieldLevelMessage = fieldLevelMessage;
            }
        }
    }
}

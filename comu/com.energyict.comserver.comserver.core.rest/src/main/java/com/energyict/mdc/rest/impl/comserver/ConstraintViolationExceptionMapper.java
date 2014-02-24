package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.NlsService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
        public final boolean success = false;
        public String message;
        public String error;
        public List<FieldError> errors = new ArrayList<>();

        public ConstraintViolationInfo(ConstraintViolationException exception) {
            message=exception.getMessage();
            error=exception.getLocalizedMessage();

            for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
                errors.add(new FieldError(constraintViolation.getPropertyPath().toString(), nlsService.interpolate(constraintViolation)));
            }
        }

        class FieldError {
            public String field;
            public String msg;

            FieldError(String field, String msg) {
                this.field = field;
                this.msg = msg;
            }
        }
    }
}

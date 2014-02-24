package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.NlsService;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

public class ConstraintViolationInfo {
    public final boolean success = false;
    public String message;
    public String error;
    public List<FieldError> errors = new ArrayList<>();

    public ConstraintViolationInfo(ConstraintViolationException exception, NlsService nlsService) {
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

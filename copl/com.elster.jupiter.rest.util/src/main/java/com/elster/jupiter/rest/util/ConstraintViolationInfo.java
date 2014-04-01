package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * Whenever a REST call results in a ConstraintViolationException(or other), this mapper will convert the exception in a Result understood by our
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
public class ConstraintViolationInfo {
    @JsonIgnore
    private final NlsService nlsService;
    @JsonProperty("success")
    public final boolean success = false;
    @JsonProperty("message")
    public String message;
    @JsonProperty("error")
    public String error;
    @JsonProperty("errors")
    public List<FieldError> errors = new ArrayList<>();

    public ConstraintViolationInfo(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    public ConstraintViolationInfo from(ConstraintViolationException exception) {
        message=exception.getMessage();
        error=exception.getLocalizedMessage();

        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            errors.add(new FieldError(constraintViolation.getPropertyPath().toString(), nlsService.interpolate(constraintViolation)));
        }
        return this;
    }

    public ConstraintViolationInfo from(LocalizedFieldValidationException exception) {
        return this.from(exception, new HashMap<String, String>());
    }

    public ConstraintViolationInfo from(LocalizedFieldValidationException exception, Map<String, String> fieldMappings) {
        if (fieldMappings.containsKey(exception.getViolatingProperty())) {
            errors.add(new FieldError(fieldMappings.get(exception.getViolatingProperty()), exception.getLocalizedMessage()));
        } else {
            errors.add(new FieldError(exception.getViolatingProperty(), exception.getLocalizedMessage()));
        }

        return this;
    }

    public ConstraintViolationInfo from(LocalizedException exception) {
        return this.from(exception, new HashMap<String, String>());
    }

    public ConstraintViolationInfo from(LocalizedException exception, Map<String, String> fieldMappings) {
        message=exception.getLocalizedMessage();
        error=exception.getMessageSeed().getKey();

        return this;
    }

    public ConstraintViolationInfo from(JsonMappingException exception) {
        errors.add(new FieldError(exception.getPath().get(exception.getPath().size()-1).getFieldName(), "{"+MessageSeeds.INVALID_VALUE.getKey()+"}"));

        return this;
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

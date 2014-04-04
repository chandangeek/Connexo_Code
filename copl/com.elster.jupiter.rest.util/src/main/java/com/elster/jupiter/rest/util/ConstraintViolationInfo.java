package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
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

    @Inject
    public ConstraintViolationInfo(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    public ConstraintViolationInfo from(ConstraintViolationException exception) {
        message=exception.getMessage();
        error=exception.getLocalizedMessage();

        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            if (constraintViolation.getPropertyPath()!=null) {
                errors.add(new FieldError(constraintViolation.getPropertyPath().toString(), nlsService.interpolate(constraintViolation)));
            }
        }
        return this;
    }

    public void addFieldError(String fieldIdentifier, String fieldLevelMessage) {
        errors.add(new FieldError(fieldIdentifier, fieldLevelMessage));
    }

    public ConstraintViolationInfo from(JsonMappingException exception) {
        errors.add(new FieldError(exception.getPath().get(exception.getPath().size()-1).getFieldName(),
                nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.REST).getString(MessageSeeds.INVALID_VALUE.getKey(), "Invalid value")));

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

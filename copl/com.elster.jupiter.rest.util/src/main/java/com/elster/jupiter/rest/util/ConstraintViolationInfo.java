/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.domain.util.FormValidationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.rest.util.impl.MessageSeeds;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final Thesaurus thesaurus;
    @JsonProperty("success")
    public final boolean success = false;
    @JsonProperty("message")
    public String message;
    @JsonProperty("error")
    public String error;
    @JsonProperty("errorCode")
    public String errorCode;
    @JsonProperty("errors")
    public List<FieldError> errors = new ArrayList<>();

    @Inject
    public ConstraintViolationInfo(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ConstraintViolationInfo from(ConstraintViolationException exception) {
        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            if (constraintViolation.getPropertyPath() != null) {
                errors.add(new FieldError(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()));
            }
        }
        return this;
    }

    public void addFieldError(String fieldIdentifier, String fieldLevelMessage) {
        errors.add(new FieldError(fieldIdentifier, fieldLevelMessage));
    }

    public ConstraintViolationInfo from(JsonMappingException exception) {
        String property = getPathAsSingleProperty(exception);
        addFieldError(property, thesaurus.getString(MessageSeeds.INVALID_VALUE.getKey(), "Invalid value"));
        return this;
    }

    public ConstraintViolationInfo from(RestValidationBuilder.RestValidationException exception) {
        exception.getErrors().forEach(this::from);
        return this;
    }

    public ConstraintViolationInfo from(FormValidationException exception) {
        exception.getExceptions().forEach((key, messages) -> messages.forEach(message -> addFieldError(key, message)));
        return this;
    }

    private String getPathAsSingleProperty(JsonMappingException exception) {
        return exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining("."));
    }

    public ConstraintViolationInfo from(LocalizedFieldValidationException fieldException) {
        String messageTemplate = thesaurus.getString(fieldException.getMessageSeed().getKey(), fieldException.getMessageSeed().getDefaultFormat());
        StringBuffer formattedMessage = new MessageFormat(messageTemplate).format(fieldException.getArgs(), new StringBuffer(), null);
        addFieldError(fieldException.getViolatingProperty(), formattedMessage.toString());
        return this;
    }

    public ConstraintViolationInfo from(LocalizedException exception) {
        this.message = exception.getLocalizedMessage();
        this.error = exception.getMessageSeed().getKey();
        this.errorCode = exception.getErrorCode();
        return this;
    }

    public ConstraintViolationInfo from(OptimisticLockException exception) {
        this.message = thesaurus.getFormat(MessageSeeds.OPTIMISTIC_LOCK_FAILED).format();
        this.error = exception.getMessageSeed().getKey();
        this.errorCode = exception.getErrorCode();
        return this;
    }

    static class FieldError {
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

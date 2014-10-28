package com.elster.jupiter.issue.rest.response.cep;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.elster.jupiter.issue.share.cep.CreationRuleOrActionValidationException;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;

public class CreationRuleOrActionValidationExceptionMapper implements ExceptionMapper<CreationRuleOrActionValidationException> {
    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public CreationRuleOrActionValidationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(CreationRuleOrActionValidationException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.message= exception.getLocalizedMessage();
        constraintViolationInfo.error= exception.getMessageSeed().getKey();
        for (Map.Entry<String, String> detail : exception.getErrors().entrySet()) {
            constraintViolationInfo.addFieldError(detail.getKey(), detail.getValue());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(constraintViolationInfo).build();
    }
}

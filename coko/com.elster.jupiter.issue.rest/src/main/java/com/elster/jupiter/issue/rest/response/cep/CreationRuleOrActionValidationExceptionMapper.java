package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.CreationRuleOrActionValidationException;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;

public final class CreationRuleOrActionValidationExceptionMapper implements ExceptionMapper<CreationRuleOrActionValidationException> {
    private final Provider<ConstraintViolationInfo> infoProvider;

    @Inject
    public CreationRuleOrActionValidationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Override
    public Response toResponse(CreationRuleOrActionValidationException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        // uncomment only if you want the 'Request failed' pop-up for form validation
        // constraintViolationInfo.message= exception.getLocalizedMessage();
        constraintViolationInfo.error= exception.getMessageSeed().getKey();
        for (Map.Entry<String, String> detail : exception.getErrors().entrySet()) {
            constraintViolationInfo.addFieldError(detail.getKey(), detail.getValue());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(constraintViolationInfo).build();
    }
}

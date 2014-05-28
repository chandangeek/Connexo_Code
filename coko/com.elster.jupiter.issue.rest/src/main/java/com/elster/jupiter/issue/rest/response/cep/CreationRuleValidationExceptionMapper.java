package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.CreationRuleValidationException;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;

public class CreationRuleValidationExceptionMapper implements ExceptionMapper<CreationRuleValidationException> {
    private final Provider<ConstraintViolationInfo> infoProvider;
    private final NlsService nlsService;

    @Inject
    public CreationRuleValidationExceptionMapper(Provider<ConstraintViolationInfo> infoProvider, NlsService nlsService) {
        this.infoProvider = infoProvider;
        this.nlsService = nlsService;
    }

    @Override
    public Response toResponse(CreationRuleValidationException exception) {
        ConstraintViolationInfo constraintViolationInfo = infoProvider.get();
        constraintViolationInfo.message= exception.getLocalizedMessage();
        constraintViolationInfo.error= exception.getMessageSeed().getKey();
        for (Map.Entry<String, String> detail : exception.getErrors().entrySet()) {
            constraintViolationInfo.addFieldError(detail.getKey(), detail.getValue());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(constraintViolationInfo).build();
    }
}

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class LocalizedFieldValidationExceptionMapper implements ExceptionMapper<LocalizedFieldValidationException>{

    private final NlsService nlsService;
    private final Map<String, String> fieldMappings = new HashMap<>();

    @Inject
    public LocalizedFieldValidationExceptionMapper(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public Response toResponse(LocalizedFieldValidationException fieldException) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ConstraintViolationInfo(nlsService).from(fieldException,fieldMappings)).build();
    }

    public LocalizedFieldValidationExceptionMapper fieldMappings(Map<String, String> mappings) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            fieldMappings.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

}

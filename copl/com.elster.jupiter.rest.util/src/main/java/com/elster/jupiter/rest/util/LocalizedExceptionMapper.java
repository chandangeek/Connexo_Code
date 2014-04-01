package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;


@Provider
public class LocalizedExceptionMapper implements ExceptionMapper<LocalizedException>{

    private final NlsService nlsService;
    private final Map<String, String> fieldMappings = new HashMap<>();

    @Inject
    public LocalizedExceptionMapper(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public Response toResponse(LocalizedException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ConstraintViolationInfo(nlsService).from(exception,fieldMappings)).build();
    }

    public LocalizedExceptionMapper fieldMappings(Map<String, String> mappings) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            fieldMappings.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

}

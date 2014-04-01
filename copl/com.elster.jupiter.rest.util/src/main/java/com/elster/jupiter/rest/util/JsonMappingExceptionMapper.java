package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.NlsService;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * When an JavaXmlBasedAdapter fails to convert JSON into a valid object, an exception thrown. This mapper will convert
 * that exception into a HTTP 400 and provide a JSON description of the issue in a format understood out of the box
 * by ExtJS
 */
public  class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private final NlsService nlsService;

    @Inject
    public JsonMappingExceptionMapper(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public Response toResponse(JsonMappingException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ConstraintViolationInfo(nlsService).from(exception)).build();
    }
}
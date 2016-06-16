package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.FieldResource;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/15/16.
 */
@Path("/fields")
public class WebServicesFieldResource extends FieldResource {

    @Inject
    public WebServicesFieldResource(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @GET
    @Transactional
    @Path("/logLevel")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Object getLogLevelValues() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", Arrays.asList(LogLevel.values()), Stream.of(LogLevel
                .values())
                .map(LogLevel::getTranslationKey)
                .collect(toList()));
    }

    @GET
    @Transactional
    @Path("/direction")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Object getDirectionValues() {
        return asJsonArrayObjectWithTranslation("directions", "direction", Arrays.asList(WebServiceDirection.values()),
                Stream.of(WebServiceDirection.values())
                        .map(WebServiceDirection::getTranslationKey)
                        .collect(toList()));
    }

    @GET
    @Transactional
    @Path("/authenticationMethod")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Object getAuthenticationMethods() {
        return asJsonArrayObjectWithTranslation("authenticationMethods", "authenticationMethod", Arrays.asList(EndPointAuthentication
                        .values()),
                Stream.of(EndPointAuthentication.values())
                        .map(EndPointAuthentication::getTranslationKey)
                        .collect(toList()));
    }


}

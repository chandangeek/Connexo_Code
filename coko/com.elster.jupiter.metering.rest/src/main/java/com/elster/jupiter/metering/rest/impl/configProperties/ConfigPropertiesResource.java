/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.configProperties;

import com.elster.jupiter.metering.ConfigPropertiesService;
import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/cfgprops")
public class ConfigPropertiesResource {
    private final ConfigPropertiesService configPropertiesService;
    private final ConfigPropertiesInfoFactory configPropertiesInfoFactory;

    @Inject
    public ConfigPropertiesResource(ConfigPropertiesService configPropertiesService, ConfigPropertiesInfoFactory configPropertiesInfoFactory){
        this.configPropertiesService = configPropertiesService;
        this.configPropertiesInfoFactory = configPropertiesInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{scope}")
    public ConfigPropertiesInfo getConfigProeprties(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("scope") String scope) {
        return configPropertiesService.findConfigFroperties(scope)
                .map(cp -> configPropertiesInfoFactory.from(cp))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{scope}")
    public Response saveConfigProeprties(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("scope") String scope, ConfigPropertiesInfo configPropertiesInfo) {
        ConfigPropertiesProvider configPropertiesProvider = configPropertiesService.findConfigFroperties(scope)
               .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        configPropertiesInfo.properties.stream()
                .map(customTaskPropertiesInfo -> customTaskPropertiesInfo.properties)
                .flatMap(List::stream)
                .forEach(propertyInfo -> configPropertiesProvider.setProperty(propertyInfo.key, propertyInfo.propertyValueInfo.value));
        configPropertiesProvider.update();

        return Response.status(Response.Status.OK).build();
    }

}

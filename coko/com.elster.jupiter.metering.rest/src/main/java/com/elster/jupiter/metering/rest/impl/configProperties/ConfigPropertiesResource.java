/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.configProperties;

import com.elster.jupiter.metering.ConfigPropertiesService;
import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

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
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/cfgprops")
public class ConfigPropertiesResource {
    private final ConfigPropertiesService configPropertiesService;
    private final ConfigPropertiesInfoFactory configPropertiesInfoFactory;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public ConfigPropertiesResource(ConfigPropertiesService configPropertiesService, ConfigPropertiesInfoFactory configPropertiesInfoFactory, ThreadPrincipalService threadPrincipalService){
        this.configPropertiesService = configPropertiesService;
        this.configPropertiesInfoFactory = configPropertiesInfoFactory;
        this.threadPrincipalService = threadPrincipalService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{scope}")
    public ConfigPropertiesInfo getConfigProeprties(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("scope") String scope) {
        return configPropertiesInfoFactory
                .from(findConfigFropertiesOrThrowException(scope, "VIEW", appKey));
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{scope}")
    public Response saveConfigProeprties(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("scope") String scope, ConfigPropertiesInfo configPropertiesInfo) {
        ConfigPropertiesProvider configPropertiesProvider = findConfigFropertiesOrThrowException(scope, "EDIT", appKey);

        configPropertiesInfo.properties.stream()
                .map(customTaskPropertiesInfo -> customTaskPropertiesInfo.properties)
                .flatMap(List::stream)
                .forEach(propertyInfo -> configPropertiesProvider.setProperty(propertyInfo.key, propertyInfo.propertyValueInfo.value));
        configPropertiesProvider.update();

        return Response.status(Response.Status.OK).build();
    }

    private ConfigPropertiesProvider findConfigFropertiesOrThrowException(String scope, String action, String application){
        ConfigPropertiesProvider provider = configPropertiesService.findConfigFroperties(scope)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        List<String> privilegesInAction = new ArrayList<>();
        if (action.equals("VIEW")){
            privilegesInAction = provider.getViewPrivileges();
        }
        else if (action.equals("EDIT")){
            privilegesInAction = provider.getViewPrivileges();
        }
        if (!hasOneLeastPrivileges(privilegesInAction, application)){
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return provider;
    }

    private Optional<User> getCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return Optional.empty();
        }
        return Optional.of((User) principal);
    }

    private boolean hasOneLeastPrivileges(List<String> privileges, String application) {
        return getCurrentUser()
                .map(user ->
                        privileges.stream()
                                .filter(privilege -> user.hasPrivilege(application, privilege))
                                .count() > 0)
                .orElse(false);
    }
}

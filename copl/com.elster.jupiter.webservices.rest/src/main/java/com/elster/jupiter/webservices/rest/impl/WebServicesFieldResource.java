/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.FieldResource;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 6/15/16.
 */
@Path("/fields")
public class WebServicesFieldResource extends FieldResource {

    private final UserService userService;

    @Inject
    public WebServicesFieldResource(Thesaurus thesaurus, UserService userService) {
        super(thesaurus);
        this.userService = userService;
    }

    @GET
    @Transactional
    @Path("/logLevel")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public Object getLogLevelValues() {
        return asJsonArrayObjectWithTranslation("logLevels", "id", Arrays.asList(LogLevel.values()), Stream.of(LogLevel
                .values())
                .map(LogLevel::getTranslationKey)
                .collect(toList()));
    }

    @GET
    @Transactional
    @Path("/direction")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public Object getDirectionValues() {
        return asJsonArrayObjectWithTranslation("directions", "id", Arrays.asList(WebServiceDirection.values()),
                Stream.of(WebServiceDirection.values())
                        .map(WebServiceDirection::getTranslationKey)
                        .collect(toList()));
    }

    @GET
    @Transactional
    @Path("/authenticationMethod")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public Object getAuthenticationMethods() {
        return asJsonArrayObjectWithTranslation("authenticationMethods", "id", Arrays.asList(EndPointAuthentication
                        .values()),
                Stream.of(EndPointAuthentication.values())
                        .map(EndPointAuthentication::getTranslationKey)
                        .collect(toList()));
    }

    @GET
    @Transactional
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Object getUserRoles() {
        List<Group> groups = userService.getGroups();
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put("roles", list);
        for (final Group value : groups) {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("id", value.getId());
            subMap.put("name", value.getName());
            list.add(subMap);
        }
        return map;
    }


}

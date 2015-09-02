package com.elster.jupiter.users.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.security.Privileges;

@Path("/field")
public class UsersFieldResource {
    
    private final UserPreferencesService userPreferencesService;
    private final ThreadPrincipalService threadPrincipalService;
    
    @Inject
    public UsersFieldResource(UserPreferencesService userPreferencesService, ThreadPrincipalService threadPrincipalService) {
        this.userPreferencesService = userPreferencesService;
        this.threadPrincipalService = threadPrincipalService;
    }
    
    @GET
    @Path("/locales")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public Response getLocales() {
        List<Locale> supportedLocales = userPreferencesService.getSupportedLocales();
        Map<String, List<LocaleInfo>> infos = new HashMap<>();
        infos.put("locales", supportedLocales.stream().map((locale) -> new LocaleInfo(locale, threadPrincipalService.getLocale())).collect(Collectors.toList()));
        return Response.ok(infos).build();
    }
}
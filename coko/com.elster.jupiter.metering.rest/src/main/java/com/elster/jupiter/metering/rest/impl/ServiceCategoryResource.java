/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/servicecategory")
public class ServiceCategoryResource {

    private final MeteringService meteringService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;

    @Inject
    public ServiceCategoryResource(MeteringService meteringService, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.meteringService = meteringService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_SERVICECATEGORY})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getServiceCategories(@BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        List<ServiceCategoryInfo> categories = Arrays.stream(ServiceKind.values())
                .map(meteringService::getServiceCategory).flatMap(sc -> sc.isPresent() && sc.get().isActive() ? Stream.of(new ServiceCategoryInfo(sc.get())) : Stream.empty()).sorted((a, b) -> a.displayName.compareToIgnoreCase(b.displayName)).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("categories", categories, queryParameters);
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_SERVICECATEGORY})
    @Path("/{kind}/custompropertysets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getServiceCategories(@PathParam("kind") String kind, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values())
                .filter(sk -> sk.name().equalsIgnoreCase(kind)).findFirst()
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        ServiceCategory category = meteringService.getServiceCategory(serviceKind)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<CustomPropertySetInfo> infos = category.getCustomPropertySets()
                .stream()
                .map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                .sorted((a, b) -> a.name.compareToIgnoreCase(b.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("serviceCategoryCustomPropertySets", infos, queryParameters);
    }
}

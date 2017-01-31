/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.ComponentStatus;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.systemadmin.rest.imp.response.ApplicationInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ApplicationInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.BundleTypeInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.BundleTypeInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentStatusInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentStatusInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class FieldResource {
    private ApplicationInfoFactory applicationInfoFactory;
    private BundleTypeInfoFactory bundleTypeInfoFactory;
    private ComponentStatusInfoFactory componentStatusInfoFactory;
    private SubsystemService subsystemService;

    @Inject
    public FieldResource(ApplicationInfoFactory applicationInfoFactory, BundleTypeInfoFactory bundleTypeInfoFactory, ComponentStatusInfoFactory componentStatusInfoFactory, SubsystemService subsystemService) {
        this.applicationInfoFactory = applicationInfoFactory;
        this.bundleTypeInfoFactory = bundleTypeInfoFactory;
        this.componentStatusInfoFactory = componentStatusInfoFactory;
        this.subsystemService = subsystemService;
    }

    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getSystemInformation(@BeanParam JsonQueryParameters queryParams) {
        List<ApplicationInfo> infos = subsystemService.getSubsystems().stream().map(applicationInfoFactory::asInfo).sorted((o1, o2) -> o1.name.compareTo(o2.name)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("applications", infos, queryParams);
    }

    @GET
    @Path("/bundleTypes")
    @RolesAllowed(Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getBundleTypes(@BeanParam JsonQueryParameters queryParams) {
        List<BundleTypeInfo> bundleTypeInfoList = EnumSet.complementOf(EnumSet.of(BundleType.NOT_APPLICABLE)).stream().map(bundleTypeInfoFactory::asInfo).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("bundleTypes", bundleTypeInfoList, queryParams);
    }

    @GET
    @Path("/componentStatuses")
    @RolesAllowed(Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getComponentStatuses(@BeanParam JsonQueryParameters queryParams) {
        List<ComponentStatusInfo> componentStatusInfoList = Arrays.stream(ComponentStatus.values()).map(componentStatusInfoFactory::asInfo).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("componentStatuses", componentStatusInfoList, queryParams);
    }
}

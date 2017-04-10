/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SecurityPropertySetResource {

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final UserService userService;
    private final SecurityPropertySetInfoFactory securityPropertySetInfoFactory;

    @Inject
    public SecurityPropertySetResource(ResourceHelper resourceHelper, Thesaurus thesaurus, UserService userService, SecurityPropertySetInfoFactory securityPropertySetInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.userService = userService;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySets(@PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<Group> groups = this.userService.getGroups();
        List<SecurityPropertySetInfo> securityPropertySetInfos =
                ListPager.of(deviceConfiguration.getSecurityPropertySets(), new SecurityPropertySetComparator())
                        .from(queryParameters)
                        .find()
                        .stream()
                        .map(set -> securityPropertySetInfoFactory.from(set, groups))
                        .collect(toList());

        return PagedInfoList.fromPagedList("data", securityPropertySetInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getSecurityPropertySet(@PathParam("securityPropertySetId") long securityPropertySetId) {
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(securityPropertySetId);

        List<Group> groups = this.userService.getGroups();
        return Response.status(Response.Status.OK).entity(securityPropertySetInfoFactory.from(securityPropertySet, groups)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createSecurityPropertySet(@PathParam("deviceConfigurationId") long deviceConfigurationId, SecurityPropertySetInfo info) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);

        if (info.authenticationLevelId == null) {
            info.authenticationLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (info.encryptionLevelId == null) {
            info.encryptionLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }

        SecurityPropertySetBuilder builder = deviceConfiguration
                .createSecurityPropertySet(info.name)
                .authenticationLevel(info.authenticationLevelId)
                .encryptionLevel(info.encryptionLevelId);
        SecurityPropertySet securityPropertySet = builder.build();

        List<Group> groups = this.userService.getGroups();
        return Response.status(Response.Status.CREATED).entity(securityPropertySetInfoFactory.from(securityPropertySet, groups)).build();
    }

    @PUT
    @Transactional
    @Path("/{securityPropertySetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response updateSecurityPropertySet(@PathParam("securityPropertySetId") long securityPropertySetId, SecurityPropertySetInfo info) {
        info.id = securityPropertySetId;
        SecurityPropertySet securityPropertySet = resourceHelper.lockSecurityPropertySetOrThrowException(info);
        info.writeTo(securityPropertySet);
        securityPropertySet.update();

        List<Group> groups = this.userService.getGroups();
        return Response.status(Response.Status.OK).entity(securityPropertySetInfoFactory.from(securityPropertySet, groups)).build();
    }

    @DELETE
    @Transactional
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteSecurityPropertySet(@PathParam("securityPropertySetId") long securityPropertySetId, SecurityPropertySetInfo info) {
        info.id = securityPropertySetId;
        SecurityPropertySet securityPropertySet = resourceHelper.lockSecurityPropertySetOrThrowException(info);
        securityPropertySet.getDeviceConfiguration().removeSecurityPropertySet(securityPropertySet);

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Transactional
    @Path("/authlevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetAuthLevels(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        List<SecurityLevelInfo> securityLevelInfos = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass ->
                SecurityLevelInfo.from(deviceProtocolPluggableClass.getDeviceProtocol().getAuthenticationAccessLevels())).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", securityLevelInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/enclevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetEncLevels(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        List<SecurityLevelInfo> securityLevelInfos = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass ->
                SecurityLevelInfo.from(deviceProtocolPluggableClass.getDeviceProtocol().getEncryptionAccessLevels())).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", securityLevelInfos, queryParameters);
    }

}

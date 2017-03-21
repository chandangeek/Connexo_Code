/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.SecurityAccessorInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class SecurityAccessorResource {

    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public SecurityAccessorResource(ResourceHelper resourceHelper, SecurityAccessorInfoFactory securityAccessorInfoFactory, ExceptionFactory exceptionFactory) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getSecurityAccessors(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return asPagedInfoList(securityAccessorInfoFactory.asInfo(device, uriInfo), "securityAccessors", queryParameters);
    }

    @GET
    @Transactional
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getKeys(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return asPagedInfoList(securityAccessorInfoFactory.asInfo(device, uriInfo, CryptographicType.MetaType.KEY), "keys", queryParameters);
    }

    @GET
    @Transactional
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getCertificates(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return asPagedInfoList(securityAccessorInfoFactory.asInfo(device, uriInfo, CryptographicType.MetaType.CERTIFICATE), "certificates", queryParameters);
    }

    private PagedInfoList asPagedInfoList(List<SecurityAccessorInfo> securityAccessorInfos, String rootKeyName, JsonQueryParameters queryParameters) {
        List<SecurityAccessorInfo> pagedInfos = ListPager.of(securityAccessorInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList(rootKeyName, pagedInfos, queryParameters);
    }
}

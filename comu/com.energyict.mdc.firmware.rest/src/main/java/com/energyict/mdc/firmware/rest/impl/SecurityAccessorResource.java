/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.firmware.rest.SecurityAccessorInfo;
import com.energyict.mdc.firmware.rest.SecurityAccessorInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/devicetypes/{deviceTypeId}/securityaccessors")
public class SecurityAccessorResource {
    private final ResourceHelper resourceHelper;
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;

    @Inject
    public SecurityAccessorResource(ResourceHelper resourceHelper,
                                    SecurityAccessorInfoFactory securityAccessorInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE,
            Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    @Path("/availablesecurityaccessors")
    public Response getSecurityAccessorsWithFileOperations(@PathParam("deviceTypeId") long deviceTypeId) {
        List<SecurityAccessorInfo> infoList = resourceHelper.getCertificatesWithFileOperations()
                .stream()
                .map(securityAccessorInfoFactory::from)
                .sorted(Comparator.comparing(k -> k.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return Response.ok(infoList).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE,
            Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public Response getSecurityAccessorInfo(@PathParam("deviceTypeId") long deviceTypeId) {
        return resourceHelper.findSecurityAccessorForSignatureValidation(deviceTypeId)
                .map(sa -> Response.ok(securityAccessorInfoFactory.from(sa)).build()).orElseGet(() -> Response.ok(new SecurityAccessorInfo()).build());
    }

    @DELETE
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE,
            Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    public Response deleteSecurityAccessor(@PathParam("deviceTypeId") long deviceTypeId) {
        resourceHelper.deleteSecurityAccessorForSignatureValidation(deviceTypeId);
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE,
            Privileges.Constants.VIEW_SECURITY_ACCESSORS, Privileges.Constants.EDIT_SECURITY_ACCESSORS})
    @Path("/addsecurityaccessor/{securityAccessorId}")
    public Response addSecurityAccessor(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("securityAccessorId") long securityAccessorId) {
        resourceHelper.findSecurityAccessorForSignatureValidation(deviceTypeId).ifPresent(dt -> resourceHelper.deleteSecurityAccessorForSignatureValidation(deviceTypeId));
        resourceHelper.addSecurityAccessorForSignatureValidation(securityAccessorId, deviceTypeId);
        return Response.ok().build();
    }

}

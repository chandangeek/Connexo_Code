/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/devices/{mrid}/keyAccessorTypes")
public class ServiceKeyTestResource {
    private final DeviceService deviceService;

    @Inject
    public ServiceKeyTestResource(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorTypeName}/markservicekey")
    public Response markServiceKey(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeName") String keyAccessorTypeName) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(() -> new IllegalArgumentException("Device does not exist"));
        SecurityAccessorType securityAccessorType = device.getDeviceType().getSecurityAccessorTypes().stream().filter(keyAccessorType -> keyAccessorType.getName().equals(keyAccessorTypeName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such security accessor on this device."));
        SecurityAccessor<SecurityValueWrapper> securityAccessor = (SecurityAccessor<SecurityValueWrapper>)device.getSecurityAccessor(securityAccessorType)
                .orElseThrow(() -> new IllegalArgumentException("No values defined for such security accessor on this device."));
        securityAccessor.setServiceKey(true);
        securityAccessor.save();
        return Response.ok().build();
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.ProtocolInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 12/2/14.
 */
public class ProtocolPropertiesResource {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final ResourceHelper resourceHelper;

    @Inject
    public ProtocolPropertiesResource(MdcPropertyUtils mdcPropertyUtils, ResourceHelper resourceHelper) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getDeviceProperties(@PathParam("deviceConfigurationId") long deviceConfigurationId) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        DeviceProtocolConfigurationProperties deviceProperties = deviceConfiguration.getDeviceProtocolProperties();
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass();
        ProtocolInfo protocolInfo = new ProtocolInfo();
        if (deviceProtocolPluggableClass.isPresent()) {
            protocolInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(
                    deviceProtocolPluggableClass.get().getDeviceProtocol().getPropertySpecs(),
                    deviceProperties.getTypedProperties());
            protocolInfo.id = deviceProtocolPluggableClass.get().getId();
            protocolInfo.name = deviceProtocolPluggableClass.get().getName();
            protocolInfo.deviceConfiguration = new DeviceConfigurationInfo(deviceConfiguration);
        }
        return Response.ok(protocolInfo).build();
    }

    @GET
    @Transactional
    @Path("/{protocolId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getDeviceProperties(@PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("protocolId") Long protocolId) {
        return this.getDeviceProperties(deviceConfigurationId);
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Response updateDeviceProperties(@PathParam("deviceConfigurationId") long deviceConfigurationId, ProtocolInfo protocolInfo) {
        DeviceConfiguration deviceConfiguration = resourceHelper.lockDeviceConfigurationOrThrowException(protocolInfo.deviceConfiguration);
        List<PropertySpec> propertySpecs = deviceConfiguration.getDeviceType()
                .getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs())
                .orElse(Collections.emptyList());
        DeviceProtocolConfigurationProperties deviceProtocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = mdcPropertyUtils.findPropertyValue(propertySpec, protocolInfo.properties);
            if (value == null || "".equals(value)) {
                deviceProtocolProperties.removeProperty(propertySpec.getName());
            } else {
                deviceProtocolProperties.setProperty(propertySpec.getName(), value);
            }
        }
        deviceConfiguration.save();
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{protocolId}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Response updateDevicePropertiesForProtocol(@PathParam("deviceConfigurationId") long deviceConfigurationId, ProtocolInfo protocolInfo) {
        return this.updateDeviceProperties(deviceConfigurationId, protocolInfo);
    }

}

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.configuration.rest.ProtocolInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is an artificial const5ruction to allow FrontEnd to re-use the common component for properties. Yes, this is poor design, but at the
 * same time we could avoid adding the properties to the device itself (where they belong).
 * Created by bvn on 12/1/14.
 */
@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class DeviceProtocolPropertyResource {

    private final MdcPropertyUtils mdcPropertyUtils;

    private Device device;

    @Inject
    public DeviceProtocolPropertyResource(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    DeviceProtocolPropertyResource with(Device device) {
        this.device=device;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getDeviceProperties() {
        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
        List <PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs() ,deviceProperties);
        ProtocolInfo protocolInfo = new ProtocolInfo();
        protocolInfo.id = device.getDeviceType().getDeviceProtocolPluggableClass().getId();
        protocolInfo.name = device.getDeviceType().getDeviceProtocolPluggableClass().getName();
        protocolInfo.properties = propertyInfos;
        return Response.ok(protocolInfo).build();
    }
    
    @GET
    @Path("/{protocolId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getDeviceProperties(@PathParam("protocolId") Long protocolId) {
        return this.getDeviceProperties();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{protocolId}")
    public Response updateDeviceProperties(ProtocolInfo protocolInfo, @PathParam("protocolId") Long protocolId) {
        List<PropertySpec> propertySpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = mdcPropertyUtils.findPropertyValue(propertySpec, protocolInfo.properties);
            if (value == null || "".equals(value)) {
                device.removeProtocolProperty(propertySpec.getName());
            } else {
                device.setProtocolProperty(propertySpec.getName(), value);
            }
        }
        return Response.ok().build();
    }
}

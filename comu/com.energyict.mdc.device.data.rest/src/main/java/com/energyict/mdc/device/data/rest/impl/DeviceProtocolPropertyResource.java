package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

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
import java.util.List;

/**
 * This is an artificial const5ruction to allow FrontEnd to re-use the common component for properties. Yes, this is poor design, but at the
 * same time we could avoid adding the properties to the device itself (where they belong).
 * Created by bvn on 12/1/14.
 */
@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class DeviceProtocolPropertyResource {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final ResourceHelper resourceHelper;

    private Device device;

    @Inject
    public DeviceProtocolPropertyResource(MdcPropertyUtils mdcPropertyUtils, ResourceHelper resourceHelper) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.resourceHelper = resourceHelper;
    }

    DeviceProtocolPropertyResource with(Device device) {
        this.device=device;
        return this;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getDeviceProperties() {
        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
        DeviceProtocolPluggableClass pluggableClass = device.getDeviceType().getDeviceProtocolPluggableClass();
        List <PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(pluggableClass.getDeviceProtocol().getPropertySpecs(), deviceProperties);
        DeviceProtocolInfo info = new DeviceProtocolInfo();
        info.id = pluggableClass.getId();
        info.name = pluggableClass.getName();
        info.properties = propertyInfos;
        info.version = pluggableClass.getEntityVersion();
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return Response.ok(info).build();
    }
    
    @GET @Transactional
    @Path("/{protocolId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getDeviceProperties(@PathParam("protocolId") Long protocolId) {
        return this.getDeviceProperties();
    }

    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{protocolId}")
    public Response updateDeviceProperties(DeviceProtocolInfo info, @PathParam("protocolId") Long protocolId) {
        info.id = protocolId;
        DeviceProtocolPluggableClass pluggableClass = resourceHelper.lockDeviceProtocolPluggableClassOrThrowException(info);
        List<PropertySpec> propertySpecs = pluggableClass.getDeviceProtocol().getPropertySpecs();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);
            if (value == null || "".equals(value)) {
                device.removeProtocolProperty(propertySpec.getName());
            } else {
                device.setProtocolProperty(propertySpec.getName(), value);
            }
        }
        return Response.ok().build();
    }
}

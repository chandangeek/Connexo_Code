package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.configuration.rest.ProtocolInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by bvn on 12/2/14.
 */
public class ProtocolPropertiesResource {

    private final MdcPropertyUtils mdcPropertyUtils;

    private DeviceConfiguration deviceConfiguration;

    @Inject
    public ProtocolPropertiesResource(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    ProtocolPropertiesResource with(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getDeviceProperties() {
        DeviceProtocolConfigurationProperties deviceProperties = deviceConfiguration.getDeviceProtocolProperties();
        List<PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs() ,deviceProperties.getTypedProperties());
        ProtocolInfo protocolInfo = new ProtocolInfo();
        protocolInfo.properties=propertyInfos;
        protocolInfo.id=this.deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getId();
        protocolInfo.name=this.deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getName();
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
    public Response updateDeviceProperties(ProtocolInfo protocolInfo) {
        List<PropertySpec> propertySpecs = deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs();
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{protocolId}")
    public Response updateDeviceProperties(ProtocolInfo protocolInfo, @PathParam("protocolId") Long protocolId) {
        return this.updateDeviceProperties(protocolInfo);
    }

}

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceProperties(@BeanParam QueryParameters queryParameters) {
        DeviceProtocolConfigurationProperties deviceProperties = deviceConfiguration.getDeviceProtocolProperties();
        List<PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs() ,deviceProperties.getTypedProperties());
        Collections.sort(propertyInfos, (o1, o2) -> o1.key.compareToIgnoreCase(o2.key));
        return PagedInfoList.asJson("properties", propertyInfos, queryParameters);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDeviceProperties(List<PropertyInfo> propertyInfos) {
        List<PropertySpec> propertySpecs = deviceConfiguration.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs();
        DeviceProtocolConfigurationProperties deviceProtocolProperties = deviceConfiguration.getDeviceProtocolProperties();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = mdcPropertyUtils.findPropertyValue(propertySpec, propertyInfos);
            if (value == null || "".equals(value)) {
                deviceProtocolProperties.removeProperty(propertySpec.getName());
            } else {
                deviceProtocolProperties.setProperty(propertySpec.getName(), value);
            }
        }
        deviceConfiguration.save();
        return Response.ok().build();
    }

}

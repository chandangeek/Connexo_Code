package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.Device;
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
 * Created by bvn on 12/1/14.
 */
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
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceProperties(@BeanParam QueryParameters queryParameters) {
        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
        List <PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs() ,deviceProperties);
        Collections.sort(propertyInfos, (o1, o2) -> o1.key.compareToIgnoreCase(o2.key));
        return PagedInfoList.asJson("properties", propertyInfos, queryParameters);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDeviceProperties(List<PropertyInfo> propertyInfos) {
        List<PropertySpec> propertySpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = mdcPropertyUtils.findPropertyValue(propertySpec, propertyInfos);
            if (value == null || "".equals(value)) {
                device.removeProtocolProperty(propertySpec.getName());
            } else {
                device.setProtocolProperty(propertySpec.getName(), value);
            }
        }
        device.save();
        return Response.ok().build();
    }
}

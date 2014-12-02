package com.energyict.mdc.device.data.rest.impl;

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
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by bvn on 12/1/14.
 */
public class DevicePropertyResource {

    private final MdcPropertyUtils mdcPropertyUtils;

    private Device device;

    @Inject
    public DevicePropertyResource(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    DevicePropertyResource init(Device device) {
        this.device=device;
        return this;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceProperties(@BeanParam QueryParameters queryParameters) {
        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
        List <PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getPropertySpecs() ,deviceProperties);
        Collections.sort(propertyInfos, (o1, o2) -> o1.key.compareTo(o2.key));
        return PagedInfoList.asJson("deviceProperties", propertyInfos, queryParameters);
    }
}

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 1/04/14
 * Time: 8:59
 */
public class ConnectionMethodResource {
    private final DeviceConfigurationService deviceConfigurationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public ConnectionMethodResource(DeviceConfigurationService deviceConfigurationService, ResourceHelper resourceHelper) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
//        List<Connection> pagedRegisterSpecs = ListPager.of(deviceConfiguration.getRegisterSpecs(), new RegisterConfigurationComparator()).from(queryParameters).find();
        List<ConnectionMethodInfo> connectionMethodInfos = new ArrayList<>();
        connectionMethodInfos.add(ConnectionMethodInfo.from(1, "aaaaaaaaa"));
        connectionMethodInfos.add(ConnectionMethodInfo.from(2, "bbbbbbbbb"));
//                RegisterConfigInfo.from(pagedRegisterSpecs);
        return PagedInfoList.asJson("connectionmethods", connectionMethodInfos, queryParameters);
    }

    @GET
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConnectionMethodInfo getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("connectionMethodId") long connectionMethodId) {
        return ConnectionMethodInfo.from(1, "aaaaaaaaa");
    }
}

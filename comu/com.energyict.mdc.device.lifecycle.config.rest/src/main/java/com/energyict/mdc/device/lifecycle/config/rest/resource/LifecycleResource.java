package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifecycleInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;


@Path("/devicelifecycles")
public class LifecycleResource {
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getDeviceLifecycles(@BeanParam QueryParameters queryParams) {
        List<LifecycleInfo> lifecycles = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().from(queryParams).stream()
                .map(LifecycleInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycles", lifecycles, queryParams);
    }

    @Inject
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }
}
package com.energyict.mdc.rest.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LoadProfileType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/loadprofiletypes")
public class LoadProfileTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public LoadProfileTypeResource(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LoadProfileTypeInfos getLoadProfileTypePropertyContext(@Context UriInfo uriInfo) {
        LoadProfileTypeInfos loadProfileTypeInfos = new LoadProfileTypeInfos();
        for (LoadProfileType loadProfileType : this.deviceConfigurationService.findAllLoadProfileTypes()) {
            loadProfileTypeInfos.loadProfileTypeInfos.add(new LoadProfileTypeInfo(loadProfileType));
        }
        return loadProfileTypeInfos;
    }


}

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.device.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/loadprofiletypes")
public class LoadProfileTypeResource {

    private final MasterDataService masterDataService;

    @Inject
    public LoadProfileTypeResource(MasterDataService masterDataService) {
        super();
        this.masterDataService = masterDataService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE_CONFIGURATION)
    public LoadProfileTypeInfos getLoadProfileTypePropertyContext(@Context UriInfo uriInfo) {
        LoadProfileTypeInfos loadProfileTypeInfos = new LoadProfileTypeInfos();
        for (LoadProfileType loadProfileType : this.masterDataService.findAllLoadProfileTypes()) {
            loadProfileTypeInfos.loadProfileTypeInfos.add(new LoadProfileTypeInfo(loadProfileType));
        }
        return loadProfileTypeInfos;
    }

}
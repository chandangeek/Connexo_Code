package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public LoadProfileTypeInfos getLoadProfileTypePropertyContext(@Context UriInfo uriInfo) {
        LoadProfileTypeInfos loadProfileTypeInfos = new LoadProfileTypeInfos();
        for (LoadProfileType loadProfileType : this.masterDataService.findAllLoadProfileTypes()) {
            loadProfileTypeInfos.loadProfileTypeInfos.add(new LoadProfileTypeInfo(loadProfileType));
        }
        return loadProfileTypeInfos;
    }

}
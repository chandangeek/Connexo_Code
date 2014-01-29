package com.energyict.mdc.rest.impl;

import com.energyict.mdw.core.LoadProfileType;
import com.energyict.mdw.coreimpl.LoadProfileTypeFactoryImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/loadprofiletypes")
public class LoadProfileTypeResource {

    public LoadProfileTypeResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LoadProfileTypeInfos getLoadProfileTypePropertyContext(@Context UriInfo uriInfo) {
        LoadProfileTypeInfos loadProfileTypeInfos = new LoadProfileTypeInfos();
        for (LoadProfileType loadProfileType : new LoadProfileTypeFactoryImpl().findAll()) {
            loadProfileTypeInfos.loadProfileTypeInfos.add(new LoadProfileTypeInfo(loadProfileType));
        }
        return loadProfileTypeInfos;
    }


}

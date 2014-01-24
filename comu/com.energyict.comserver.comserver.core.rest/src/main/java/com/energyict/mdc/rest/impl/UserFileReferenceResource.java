package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdw.coreimpl.UserFileFactoryImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/userfilereferences")
public class UserFileReferenceResource {

    public UserFileReferenceResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserFileReferenceInfos getUserFileReferencePropertyContext(@Context UriInfo uriInfo) {
        UserFileReferenceInfos userFileReferenceInfos = new UserFileReferenceInfos();
        for (UserFile userFile : new UserFileFactoryImpl().findAll()) {
            userFileReferenceInfos.userFileReferenceInfos.add(new UserFileReferenceInfo(userFile));
        }
        return userFileReferenceInfos;
    }
}

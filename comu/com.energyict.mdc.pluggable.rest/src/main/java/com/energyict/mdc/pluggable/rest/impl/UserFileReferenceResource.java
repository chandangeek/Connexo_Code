package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.UserFileFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/userfilereferences")
public class UserFileReferenceResource {

    public UserFileReferenceResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserFileReferenceInfos getUserFileReferencePropertyContext(@Context UriInfo uriInfo) {
        UserFileReferenceInfos userFileReferenceInfos = new UserFileReferenceInfos();
        List<UserFileFactory> userFileFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(UserFileFactory.class);
        for (UserFileFactory userFileFactory : userFileFactories) {
            for (UserFile userFile : userFileFactory.findAllUserFiles()) {
                userFileReferenceInfos.userFileReferenceInfos.add(new UserFileReferenceInfo(userFile));
            }
        }
        return userFileReferenceInfos;
    }
}

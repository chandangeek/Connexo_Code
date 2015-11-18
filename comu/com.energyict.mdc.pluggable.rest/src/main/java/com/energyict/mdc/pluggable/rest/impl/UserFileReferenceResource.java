package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.rest.Transactional;
import com.energyict.mdc.protocol.api.UserFileFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/userfilereferences")
public class UserFileReferenceResource {

    private final UserFileFactory userFileFactory;

    @Inject
    public UserFileReferenceResource(UserFileFactory userFileFactory) {
        this.userFileFactory = userFileFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public UserFileReferenceInfos getUserFileReferencePropertyContext(@Context UriInfo uriInfo) {
        UserFileReferenceInfos userFileReferenceInfos = new UserFileReferenceInfos();
        this.userFileFactory
                .findAllUserFiles()
                .stream()
                .map(UserFileReferenceInfo::new)
                .forEach(userFileReferenceInfos.userFileReferenceInfos::add);
        return userFileReferenceInfos;
    }

}
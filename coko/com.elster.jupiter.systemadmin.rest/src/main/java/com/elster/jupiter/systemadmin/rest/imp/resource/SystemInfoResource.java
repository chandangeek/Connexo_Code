/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.systemadmin.rest.imp.response.SystemInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.SystemInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/systeminfo")
public class SystemInfoResource {

    private SystemInfoFactory systemInfoFactory;

    @Inject
    public SystemInfoResource(SystemInfoFactory systemInfoFactory) {
        this.systemInfoFactory = systemInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION)
    public SystemInfo getSystemInformation() {
        return this.systemInfoFactory.asInfo();
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/enddeviceeventtypes")
public class EndDeviceEventTypeResource {

    private final Thesaurus thesaurus;

    @Inject
    public EndDeviceEventTypeResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    /**
     * This method will return a JSON list of all EndDeviceType's
     */
    @GET
    @Path("/devicetypes")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public EndDeviceEventTypePartInfos getDeviceTypes() {
        EndDeviceEventTypePartInfos result = new EndDeviceEventTypePartInfos();
        result.from(EndDeviceType.values(), thesaurus);
        return result;
    }

    /**
     * This method will return a JSON list of all EndDeviceDomain's
     */
    @GET
    @Path("/devicedomains")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public EndDeviceEventTypePartInfos getDomains() {
        EndDeviceEventTypePartInfos result = new EndDeviceEventTypePartInfos();
        result.from(EndDeviceDomain.values(), thesaurus);
        return result;
    }

    /**
     * This method will return a JSON list of all EndDeviceSubDomain's
     */
    @GET
    @Path("/devicesubdomains")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public EndDeviceEventTypePartInfos getSubDomains() {
        EndDeviceEventTypePartInfos result = new EndDeviceEventTypePartInfos();
        result.from(EndDeviceSubDomain.values(), thesaurus);
        return result;
    }

    /**
     * This method will return a JSON list of all EndDeviceSubDomain's
     */
    @GET
    @Path("/deviceeventoractions")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public EndDeviceEventTypePartInfos getEventOrActions() {
        EndDeviceEventTypePartInfos result = new EndDeviceEventTypePartInfos();
        result.from(EndDeviceEventOrAction.values(), thesaurus);
        return result;
    }
}

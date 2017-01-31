/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor.app.impl.rest.resource;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.monitor.app.security.MdcMonitorAppPrivileges;
import com.energyict.mdc.engine.status.StatusService;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/monitoringResults")
@RolesAllowed({MdcMonitorAppPrivileges.MONITOR_COMMUNICATION_SERVER})
public class MonitorResource{

    private final JSonConverter jSonConverter;

    @Inject
    public MonitorResource(StatusService statusService,
                           EngineConfigurationService engineConfigurationService,
                           ThreadPrincipalService threadPrincipalService,
                           UserService userService) {
        super();
        this.jSonConverter = new JSonConverter(statusService, engineConfigurationService, threadPrincipalService, userService);
    }

    @GET
    @Path("/serverDetails")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getServerDetails() {
        try {
           return this.jSonConverter.convertDetails().asReadEvent().toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/generalInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getGeneralInfo() {
        try {
            return this.jSonConverter.convertGeneralInfo().asReadEvent().toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/runningInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getRunningInfo() {
        try {
            return this.jSonConverter.convertRunningInfo().asReadEvent().toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/remoteServers/{active}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getRemoteServers(@PathParam("active") int active) {
        try {
            return this.jSonConverter.convertConnectedRemoteServers(active == 1).asArray("servers").toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/comPortInfo/{active}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getComPortInfo(@PathParam("active") int active) {
        try {
            return this.jSonConverter.convertCommunicationPorts(active == 1).asArray("comPorts").toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/comPortPoolInfo/{active}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getComPortPoolInfo(@PathParam("active") int active) {
        try {
            return this.jSonConverter.convertCommunicationPortPools(active == 1).asArray("comPortPools").toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/comPortPoolGraphInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getComPortPoolGraphInfo() {
        try {
            JSONObject result = this.jSonConverter.convertCommunicationPortPools().asArray("pools");
            result.put("priority", "High");
            return result.toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/collectedDataStorageStatistics")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getCollectedDataStorageStatistics() {
        try {
            return this.jSonConverter.convertCollectedDataStorageStatistics().asReadEvent().toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

    @GET
    @Path("/threadsInUse")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized String getThreadsInUse() {
        try {
            return this.jSonConverter.convertThreadsInUse().asReadEvent().toString();
        } catch (JSONException e) {
            throw new JSonException(e);
        }
    }

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/comservers")
public class ComServerResource {

    private final EngineConfigurationService engineConfigurationService;
    private final Provider<ComServerComPortResource> comServerComPortResourceProvider;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ResourceHelper resourceHelper;
    private final ComServerInfoFactory comServerInfoFactory;

    @Inject
    public ComServerResource(EngineConfigurationService engineConfigurationService,
                             Provider<ComServerComPortResource> comServerComPortResourceProvider, ConcurrentModificationExceptionFactory conflictFactory, ComServerInfoFactory comServerInfoFactory, ResourceHelper resourceHelper) {
        this.engineConfigurationService = engineConfigurationService;
        this.comServerComPortResourceProvider = comServerComPortResourceProvider;
        this.conflictFactory = conflictFactory;
        this.comServerInfoFactory = comServerInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComServers(@BeanParam JsonQueryParameters queryParameters) {
        List<ComServerInfo<?,?>> comServers = this.getSortedComServers(queryParameters)
                .map(comServer -> comServerInfoFactory.asInfo(comServer, comServer.getComPorts(), engineConfigurationService))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", comServers, queryParameters);
    }

    private Stream<ComServer> getSortedComServers(JsonQueryParameters queryParameters) {
        return engineConfigurationService.findAllComServers().from(queryParameters).stream()
                .sorted(Comparator.comparing(ComServer::getName, String.CASE_INSENSITIVE_ORDER));
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComServerInfo<?,?> getComServer(@PathParam("id") long id) {
        ComServer comServer = resourceHelper.findComServerOrThrowException(id);
        return comServerInfoFactory.asInfo(comServer, comServer.getComPorts(), engineConfigurationService);
    }

    @DELETE @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response deleteComServer(@PathParam("id") long id, ComServerInfo info) {
        info.id = id;
        try {
            resourceHelper.lockComServerOrThrowException(info).makeObsolete();
            return Response.noContent().build();
        } catch (ConcurrentModificationException ex){
            throw ex;
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response createComServer(ComServerInfo<ComServer.ComServerBuilder, ComServer> comServerInfo) {
        ComServer.ComServerBuilder comServerBuilder = comServerInfo.createNew(engineConfigurationService);
        comServerInfo.writeTo(comServerBuilder, engineConfigurationService);
        final ComServer comServer = comServerBuilder.create();

        Optional<List<InboundComPortInfo>> inboundComPorts = Optional.ofNullable(comServerInfo.inboundComPorts);
        Optional<List<OutboundComPortInfo>> outboundComPorts = Optional.ofNullable(comServerInfo.outboundComPorts);
        List<ComPortInfo> allComPorts = new ArrayList<>();
        if(inboundComPorts.isPresent()) {
            allComPorts.addAll(inboundComPorts.get());
        }
        if(outboundComPorts.isPresent()) {
            allComPorts.addAll(outboundComPorts.get());
        }

        for (ComPortInfo comPortInfo : allComPorts) {
            comPortInfo.createNew(comServer, engineConfigurationService);
        }
        return Response.status(Response.Status.CREATED).entity(comServerInfoFactory.asInfo(comServer)).build();
    }

    @PUT @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComServerInfo updateComServer(@PathParam("id") long id, ComServerInfo<ComServer.ComServerBuilder, ComServer> comServerInfo) {
        comServerInfo.id = id;
        ComServer comServer = resourceHelper.lockComServerOrThrowException(comServerInfo);
        comServerInfo.updateTo(comServer, engineConfigurationService);
        return comServerInfoFactory.asInfo(comServer, comServer.getComPorts(), engineConfigurationService);
    }

    @PUT @Transactional
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComServerInfo updateComServerStatus(@PathParam("id") long id, ComServerInfo info) {
        info.id = id;
        ComServer comServer = resourceHelper.findComServerOrThrowException(info.id);
        if (info.active != null && comServer.isActive() != info.active) {
            comServer = resourceHelper.getLockedComServer(info.id, info.version)
                    .orElseThrow(conflictFactory.conflict()
                            .withActualVersion(() -> resourceHelper.getCurrentComServerVersion(info.id))
                            .withMessageTitle(MessageSeeds.CHANGE_STATUS_CONCURRENT_TITLE, info.name)
                            .withMessageBody(MessageSeeds.CHANGE_STATUS_CONCURRENT_BODY, info.name)
                            .supplier());
            comServer.setActive(info.active);
            comServer.update();
        }
        return comServerInfoFactory.asInfo(comServer, comServer.getComPorts(), engineConfigurationService);
    }

    @Path("/{comServerId}/comports")
    public ComServerComPortResource getComPortResource() {
        return comServerComPortResourceProvider.get();
    }
}

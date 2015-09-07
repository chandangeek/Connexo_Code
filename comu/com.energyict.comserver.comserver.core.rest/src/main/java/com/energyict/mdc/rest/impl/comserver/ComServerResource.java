package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;
import java.util.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@Path("/comservers")
public class ComServerResource {

    private final EngineConfigurationService engineConfigurationService;
    private final Provider<ComServerComPortResource> comServerComPortResourceProvider;

    @Inject
    public ComServerResource(EngineConfigurationService engineConfigurationService,
                             Provider<ComServerComPortResource> comServerComPortResourceProvider) {
        this.engineConfigurationService = engineConfigurationService;
        this.comServerComPortResourceProvider = comServerComPortResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComServers(@BeanParam JsonQueryParameters queryParameters) {
        List<ComServerInfo<?>> comServers = new ArrayList<>();
        List<ComServer> allComServers = this.getSortedComServers(queryParameters);

        for (ComServer comServer : allComServers) {
            comServers.add(ComServerInfoFactory.asInfo(comServer, comServer.getComPorts(), engineConfigurationService));
        }

        return PagedInfoList.fromPagedList("data", comServers, queryParameters);
    }

    private List<ComServer> getSortedComServers(JsonQueryParameters queryParameters) {
        List<ComServer> comServers = engineConfigurationService.findAllComServers().from(queryParameters).find();
        Collections.sort(
                comServers,
                new Comparator<ComServer>() {
                    @Override
                    public int compare(ComServer o1, ComServer o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
        return comServers;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComServerInfo<?> getComServer(@PathParam("id") long id) {
        Optional<ComServer> comServer = findComServerOrThrowException(id);
        return ComServerInfoFactory.asInfo(comServer.get(), comServer.get().getComPorts(), engineConfigurationService);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response deleteComServer(@PathParam("id") long id) {
        try {
            Optional<ComServer> comServer = engineConfigurationService.findComServer(id);
            if (!comServer.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id "+id).build();
            }
            comServer.get().makeObsolete();
            return Response.noContent().build();
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response createComServer(ComServerInfo<ComServer> comServerInfo) {
        ComServer comServer = comServerInfo.createNew(engineConfigurationService);
        comServerInfo.writeTo(comServer, engineConfigurationService);
        comServer.save();

        Optional<List<InboundComPortInfo>> inboundComPorts = Optional.ofNullable(comServerInfo.inboundComPorts);
        Optional<List<OutboundComPortInfo>> outboundComPorts = Optional.ofNullable(comServerInfo.outboundComPorts);
        List<ComPortInfo> allComPorts = new ArrayList<>();
        if(inboundComPorts.isPresent()) {
            allComPorts.addAll(inboundComPorts.get());
        }
        if(outboundComPorts.isPresent()) {
            allComPorts.addAll(outboundComPorts.get());
        }

        for (ComPortInfo<?,?> comPortInfo : allComPorts) {
            comPortInfo.createNew(comServer, engineConfigurationService);
        }
        return Response.status(Response.Status.CREATED).entity(ComServerInfoFactory.asInfo(comServer)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComServerInfo updateComServer(@PathParam("id") long id, ComServerInfo<ComServer> comServerInfo) {
        Optional<ComServer> comServer = findComServerOrThrowException(id);

        Optional<List<InboundComPortInfo>> inboundComPorts = Optional.ofNullable(comServerInfo.inboundComPorts);
        Optional<List<OutboundComPortInfo>> outboundComPorts = Optional.ofNullable(comServerInfo.outboundComPorts);
        List<ComPortInfo> allComPortInfos = new ArrayList<>();
        if (inboundComPorts.isPresent()) {
            allComPortInfos.addAll(inboundComPorts.get());
        }

        if (outboundComPorts.isPresent()) {
            allComPortInfos.addAll(outboundComPorts.get());
        }

        comServerInfo.writeTo(comServer.get(), engineConfigurationService);
        //updateComPorts(comServer.get(), allComPortInfos);

        comServer.get().save();
        return ComServerInfoFactory.asInfo(comServer.get(), comServer.get().getComPorts(), engineConfigurationService);
    }

    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComServerInfo updateComServerStatus(@PathParam("id") long id, ComServerStatusInfo comServerStatusInfo) {
        Optional<ComServer> comServer = findComServerOrThrowException(id);
        if(comServerStatusInfo.active != null){
            comServer.get().setActive(comServerStatusInfo.active);
            comServer.get().save();
        }
        return ComServerInfoFactory.asInfo(comServer.get(), comServer.get().getComPorts(), engineConfigurationService);
    }

    private Optional<ComServer> findComServerOrThrowException(long id) {
        Optional<ComServer> comServer = engineConfigurationService.findComServer(id);
        if (!comServer.isPresent()) {
            throw new WebApplicationException("No ComServer with id " + id,
                    Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id " + id).build());
        }
        return comServer;
    }

    @Path("/{comServerId}/comports")
    public ComServerComPortResource getComPortResource() {
        return comServerComPortResourceProvider.get();
    }

    private void updateComPorts(ComServer comServer, List<ComPortInfo> newComPorts) {
        Map<Long, ComPortInfo> newComPortIdMap = asIdz(newComPorts);
        for (ComPort comPort : comServer.getComPorts()) {
            if (newComPortIdMap.containsKey(comPort.getId())) {
                newComPortIdMap.get(comPort.getId()).writeTo(comPort, engineConfigurationService);
                newComPortIdMap.remove(comPort.getId());
                comPort.save();
            } else {
                comServer.removeComPort(comPort.getId());
            }
        }

        for (ComPortInfo comPortInfo : newComPortIdMap.values()) {
            comPortInfo.createNew(comServer, engineConfigurationService);
        }
    }

    private Map<Long, ComPortInfo> asIdz(Collection<ComPortInfo> comPortInfos) {
        Map<Long, ComPortInfo> comPortIdMap = new HashMap<>();
        for (ComPortInfo comPort : comPortInfos) {
            comPortIdMap.put(comPort.id, comPort);
        }
        return comPortIdMap;
    }

}

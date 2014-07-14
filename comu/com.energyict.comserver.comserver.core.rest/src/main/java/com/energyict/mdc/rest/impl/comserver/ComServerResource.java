package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final EngineModelService engineModelService;
    private final Provider<ComServerComPortResource> comServerComPortResourceProvider;

    @Inject
    public ComServerResource(EngineModelService engineModelService,
                             Provider<ComServerComPortResource> comServerComPortResourceProvider) {
        this.engineModelService = engineModelService;
        this.comServerComPortResourceProvider = comServerComPortResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComServers(@BeanParam QueryParameters queryParameters) {
        List<ComServerInfo<?>> comServers = new ArrayList<>();
        List<ComServer> allComServers = this.getSortedComServers(queryParameters);

        for (ComServer comServer : allComServers) {
            comServers.add(ComServerInfoFactory.asInfo(comServer));
        }

        return PagedInfoList.asJson("data", comServers, queryParameters);
    }

    private List<ComServer> getSortedComServers(QueryParameters queryParameters) {
        List<ComServer> comServers = engineModelService.findAllComServers().from(queryParameters).find();
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
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo<?> getComServer(@PathParam("id") int id) {
        Optional<ComServer> comServer = Optional.fromNullable(engineModelService.findComServer(id));
        if (!comServer.isPresent()) {
            throw new WebApplicationException("No ComServer with id "+id,
                Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id "+id).build());
        }
        return ComServerInfoFactory.asInfo(comServer.get(), comServer.get().getComPorts(), engineModelService);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComServer(@PathParam("id") int id) {
        try {
            Optional<ComServer> comServer = Optional.fromNullable(engineModelService.findComServer(id));
            if (!comServer.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id "+id).build();
            }
            comServer.get().delete();
            return Response.noContent().build();
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComServer(ComServerInfo<ComServer> comServerInfo) {
        ComServer comServer = comServerInfo.createNew(engineModelService);
        comServerInfo.writeTo(comServer,engineModelService);
        comServer.save();

        Optional<List<InboundComPortInfo>> inboundComPorts = Optional.fromNullable(comServerInfo.inboundComPorts);
        Optional<List<OutboundComPortInfo>> outboundComPorts = Optional.fromNullable(comServerInfo.outboundComPorts);
        List<ComPortInfo> allComPorts = new ArrayList<>();
        if(inboundComPorts.isPresent()) {
            allComPorts.addAll(inboundComPorts.get());
        }
        if(outboundComPorts.isPresent()) {
            allComPorts.addAll(outboundComPorts.get());
        }

        for (ComPortInfo<?,?> comPortInfo : allComPorts) {
            comPortInfo.createNew(comServer, engineModelService);
        }
        return Response.status(Response.Status.CREATED).entity(ComServerInfoFactory.asInfo(comServer)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo updateComServer(@PathParam("id") int id, ComServerInfo<ComServer> comServerInfo) {
        Optional<ComServer> comServer = Optional.fromNullable(engineModelService.findComServer(id));
        if (!comServer.isPresent()) {
            throw new WebApplicationException("No ComServer with id "+id,
                    Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id "+id).build());
        }

        Optional<List<InboundComPortInfo>> inboundComPorts = Optional.fromNullable(comServerInfo.inboundComPorts);
        Optional<List<OutboundComPortInfo>> outboundComPorts = Optional.fromNullable(comServerInfo.outboundComPorts);
        List<ComPortInfo> allComPortInfos = new ArrayList<>();
        if (inboundComPorts.isPresent()) {
            allComPortInfos.addAll(inboundComPorts.get());
        }

        if (outboundComPorts.isPresent()) {
            allComPortInfos.addAll(outboundComPorts.get());
        }

        comServerInfo.writeTo(comServer.get(),engineModelService);
        updateComPorts(comServer.get(), allComPortInfos);

        comServer.get().save();
        return ComServerInfoFactory.asInfo(comServer.get());
    }

    @Path("/{comServerId}/comports")
    public ComServerComPortResource getComPortResource() {
        return comServerComPortResourceProvider.get();
    }

    private void updateComPorts(ComServer comServer, List<ComPortInfo> newComPorts) {
        Map<Long, ComPortInfo> newComPortIdMap = asIdz(newComPorts);
        for (ComPort comPort : comServer.getComPorts()) {
            if (newComPortIdMap.containsKey(comPort.getId())) {
                newComPortIdMap.get(comPort.getId()).writeTo(comPort, engineModelService);
                newComPortIdMap.remove(comPort.getId());
                comPort.save();
            } else {
                comServer.removeComPort(comPort.getId());
            }
        }

        for (ComPortInfo comPortInfo : newComPortIdMap.values()) {
            comPortInfo.createNew(comServer, engineModelService);
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

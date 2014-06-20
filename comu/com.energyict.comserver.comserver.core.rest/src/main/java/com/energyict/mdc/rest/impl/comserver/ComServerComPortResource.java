package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.*;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ComServerComPortResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComServerComPortResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComPorts(@PathParam("comServerId") long comServerId, @BeanParam QueryParameters queryParameters) {
        ComServer comServer = findComServerOrThrowException(comServerId);

        List<ComPort> comPorts = comServer.getComPorts();
        List<ComPortInfo> comPortInfos = new ArrayList<>(comPorts.size());

        for (ComPort comPort : comPorts) {
            comPortInfos.add(ComPortInfoFactory.asInfo(comPort));
        }
        return PagedInfoList.asJson("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        return ComPortInfoFactory.asInfo(comPort);
    }

    @POST
    @Path("/inbound")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo createInboundComPort(@PathParam("comServerId") long comServerId, InboundComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort newComPort = comPortInfo.createNew(comServer, engineModelService);
        return ComPortInfoFactory.asInfo(newComPort);
    }

    @PUT
    @Path("/inbound/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo updateInboundComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id, InboundComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        comPortInfo.writeTo((InboundComPort) comPort, engineModelService);
        comPort.save();
        return ComPortInfoFactory.asInfo(comPort);
    }

    @POST
    @Path("/outbound")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo createOutboundComPort(@PathParam("comServerId") long comServerId, OutboundComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort newComPort = comPortInfo.createNew(comServer, engineModelService);
        return ComPortInfoFactory.asInfo(newComPort);
    }

    @PUT
    @Path("/outbound/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo updateOutboundComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id, OutboundComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        comPortInfo.writeTo((OutboundComPort) comPort, engineModelService);
        comPort.save();
        return ComPortInfoFactory.asInfo(comPort);
    }

    @DELETE
    @Path("/{id}")
    public Response removeComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        comServer.removeComPort(id);
        return Response.noContent().build();
    }

    private ComServer findComServerOrThrowException(long id) {
        Optional<ComServer> comServer = Optional.fromNullable(engineModelService.findComServer(id));
        if (!comServer.isPresent()) {
            throw new WebApplicationException("No ComServer with id " + id,
                    Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id " + id).build());
        }

        return comServer.get();
    }

    private ComPort findComPortOrThrowException(ComServer comServer, long id) {
        for(ComPort comPort : comServer.getComPorts()) {
            if(comPort.getId() == id) {
                return comPort;
            }
        }

        throw new WebApplicationException("No ComPort with id " + id + " found for ComServer " + comServer.getId(),
                Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id + " found for ComServer " + comServer.getId()).build());
    }

}

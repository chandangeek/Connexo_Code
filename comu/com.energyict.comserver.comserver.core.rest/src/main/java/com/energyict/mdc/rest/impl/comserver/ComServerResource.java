package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
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

    @Inject
    public ComServerResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComServers(@BeanParam QueryParameters queryParameters) {
        List<ComServerInfo> comServers = new ArrayList<>();
        List<ComServer> allComServers = engineModelService.findAllComServers().from(queryParameters).find();

        for (ComServer comServer : allComServers) {
            comServers.add(ComServerInfoFactory.asInfo(comServer));
        }

        return PagedInfoList.asJson("comServers", comServers, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo getComServer(@PathParam("id") int id) {
        ComServer comServer = engineModelService.findComServer(id);
        if (comServer == null) {
            throw new WebApplicationException("No ComServer with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }
        return ComServerInfoFactory.asInfo(comServer, comServer.getComPorts());
    }

    @GET
    @Path("/{id}/comports")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPortsForComServerServer(@PathParam("id") int id) {
        ComServer comServer = engineModelService.findComServer(id);
        if (comServer == null) {
            throw new WebApplicationException("No ComServer with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }
        ComPortsInfo wrapper = new ComPortsInfo();
        for (ComPort comPort : comServer.getComPorts()) {
            wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
        }
        return wrapper;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComServer(@PathParam("id") int id) {
        try {
            ComServer comServer = engineModelService.findComServer(id);
            if (comServer == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id "+id).build();
            }
            comServer.delete();
            return Response.ok().build();
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo createComServer(ComServerInfo comServerInfo) {
        try {
            ComServer comServer = comServerInfo.createNew(engineModelService);
            comServerInfo.writeTo(comServer,engineModelService);
            comServer.save();

            List<ComPortInfo> allComPorts = new ArrayList<>();
            allComPorts.addAll(comServerInfo.inboundComPorts);
            allComPorts.addAll(comServerInfo.outboundComPorts);

            for (ComPortInfo comPortInfo : allComPorts) {
                comPortInfo.createNew(comServer, engineModelService);
            }
            return ComServerInfoFactory.asInfo(comServer);
        } catch (Exception e) {
            throw new WebApplicationException(e.getLocalizedMessage(), e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build());
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerInfo updateComServer(@PathParam("id") int id, ComServerInfo<ComServer> comServerInfo) {
        if (comServerInfo.inboundComPorts ==null) {
            throw new WebApplicationException("ComServer is missing list of inbound ComPorts",
                Response.status(Response.Status.BAD_REQUEST).build());
        }
        if (comServerInfo.outboundComPorts ==null) {
            throw new WebApplicationException("ComServer is missing list of outbound ComPorts",
                Response.status(Response.Status.BAD_REQUEST).build());
        }

        ComServer comServer = engineModelService.findComServer(id);
        if (comServer == null) {
            throw new WebApplicationException("No ComServer with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }

        comServerInfo.writeTo(comServer,engineModelService);
        List<ComPortInfo> allComPortInfos = new ArrayList<>();
        allComPortInfos.addAll(comServerInfo.inboundComPorts);
        allComPortInfos.addAll(comServerInfo.outboundComPorts);
        updateComPorts(comServer, allComPortInfos);

        comServer.save();
        return ComServerInfoFactory.asInfo(comServer);
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

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.security.Privileges;

import javax.annotation.security.RolesAllowed;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ComServerComPortResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComServerComPortResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE,Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
    public PagedInfoList getComPorts(@PathParam("comServerId") long comServerId, @BeanParam QueryParameters queryParameters) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        List<ComPort> comPorts = ListPager.of(comServer.getComPorts(), new Comparator<ComPort>() {
            @Override
            public int compare(ComPort o1, ComPort o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }).from(queryParameters).find();

        List<ComPortInfo> comPortInfos = new ArrayList<>(comPorts.size());

        for (ComPort comPort : comPorts) {
            comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
        }

        return PagedInfoList.asJson("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE,Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
    public ComPortInfo getComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        return ComPortInfoFactory.asInfo(comPort, engineModelService);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE)
    public ComPortInfo createOutboundComPort(@PathParam("comServerId") long comServerId, ComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort newComPort = comPortInfo.createNew(comServer, engineModelService);
        return ComPortInfoFactory.asInfo(newComPort, engineModelService);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE)
    public ComPortInfo updateOutboundComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id, ComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        comPortInfo.writeTo(comPort, engineModelService);
        comPort.save();
        return ComPortInfoFactory.asInfo(comPort, engineModelService);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        findComPortOrThrowException(comServer, id);
        comServer.removeComPort(id);
        return Response.noContent().build();
    }

    private ComServer findComServerOrThrowException(long id) {
        Optional<ComServer> comServer = engineModelService.findComServer(id);
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

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;

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

    private final EngineConfigurationService engineConfigurationService;

    @Inject
    public ComServerComPortResource(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComPorts(@PathParam("comServerId") long comServerId, @BeanParam JsonQueryParameters queryParameters) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        List<ComPort> comPorts = ListPager.of(comServer.getComPorts(), new Comparator<ComPort>() {
            @Override
            public int compare(ComPort o1, ComPort o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }).from(queryParameters).find();

        List<ComPortInfo> comPortInfos = new ArrayList<>(comPorts.size());

        for (ComPort comPort : comPorts) {
            comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineConfigurationService));
        }

        return PagedInfoList.fromPagedList("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComPortInfo getComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        return ComPortInfoFactory.asInfo(comPort, engineConfigurationService);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComPortInfo createOutboundComPort(@PathParam("comServerId") long comServerId, ComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort newComPort = comPortInfo.createNew(comServer, engineConfigurationService);
        return ComPortInfoFactory.asInfo(newComPort, engineConfigurationService);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComPortInfo updateOutboundComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id, ComPortInfo comPortInfo) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        ComPort comPort = findComPortOrThrowException(comServer, id);
        comPortInfo.writeTo(comPort, engineConfigurationService);
        comPort.save();
        return ComPortInfoFactory.asInfo(comPort, engineConfigurationService);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response removeComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComServer comServer = findComServerOrThrowException(comServerId);
        findComPortOrThrowException(comServer, id);
        comServer.removeComPort(id);
        return Response.noContent().build();
    }

    private ComServer findComServerOrThrowException(long id) {
        Optional<ComServer> comServer = engineConfigurationService.findComServer(id);
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

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComPortPoolComPortResource {

    private final EngineConfigurationService engineConfigurationService;

    @Inject
    public ComPortPoolComPortResource(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComPorts(@PathParam("comPortPoolId") long comPortPoolId, @BeanParam JsonQueryParameters queryParameters) {
        ComPortPool comPortPool = findComPortPoolOrThrowException(comPortPoolId);
        List<ComPort> comPorts = new ArrayList<>(comPortPool.getComPorts());

        comPorts = ListPager.of(comPorts, new Comparator<ComPort>() {
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
    public ComPortInfo getComPort(@PathParam("comPortPoolId") long comPortPoolId, @PathParam("id") long id) {
        ComPortPool comPortPool = findComPortPoolOrThrowException(comPortPoolId);
        ComPort comPort = findComPortOrThrowException(comPortPool, id);
        return ComPortInfoFactory.asInfo(comPort, engineConfigurationService);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response removeComPort(@PathParam("comPortPoolId") long comPortPoolId, @PathParam("id") long id) {
        ComPortPool comPortPool = findComPortPoolOrThrowException(comPortPoolId);
        removeComPortFromComPortPool(comPortPool, id);
        return Response.noContent().build();
    }

    private void removeComPortFromComPortPool(ComPortPool comPortPool, long comPortId) {
        if(OutboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            ComPort comPort = findComPortOrThrowException(comPortPool, comPortId);
            ((OutboundComPortPool)comPortPool).removeOutboundComPort((OutboundComPort)comPort);
            return;
        }
        if(InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            ComPort comPort = findComPortOrThrowException(comPortPool, comPortId);
            if(InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                ((InboundComPort)comPort).setComPortPool(null);
                comPort.save();
            }
            return;
        }
    }

    private ComPortPool findComPortPoolOrThrowException(long id) {
        return engineConfigurationService
                .findComPortPool(id)
                .orElseThrow(() -> new WebApplicationException(
                        "No ComPortPool with id " + id,
                        Response.status(Response.Status.NOT_FOUND)
                                .entity("No ComPortPool with id " + id)
                                .build()));
    }

    private ComPort findComPortOrThrowException(ComPortPool comPortPool, long id) {
        for (ComPort comPort : comPortPool.getComPorts()) {
            if (comPort.getId() == id) {
                return comPort;
            }
        }

        throw new WebApplicationException("No ComPort with id " + id + " found for ComPortPool " + comPortPool.getId(),
                Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id + " found for ComPortPool " + comPortPool.getId()).build());
    }

}

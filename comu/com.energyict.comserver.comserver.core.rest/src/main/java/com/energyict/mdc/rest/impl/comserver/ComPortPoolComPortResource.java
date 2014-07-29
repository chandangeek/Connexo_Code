package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.model.*;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComPortPoolComPortResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComPortPoolComPortResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComPorts(@PathParam("comPortPoolId") long comPortPoolId, @BeanParam QueryParameters queryParameters) {
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
            comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
        }
        return PagedInfoList.asJson("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("comPortPoolId") long comPortPoolId, @PathParam("id") long id) {
        ComPortPool comPortPool = findComPortPoolOrThrowException(comPortPoolId);
        ComPort comPort = findComPortOrThrowException(comPortPool, id);
        return ComPortInfoFactory.asInfo(comPort, engineModelService);
    }

    @DELETE
    @Path("/{id}")
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
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (!comPortPool.isPresent()) {
            throw new WebApplicationException("No ComPortPool with id " + id,
                    Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
        }

        return comPortPool.get();
    }

    private ComPort findComPortOrThrowException(ComPortPool comPortPool, long id) {
        for(ComPort comPort : comPortPool.getComPorts()) {
            if(comPort.getId() == id) {
                return comPort;
            }
        }

        throw new WebApplicationException("No ComPort with id " + id + " found for ComPortPool " + comPortPool.getId(),
                Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id + " found for ComPortPool " + comPortPool.getId()).build());
    }

}

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/comportpools")
public class ComPortPoolResource {

    private final EngineModelService engineModelService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final Provider<ComPortPoolComPortResource> comPortPoolComPortResourceProvider;

    @Inject
    public ComPortPoolResource(EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, Provider<ComPortPoolComPortResource> comPortPoolComPortResourceProvider, DeviceConfigurationService deviceConfigurationService) {
        this.engineModelService = engineModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.comPortPoolComPortResourceProvider = comPortPoolComPortResourceProvider;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo getComPortPool(@PathParam("id") int id) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (comPortPool.isPresent()) {
            return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineModelService);
        }

        throw new WebApplicationException("No ComPortPool with id " + id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllComPortPools(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        List<? super ComPortPoolInfo> comPortPoolInfos = new ArrayList<>();
        List<ComPortPool> comPortPools = new ArrayList<>();
        String compatibleWithConnectionType = uriInfo.getQueryParameters().getFirst("compatibleWithConnectionType");
        String compatibleWithConnectionTask = uriInfo.getQueryParameters().getFirst("compatibleWithConnectionTask");
        if (compatibleWithConnectionType != null) {
            getComPortPoolsByConnectionType(comPortPools, compatibleWithConnectionType);
        } else if (compatibleWithConnectionTask!= null){
            getComPortPoolsByConnectionTask(comPortPools, compatibleWithConnectionTask);
        } else {
            comPortPools.addAll(engineModelService.findAllComPortPools());
        }
        Collections.sort(comPortPools, new Comparator<ComPortPool>() {
            @Override
            public int compare(ComPortPool cpp1, ComPortPool cpp2) {
                return cpp1.getName().compareToIgnoreCase(cpp2.getName());
            }
        });
        for (ComPortPool comPortPool : comPortPools) {
            comPortPoolInfos.add(ComPortPoolInfoFactory.asInfo(comPortPool, engineModelService));
        }
        return PagedInfoList.asJson("data", comPortPoolInfos, queryParameters);
    }

    private void getComPortPoolsByConnectionType(List<ComPortPool> comPortPools, String compatibleWithConnectionType) {
        ConnectionTypePluggableClass connectionTypePluggableClass = this.protocolPluggableService.findConnectionTypePluggableClass(Integer.parseInt(compatibleWithConnectionType));
        Set<ComPortType> supportedComPortTypes = connectionTypePluggableClass.getConnectionType().getSupportedComPortTypes();
        for (ComPortType supportedComPortType : supportedComPortTypes) {
            if(connectionTypePluggableClass.getConnectionType().getDirection().equals(ConnectionType.Direction.OUTBOUND)){
                comPortPools.addAll(engineModelService.findOutboundComPortPoolByType(supportedComPortType));
            } else {
                comPortPools.addAll(engineModelService.findInboundComPortPoolByType(supportedComPortType));
            }
        }
    }

    private void getComPortPoolsByConnectionTask(List<ComPortPool> comPortPools, String compatibleWithConnectionTask) {
        PartialConnectionTask partialConnectionTask = this.deviceConfigurationService.getPartialConnectionTask(Integer.parseInt(compatibleWithConnectionTask)).get();
        Set<ComPortType> supportedComPortTypes =  partialConnectionTask.getConnectionType().getSupportedComPortTypes();
        for (ComPortType supportedComPortType : supportedComPortTypes) {
            if(partialConnectionTask.getPluggableClass().getConnectionType().getDirection().equals(ConnectionType.Direction.OUTBOUND)){
                comPortPools.addAll(engineModelService.findOutboundComPortPoolByType(supportedComPortType));
            } else {
                comPortPools.addAll(engineModelService.findInboundComPortPoolByType(supportedComPortType));
            }
        }
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteComPortPool(@PathParam("id") int id) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (!comPortPool.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build();
        }
        comPortPool.get().delete();
        return Response.noContent().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComPortPool(ComPortPoolInfo<? super ComPortPool> comPortPoolInfo) {
        ComPortPool comPortPool = comPortPoolInfo.writeTo(comPortPoolInfo.createNew(engineModelService), protocolPluggableService);
        comPortPool.save();
        comPortPoolInfo.handlePools(comPortPool, engineModelService);
        return Response.status(Response.Status.CREATED).entity(ComPortPoolInfoFactory.asInfo(comPortPool, engineModelService)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo updateComPortPool(@PathParam("id") int id, ComPortPoolInfo<? super ComPortPool> comPortPoolInfo) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (!comPortPool.isPresent()) {
            throw new WebApplicationException("No ComPortPool with id " + id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
        }
        comPortPoolInfo.writeTo(comPortPool.get(), protocolPluggableService);
        comPortPoolInfo.handlePools(comPortPool.get(), engineModelService);
        comPortPool.get().save();
        return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineModelService);
    }

    @Path("/{comPortPoolId}/comports")
    public ComPortPoolComPortResource getComPortResource() {
        return comPortPoolComPortResourceProvider.get();
    }

}

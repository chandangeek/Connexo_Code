package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/comportpools")
public class ComPortPoolResource {

    public static final String ALL = "all";
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
    public ComPortPoolInfo<?> getComPortPool(@PathParam("id") int id) {
        Optional<ComPortPool> comPortPool = Optional.fromNullable(engineModelService.findComPortPool(id));
        if (comPortPool.isPresent()) {
            return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineModelService);
        }

        throw new WebApplicationException("No ComPortPool with id " + id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllComPortPools(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        List<ComPortPoolInfo<?>> comPortPoolInfos = new ArrayList<>();
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

        comPortPools = ListPager.of(comPortPools, new Comparator<ComPortPool>() {
            @Override
            public int compare(ComPortPool cpp1, ComPortPool cpp2) {
                return cpp1.getName().compareToIgnoreCase(cpp2.getName());
            }
        }).from(queryParameters).find();

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
    public Response createComPortPool(ComPortPoolInfo<OutboundComPortPool> comPortPoolInfo, @Context UriInfo uriInfo) {
        OutboundComPortPool comPortPool = comPortPoolInfo.writeTo(comPortPoolInfo.createNew(engineModelService), protocolPluggableService);
        comPortPool.save();
        handlePools(comPortPoolInfo, comPortPool, engineModelService, getBoolean(uriInfo, ALL));
        return Response.status(Response.Status.CREATED).entity(ComPortPoolInfoFactory.asInfo(comPortPool, engineModelService)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortPoolInfo<?> updateComPortPool(@PathParam("id") int id, ComPortPoolInfo<OutboundComPortPool> comPortPoolInfo, @Context UriInfo uriInfo) {
        Optional<OutboundComPortPool> comPortPool = Optional.fromNullable(engineModelService.findOutboundComPortPool(id));
        if (!comPortPool.isPresent()) {
            throw new WebApplicationException("No ComPortPool with id " + id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
        }
        comPortPoolInfo.writeTo(comPortPool.get(), protocolPluggableService);
        handlePools(comPortPoolInfo, comPortPool.get(), engineModelService, getBoolean(uriInfo, ALL));
        comPortPool.get().save();
        return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineModelService);
    }

    @Path("/{comPortPoolId}/comports")
    public ComPortPoolComPortResource getComPortResource() {
        return comPortPoolComPortResourceProvider.get();
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

    protected void handlePools(ComPortPoolInfo<OutboundComPortPool> comPortPoolInfo, OutboundComPortPool outboundComPortPool, EngineModelService engineModelService, boolean all) {
        if (!all) {
            updateComPorts(outboundComPortPool, comPortPoolInfo.outboundComPorts, engineModelService);
        } else {
            addAllComPorts(outboundComPortPool, engineModelService);
        }
    }

    private void addAllComPorts(OutboundComPortPool outboundComPortPool, EngineModelService engineModelService) {
        List<Long> alreadyContained = new ArrayList<>(outboundComPortPool.getComPorts().size());
        for (OutboundComPort comPort : outboundComPortPool.getComPorts()) {
            alreadyContained.add(comPort.getId());
        }
        for (OutboundComPort comPort : engineModelService.findAllOutboundComPorts()) {
            if (!alreadyContained.contains(comPort.getId())) {
                outboundComPortPool.addOutboundComPort(comPort);
            }
        }

    }

    private void updateComPorts(OutboundComPortPool outboundComPortPool, List<OutboundComPortInfo> newComPorts, EngineModelService engineModelService) {
        Map<Long, OutboundComPortInfo> newComPortIdMap = asIdz(newComPorts);
        for (OutboundComPort comPort : outboundComPortPool.getComPorts()) {
            if (newComPortIdMap.containsKey(comPort.getId())) {
                // Updating ComPorts not allowed here
                newComPortIdMap.remove(comPort.getId());
            } else {
                outboundComPortPool.removeOutboundComPort(comPort);
            }
        }

        for (OutboundComPortInfo comPortInfo : newComPortIdMap.values()) {
            Optional<? extends ComPort> comPort = Optional.fromNullable(engineModelService.findComPort(comPortInfo.id));
            if (!comPort.isPresent()) {
                throw new WebApplicationException("No ComPort with id "+comPortInfo.id,
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id "+comPortInfo.id).build());
            }
            if (!OutboundComPort.class.isAssignableFrom(comPort.get().getClass())) {
                throw new WebApplicationException("ComPort with id "+comPortInfo.id+" should have been OutboundComPort, but was "+comPort.get().getClass().getSimpleName(),
                        Response.status(Response.Status.BAD_REQUEST).entity("ComPort with id "+comPortInfo.id+" should have been OutboundComPort, but was "+comPort.get().getClass().getSimpleName()).build());
            }

            outboundComPortPool.addOutboundComPort((OutboundComPort) comPort.get());
        }
    }

    private Map<Long, OutboundComPortInfo> asIdz(Collection<? extends OutboundComPortInfo> comPortInfos) {
        Map<Long, OutboundComPortInfo> comPortIdMap = new HashMap<>();
        for (OutboundComPortInfo comPort : comPortInfos) {
            comPortIdMap.put(comPort.id, comPort);
        }
        return comPortIdMap;
    }

}

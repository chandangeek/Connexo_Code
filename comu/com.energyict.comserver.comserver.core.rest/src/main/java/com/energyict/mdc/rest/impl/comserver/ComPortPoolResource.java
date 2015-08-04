package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Path("/comportpools")
public class ComPortPoolResource {

    public static final String ALL = "all";
    private final EngineConfigurationService engineConfigurationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final Provider<ComPortPoolComPortResource> comPortPoolComPortResourceProvider;

    @Inject
    public ComPortPoolResource(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, Provider<ComPortPoolComPortResource> comPortPoolComPortResourceProvider, DeviceConfigurationService deviceConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.comPortPoolComPortResourceProvider = comPortPoolComPortResourceProvider;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComPortPoolInfo<?> getComPortPool(@PathParam("id") long id) {
        return engineConfigurationService
                .findComPortPool(id)
                .map(comPortPool -> ComPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService))
                .orElseThrow(() -> new WebApplicationException(
                        "No ComPortPool with id " + id,
                        Response.status(Response.Status.NOT_FOUND)
                                .entity("No ComPortPool with id " + id)
                                .build()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getAllComPortPools(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComPortPoolInfo<?>> comPortPoolInfos = new ArrayList<>();
        List<ComPortPool> comPortPools = new ArrayList<>();
        String compatibleWithConnectionType = uriInfo.getQueryParameters().getFirst("compatibleWithConnectionType");
        String compatibleWithConnectionTask = uriInfo.getQueryParameters().getFirst("compatibleWithConnectionTask");
        if (compatibleWithConnectionType != null) {
            getComPortPoolsByConnectionType(comPortPools, compatibleWithConnectionType);
        } else if (compatibleWithConnectionTask!= null){
            getComPortPoolsByConnectionTask(comPortPools, compatibleWithConnectionTask);
        } else {
            comPortPools.addAll(engineConfigurationService.findAllComPortPools());
        }

        comPortPools = ListPager.of(comPortPools, (cpp1, cpp2) -> cpp1.getName().compareToIgnoreCase(cpp2.getName())).from(queryParameters).find();

        for (ComPortPool comPortPool : comPortPools) {
            comPortPoolInfos.add(ComPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService));
        }
        return PagedInfoList.fromPagedList("data", comPortPoolInfos, queryParameters);
    }

    private void getComPortPoolsByConnectionType(List<ComPortPool> comPortPools, String compatibleWithConnectionType) {
        ConnectionTypePluggableClass connectionTypePluggableClass = this.findConnectionTypePluggableClassOrThrowException(Long.parseLong(compatibleWithConnectionType));
        Set<ComPortType> supportedComPortTypes = connectionTypePluggableClass.getConnectionType().getSupportedComPortTypes();
        for (ComPortType supportedComPortType : supportedComPortTypes) {
            if(connectionTypePluggableClass.getConnectionType().getDirection().equals(ConnectionType.Direction.OUTBOUND)){
                comPortPools.addAll(engineConfigurationService.findOutboundComPortPoolsByType(supportedComPortType));
            } else {
                comPortPools.addAll(engineConfigurationService.findInboundComPortPoolsByType(supportedComPortType));
            }
        }
    }

    private ConnectionTypePluggableClass findConnectionTypePluggableClassOrThrowException(long id) {
        return this.protocolPluggableService
                .findConnectionTypePluggableClass(id)
                .orElseThrow(() -> new WebApplicationException(
                        "No connection type with id " + id + " found",
                        Response.status(Response.Status.NOT_FOUND).entity("No connection type with id " + id + " found").build()));
    }

    private void getComPortPoolsByConnectionTask(List<ComPortPool> comPortPools, String compatibleWithConnectionTask) {
        PartialConnectionTask partialConnectionTask = this.deviceConfigurationService.findPartialConnectionTask(Integer.parseInt(compatibleWithConnectionTask)).get();
        Set<ComPortType> supportedComPortTypes =  partialConnectionTask.getConnectionType().getSupportedComPortTypes();
        for (ComPortType supportedComPortType : supportedComPortTypes) {
            if(partialConnectionTask.getPluggableClass().getConnectionType().getDirection().equals(ConnectionType.Direction.OUTBOUND)){
                comPortPools.addAll(engineConfigurationService.findOutboundComPortPoolsByType(supportedComPortType));
            } else {
                comPortPools.addAll(engineConfigurationService.findInboundComPortPoolsByType(supportedComPortType));
            }
        }
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response deleteComPortPool(@PathParam("id") long id) {
        Optional<? extends ComPortPool> comPortPool = engineConfigurationService.findComPortPool(id);
        if (!comPortPool.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build();
        }
        comPortPool.get().makeObsolete();
        return Response.noContent().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response createComPortPool(ComPortPoolInfo<ComPortPool> comPortPoolInfo, @Context UriInfo uriInfo) {
        ComPortPool comPortPool = comPortPoolInfo.createNew(engineConfigurationService, protocolPluggableService);
        comPortPool.save();
        if (comPortPool instanceof OutboundComPortPool) { // TODO Polymorphism is in place here: get rid of these checks!
            handlePools(comPortPoolInfo, (OutboundComPortPool) comPortPool, engineConfigurationService, getBoolean(uriInfo, ALL));
        } else if (comPortPool instanceof InboundComPortPool) {
            handleInboundPoolPorts((InboundComPortPool)comPortPool, Optional.ofNullable(comPortPoolInfo.inboundComPorts));
        }
        return Response.status(Response.Status.CREATED).entity(ComPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComPortPoolInfo<?> updateComPortPool(@PathParam("id") long id, ComPortPoolInfo<ComPortPool> comPortPoolInfo, @Context UriInfo uriInfo) {
        Optional<? extends ComPortPool> comPortPool = engineConfigurationService.findComPortPool(id);
        if (!comPortPool.isPresent()) {
            throw new WebApplicationException("No ComPortPool with id " + id, Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id).build());
        }
        comPortPoolInfo.writeTo(comPortPool.get(), protocolPluggableService);
        if (comPortPool.get() instanceof OutboundComPortPool) {
            handlePools(comPortPoolInfo, (OutboundComPortPool) comPortPool.get(), engineConfigurationService, getBoolean(uriInfo, ALL));
        }
        if (InboundComPortPool.class.isAssignableFrom(comPortPool.get().getClass())) {
            handleInboundPoolPorts((InboundComPortPool)comPortPool.get(), Optional.ofNullable(comPortPoolInfo.inboundComPorts));
        }
        comPortPool.get().save();
        return ComPortPoolInfoFactory.asInfo(comPortPool.get(), engineConfigurationService);
    }

    @Path("/{comPortPoolId}/comports")
    public ComPortPoolComPortResource getComPortResource() {
        return comPortPoolComPortResourceProvider.get();
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

    private void handleInboundPoolPorts(InboundComPortPool inboundComPortPool, Optional<List<InboundComPortInfo>> inboundComPortInfos) {
        if (inboundComPortInfos.isPresent()) {
            Map<Long, ComPortInfo> newComPortIdMap = asIdz(inboundComPortInfos.get());

            for (InboundComPort comPort : inboundComPortPool.getComPorts()) {
                if (newComPortIdMap.containsKey(comPort.getId())) {
                    newComPortIdMap.remove(comPort.getId());
                } else {
                    comPort.setComPortPool(null);
                    comPort.save();
                }
            }

            for (ComPortInfo inboundComPortInfo : newComPortIdMap.values()) {
                Optional<? extends ComPort> comPort = engineConfigurationService.findComPort(inboundComPortInfo.id);
                if (comPort.isPresent() && (comPort.get() instanceof InboundComPort)) {
                    ((InboundComPort) comPort.get()).setComPortPool(inboundComPortPool);
                    comPort.get().save();
                }
            }
        } else {
            for (InboundComPort comPort : inboundComPortPool.getComPorts()) {
                comPort.setComPortPool(null);
                comPort.save();
            }
        }
    }

    protected void handlePools(ComPortPoolInfo<? extends ComPortPool> comPortPoolInfo, OutboundComPortPool outboundComPortPool, EngineConfigurationService engineConfigurationService, boolean all) {
        if (!all) {
            updateComPorts(outboundComPortPool, comPortPoolInfo.outboundComPorts, engineConfigurationService);
        } else {
            addAllComPorts(outboundComPortPool, engineConfigurationService);
        }
    }

    private void addAllComPorts(OutboundComPortPool outboundComPortPool, EngineConfigurationService engineConfigurationService) {
        List<Long> alreadyContained = new ArrayList<>(outboundComPortPool.getComPorts().size());
        for (OutboundComPort comPort : outboundComPortPool.getComPorts()) {
            alreadyContained.add(comPort.getId());
        }
        for (OutboundComPort comPort : engineConfigurationService.findAllOutboundComPorts()) {
            if (!alreadyContained.contains(comPort.getId())) {
                outboundComPortPool.addOutboundComPort(comPort);
            }
        }

    }

    private void updateComPorts(OutboundComPortPool outboundComPortPool, List<OutboundComPortInfo> newComPorts, EngineConfigurationService engineConfigurationService) {
        Map<Long, ComPortInfo> newComPortIdMap = asIdz(newComPorts);
        for (OutboundComPort comPort : outboundComPortPool.getComPorts()) {
            if (newComPortIdMap.containsKey(comPort.getId())) {
                // Updating ComPorts not allowed here
                newComPortIdMap.remove(comPort.getId());
            } else {
                outboundComPortPool.removeOutboundComPort(comPort);
            }
        }

        for (ComPortInfo comPortInfo : newComPortIdMap.values()) {
            Optional<? extends ComPort> comPort = engineConfigurationService.findComPort(comPortInfo.id);
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

    private Map<Long, ComPortInfo> asIdz(Collection<? extends ComPortInfo> comPortInfos) {
        Map<Long, ComPortInfo> comPortIdMap = new HashMap<>();
        for (ComPortInfo comPort : comPortInfos) {
            comPortIdMap.put(comPort.id, comPort);
        }
        return comPortIdMap;
    }

}
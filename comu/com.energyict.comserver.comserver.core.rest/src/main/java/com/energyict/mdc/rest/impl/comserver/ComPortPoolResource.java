/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.ports.ComPortType;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
    private final ResourceHelper resourceHelper;
    private final ComPortPoolInfoFactory comPortPoolInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ComPortPoolResource(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, Provider<ComPortPoolComPortResource> comPortPoolComPortResourceProvider, DeviceConfigurationService deviceConfigurationService, ResourceHelper resourceHelper, ComPortPoolInfoFactory comPortPoolInfoFactory, MdcPropertyUtils mdcPropertyUtils) {
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.comPortPoolComPortResourceProvider = comPortPoolComPortResourceProvider;
        this.deviceConfigurationService = deviceConfigurationService;
        this.resourceHelper = resourceHelper;
        this.comPortPoolInfoFactory = comPortPoolInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComPortPoolInfo getComPortPool(@PathParam("id") long id) {
        return engineConfigurationService
                .findComPortPool(id)
                .map(comPortPool -> comPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService, mdcPropertyUtils))
                .orElseThrow(() -> new WebApplicationException(
                        "No ComPortPool with id " + id,
                        Response.status(Response.Status.NOT_FOUND)
                                .entity("No ComPortPool with id " + id)
                                .build()));
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getAllComPortPools(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComPortPoolInfo> comPortPoolInfos = new ArrayList<>();
        List<ComPortPool> comPortPools = new ArrayList<>();
        String compatibleWithConnectionType = uriInfo.getQueryParameters().getFirst("compatibleWithConnectionType");
        String compatibleWithConnectionTask = uriInfo.getQueryParameters().getFirst("compatibleWithConnectionTask");
        if (compatibleWithConnectionType != null) {
            getComPortPoolsByConnectionType(comPortPools, compatibleWithConnectionType);
        } else if (compatibleWithConnectionTask != null) {
            getComPortPoolsByConnectionTask(comPortPools, compatibleWithConnectionTask);
        } else {
            comPortPools.addAll(engineConfigurationService.findAllComPortPools());
        }

        comPortPools = ListPager.of(comPortPools, Comparator.comparing(ComPortPool::getName, String.CASE_INSENSITIVE_ORDER)).from(queryParameters).find();

        for (ComPortPool comPortPool : comPortPools) {
            comPortPoolInfos.add(comPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService, mdcPropertyUtils));
        }
        return PagedInfoList.fromPagedList("data", comPortPoolInfos, queryParameters);
    }

    private void getComPortPoolsByConnectionType(List<ComPortPool> comPortPools, String compatibleWithConnectionType) {
        ConnectionTypePluggableClass connectionTypePluggableClass = this.findConnectionTypePluggableClassOrThrowException(Long.parseLong(compatibleWithConnectionType));
        Set<ComPortType> supportedComPortTypes = connectionTypePluggableClass.getConnectionType().getSupportedComPortTypes();
        for (ComPortType supportedComPortType : supportedComPortTypes) {
            if (connectionTypePluggableClass.getConnectionType().getDirection().equals(ConnectionType.ConnectionTypeDirection.OUTBOUND)) {
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
        Set<ComPortType> supportedComPortTypes = partialConnectionTask.getConnectionType().getSupportedComPortTypes();
        for (ComPortType supportedComPortType : supportedComPortTypes) {
            if (partialConnectionTask.getPluggableClass().getConnectionType().getDirection().equals(ConnectionType.ConnectionTypeDirection.OUTBOUND)) {
                comPortPools.addAll(engineConfigurationService.findOutboundComPortPoolsByType(supportedComPortType));
            } else {
                comPortPools.addAll(engineConfigurationService.findInboundComPortPoolsByType(supportedComPortType));
            }
        }
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteComPortPool(@PathParam("id") long id, ComPortPoolInfo info) {
        resourceHelper.lockComPortPoolOrThrowException(info).makeObsolete();
        return Response.noContent().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response createComPortPool(ComPortPoolInfo<ComPortPool> comPortPoolInfo, @Context UriInfo uriInfo) {
        ComPortPool comPortPool = comPortPoolInfo.createNew(engineConfigurationService, protocolPluggableService, mdcPropertyUtils);
        if (comPortPool instanceof OutboundComPortPool) { // TODO Polymorphism is in place here: get rid of these checks!
            handlePools(comPortPoolInfo, (OutboundComPortPool) comPortPool, engineConfigurationService, getBoolean(uriInfo, ALL));
        } else if (comPortPool instanceof InboundComPortPool) {
            handleInboundPoolPorts((InboundComPortPool) comPortPool, Optional.ofNullable(comPortPoolInfo.inboundComPorts));
        }
        return Response.status(Response.Status.CREATED).entity(comPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService, mdcPropertyUtils)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComPortPoolInfo updateComPortPool(@PathParam("id") long id, ComPortPoolInfo<ComPortPool> info, @Context UriInfo uriInfo) {
        ComPortPool comPortPool = resourceHelper.lockComPortPoolOrThrowException(info);
        info.writeTo(comPortPool, protocolPluggableService, mdcPropertyUtils);
        if (comPortPool instanceof OutboundComPortPool) {
            handlePools(info, (OutboundComPortPool) comPortPool, engineConfigurationService, getBoolean(uriInfo, ALL));
        }
        if (InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            handleInboundPoolPorts((InboundComPortPool) comPortPool, Optional.ofNullable(info.inboundComPorts));
        }
        comPortPool.update();
        return comPortPoolInfoFactory.asInfo(comPortPool, engineConfigurationService, mdcPropertyUtils);
    }

    @Path("/{comPortPoolId}/comports")
    public ComPortPoolComPortResource getComPortResource() {
        return comPortPoolComPortResourceProvider.get();
    }

    @GET
    @Transactional
    @Path("/{id}/maxPriorityConnections")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public long getMaxPriorityConnections(@PathParam("id") long id, @QueryParam("pctHighPrioTasks") long pctHighPrioTasks) {
        Optional<? extends ComPortPool> comPortPool = engineConfigurationService.findComPortPool(id);

        if (comPortPool.isPresent()) {
            return engineConfigurationService.calculateMaxPriorityConnections(comPortPool.get(), pctHighPrioTasks);
        }

        return 0;
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
                    comPort.update();
                }
            }

            for (ComPortInfo inboundComPortInfo : newComPortIdMap.values()) {
                Optional<? extends ComPort> comPort = engineConfigurationService.findComPort(inboundComPortInfo.id);
                if (comPort.isPresent() && (comPort.get() instanceof InboundComPort)) {
                    ((InboundComPort) comPort.get()).setComPortPool(inboundComPortPool);
                    comPort.get().update();
                }
            }
        } else {
            for (InboundComPort comPort : inboundComPortPool.getComPorts()) {
                comPort.setComPortPool(null);
                comPort.update();
            }
        }
    }

    protected void handlePools(ComPortPoolInfo<? extends ComPortPool> comPortPoolInfo, OutboundComPortPool outboundComPortPool, EngineConfigurationService engineConfigurationService, boolean all) {
        if(comPortPoolInfo.taskExecutionTimeout != null) {
            outboundComPortPool.setTaskExecutionTimeout(comPortPoolInfo.taskExecutionTimeout.asTimeDuration());
        }
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
                throw new WebApplicationException("No ComPort with id " + comPortInfo.id,
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + comPortInfo.id).build());
            }
            if (!OutboundComPort.class.isAssignableFrom(comPort.get().getClass())) {
                throw new WebApplicationException("ComPort with id " + comPortInfo.id + " should have been OutboundComPort, but was " + comPort.get().getClass().getSimpleName(),
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity("ComPort with id " + comPortInfo.id + " should have been OutboundComPort, but was " + comPort.get().getClass().getSimpleName())
                                .build());
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

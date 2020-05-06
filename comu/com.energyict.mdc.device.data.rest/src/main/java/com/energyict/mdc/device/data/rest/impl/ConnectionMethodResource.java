/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.data.rest.DeviceStagesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by bvn on 10/3/14.
 */
@DeviceStagesRestricted(value = {EndDeviceStage.POST_OPERATIONAL}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class ConnectionMethodResource {

    private final ResourceHelper resourceHelper;
    private final ConnectionMethodInfoFactory connectionMethodInfoFactory;
    private final EngineConfigurationService engineConfigurationService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final ConnectionTaskService connectionTaskService;
    private final TopologyService topologyService;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final Provider<ComSessionResource> comTaskExecutionResourceProvider;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, ConnectionMethodInfoFactory connectionMethodInfoFactory, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils, ConnectionTaskService connectionTaskService, TopologyService topologyService, ExceptionFactory exceptionFactory, Thesaurus thesaurus, Provider<ComSessionResource> comTaskExecutionResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.connectionMethodInfoFactory = connectionMethodInfoFactory;
        this.engineConfigurationService = engineConfigurationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.connectionTaskService = connectionTaskService;
        this.topologyService = topologyService;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.comTaskExecutionResourceProvider = comTaskExecutionResourceProvider;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnectionMethods(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Optional<String> fullTopologyBoolean = getParameterFromUriParams(uriInfo, "fullTopology");
        boolean fullTopology = fullTopologyBoolean.isPresent() ? Boolean.valueOf(fullTopologyBoolean.get()) : false;
        List<ConnectionTask<?, ?>> correspondingConnectionTasks = fullTopology ? topologyService.findAllConnectionTasksForTopology(device) : device.getConnectionTasks();
        List<ConnectionTask<?, ?>> connectionTasks = ListPager.of(correspondingConnectionTasks, new ConnectionTaskComparator()).from(queryParameters).find();
        List<ConnectionMethodInfo<?>> connectionMethodInfos = connectionMethodInfoFactory.asInfoList(connectionTasks, uriInfo);
        return Response.ok(PagedInfoList.fromPagedList("connectionMethods", connectionMethodInfos, queryParameters)).build();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response createConnectionMethod(@PathParam("name") String name, @Context UriInfo uriInfo, ConnectionMethodInfo<?> connectionMethodInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionMethodInfo.name);
        Optional<ConfirmationInfo> confirmationInfoOptional = validateTask(connectionMethodInfo, partialConnectionTask);
        if (confirmationInfoOptional.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(confirmationInfoOptional.get()).build();
        }
        ConnectionTask<?, ?> task = connectionMethodInfo.createTask(engineConfigurationService, device, mdcPropertyUtils, partialConnectionTask);
        if (connectionMethodInfo.isDefault) {
            connectionTaskService.setDefaultConnectionTask(task);
        } else if (task.getPartialConnectionTask().getConnectionFunction().isPresent()) {
            connectionTaskService.setConnectionTaskHavingConnectionFunction(task, Optional.empty());
        }
        return Response.status(Response.Status.CREATED).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    private Optional<ConfirmationInfo> validateTask(ConnectionMethodInfo<?> connectionMethodInfo, PartialConnectionTask task) {
        ConfirmationInfo confirmationInfo = new ConfirmationInfo();
        if (connectionMethodInfo.status == ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE && !hasAllRequiredProps(connectionMethodInfo, task)) {
            confirmationInfo.errors.add(new ErrorInfo(thesaurus.getSimpleFormat(MessageSeeds.NOT_ALL_PROPS_ARE_DEFINED).format()));
        }
        return Optional.of(confirmationInfo)
                .filter(confirmation -> !confirmation.errors.isEmpty());
    }

    private Optional<ConfirmationInfo> pauseOrResumeTaskIfNeeded(ConnectionMethodInfo<?> connectionMethodInfo, ConnectionTask<?, ?> task) {
        ConfirmationInfo confirmationInfo = new ConfirmationInfo();
        switch (connectionMethodInfo.status) {
            case ACTIVE:
                if (!hasAllRequiredProps(task)) {
                    confirmationInfo.errors.add(new ErrorInfo(thesaurus.getSimpleFormat(MessageSeeds.NOT_ALL_PROPS_ARE_DEFINED).format()));
                } else if (!task.isActive()) {
                    task.activate();
                }
                break;
            case INACTIVE:
                if (task.getStatus() != ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE) {
                    task.deactivate();
                }
                break;
            case INCOMPLETE:
                task.invalidateStatus();
                break;
        }
        return Optional.of(confirmationInfo)
                .filter(confirmation -> !confirmation.errors.isEmpty());
    }

    private boolean hasAllRequiredProps(ConnectionMethodInfo<?> connectionMethodInfo, PartialConnectionTask task) {
        //if the connection is inbound don't check the props: host name and portPool
        if (PartialInboundConnectionTask.class.isAssignableFrom(task.getClass())) {
            return true;
        }
        List<PropertyInfo> props = connectionMethodInfo.properties;

        //for Outbound TLS only
        if (isOutboundTLS(task.getPluggableClass()) && props.stream().filter(prop -> prop.key.equals("ServerTLSCertificate")).noneMatch(this::hasValue)) {
            return false;
        }
        // TCP/IP
        if (isOutBoundTcpIp(task.getPluggableClass())) {
            return !Checks.is(connectionMethodInfo.comPortPool).empty() &&
                    props.stream().anyMatch(prop -> prop.key.equals("host") && hasValue(prop))
                    && props.stream().anyMatch(prop -> prop.key.equals("portNumber") && hasValue(prop));
        }
        //Serial Optical
        return !Checks.is(connectionMethodInfo.comPortPool).empty();
    }

    private boolean hasValue(PropertyInfo prop) {
        return prop.propertyValueInfo != null &&
                ((prop.propertyValueInfo.value != null && !"".equals(prop.propertyValueInfo.value))
                        || (prop.propertyValueInfo.inheritedValue != null && !"".equals(prop.propertyValueInfo.inheritedValue))
                );
    }

    static boolean hasAllRequiredProps(ConnectionTask<?, ?> task) {
        //if the connection is inbound don't check the props: host name and portPool
        if (InboundConnectionTask.class.isAssignableFrom(task.getClass())) {
            return true;
        }
        List<ConnectionTaskProperty> props = task.getProperties();

        //for Outbound TLS only
        if (isOutboundTLS(task.getPluggableClass()) && !getConnnectionTaskProperty(props, "ServerTLSCertificate").isPresent()) {
            return false;
        }

        // TCP/IP
        if (isOutBoundTcpIp(task.getPluggableClass())) {
            return Objects.nonNull(task.getComPortPool()) && getConnnectionTaskProperty(props, "host").isPresent() && getConnnectionTaskProperty(props, "portNumber").isPresent();
        }

        //Serial Optical
        return Objects.nonNull(task.getComPortPool());
    }

    static boolean isOutBoundTcpIp(PluggableClass pluggableClass) {
        //todo: use com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnectionTypePluggableClassTranslationKeys.OutboundTcpIpConnectionType
        return pluggableClass.getName().equals("Outbound TCP/IP");
    }

    static boolean isOutboundTLS(PluggableClass pluggableClass) {
        return pluggableClass.getName().equals("Outbound TLS");
    }

    static Optional<ConnectionTaskProperty> getConnnectionTaskProperty(List<ConnectionTaskProperty> properties, String name) {
        return properties.stream().filter(prop -> prop.getName().equals(name)).findFirst();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(connectionTask, uriInfo)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/activate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId,
                                             @Context UriInfo uriInfo,
                                             ConnectionMethodInfo<ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> info) {
        info.id = connectionMethodId;
        ConnectionTask task = resourceHelper.lockConnectionTaskOrThrowException(info);
        switch (info.status) {
            case ACTIVE:
                if (!ConnectionMethodResource.hasAllRequiredProps(task)) {
                    throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_ALL_PROPS_ARE_DEFINED);
                } else if (!task.isActive()) {
                    task.activate();
                }
                break;
            case INACTIVE:
                task.deactivate();
                break;
            default:
                break;
        }
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId,
                                           @Context UriInfo uriInfo,
                                           ConnectionMethodInfo<ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> info) {
        info.id = connectionMethodId;
        ConnectionTask task = resourceHelper.lockConnectionTaskOrThrowException(info);
        Device device = task.getDevice();
        boolean wasConnectionTaskDefault = task.isDefault();
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, info.name);
        if (info.protocolDialect != null && !info.protocolDialect.isEmpty()) {
            List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList = task.getDevice().getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
            Optional<ProtocolDialectConfigurationProperties> dialectConfigurationProperties = protocolDialectConfigurationPropertiesList.stream()
                    .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties
                            .getDeviceProtocolDialectName()
                            .equals(info.protocolDialect))
                    .findFirst();
            if (!dialectConfigurationProperties.isPresent()) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_PROTOCOL_PROPERTIES, info.protocolDialect);
            }
            connectionTaskService.updateProtocolDialectConfigurationProperties(task, dialectConfigurationProperties.get());
        }
        info.writeTo(task, partialConnectionTask, engineConfigurationService, mdcPropertyUtils);
        task.saveAllProperties();
        Optional<ConfirmationInfo> confirmationInfoOptional = pauseOrResumeTaskIfNeeded(info, task);
        if (confirmationInfoOptional.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(confirmationInfoOptional.get()).build();
        }
        task.save();
        if (info.isDefault && !wasConnectionTaskDefault) {
            connectionTaskService.setDefaultConnectionTask(task);
        } else if (!info.isDefault && wasConnectionTaskDefault) {
            connectionTaskService.clearDefaultConnectionTask(device);
        }
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    private PartialConnectionTask findPartialConnectionTaskOrThrowException(Device device, String name) {
        for (PartialConnectionTask partialConnectionTask : device.getDeviceConfiguration().getPartialConnectionTasks()) {
            if (partialConnectionTask.getName().equals(name)) {
                return partialConnectionTask;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK);
    }


    @DELETE
    @Transactional
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId, ConnectionMethodInfo<ConnectionTask<?, ?>> info) {
        ConnectionTask connectionTask = resourceHelper.lockConnectionTaskOrThrowException(info);
        connectionTask.getDevice().removeConnectionTask(connectionTask);
        return Response.ok().build();
    }

    @Path("/{connectionMethodId}/comsessions")
    public ComSessionResource getComTaskExecutionResource() {
        return comTaskExecutionResourceProvider.get();
    }

    private Optional<String> getParameterFromUriParams(UriInfo uriInfo, String parameter) {
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey(parameter) && !uriParams.getFirst(parameter).isEmpty()) {
            return Optional.of(uriParams.getFirst(parameter));
        } else {
            return Optional.empty();
        }
    }

    private static class ConfirmationInfo {
        public final boolean confirmation = true;
        public final boolean success = false;
        public List<ErrorInfo> errors = new ArrayList<>();
    }

    private static class ErrorInfo {
        public String id;
        public String msg;

        private ErrorInfo(String msg) {
            this.msg = msg;
        }
    }
}

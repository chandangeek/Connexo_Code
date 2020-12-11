/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.ObjectParser;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Elements of the QueryMethod enum correspond to every method that is defined
 * in the ComServerDAO interface.
 * The last element represents the fact that a remote client executes
 * a method that does not actually exist on the ComServerDAO interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:18)
 */
public enum QueryMethod {

    GetThisComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            return serviceProvider.comServerDAO().getThisComServer();
        }
    },
    GetComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            String hostName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.HOSTNAME);
            return serviceProvider.comServerDAO().getComServer(hostName);
        }
    },
    RefreshComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comServerId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            if (comServer.isPresent()) {
                Instant modificationDate = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE);
                if (comServer.get().getModTime().isAfter(modificationDate)) {
                    return comServer.get();
                }
            }
            return null;
        }
    },
    FindPendingOutboundComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Integer comPortId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                return serviceProvider.comServerDAO().findPendingOutboundComTasks((OutboundComPort) comPort.get());
            }
            return null;
        }
    },
    FindExecutableOutboundComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Integer comPortId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                return serviceProvider.comServerDAO().findExecutableOutboundComTasks((OutboundComPort) comPort.get());
            }
            return null;
        }
    },
    FindExecutableHighPriorityOutboundComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comServerId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            Map<Long, Integer> currentHighPriorityLoadPerComPortPool = extractCurrentHighPriorityLoadPerComPortPool(parameters);
            if (comServer.isPresent()) {
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.CURRENT_DATE)) {
                    Instant date = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.CURRENT_DATE);
                    return serviceProvider.comServerDAO().findExecutableHighPriorityOutboundComTasks((OutboundCapableComServer) comServer.get(), currentHighPriorityLoadPerComPortPool, date);
                } else {
                    return serviceProvider.comServerDAO().findExecutableHighPriorityOutboundComTasks((OutboundCapableComServer) comServer.get(), currentHighPriorityLoadPerComPortPool);
                }
            }
            return null;
        }
    },
    FindContainingComPortPoolsForComPort {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comPortId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                return serviceProvider.comServerDAO().findContainingActiveComPortPoolsForComPort((OutboundComPort) comPort.get());
            }
            return null;
        }
    },
    FindExecutableInboundComTasks,
    ExecutionStarted {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Long comportId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comportId);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (comPort.isPresent() && comTaskExecution.isPresent()) {
                    this.executionStarted(serviceProvider, comPort.get(), comTaskExecution.get());
                }
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Long comPortId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
                if (comPort.isPresent()) {
                    this.executionStarted(serviceProvider, connectionTask, comPort.get());
                }
            }
            return null;
        }
    },
    AttemptLock {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Long comportId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comportId);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (comPort.isPresent() && comTaskExecution.isPresent()) {
                    this.attemptLock(serviceProvider, comPort.get(), comTaskExecution.get());
                }
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Long comportId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
                OutboundConnectionTask connectionTask = serviceProvider.connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comportId);
                if (comPort.isPresent()) {
                    this.attemptLock(serviceProvider, connectionTask, comPort.get());
                }
            }
            return null;
        }
    },
    Unlock {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.unlock(serviceProvider, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                OutboundConnectionTask connectionTask = serviceProvider.connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                this.unlock(serviceProvider, connectionTask);
            }
            return null;
        }
    },
    ExecutionCompleted {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.executionCompleted(serviceProvider, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                this.executionCompleted(serviceProvider, connectionTask);
            }
            return null;
        }
    },
    ExecutionRescheduled {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION)) {
                Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE)) {
                    Instant rescheduleDate = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE);
                    if (comTaskExecution.isPresent()) {
                        this.executionRescheduled(serviceProvider, comTaskExecution.get(), rescheduleDate);
                    }
                }
            } else if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK)) {
                Integer connectionTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                Optional<ConnectionTask> connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId);
                if (connectionTask.isPresent()) {
                    executionRescheduled(serviceProvider, connectionTask.get());
                }
            }
            return null;
        }
    },
    ExecutionRescheduledToComWindow {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION)) {
                Integer comTaskExecutionId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE)) {
                    Instant rescheduleDate = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE);
                    if (comTaskExecution.isPresent()) {
                        executionRescheduledToComWindow(serviceProvider, comTaskExecution.get(), rescheduleDate);
                    }
                }
            }
            return null;
        }
    },
    ExecutionFailed {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            if (comTaskExecutionId != null) {
                Optional<ComTaskExecution> comTaskExecution = serviceProvider.communicationTaskService().findComTaskExecution(comTaskExecutionId);
                comTaskExecution.ifPresent(cte -> this.executionFailed(serviceProvider, cte));
            } else {
                // Must be a ConnectionTask
                Long connectionTaskId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                this.executionFailed(serviceProvider, connectionTask);
            }
            return null;
        }
    },
    ReleaseInterruptedComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comPortId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                serviceProvider.comServerDAO().releaseInterruptedTasks(comPort.get());
            }
            return null;
        }
    },
    ReleaseTimedOutComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comPortId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            return new TimeDurationXmlWrapper(serviceProvider.comServerDAO().releaseTimedOutTasks(comPort.get()));
        }
    },
    ReleaseComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Integer comPortId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                serviceProvider.comServerDAO().releaseTasksFor(comPort.get());
            }
            return null;
        }
    },
    /* Message used by a Remote ComServer telling its online that it is shutting down */
    DisconnectRemoteComServer {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            return serviceProvider.engineConfigurationService().findComServer(comServerId);
        }
    },
    GetCompletionCodeLookupEntries {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            return serviceProvider.comServerDAO().getCompletionCodeLookupEntries();
        }
    },
    GetDeviceProtocolSecurityProperties {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) parameters.get(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
            Long comPortId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                return serviceProvider.comServerDAO().getDeviceProtocolSecurityProperties(deviceIdentifier, (InboundComPort) comPort.get());
            }
            return null;
        }
    },
    GetPropertiesFromSecurityPropertySet {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                Integer securityPropertySetId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.SECURITY_PROPERTY_SET_IDENTIFIER);
                return serviceProvider.comServerDAO().getPropertiesFromSecurityPropertySet(deviceIdentifier, Long.valueOf(securityPropertySetId));
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GetDeviceProtocolProperties {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                return serviceProvider.comServerDAO().getDeviceProtocolProperties(deviceIdentifier);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GetDeviceProtocolPropertyValueType {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                String protocolProperty = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME);
                return serviceProvider.comServerDAO().getDeviceProtocolPropertyValueType(deviceIdentifier, protocolProperty);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateDeviceDialectProperty {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> deviceIdentifierObjectParser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = deviceIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);

                ObjectParser<TypedProperties> typedPropertiesObjectParser = new ObjectParser<>();
                TypedProperties protocolProperties = typedPropertiesObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES);
                String propertyName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME);
                Object propertyValue = protocolProperties.getProperty(propertyName);

                serviceProvider.comServerDAO().updateDeviceDialectProperty(deviceIdentifier, propertyName, propertyValue);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateDeviceSecurityProperty {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> deviceIdentifierObjectParser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = deviceIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);

                ObjectParser<TypedProperties> typedPropertiesObjectParser = new ObjectParser<>();
                TypedProperties securityProperties = typedPropertiesObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES);
                String propertyName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME);
                Object propertyValue = securityProperties.getProperty(propertyName);

                serviceProvider.comServerDAO().updateDeviceSecurityProperty(deviceIdentifier, propertyName, propertyValue);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindComTaskEnablementByDeviceAndComTask {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                Integer comTaskId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMTASK);
                return serviceProvider.comServerDAO().findComTaskEnablementByDeviceAndComTask(deviceIdentifier, comTaskId);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindProtocolDialectPropertiesForComTaskExecution {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
            return serviceProvider.comServerDAO().findProtocolDialectPropertiesFor(comTaskExecutionId);
        }
    },
    FindAllSecurityPropertySetsForDevice {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            ObjectParser<DeviceIdentifier> deviceIdentifierParser = new ObjectParser<>();
            try {
                DeviceIdentifier deviceIdentifier = deviceIdentifierParser.parseObject(new JSONObject(parameters), RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                return serviceProvider.comServerDAO().findAllSecurityPropertySetsForDevice(deviceIdentifier);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GetUsersCredentialInformation {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            return serviceProvider.comServerDAO().getUsersCredentialInformation();
        }
    },
    CheckAuthentication {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            return serviceProvider.comServerDAO().checkAuthentication((String) parameters.get(RemoteComServerQueryJSonPropertyNames.AUTH_DATA));
        }
    },
    GetInboundComTaskOnHold {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                Integer comPortId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
                if (comPort.isPresent()) {
                    return serviceProvider.comServerDAO().getInboundComTaskOnHold(deviceIdentifier, (InboundComPort) comPort.get());
                } else {
                    return null;
                }
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GetDeviceDialectProperties {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                Integer inboundComPortId = (Integer) jsonObject.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(inboundComPortId);
                if (comPort.isPresent()) {
                    return serviceProvider.comServerDAO().getDeviceDialectProperties(deviceIdentifier, (InboundComPort) comPort.get());
                } else {
                    return null;
                }
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GetOutboundConnectionTypeProperties {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                return serviceProvider.comServerDAO().getOutboundConnectionTypeProperties(deviceIdentifier);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GoOfflineWithContext {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> deviceIdentifierObjectParser = new ObjectParser<>();
                ObjectParser<OfflineDeviceContext> offlineDeviceContextObjectParser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                OfflineDeviceContext offlineDeviceContext = offlineDeviceContextObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.OFFLINE_DEVICE_CONTEXT);
                DeviceIdentifier deviceIdentifier = deviceIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                return serviceProvider.comServerDAO().getOfflineDevice(deviceIdentifier, offlineDeviceContext);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    StoreMeterReading {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> deviceIdentifierObjectParser = new ObjectParser<>();
                ObjectParser<MeterReading> meterReadingDataObjectParser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = deviceIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                MeterReading meterReading = meterReadingDataObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.METER_READING);
                this.storeMeterReadings(serviceProvider, deviceIdentifier, meterReading);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindOfflineRegister {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<RegisterIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                RegisterIdentifier registerIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.REGISTER_IDENTIFIER);
                Instant when = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.WHEN);
                return serviceProvider.comServerDAO().findOfflineRegister(registerIdentifier, when);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindOfflineLoadProfile {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<LoadProfileIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                LoadProfileIdentifier loadProfileIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOADPROFILE_IDENTIFIER);
                return serviceProvider.comServerDAO().findOfflineLoadProfile(loadProfileIdentifier);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindOfflineLogBook {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<LogBookIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                LogBookIdentifier logBookIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER);
                return serviceProvider.comServerDAO().findOfflineLogBook(logBookIdentifier);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindOfflineDeviceMessage {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<MessageIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                MessageIdentifier messageIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.MESSAGE_IDENTIFIER);
                return serviceProvider.comServerDAO().findOfflineDeviceMessage(messageIdentifier);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateDeviceProtocolProperty {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> deviceIdentifierObjectParser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = deviceIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);

                ObjectParser<TypedProperties> typedPropertiesObjectParser = new ObjectParser<>();
                TypedProperties protocolProperties = typedPropertiesObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES);
                String propertyName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME);
                Object propertyValue = protocolProperties.getProperty(propertyName);

                serviceProvider.comServerDAO().updateDeviceProtocolProperty(deviceIdentifier, propertyName, propertyValue);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateGateway {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                DeviceIdentifier gatewayIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.GATEWAY_DEVICE_IDENTIFIER);
                serviceProvider.comServerDAO().updateGateway(deviceIdentifier, gatewayIdentifier);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    StoreConfigurationFile {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                String dateFormatPattern = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.DATE_FORMAT);
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(dateFormatPattern);
                String fileExtension = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.FILE_EXTENSION);
                String hexContent = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.HEX_DATA);
                String fileName = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.FILE_NAME);
                byte[] content = getBytesFromHexString(hexContent);
                serviceProvider.comServerDAO().storeConfigurationFile(deviceIdentifier, dateFormat, fileName, fileExtension, content);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateDeviceMessageInformation {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<MessageIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                MessageIdentifier messageIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.MESSAGE_IDENTIFIER);
                DeviceMessageStatus newMessageStatus = DeviceMessageStatus.valueOf((String) parameters.get(RemoteComServerQueryJSonPropertyNames.MESSAGE_STATUS));
                Instant sentDate = null;
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.SENT_DATE)) {
                    sentDate = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.SENT_DATE);
                }
                String protocolInformation = null;
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.MESSAGE_INFORMATION)) {
                    protocolInformation = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.MESSAGE_INFORMATION);
                }
                this.updateDeviceMessageInformation(serviceProvider, messageIdentifier, newMessageStatus, sentDate, protocolInformation);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    ConfirmSentMessagesAndGetPending {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                Integer confirmationCount = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.MESSAGE_CONFIRMATION_COUNT);
                return serviceProvider.comServerDAO().confirmSentMessagesAndGetPending(deviceIdentifier, confirmationCount);
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    GetDeviceConnectionTypeProperties {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                Integer inboundComPortId = (Integer) jsonObject.get(RemoteComServerQueryJSonPropertyNames.COMPORT);
                Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(inboundComPortId);
                if (comPort.isPresent()) {
                    return serviceProvider.comServerDAO().getDeviceConnectionTypeProperties(deviceIdentifier, (InboundComPort) comPort.get());
                } else {
                    return null;
                }
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    CreateAndUpdateComSession {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<ComSessionBuilder> builderParser = new ObjectParser<>();
                ComSessionBuilder builder = builderParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.COMSESSION_BUILDER);
                Instant stopDate = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.COMSESSION_STOP_DATE);
                ComSession.SuccessIndicator successIndicator = ComSession.SuccessIndicator.valueOf(jsonObject.getString(RemoteComServerQueryJSonPropertyNames.COMSESSION_SUCCESS_INDICATOR));
                this.createComSession(serviceProvider, builder, stopDate, successIndicator);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    CreateOrUpdateDeviceCache {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<DeviceIdentifier> deviceIdentifierParser = new ObjectParser<>();
                DeviceIdentifier deviceIdentifier = deviceIdentifierParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                DeviceProtocolCacheParser deviceProtocolCacheParser = new DeviceProtocolCacheParser();
                DeviceProtocolCache deviceProtocolCache = deviceProtocolCacheParser.parse(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_CACHE);
                this.createOrUpdateDeviceCache(serviceProvider, deviceIdentifier, new DeviceProtocolCacheXmlWrapper(deviceProtocolCache));
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    StoreLoadProfile {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<LoadProfileIdentifier> loadProfileIdentifierObjectParser = new ObjectParser<>();
                ObjectParser<CollectedLoadProfile> collectedLoadProfileObjectParser = new ObjectParser<>();
                LoadProfileIdentifier loadProfileIdentifier = loadProfileIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOADPROFILE_IDENTIFIER);
                CollectedLoadProfile collectedLoadProfile = collectedLoadProfileObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.COLLECTED_LOADPROFILE);
                Instant date = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.CURRENT_DATE);
                this.storeLoadProfile(serviceProvider, loadProfileIdentifier, collectedLoadProfile, date);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    StoreLogBookData {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<LogBookIdentifier> logBookIdentifierObjectParser = new ObjectParser<>();
                ObjectParser<CollectedLogBook> collectedLogBookObjectParser = new ObjectParser<>();
                LogBookIdentifier logBookIdentifier = logBookIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER);
                CollectedLogBook collectedLogBook = collectedLogBookObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.COLLECTED_LOGBOOK);
                Instant date = getInstant(parameters, RemoteComServerQueryJSonPropertyNames.CURRENT_DATE);
                this.storeLogBookData(serviceProvider, logBookIdentifier, collectedLogBook, date);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateLogBookLastReading {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<LogBookIdentifier> logBookIdentifierObjectParser = new ObjectParser<>();
                LogBookIdentifier logBookIdentifier = logBookIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER);
                Date logBookLastReading = getDate(parameters.get(RemoteComServerQueryJSonPropertyNames.LOGBOOK_LAST_READING));
                serviceProvider.comServerDAO().updateLogBookLastReading(logBookIdentifier, logBookLastReading);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateLogBookLastReadingFromTask {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                ObjectParser<LogBookIdentifier> logBookIdentifierObjectParser = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                LogBookIdentifier logBookIdentifier = logBookIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER);
                Long comTaskExecutionId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION);
                this.updateLogBookLastReadingFromTask(serviceProvider, logBookIdentifier, comTaskExecutionId);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateDataSourceReadings {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<Map> mapParser = new ObjectParser<>();
                Map<LoadProfileIdentifier, Instant> lastReadings = mapParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LAST_READINGS);
                if (lastReadings == null) {
                    lastReadings = new HashMap<LoadProfileIdentifier, Instant>();
                }
                Map<LogBookIdentifier, Instant> lastLogBooks = mapParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LAST_LOGBOOKS);
                if (lastLogBooks == null) {
                    lastLogBooks = new HashMap<LogBookIdentifier, Instant>();
                }
                serviceProvider.comServerDAO().updateLastDataSourceReadingsFor(lastReadings, lastLogBooks);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    FindDevice,
    UpdateIpAddress,
    CreateDeviceEvent,
    SignalEvent,
    IsStillPending,
    AreStillPending,
    MessageNotUnderstood;

    private static Map<Long, Integer> extractCurrentHighPriorityLoadPerComPortPool(Map<String, Object> parameters) {
        try {
            JSONObject jsonObject = (JSONObject) parameters.get(RemoteComServerQueryJSonPropertyNames.CURRENT_HIGH_PRIORITY_LOAD);
            ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, Long.class, Integer.class);
            return mapper.readValue(new StringReader(jsonObject.toString()), mapType);
        } catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }


    /**
     * Defines the services that are required by all QueryMethods.
     */
    public interface ServiceProvider {

        public ComServerDAO comServerDAO();

        public EngineConfigurationService engineConfigurationService();

        public ConnectionTaskService connectionTaskService();

        public CommunicationTaskService communicationTaskService();

        public TransactionService transactionService();

        public ThreadPrincipalService threadPrincipalService();

        public UserService userService();

    }

    private static Date getDate(Object parameter) {
        if (parameter instanceof Date) {
            return (Date) parameter;
        }
        if (parameter instanceof Long) {
            return new Date((Long) parameter);
        }
        return null;
    }

    private static Instant getInstant(Map<String, Object> parameters, String jsonPropertyName) {
        Date date = getDate(parameters.get(jsonPropertyName));
        if (date != null) {
            return date.toInstant();
        }
        return null;
    }

    private static Long getLong(Map<String, Object> parameters, String jsonPropertyName) {
        Object parameter = parameters.get(jsonPropertyName);
        if (parameter instanceof Long) {
            return (Long) parameter;
        } else if (parameter instanceof Integer) {
            return Long.valueOf((Integer) parameter);
        } else {
            return null;
        }
    }

    protected void executionStarted(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComPort comPort) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionStarted(connectionTask, comPort);
            }
        });
    }

    protected void attemptLock(ServiceProvider serviceProvider, OutboundConnectionTask connectionTask, ComPort comPort) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().attemptLock(connectionTask, comPort);
            }
        });
    }

    protected void attemptLock(ServiceProvider serviceProvider, ComPort comPort, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().attemptLock(comTaskExecution, comPort);
            }
        });
    }

    protected void unlock(ServiceProvider serviceProvider, OutboundConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().unlock(connectionTask);
            }
        });
    }

    protected void unlock(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().unlock(comTaskExecution);
            }
        });
    }

    protected void executionCompleted(ServiceProvider serviceProvider, ConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionCompleted(connectionTask);
            }
        });
    }

    protected void executionFailed(ServiceProvider serviceProvider, ConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionFailed(connectionTask);
            }
        });
    }

    protected void executionRescheduled(ServiceProvider serviceProvider, ConnectionTask connectionTask) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionRescheduled(connectionTask);
            }
        });
    }

    protected void executionRescheduled(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionRescheduled(comTaskExecution, rescheduleDate);
            }
        });
    }

    protected void executionRescheduledToComWindow(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionRescheduledToComWindow(comTaskExecution, rescheduleDate);
            }
        });
    }

    protected void executionStarted(ServiceProvider serviceProvider, ComPort comPort, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionStarted(comTaskExecution, comPort, false);
            }
        });
    }

    protected void executionCompleted(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionCompleted(comTaskExecution);
            }
        });
    }

    protected void executionFailed(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionFailed(comTaskExecution);
            }
        });
    }

    protected void createComSession(ServiceProvider serviceProvider, ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().createComSession(builder, stopDate, successIndicator);
            }
        });
    }

    protected void storeMeterReadings(ServiceProvider serviceProvider, DeviceIdentifier identifier, MeterReading meterReading) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().storeMeterReadings(identifier, meterReading);
            }
        });
    }

    public void storeLoadProfile(ServiceProvider serviceProvider, LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile, Instant currentDate) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().storeLoadProfile(loadProfileIdentifier, collectedLoadProfile, currentDate);
            }
        });
    }

    public void storeLogBookData(ServiceProvider serviceProvider, LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook, Instant currentDate) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().storeLogBookData(logBookIdentifier, collectedLogBook, currentDate);
            }
        });
    }

    public void updateLogBookLastReadingFromTask(ServiceProvider serviceProvider, LogBookIdentifier logBookIdentifier, long comTaskExecutionId) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().updateLogBookLastReadingFromTask(logBookIdentifier, comTaskExecutionId);
            }
        });
    }

    public void updateDeviceMessageInformation(ServiceProvider serviceProvider, MessageIdentifier messageIdentifier, DeviceMessageStatus newMessageStatus, Instant sentDate, String protocolInformation) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().updateDeviceMessageInformation(messageIdentifier, newMessageStatus, sentDate, protocolInformation);
            }
        });
    }

    public void createOrUpdateDeviceCache(ServiceProvider serviceProvider, DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().createOrUpdateDeviceCache(deviceIdentifier, cache);
            }
        });
    }

    private <T> T executeTransaction(ServiceProvider serviceProvider, ExceptionThrowingSupplier<T, RuntimeException> transaction) {
        return serviceProvider.transactionService().execute(transaction);
    }

    private boolean nameMatches(String name) {
        return this.name().equalsIgnoreCase(name);
    }

    public static QueryMethod byName(String methodName) {
        for (QueryMethod queryMethod : values()) {
            if (queryMethod.nameMatches(methodName)) {
                return queryMethod;
            }
        }
        return MessageNotUnderstood;
    }

    /**
     * Executes the query with the specified parameters
     * and uses the Writer to marshall the result to JSON.
     *
     * @param parameters The parameters of the query
     * @param serviceProvider The ServiceProvider
     * @return The result of the query
     */
    public Object execute(Map<String, Object> parameters, ServiceProvider serviceProvider) throws IOException {
        return this.doExecute(parameters, serviceProvider);
    }

    protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
        throw new UnsupportedOperationException(this.name() + " does not implement doExecute(Map<String,Object>,ComServerDAO) yet");
    }

    protected byte[] getBytesFromHexString(final String hexString) {
        int charsPerByte = 2;
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += charsPerByte) {
            bb.write(Integer.parseInt(hexString.substring(i, i + charsPerByte), 16));
        }
        return bb.toByteArray();
    }
}
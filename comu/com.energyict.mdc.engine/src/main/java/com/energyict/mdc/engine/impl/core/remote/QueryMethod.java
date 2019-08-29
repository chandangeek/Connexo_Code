/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.ObjectParser;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.*;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.properties.TypedProperties;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                Instant modificationDate = this.getModificationDate(parameters);
                if (comServer.get().getModTime().isAfter(modificationDate)) {
                    return comServer.get();
                }
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
            } else {
                return null;
            }
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
                Long comServerId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMSERVER);
                ConnectionTask connectionTask = serviceProvider.connectionTaskService().findConnectionTask(connectionTaskId).get();
                Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
                if (comServer.isPresent()) {
                    this.executionStarted(serviceProvider, connectionTask, comServer.get());
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
                Long comServerId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMSERVER);
                OutboundConnectionTask connectionTask = serviceProvider.connectionTaskService().findOutboundConnectionTask(connectionTaskId).get();
                Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
                if (comServer.isPresent()) {
                    this.attemptLock(serviceProvider, connectionTask, comServer.get());
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
                    Date rescheduleDate = new Date(getLong(parameters, RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE));
                    if(comTaskExecution.isPresent()){
                        this.executionRescheduled(serviceProvider, comTaskExecution.get(), rescheduleDate.toInstant());
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
            Integer comServerId = (Integer) parameters.get(RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            if (comServer.isPresent()) {
                serviceProvider.comServerDAO().releaseInterruptedTasks(comServer.get());
            }
            return null;
        }
    },
    ReleaseTimedOutComTasks {
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            Long comServerId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMSERVER);
            Optional<ComServer> comServer = serviceProvider.engineConfigurationService().findComServer(comServerId);
            return new TimeDurationXmlWrapper(serviceProvider.comServerDAO().releaseTimedOutTasks(comServer.get()));
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
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier)parameters.get(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
            Long comPortId = getLong(parameters, RemoteComServerQueryJSonPropertyNames.COMPORT);
            Optional<? extends ComPort> comPort = serviceProvider.engineConfigurationService().findComPort(comPortId);
            if (comPort.isPresent()) {
                return serviceProvider.comServerDAO().getDeviceProtocolSecurityProperties(deviceIdentifier, (InboundComPort)comPort.get());
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
                serviceProvider.comServerDAO().storeMeterReadings(deviceIdentifier, meterReading);
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
                ObjectParser<Instant> parserInstant = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                RegisterIdentifier registerIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.REGISTER_IDENTIFIER);
                Instant when = parserInstant.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.WHEN);
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
                ObjectParser<Instant> parserInstant = new ObjectParser<>();
                JSONObject jsonObject = new JSONObject(parameters);
                MessageIdentifier messageIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.MESSAGE_IDENTIFIER);
                DeviceMessageStatus newMessageStatus = DeviceMessageStatus.valueOf((String) parameters.get(RemoteComServerQueryJSonPropertyNames.MESSAGE_STATUS));
                Instant instantDate = parserInstant.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.SENT_DATE);
                String protocolInformation = null;
                if (parameters.containsKey(RemoteComServerQueryJSonPropertyNames.MESSAGE_INFORMATION)) {
                    protocolInformation = (String) parameters.get(RemoteComServerQueryJSonPropertyNames.MESSAGE_INFORMATION);
                }

                serviceProvider.comServerDAO().updateDeviceMessageInformation(messageIdentifier, newMessageStatus, instantDate, protocolInformation);
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
    CreateAndUpdateComSession{
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<ComSessionBuilder> parser = new ObjectParser<>();
                ComSessionBuilder builder = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.COMSESSION_BUILDER);
                Instant stopDate =  (Instant) jsonObject.get(RemoteComServerQueryJSonPropertyNames.COMSESSION_STOP_DATE);
                ComSession.SuccessIndicator successIndicator =  (ComSession.SuccessIndicator) jsonObject.get(RemoteComServerQueryJSonPropertyNames.COMSESSION_SUCCESS_INDICATOR);
                serviceProvider.comServerDAO().createComSession(builder, stopDate, successIndicator);
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
                ObjectParser<DeviceIdentifier> parser = new ObjectParser<>();
                DeviceIdentifier deviceIdentifier = parser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER);
                DeviceProtocolCacheParser deviceProtocolCacheParser = new DeviceProtocolCacheParser();
                DeviceProtocolCache deviceProtocolCache = deviceProtocolCacheParser.parse(jsonObject, RemoteComServerQueryJSonPropertyNames.DEVICE_CACHE);
                serviceProvider.comServerDAO().createOrUpdateDeviceCache(new DeviceProtocolCacheXmlWrapper(deviceIdentifier, deviceProtocolCache));
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    StoreLoadProfile{
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<LoadProfileIdentifier> loadProfileIdentifierObjectParser = new ObjectParser<>();
                ObjectParser<CollectedLoadProfile> collectedLoadProfileObjectParser = new ObjectParser<>();
                LoadProfileIdentifier loadProfileIdentifier = loadProfileIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOADPROFILE_IDENTIFIER);
                CollectedLoadProfile collectedLoadProfile = collectedLoadProfileObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.COLLECTED_LOADPROFILE);
                serviceProvider.comServerDAO().storeLoadProfile(loadProfileIdentifier, collectedLoadProfile);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    StoreLogBookData{
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<LogBookIdentifier> logBookIdentifierObjectParser = new ObjectParser<>();
                ObjectParser<CollectedLogBook> collectedLogBookObjectParser = new ObjectParser<>();
                LogBookIdentifier logBookIdentifier = logBookIdentifierObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER);
                CollectedLogBook collectedLogBook = collectedLogBookObjectParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.COLLECTED_LOGBOOK);
                serviceProvider.comServerDAO().storeLogBookData(logBookIdentifier, collectedLogBook);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateLogBookLastReading{
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
                serviceProvider.comServerDAO().updateLogBookLastReadingFromTask(logBookIdentifier, comTaskExecutionId);
                return null;
            } catch (JSONException e) {
                throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
            }
        }
    },
    UpdateDataSourceReadings{
        @Override
        protected Object doExecute(Map<String, Object> parameters, ServiceProvider serviceProvider) {
            try {
                JSONObject jsonObject = new JSONObject(parameters);
                ObjectParser<Map> mapParser = new ObjectParser<>();
                Map<LoadProfileIdentifier, Instant> lastReadings = mapParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LAST_READINGS);
                if (lastReadings == null)
                    lastReadings = new HashMap<LoadProfileIdentifier, Instant>();
                Map<LogBookIdentifier, Instant> lastLogBooks = mapParser.parseObject(jsonObject, RemoteComServerQueryJSonPropertyNames.LAST_LOGBOOKS);
                if(lastLogBooks == null)
                    lastLogBooks = new HashMap<LogBookIdentifier, Instant>();
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

    private static Date getDate(Object parameter) {
        if (parameter instanceof Date) {
            return (Date)parameter;
        }
        if (parameter instanceof Long) {
            return new Date((Long)parameter);
        }
        return null;
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

    Long getLong(Map<String, Object> parameters, String jsonPropertyName) {
        Object parameter = parameters.get(jsonPropertyName);
        if (parameter instanceof Long) {
            return (Long) parameter;
        } else if (parameter instanceof Integer) {
            return Long.valueOf((Integer) parameter);
        } else
            return null;
    }

    protected void executionStarted(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComServer comServer) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionStarted(connectionTask, comServer);
            }
        });
    }

    protected void attemptLock(ServiceProvider serviceProvider, OutboundConnectionTask connectionTask, ComServer comServer) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().attemptLock(connectionTask, comServer);
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

    protected void executionRescheduled(ServiceProvider serviceProvider, ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        this.executeTransaction(serviceProvider, new VoidTransaction() {
            @Override
            public void doPerform() {
                serviceProvider.comServerDAO().executionRescheduled(comTaskExecution, rescheduleDate);
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

    private <T> T executeTransaction(ServiceProvider serviceProvider, ExceptionThrowingSupplier<T, RuntimeException> transaction) {
        return serviceProvider.transactionService().execute(transaction);
    }

    protected Instant getModificationDate(Map<String, Object> parameters) {
        Long utcMillis = (Long) parameters.get(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE);
        return Instant.ofEpochMilli(utcMillis);
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
     * @param parameters   The parameters of the query
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
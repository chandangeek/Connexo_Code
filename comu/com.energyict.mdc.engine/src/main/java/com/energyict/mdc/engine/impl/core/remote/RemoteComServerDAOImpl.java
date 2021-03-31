/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.config.impl.SecurityPropertySetImpl;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.ObjectParser;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionGroup;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.tools.ProtocolUtils;
import com.energyict.mdc.engine.impl.web.queryapi.DataCompressor;
import com.energyict.mdc.engine.users.OfflineUserInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides an implementation for the {@link ComServerDAO} interface
 * that will post a JSon representation of the query
 * to a companion {@link OnlineComServer}
 * that has a servlet running that will listen for these queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:56)
 */
public class RemoteComServerDAOImpl implements ComServerDAO {

    public final static String CLIENT_PROPERTY = "client-name";

    private ServerProcess comServer;
    private long timeout;
    private String queryAPIPostUri;
    private RemoteProperties remoteProperties;
    private ServerProcessStatus status = ServerProcessStatus.STARTING;
    private final ServiceProvider serviceProvider;
    private final Boolean compressData;

    private static final String HEXES = "0123456789ABCDEF";

    private QueryWebSocket webSocket = new QueryWebSocket();
    private final QueryList queries = new QueryList();

    public RemoteComServerDAOImpl(RemoteProperties remoteProperties, ServiceProvider serviceProvider) {
        this.remoteProperties = remoteProperties;
        this.serviceProvider = serviceProvider;
        this.queryAPIPostUri = remoteProperties.getRemoteQueryApiUrl();
        this.timeout = remoteProperties.getTimeout();
        this.compressData = remoteProperties.getCompressQueryData();
    }

    public void setComServer(ServerProcess comServer) {
        this.comServer = comServer;
    }

    @Override
    public ComServer getThisComServer() {
        return getComServer(getSystemName());
    }

    @Override
    public ComServer getComServer(String hostName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.HOSTNAME, hostName);
        JSONObject response = this.post(QueryMethod.GetComServer, queryParameters);
        return this.toComServer(response);
    }

    @Override
    public ComServer refreshComServer(ComServer comServer) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, comServer.getModTime());
            JSONObject response = this.post(QueryMethod.RefreshComServer, queryParameters);
            return this.toComServer(response);
        } else {
            return comServer;
        }
    }

    public void disconnectRemoteComServer(ComServer comServer) {
        if (isWebSocketConnectionValid()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
            post(QueryMethod.DisconnectRemoteComServer, queryParameters);
        }
    }

    public List<OfflineUserInfo> getUsersCredentialInformation() {
        JSONObject response = post(QueryMethod.GetUsersCredentialInformation, new HashMap<String, Object>());
        OfflineUserInfo[] userInfoArray = toArrayObject(response, new ObjectParser<OfflineUserInfo[]>(), OfflineUserInfo[].class);
        return CollectionConverter.convertGenericArrayToList(userInfoArray);
    }

    @Override
    public Optional<OfflineUserInfo> checkAuthentication(String loginPassword) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.AUTH_DATA, Base64.getEncoder().encodeToString(loginPassword.getBytes()));
        JSONObject response = post(QueryMethod.CheckAuthentication, queryParameters);
        return Optional.ofNullable(toObject(response, new ObjectParser<>(), OfflineUserInfo.class));
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ComJob> findPendingOutboundComTasks(OutboundComPort comPort) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
            JSONObject response = post(QueryMethod.FindPendingOutboundComTasks, queryParameters);
            ComJob[] comJobs = toArrayObject(response, new ObjectParser<ComTaskExecutionGroup[]>(), ComTaskExecutionGroup[].class);
            return CollectionConverter.convertGenericArrayToList(comJobs);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks(OutboundComPort comPort) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
            JSONObject response = post(QueryMethod.FindExecutableOutboundComTasks, queryParameters);
            ComJob[] comJobs = toArrayObject(response, new ObjectParser<ComTaskExecutionGroup[]>(), ComTaskExecutionGroup[].class);
            return CollectionConverter.convertGenericArrayToList(comJobs);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.CURRENT_HIGH_PRIORITY_LOAD, currentHighPriorityLoadPerComPortPool);
            JSONObject response = post(QueryMethod.FindExecutableHighPriorityOutboundComTasks, queryParameters);
            HighPriorityComJob[] comJobs = toArrayObject(response, new ObjectParser<HighPriorityComJob[]>(), HighPriorityComJob[].class);
            return CollectionConverter.convertGenericArrayToList(comJobs);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<HighPriorityComJob> findExecutableHighPriorityOutboundComTasks(OutboundCapableComServer comServer, Map<Long, Integer> currentHighPriorityLoadPerComPortPool, Instant date) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.CURRENT_HIGH_PRIORITY_LOAD, currentHighPriorityLoadPerComPortPool);
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.CURRENT_DATE, Date.from(date));
            JSONObject response = post(QueryMethod.FindExecutableHighPriorityOutboundComTasks, queryParameters);
            HighPriorityComJob[] comJobs = toArrayObject(response, new ObjectParser<HighPriorityComJob[]>(), HighPriorityComJob[].class);
            return CollectionConverter.convertGenericArrayToList(comJobs);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Long> findContainingActiveComPortPoolsForComPort(OutboundComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = post(QueryMethod.FindContainingComPortPoolsForComPort, queryParameters);
        Long[] comPortPoolIds = toArrayObject(response, new ObjectParser<Long[]>(), Long[].class);
        return CollectionConverter.convertGenericArrayToList(comPortPoolIds);
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks(OfflineDevice device, InboundComPort comPort) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE, device.getId());
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
            JSONObject response = post(QueryMethod.FindExecutableInboundComTasks, queryParameters);
            ComTaskExecution[] comTaskExecutions = toArrayObject(response, new ObjectParser<ComTaskExecution[]>(), ComTaskExecution[].class);
            return CollectionConverter.convertGenericArrayToList(comTaskExecutions);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TypedProperties findProtocolDialectPropertiesFor(long comTaskExecutionId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecutionId);
        JSONObject response = post(QueryMethod.FindProtocolDialectPropertiesForComTaskExecution, queryParameters);
        return toObject(response, new ObjectParser<TypedProperties>());
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        JSONObject response = post(QueryMethod.GetDeviceProtocolProperties, queryParameters);
        return toObject(response, new ObjectParser<TypedProperties>());
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask) {
        return connectionTask.getProperties();
    }

    @Override
    public ComTaskEnablement findComTaskEnablementByDeviceAndComTask(DeviceIdentifier deviceIdentifier, long comTaskId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASK, comTaskId);
        JSONObject response = post(QueryMethod.FindComTaskEnablementByDeviceAndComTask, queryParameters);
        return toObject(response, new ObjectParser<ComTaskEnablement>());
    }

    public List<SecurityPropertySet> findAllSecurityPropertySetsForDevice(DeviceIdentifier deviceIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        JSONObject response = post(QueryMethod.FindAllSecurityPropertySetsForDevice, queryParameters);
        SecurityPropertySet[] securityPropertySets = toArrayObject(response, new ObjectParser<SecurityPropertySetImpl[]>(), SecurityPropertySetImpl[].class);
        return CollectionConverter.convertGenericArrayToList(securityPropertySets);
    }

    @Override
    public ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return null; // TODO properly implement using orm's locking mechanism
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return true; // TODO properly implement using orm's locking mechanism
    }

    @Override
    public void unlock(OutboundConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.Unlock, queryParameters);
    }

    @Override
    public boolean attemptLock(ComTaskExecution comTaskExecution, ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = post(QueryMethod.AttemptLock, queryParameters);
        return toBoolean(response);
    }

    @Override
    public boolean attemptLock(PriorityComTaskExecutionLink comTaskExecution, ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.HIGH_PRIORITY_COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return this.toBoolean(response);
    }

    @Override
    public void unlock(ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        this.post(QueryMethod.Unlock, queryParameters);
    }

    @Override
    public ConnectionTask<?, ?> executionStarted(ConnectionTask connectionTask, ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        this.post(QueryMethod.ExecutionStarted, queryParameters);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted(ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionFailed(ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionFailed, queryParameters);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionRescheduled(ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        post(QueryMethod.ExecutionRescheduled, queryParameters);
        return connectionTask;
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return transaction.perform();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LoadProfileIdentifier loadProfileIdentifier) {
        return null;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifierFor(LogBookIdentifier logBookIdentifier) {
        return null;
    }

    @Override
    public PropertyValueType getDeviceProtocolPropertyValueType(DeviceIdentifier deviceIdentifier, String propertyName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME, propertyName);
        JSONObject response = post(QueryMethod.GetDeviceProtocolPropertyValueType, queryParameters);
        return PropertyValueType.valueOf(toString(response));
    }

    @Override
    public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        Map<String, Object> queryParameters = new HashMap<>();
        TypedProperties protocolTypedProperties = TypedProperties.empty();
        protocolTypedProperties.setProperty(propertyName, propertyValue);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME, propertyName);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES, protocolTypedProperties);  // The property is wrapped into TypedProperties, for easier transmit
        post(QueryMethod.UpdateDeviceDialectProperty, queryParameters);
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        Map<String, Object> queryParameters = new HashMap<>();
        TypedProperties protocolTypedProperties = TypedProperties.empty();
        protocolTypedProperties.setProperty(propertyName, propertyValue);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME, propertyName);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES, protocolTypedProperties);  // The property is wrapped into TypedProperties, for easier transmit
        post(QueryMethod.UpdateDeviceSecurityProperty, queryParameters);
    }

    @Override
    public void addTrustedCertificates(List<CollectedCertificateWrapper> collectedCertificates) {
    }

    @Override
    public void addCACertificate(CertificateWrapper certificateWrapper) {
    }

    @Override
    public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        return 0;
    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue, ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        TypedProperties protocolTypedProperties = TypedProperties.empty();
        protocolTypedProperties.setProperty(propertyName, propertyValue);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME, propertyName);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES, protocolTypedProperties);  // The property is wrapped into TypedProperties, for easier transmit
        post(QueryMethod.UpdateDeviceSecurityProperty, queryParameters);
    }

    @Override
    public void activateSecurityAccessorPassiveValue(DeviceIdentifier deviceIdentifier, String propertyName, ComTaskExecution comTaskExecution) {
    }

    @Override
    public Optional<Device> getDeviceFor(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public List<Device> getAllDevicesFor(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public void updateLastReadingFor(LoadProfileIdentifier loadProfileIdentifier, Instant lastReading) {
    }

    @Override
    public void updateLastLogBook(LogBookIdentifier logBookIdentifier, Instant lastLogBook) {
    }

    @Override
    public void updateLastDataSourceReadingsFor(Map<LoadProfileIdentifier, Instant> lastReadings, Map<LogBookIdentifier, Instant> lastLogBooks) {
        Map<String, Object> queryParameters = new HashMap<>();
        if (lastReadings.size() > 0) {
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.LAST_READINGS, lastReadings);
        }
        if (lastLogBooks.size() > 0) {
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.LAST_LOGBOOKS, lastLogBooks);
        }
        post(QueryMethod.UpdateDataSourceReadings, queryParameters);
    }

    @Override
    public void storePathSegments(List<TopologyPathSegment> topologyPathSegment) {

    }

    @Override
    public void storeNeighbours(DeviceIdentifier sourceDeviceIdentifier, List<TopologyNeighbour> topologyNeighbours) {
    }

    @Override
    public void storeG3IdentificationInformation(G3TopologyDeviceAddressInformation topologyDeviceAddressInformation) {

    }

    @Override
    public void updateFirmwareVersions(CollectedFirmwareVersion collectedFirmwareVersions) {

    }

    @Override
    public void updateBreakerStatus(CollectedBreakerStatus collectedBreakerStatus) {

    }

    @Override
    public void updateCreditAmount(CollectedCreditAmount collectedBreakerStatus) {

    }

    @Override
    public void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr) {

    }

    @Override
    public void updateCalendars(CollectedCalendar collectedCalendar) {

    }

    @Override
    public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        if (!isComServerDAOShutDown()) {
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
            JSONObject response = post(QueryMethod.GetInboundComTaskOnHold, queryParameters);
            return toBoolean(response);
        } else {
            return null;
        }
    }

    @Override
    public void cleanupOutdatedComTaskExecutionTriggers() {

    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, inboundComPort.getId());
        JSONObject response = post(QueryMethod.GetDeviceDialectProperties, queryParameters);
        return toObject(response, new ObjectParser<TypedProperties>());
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        JSONObject response = post(QueryMethod.GetOutboundConnectionTypeProperties, queryParameters);
        return toObject(response, new ObjectParser<TypedProperties>());
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolProperties(DeviceIdentifier deviceIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        JSONObject response = post(QueryMethod.GetDeviceProtocolProperties, queryParameters);
        return toObject(response, new ObjectParser<TypedProperties>());
    }

    @Override
    public com.energyict.mdc.upl.offline.OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.OFFLINE_DEVICE_CONTEXT, context);
        JSONObject response = post(QueryMethod.GoOfflineWithContext, queryParameters);
        return toObject(response, new ObjectParser<OfflineDevice>());
    }

    @Override
    public String getDeviceProtocolClassName(DeviceIdentifier identifier) {
        return null;
    }

    @Override
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        this.post(QueryMethod.ExecutionStarted, queryParameters);
    }

    @Override
    public void executionCompleted(ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE, Date.from(rescheduleDate));
        this.post(QueryMethod.ExecutionRescheduled, queryParameters);
    }

    @Override
    public void executionRescheduledToComWindow(ComTaskExecution comTaskExecution, Instant comWindowStartDate) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE, Date.from(comWindowStartDate));
        post(QueryMethod.ExecutionRescheduledToComWindow, queryParameters);
    }

    @Override
    public void executionCompleted(List<? extends ComTaskExecution> comTaskExecutions) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION_COLLECTION, this.collectIds(comTaskExecutions));
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        this.post(QueryMethod.ExecutionFailed, queryParameters);
    }

    @Override
    public void executionFailed(ComTaskExecution comTaskExecution, boolean noRetry) {
        executionFailed(comTaskExecution);
    }

    @Override
    public void executionFailed(List<? extends ComTaskExecution> comTaskExecutions) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION_COLLECTION, this.collectIds(comTaskExecutions));
        this.post(QueryMethod.ExecutionFailed, queryParameters);
    }

    @Override
    public void releaseInterruptedTasks(ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        this.post(QueryMethod.ReleaseInterruptedComTasks, queryParameters);
    }

    @Override
    public TimeDuration releaseTimedOutTasks(ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = this.post(QueryMethod.ReleaseTimedOutComTasks, queryParameters);
        return this.toTimeDuration(response);
    }

    @Override
    public void releaseTasksFor(ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        this.post(QueryMethod.ReleaseComTasks, queryParameters);
    }

    public List<LookupEntry> getCompletionCodeLookupEntries() {
        JSONObject response = post(QueryMethod.GetCompletionCodeLookupEntries, new HashMap<String, Object>());
        LookupEntry[] lookupEntries = toArrayObject(response, new ObjectParser<LookupEntry[]>(), LookupEntry[].class);
        return CollectionConverter.convertGenericArrayToList(lookupEntries);
    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSESSION_BUILDER, builder);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSESSION_STOP_DATE, Date.from(stopDate));
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSESSION_SUCCESS_INDICATOR, successIndicator);
        post(QueryMethod.CreateAndUpdateComSession, queryParameters);
        return null;
    }

    @Override
    public void createOrUpdateDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCacheXmlWrapper cache) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_CACHE, cache);
        post(QueryMethod.CreateOrUpdateDeviceCache, queryParameters);
    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.METER_READING, meterReading);
        post(QueryMethod.StoreMeterReading, queryParameters);
    }


    @Override
    public void storeLoadProfile(LoadProfileIdentifier loadProfileIdentifier, CollectedLoadProfile collectedLoadProfile, Instant currentDate) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOADPROFILE_IDENTIFIER, loadProfileIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COLLECTED_LOADPROFILE, collectedLoadProfile);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CURRENT_DATE, Date.from(currentDate));
        post(QueryMethod.StoreLoadProfile, queryParameters);

    }

    @Override
    public void storeLogBookData(LogBookIdentifier logBookIdentifier, CollectedLogBook collectedLogBook, Instant currentDate) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER, logBookIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COLLECTED_LOGBOOK, collectedLogBook);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CURRENT_DATE, Date.from(currentDate));
        post(QueryMethod.StoreLogBookData, queryParameters);
    }

    @Override
    public void updateLogBookLastReading(LogBookIdentifier logBookIdentifier, Date lastExecutionStartTimestamp) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER, logBookIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOGBOOK_LAST_READING, lastExecutionStartTimestamp);
        post(QueryMethod.UpdateLogBookLastReading, queryParameters);
    }

    @Override
    public void updateLogBookLastReadingFromTask(final LogBookIdentifier logBookIdentifier, final long comTaskExecutionId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER, logBookIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecutionId);
        post(QueryMethod.UpdateLogBookLastReadingFromTask, queryParameters);
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return null;
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, identifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.OFFLINE_DEVICE_CONTEXT, offlineDeviceContext);
        JSONObject response = post(QueryMethod.GoOfflineWithContext, queryParameters);
        return Optional.of(toObject(response, new ObjectParser<OfflineDevice>()));
    }

    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.REGISTER_IDENTIFIER, identifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.WHEN, Date.from(when));
        JSONObject response = post(QueryMethod.FindOfflineRegister, queryParameters);
        return Optional.of(toObject(response, new ObjectParser<OfflineRegister>()));
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOADPROFILE_IDENTIFIER, loadProfileIdentifier);
        JSONObject response = post(QueryMethod.FindOfflineLoadProfile, queryParameters);
        return Optional.of(toObject(response, new ObjectParser<OfflineLoadProfile>()));
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.LOGBOOK_IDENTIFIER, logBookIdentifier);
        JSONObject response = post(QueryMethod.FindOfflineLogBook, queryParameters);
        return Optional.of(toObject(response, new ObjectParser<OfflineLogBook>()));
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.MESSAGE_IDENTIFIER, identifier);
        JSONObject response = post(QueryMethod.FindOfflineDeviceMessage, queryParameters);
        return Optional.of(toObject(response, new ObjectParser<OfflineDeviceMessage>()));
    }

    @Override
    public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
    }

    @Override
    public void updateConnectionTaskProperties(ConnectionTask connectionTask, Map<String, Object> connectionPropertyNameAndValue) {
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        Map<String, Object> queryParameters = new HashMap<>();
        TypedProperties protocolTypedProperties = TypedProperties.empty();
        protocolTypedProperties.setProperty(propertyName, propertyValue);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_PROPERTY_NAME, propertyName);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.PROTOCOL_TYPED_PROPERTIES, protocolTypedProperties);  // The property is wrapped into TypedProperties, for easier transmit
        post(QueryMethod.UpdateDeviceProtocolProperty, queryParameters);
    }

    @Override
    public void updateGateway(DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.GATEWAY_DEVICE_IDENTIFIER, gatewayDeviceIdentifier);
        post(QueryMethod.UpdateGateway, queryParameters);
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DATE_FORMAT, timeStampFormat.toString());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.FILE_NAME, fileName);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.FILE_EXTENSION, fileExtension);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.HEX_DATA, getHexStringFromBytes(contents));
        post(QueryMethod.StoreConfigurationFile, queryParameters);
    }

    @Override
    public void signalEvent(String topic, Object source) {
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.MESSAGE_IDENTIFIER, messageIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.MESSAGE_STATUS, newDeviceMessageStatus);
        if (sentDate != null) {
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.SENT_DATE, Date.from(sentDate));
        }
        if (protocolInformation != null) {
            queryParameters.put(RemoteComServerQueryJSonPropertyNames.MESSAGE_INFORMATION, protocolInformation);
        }
        post(QueryMethod.UpdateDeviceMessageInformation, queryParameters);
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.MESSAGE_CONFIRMATION_COUNT, confirmationCount);
        JSONObject response = post(QueryMethod.ConfirmSentMessagesAndGetPending, queryParameters);
        OfflineDeviceMessage[] offlineDeviceMessages = toArrayObject(response, new ObjectParser<OfflineDeviceMessage[]>(), OfflineDeviceMessage[].class);
        return CollectionConverter.convertGenericArrayToList(offlineDeviceMessages);
    }

    @Override
    public List<DeviceMasterDataExtractor.SecurityProperty> getPropertiesFromSecurityPropertySet(DeviceIdentifier deviceIdentifier, Long securityPropertySetId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.SECURITY_PROPERTY_SET_IDENTIFIER, securityPropertySetId);
        JSONObject response = post(QueryMethod.GetPropertiesFromSecurityPropertySet, queryParameters);
        DeviceMasterDataExtractor.SecurityProperty[] securityProperties = toArrayObject(response, new ObjectParser<DeviceMasterDataExtractor.SecurityProperty[]>(), DeviceMasterDataExtractor.SecurityProperty[].class);
        return CollectionConverter.convertGenericArrayToList(securityProperties);
    }

    @Override
    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.DEVICE_IDENTIFIER, deviceIdentifier);
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, inboundComPort.getId());
        JSONObject response = post(QueryMethod.GetDeviceConnectionTypeProperties, queryParameters);
        return toObject(response, new ObjectParser<TypedProperties>());
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceProtocolSecurityProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public boolean isStillPending(long comTaskExecutionId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecutionId);
        JSONObject response = this.post(QueryMethod.IsStillPending, queryParameters);
        return this.toBoolean(response);
    }

    @Override
    public boolean areStillPending(Collection<Long> comTaskExecutionIds) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION_COLLECTION, comTaskExecutionIds);
        JSONObject response = this.post(QueryMethod.AreStillPending, queryParameters);
        return this.toBoolean(response);
    }

    @Override
    public boolean areStillPendingWithHighPriority(Collection<Long> priorityComTaskExecutionIds) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.HIGH_PRIORITY_COMTASKEXECUTION_COLLECTION, priorityComTaskExecutionIds);
        JSONObject response = post(QueryMethod.AreStillPending, queryParameters);
        return toBoolean(response);
    }

    private ComServer toComServer(JSONObject response) {
        try {
            return new ComServerParser().parse(response);
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private TimeDuration toTimeDuration(JSONObject response) {
        try {
            return new TimeDurationParser().parse(response);
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private boolean toBoolean(JSONObject response) {
        try {
            return new BooleanParser().parse(response);
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private <T> T toObject(JSONObject response, ObjectParser<T> objectParser, Class clazz) {
        try {
            return response != null
                    ? objectParser.parseObject(response, RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT, clazz)
                    : null;
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private <T> T toObject(JSONObject response, ObjectParser<T> objectParser) {
        return toObject(response, objectParser, null);
    }

    private <T> T toArrayObject(JSONObject response, ObjectParser<T> objectParser, Class clazz) {
        try {
            return response != null
                    ? objectParser.parseArray(response, RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT, clazz)
                    : null;
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private JSONObject post(QueryMethod queryMethod) {
        return this.post(queryMethod, new HashMap<String, Object>(0));
    }

    private JSONObject post(QueryMethod queryMethod, Map<String, Object> queryParameters) {
        try {
            QuerySpecs querySpecs = this.newQuerySpecs(queryMethod, queryParameters);
            Query query = this.queries.newQueryFor(querySpecs);
            this.post(query);
            return query.getResponse().get(timeout, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_WEBSOCKET_ERROR);
        } catch (TimeoutException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_WEBSOCKET_ERROR);
        } catch (InterruptedException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_WEBSOCKET_ERROR); // TODO thread safety hazard : this is not responsive to interruption
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private QuerySpecs newQuerySpecs(QueryMethod method, Map<String, Object> queryParameters) throws JSONException {
        QuerySpecs querySpecs = new QuerySpecs();
        querySpecs.put(RemoteComServerQueryJSonPropertyNames.METHOD, method.name());
        for (Map.Entry<String, Object> paramSpec : queryParameters.entrySet()) {
            querySpecs.put(paramSpec.getKey(), paramSpec.getValue());
        }
        return querySpecs;
    }

    private void post(Query query) throws IOException, JSONException {
        this.webSocket.post(query);
    }

    @Override
    public ServerProcessStatus getStatus() {
        return this.status;
    }

    @Override
    public void start() {
        try {
            connectClient(TimeUnit.SECONDS.toMillis(5));
            status = ServerProcessStatus.STARTED;
        } catch (URISyntaxException e) {
            throw CodingException.validationFailed(OnlineComServer.class, "queryAPIPostUri", MessageSeeds.VALIDATION_FAILED);
        } catch (Exception e) {
            throw new ApplicationException("Unable to start connection with online comserver at " + queryAPIPostUri, e);
        }
    }

    private void connectClient(long connectionTimeout) throws Exception {
        WebSocketClient webSocketClient = new WebSocketClient();
        webSocketClient.setConnectTimeout(TimeUnit.SECONDS.toMillis(connectionTimeout));
        webSocketClient.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        Future<Session> future = webSocketClient.connect(webSocket, new URI(queryAPIPostUri), request);
        future.get();
    }

    @Override
    public void shutdown() {
        this.webSocket.disconnect();
    }

    @Override
    public void shutdownImmediate() {
        this.shutdown();
    }

    private void webSocketClosed() {
        if (this.comServer != null) {
            this.comServer.shutdownImmediate();
        }
    }

    private Collection<Long> collectIds(List<? extends HasId> businessObjects) {
        Collection<Long> ids = new ArrayList<>(businessObjects.size());
        for (HasId businessObject : businessObjects) {
            ids.add((long) businessObject.getId());
        }
        return ids;
    }

    private class QueryWebSocket implements WebSocketListener {

        private Optional<Session> oSession = Optional.empty();

        public QueryWebSocket() {
        }

        public boolean isOpen() {
            return oSession.isPresent() && oSession.get().isOpen();
        }

        public void disconnect() {
            oSession.ifPresent(Session::close);
        }

        public synchronized void post(Query query) throws JSONException, IOException {
            if (oSession.isPresent()) {
                String serializedText = query.getSpecs().marshall();
                Session session = oSession.get();
                if (compressData) {
                    byte[] compressedData = DataCompressor.encodeAndCompress(serializedText, true);
                    session.getRemote().sendBytes(ByteBuffer.wrap(compressedData));
                } else {
                    session.getRemote().sendString(serializedText);
                }
            } else {
                throw new DataAccessException(MessageSeeds.WEBSOCKET_CLOSED);
            }
        }

        @Override
        public void onWebSocketBinary(byte[] bytes, int i, int i1) {
            try {
                String decompressedData = DataCompressor.decompressAndDecode(ProtocolUtils.getSubArray2(bytes, i, i + i1), true);
                onWebSocketText(decompressedData);
            } catch (IOException e) {
                throw new ApplicationException("Unable to parse reponse from OnlineComServer", e);
            }
        }

        @Override
        public void onWebSocketText(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                String queryId = String.valueOf(jsonObject.get(RemoteComServerQueryJSonPropertyNames.QUERY_ID));
                Query query = queries.findAndRemove(queryId);
                query.getResponse().setValue(jsonObject);
            } catch (JSONException e) {
                throw new ApplicationException("Unable to parse reponse from OnlineComServer", e);
            }
        }

        @Override
        public void onWebSocketConnect(Session session) {
            this.oSession = Optional.of(session);
        }

        @Override
        public void onWebSocketClose(int i, String s) {
            this.oSession = Optional.empty();
            webSocketClosed();
        }

        @Override
        public void onWebSocketError(Throwable throwable) {
        }
    }

    private class QueryResponse implements Future<JSONObject> {
        private JSONObject value;
        private boolean done = false;

        public synchronized void setValue(JSONObject value) {
            this.value = value;
            this.done = true;
            notifyAll();
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public synchronized JSONObject get() throws InterruptedException, ExecutionException {
            while (value == null) {
                wait();
            }
            return value;
        }

        @Override
        public synchronized JSONObject get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            long end = System.nanoTime() + unit.toNanos(timeout);
            while (value == null) {
                if (System.nanoTime() > end) {
                    throw new TimeoutException("Didn't receive a response after " + unit.toMillis(timeout) + " ms");
                }
                wait(100);
            }
            return value;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

    }

    private final class Query {
        private final QuerySpecs specs;
        private final QueryResponse response = new QueryResponse();

        private Query(QuerySpecs specs) {
            super();
            this.specs = specs;
        }

        public QuerySpecs getSpecs() {
            return specs;
        }

        public QueryResponse getResponse() {
            return response;
        }
    }

    private final class QuerySpecs {

        private Map<String, Object> specs;

        private QuerySpecs() {
            specs = new HashMap<>();
        }

        public void put(String specName, Object specValue) {
            specs.put(specName, specValue);
        }

        /**
         * Do JSON marshalling of all specs
         *
         * @return the JSON marshalled string representation
         * @throws JSONException writeValue went wrong...
         */
        public String marshall() throws JSONException {
            StringWriter writer = new StringWriter();
            ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
            try {
                mapper.writeValue(writer, specs);
            } catch (IOException e) {
                throw new JSONException(e);
            }
            return writer.toString();
        }
    }

    private final class QueryList {
        private long nextId = 1;
        private Map<String, Query> queriesInProgress = new HashMap<>();

        public synchronized Query newQueryFor(QuerySpecs querySpecs) throws JSONException {
            long queryId = this.nextId++;
            Principal principal = serviceProvider.threadPrincipalService().getPrincipal();
            Query query = new Query(querySpecs);
            this.queriesInProgress.put(String.valueOf(queryId), query);
            querySpecs.put(RemoteComServerQueryJSonPropertyNames.QUERY_ID, queryId);
            if (principal instanceof User) {
                querySpecs.put(RemoteComServerQueryJSonPropertyNames.USER_ID, ((User) principal).getId());
            }
            return query;
        }

        public synchronized Query findAndRemove(String queryId) {
            Query query = this.queriesInProgress.get(queryId);
            if (query != null) {
                this.queriesInProgress.remove(queryId);
            }
            return query;
        }

    }

    @Override
    public List<Pair<OfflineLoadProfile, Range<Instant>>> getStorageLoadProfileIdentifiers(OfflineLoadProfile loadProfile, String readingTypeMRID, Range<Instant> dataPeriod) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public User getComServerUser() {
        return null;
    }

    private String getSystemName() {
        String systemName = System.getProperty(ComServer.SYSTEM_NAME_PROPERTY);
        if (systemName == null) {
            systemName = HostName.getCurrent();
        }
        return systemName;
    }

    private boolean isComServerDAOShutDown() {
        return getStatus().equals(ServerProcessStatus.SHUTTINGDOWN) || getStatus().equals(ServerProcessStatus.SHUTDOWN);
    }

    private boolean isWebSocketConnectionValid() {
        return getWebSocket().isOpen();
    }

    protected void setWebSocket(QueryWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    private QueryWebSocket getWebSocket() {
        return webSocket;
    }

    private String toString(JSONObject response) {
        try {
            return response.getString(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT);
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private String getHexStringFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public interface ServiceProvider {

        Clock clock();

        EngineConfigurationService engineConfigurationService();

        ThreadPrincipalService threadPrincipalService();
    }
}
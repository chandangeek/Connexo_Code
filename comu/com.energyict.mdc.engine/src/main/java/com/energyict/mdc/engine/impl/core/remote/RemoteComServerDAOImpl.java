/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.PropertyValueType;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.G3TopologyDeviceAddressInformation;
import com.energyict.mdc.upl.meterdata.TopologyNeighbour;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
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

import com.google.common.collect.Range;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides an implementation for the {@link ComServerDAO} interface
 * that will post a JSon representation of the query
 * to a companion {@link com.energyict.mdc.engine.config.OnlineComServer}
 * that has a servlet running that will listen for these queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:56)
 */
public class RemoteComServerDAOImpl implements ComServerDAO {

    public interface ServiceProvider {

        Clock clock();

        EngineConfigurationService engineConfigurationService();

    }
    public final static String CLIENT_PROPERTY = "client-name";

    private ServerProcess comServer;
    private String queryAPIPostUri;
    private final ServiceProvider serviceProvider;
    private ServerProcessStatus status = ServerProcessStatus.STARTING;
    private QueryWebSocket webSocket = new QueryWebSocket();
    private final QueryList queries = new QueryList();

    public RemoteComServerDAOImpl(String queryAPIPostUri, ServiceProvider serviceProvider) {
        super();
        this.queryAPIPostUri = queryAPIPostUri;
        this.serviceProvider = serviceProvider;
    }

    public void setComServer (ServerProcess comServer) {
        this.comServer = comServer;
    }

    @Override
    public ComServer getThisComServer () {
        JSONObject response = this.post(QueryMethod.GetThisComServer);
        return this.toComServer(response);
    }

    @Override
    public ComServer getComServer (String hostName) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.HOSTNAME, hostName);
        JSONObject response = this.post(QueryMethod.GetComServer, queryParameters);
        return this.toComServer(response);
    }

    @Override
    public ComServer refreshComServer (ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, comServer.getModificationDate());
        JSONObject response = this.post(QueryMethod.RefreshComServer, queryParameters);
        return this.toComServer(response);
    }

    @Override
    public ComPort refreshComPort(ComPort comPort) {
        //TODO port remote comserver from 9.1
        return null;
    }

    @Override
    public List<ComJob> findExecutableOutboundComTasks (OutboundComPort comPort) {
        return null;
    }

    @Override
    public List<ComTaskExecution> findExecutableInboundComTasks (OfflineDevice device, InboundComPort comPort) {
        return null;
    }

    @Override
    public List<ConnectionTaskProperty> findProperties(ConnectionTask connectionTask) {
        // Todo: delegate to the other side
        return connectionTask.getProperties();
    }

    @Override
    public ScheduledConnectionTask attemptLock (ScheduledConnectionTask connectionTask, ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return null; // TODO properly implement using orm's locking mechanism
    }

    @Override
    public boolean attemptLock(OutboundConnectionTask connectionTask, ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return true; // TODO properly implement using orm's locking mechanism
    }

    @Override
    public void unlock (OutboundConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.Unlock, queryParameters);
    }

    @Override
    public boolean attemptLock (ComTaskExecution comTaskExecution, ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return this.toBoolean(response);
    }

    @Override
    public void unlock (ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        this.post(QueryMethod.Unlock, queryParameters);
    }

    @Override
    public ConnectionTask<?, ?> executionStarted (ConnectionTask connectionTask, ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        this.post(QueryMethod.ExecutionStarted, queryParameters);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionCompleted (ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
        return connectionTask;
    }

    @Override
    public ConnectionTask<?, ?> executionFailed (ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionFailed, queryParameters);
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
        return null;
    }

    @Override
    public void updateDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {

    }

    @Override
    public void updateDeviceSecurityProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {

    }

    @Override
    public void addCACertificate(CertificateWrapper certificateWrapper) {

    }

    @Override
    public long addEndDeviceCertificate(CollectedCertificateWrapper collectedCertificateWrapper) {
        return 0;
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
    public void updateDeviceCSR(DeviceIdentifier deviceIdentifier, String certificateType, String csr) {

    }

    @Override
    public void updateCalendars(CollectedCalendar collectedCalendar) {

    }

    @Override
    public Boolean getInboundComTaskOnHold(DeviceIdentifier deviceIdentifier, InboundComPort comPort) {
        return null;
    }

    @Override
    public void cleanupOutdatedComTaskExecutionTriggers() {

    }

    @Override
    public void executionStarted(ComTaskExecution comTaskExecution, ComPort comPort, boolean executeInTransaction) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        this.post(QueryMethod.ExecutionStarted, queryParameters);
    }

    @Override
    public void executionCompleted (ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
    }

    @Override
    public void executionRescheduled(ComTaskExecution comTaskExecution, Instant rescheduleDate) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.RESCHEDULE_DATE, rescheduleDate);
        this.post(QueryMethod.ExecutionRescheduled, queryParameters);
    }

    @Override
    public void executionCompleted (List<? extends ComTaskExecution> comTaskExecutions) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION_COLLECTION, this.collectIds(comTaskExecutions));
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
    }

    @Override
    public void executionFailed (ComTaskExecution comTaskExecution) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecution.getId());
        this.post(QueryMethod.ExecutionFailed, queryParameters);
    }

    @Override
    public void executionFailed (List<? extends ComTaskExecution> comTaskExecutions) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION_COLLECTION, this.collectIds(comTaskExecutions));
        this.post(QueryMethod.ExecutionFailed, queryParameters);
    }

    @Override
    public void releaseInterruptedTasks (ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        this.post(QueryMethod.ReleaseInterruptedComTasks, queryParameters);
    }

    @Override
    public TimeDuration releaseTimedOutTasks (ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        JSONObject response = this.post(QueryMethod.ReleaseTimedOutComTasks, queryParameters);
        return this.toTimeDuration(response);
    }

    @Override
    public void releaseTasksFor(ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        this.post(QueryMethod.ReleaseComTasks, queryParameters);
    }

    @Override
    public ComSession createComSession(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        return builder.endSession(stopDate, successIndicator).create();
    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        // Todo
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineDevice> findOfflineDevice(DeviceIdentifier identifier, OfflineDeviceContext offlineDeviceContext) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineRegister> findOfflineRegister(RegisterIdentifier identifier, Instant when) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineLoadProfile> findOfflineLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineLogBook> findOfflineLogBook(LogBookIdentifier logBookIdentifier) {
        return Optional.empty();
    }

    @Override
    public Optional<OfflineDeviceMessage> findOfflineDeviceMessage(MessageIdentifier identifier) {
        return Optional.empty();
    }

    @Override
    public void updateConnectionTaskProperty(Object propertyValue, ConnectionTask connectionTask, String connectionTaskPropertyName) {
    }

    @Override
    public void updateDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {

    }

    @Override
    public void updateGateway (DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
    }

    @Override
    public void storeConfigurationFile(DeviceIdentifier deviceIdentifier, DateTimeFormatter timeStampFormat, String fileName, String fileExtension, byte[] contents) {
    }

    @Override
    public void signalEvent(String topic, Object source) {
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return null;
    }

    @Override
    public DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        /* Todo: do not forget to take into account that DeviceIdentifier implementation classes
         * throw a NotFoundException when the device does not exist. */
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceDialectProperties(DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        /* Todo: do not forget to take into account that DeviceIdentifier implementation classes
         * throw a NotFoundException when the device does not exist. */
        return null;
    }

    @Override
    public TypedProperties getOutboundConnectionTypeProperties(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public TypedProperties getDeviceProtocolProperties (DeviceIdentifier deviceIdentifier) {
        /* Todo: do not forget to take into account that DeviceIdentifier implementation classes
         * throw a NotFoundException when the device does not exist. */
        return null;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getDeviceLocalProtocolProperties(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.offline.OfflineDevice getOfflineDevice(DeviceIdentifier deviceIdentifier, OfflineDeviceContext context) {
        return null;
    }

    @Override
    public String getDeviceProtocolClassName(DeviceIdentifier identifier) {
        return null;
    }

    @Override
    public boolean isStillPending (long comTaskExecutionId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, comTaskExecutionId);
        JSONObject response = this.post(QueryMethod.IsStillPending, queryParameters);
        return this.toBoolean(response);
    }

    @Override
    public boolean areStillPending (Collection<Long> comTaskExecutionIds) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION_COLLECTION, comTaskExecutionIds);
        JSONObject response = this.post(QueryMethod.AreStillPending, queryParameters);
        return this.toBoolean(response);
    }

    private ComServer toComServer (JSONObject response) {
        try {
            return new ComServerParser().parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
    }

    private ComPort toComPort (JSONObject response) {
        try {
            return new ComPortParser().parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
    }

    private TimeDuration toTimeDuration (JSONObject response) {
        try {
            return new TimeDurationParser().parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
    }

    private boolean toBoolean (JSONObject response) {
        try {
            return new BooleanParser().parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
    }

    private JSONObject post (QueryMethod queryMethod) {
        return this.post(queryMethod, new HashMap<String, Object>(0));
    }

    private JSONObject post (QueryMethod queryMethod, Map<String, Object> queryParameters) {
        try {
            JSONObject querySpecs = this.newQuerySpecs(queryMethod, queryParameters);
            Query query = this.queries.newQueryFor(querySpecs);
            this.post(query);
            return query.getResponse().get(5, TimeUnit.MINUTES);
        }
        catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
        catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
        catch (InterruptedException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR); // TODO thread safety hazard : this is not responsive to interruption
        }
        catch (ExecutionException e) {
            throw new DataAccessException(e, MessageSeeds.UNEXPECTED_SQL_ERROR);
        }
    }

    private JSONObject newQuerySpecs (QueryMethod method, Map<String, Object> queryParameters) throws JSONException {
        JSONObject querySpecs = new JSONObject();
        querySpecs.put(RemoteComServerQueryJSonPropertyNames.METHOD, method.name());
        for (Map.Entry<String, Object> paramSpec : queryParameters.entrySet()) {
            querySpecs.put(paramSpec.getKey(), paramSpec.getValue());
        }
        return querySpecs;
    }

    private void post (Query query) throws IOException {
        this.webSocket.post(query);
    }

    @Override
    public ServerProcessStatus getStatus () {
        return this.status;
    }

    @Override
    public void start () {
        try {
            WebSocketClient webSocketClient = new WebSocketClient();
            webSocketClient.setConnectTimeout(TimeUnit.SECONDS.toMillis( 5));
            webSocketClient.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            Future<Session> future = webSocketClient.connect(webSocket, new URI(queryAPIPostUri), request);
            future.get();

            this.status = ServerProcessStatus.STARTED;
        }
        catch (URISyntaxException e) {
            throw CodingException.validationFailed(OnlineComServer.class, "queryAPIPostUri", MessageSeeds.VALIDATION_FAILED);
        }
        catch (Exception e) {
            throw new ApplicationException("Unable to start connection with online comserver at " + this.queryAPIPostUri, e);
        }
    }

    @Override
    public void shutdown () {
        this.webSocket.disconnect();
    }

    @Override
    public void shutdownImmediate () {
        this.shutdown();
    }

    private void webSocketClosed () {
        this.comServer.shutdownImmediate();
    }

    private Collection<Integer> collectIds (List<? extends HasId> businessObjects) {
        Collection<Integer> ids = new ArrayList<>(businessObjects.size());
        for (HasId businessObject : businessObjects) {
            ids.add((int) businessObject.getId());
        }
        return ids;
    }


    private class QueryWebSocket  {

        private Session session;

        public QueryWebSocket() {
        }

        public QueryWebSocket(Session session) {
            this.session = session;
        }

        @OnWebSocketConnect
        public void onOpen(Session session) {
            this.session = session;
        }

        @OnWebSocketClose
        public void onClose(int closeCode, String message) {
            session = null;
            //Note that after this, the recovery mechanism will try to setup a new connection to the online ComServer
        }

        public void disconnect () {
            if (this.session != null) {
                this.session.close();
            }
        }

        public void post (Query query) throws IOException {
            this.session.getRemote().sendString(query.getSpecs().toString());
        }

        @OnWebSocketMessage
        public void onMessage(byte[] data, int offset, int length) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String queryId = String.valueOf(jsonObject.get(RemoteComServerQueryJSonPropertyNames.QUERY_ID));
                Query query = queries.findAndRemove(queryId);
                query.getResponse().setValue(jsonObject);
            }
            catch (JSONException e) {
                throw new ApplicationException("Unable to parse reponse from OnlineComServer", e);
            }
        }
    }

    private class QueryResponse implements Future<JSONObject> {
        private Lock lock = new ReentrantLock();
        private JSONObject value;
        private boolean done = false;
        private Condition condition = lock.newCondition();

        public JSONObject getValue () {
            return value;
        }

        public void setValue (JSONObject value) {
            lock.lock();
            try {
                this.value = value;
                this.done = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean isDone () {
            lock.lock();
            try {
                return this.done;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public JSONObject get () throws InterruptedException, ExecutionException {
            lock.lock();
            try {
                while (!done) {
                    condition.await();
                }
            } finally {
                lock.unlock();
            }
            return this.getValue();
        }

        @Override
        public JSONObject get (long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
            lock.lock();
            try {
                boolean timedOut = false;
                while (!done || !timedOut) {
                    timedOut = !condition.await(timeout, unit);
                }
                return this.getValue();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean isCancelled () {
            return false;
        }

        @Override
        public boolean cancel (boolean mayInterruptIfRunning) {
            return false;
        }

    }

    private final class Query {
        private final JSONObject specs;
        private final QueryResponse response = new QueryResponse();

        private Query (JSONObject specs) {
            super();
            this.specs = specs;
        }

        public JSONObject getSpecs () {
            return specs;
        }

        public QueryResponse getResponse () {
            return response;
        }
    }

    private final class QueryList {
        private long nextId = 1;
        private Map<String, Query> queriesInProgress = new HashMap<>();

        public synchronized Query newQueryFor (JSONObject querySpecs) throws JSONException {
            long queryId = this.nextId++;
            Query query = new Query(querySpecs);
            this.queriesInProgress.put(String.valueOf(queryId), query);
            querySpecs.put(RemoteComServerQueryJSonPropertyNames.QUERY_ID, queryId);
            return query;
        }

        public synchronized Query findAndRemove (String queryId) {
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
}
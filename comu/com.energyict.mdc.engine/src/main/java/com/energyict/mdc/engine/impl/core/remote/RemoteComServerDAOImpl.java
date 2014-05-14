package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.transaction.Transaction;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides an implementation for the {@link ComServerDAO} interface
 * that will post a JSon representation of the query
 * to a companion {@link com.energyict.mdc.engine.model.OnlineComServer}
 * that has a servlet running that will listen for these queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:56)
 */
public class RemoteComServerDAOImpl implements ComServerDAO {

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
    public ComPort refreshComPort (ComPort comPort) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, comPort.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, comPort.getModificationDate());
        JSONObject response = this.post(QueryMethod.RefreshComPort, queryParameters);
        return this.toComPort(response);
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
    public ScheduledConnectionTask attemptLock (ScheduledConnectionTask connectionTask, ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        JSONObject response = this.post(QueryMethod.AttemptLock, queryParameters);
        return null; // TODO properly implement using orm's locking mechanism
    }

    @Override
    public void unlock (ScheduledConnectionTask connectionTask) {
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
    public void executionStarted (ConnectionTask connectionTask, ComServer comServer) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, comServer.getId());
        this.post(QueryMethod.ExecutionStarted, queryParameters);
    }

    @Override
    public void executionCompleted (ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionCompleted, queryParameters);
    }

    @Override
    public void executionFailed (ConnectionTask connectionTask) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionFailed, queryParameters);
    }

    @Override
    public void setMaxNumberOfTries(ScheduledConnectionTask connectionTask, int maxNumberOfTries) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, connectionTask.getId());
        this.post(QueryMethod.ExecutionFailed, queryParameters);
    }

    @Override
    public <T> T executeTransaction(Transaction<T> transaction) {
        return null;
    }

    @Override
    public void executionStarted (ComTaskExecution comTaskExecution, ComPort comPort) {
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

//    @Override
//    public ComSession createOutboundComSession (ScheduledConnectionTask owner, ComSessionShadow shadow) {
//        return null;
//    }
//
//    @Override
//    public ComSession createInboundComSession (InboundConnectionTask owner, ComSessionShadow shadow) {
//        return null;
//    }

//    private EndDeviceCache createOrUpdateDeviceCache(int deviceId, DeviceCacheShadow shadow) {
//        return null;
//    }

    @Override
    public void storeMeterReadings(DeviceIdentifier deviceIdentifier, MeterReading meterReading) {
        // Not storing meterReadingData in mock mode
    }

    @Override
    public OfflineDevice findDevice (DeviceIdentifier identifier) {
        return null;
    }

    @Override
    public OfflineRegister findRegister(RegisterIdentifier identifier) {
        return null;
    }

//    @Override
//    public OfflineDeviceMessage findDeviceMessage(MessageIdentifier identifier) {
//        return null;
//    }

    @Override
    public void updateIpAddress (String ipAddress, ConnectionTask connectionTask, String connectionTaskPropertyName) {
    }

    @Override
    public void updateGateway (DeviceIdentifier deviceIdentifier, DeviceIdentifier gatewayDeviceIdentifier) {
    }

    @Override
    public void storeConfigurationFile (DeviceIdentifier deviceIdentifier, DateFormat timeStampFormat, String fileExtension, byte[] contents) {
    }

    @Override
    public void signalEvent(String topic, Object source) {
    }

    @Override
    public void updateDeviceMessageInformation(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, String protocolInformation) {
    }

    @Override
    public List<OfflineDeviceMessage> confirmSentMessagesAndGetPending(DeviceIdentifier deviceIdentifier, int confirmationCount) {
        return null;
    }

    @Override
    public List<SecurityProperty> getDeviceProtocolSecurityProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        /* Todo: do not forget to take into account that DeviceIdentifier implementation classes
         * throw a NotFoundException when the device does not exist. */
        return null;
    }

    @Override
    public TypedProperties getDeviceConnectionTypeProperties (DeviceIdentifier deviceIdentifier, InboundComPort inboundComPort) {
        /* Todo: do not forget to take into account that DeviceIdentifier implementation classes
         * throw a NotFoundException when the device does not exist. */
        return null;
    }

    @Override
    public TypedProperties getDeviceProtocolProperties (DeviceIdentifier deviceIdentifier) {
        /* Todo: do not forget to take into account that DeviceIdentifier implementation classes
         * throw a NotFoundException when the device does not exist. */
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
            return new ComServerParser(this.serviceProvider.engineModelService()).parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e);
        }
    }

    private ComPort toComPort (JSONObject response) {
        try {
            return new ComPortParser(this.serviceProvider.engineModelService()).parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e);
        }
    }

    private TimeDuration toTimeDuration (JSONObject response) {
        try {
            return new TimeDurationParser().parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e);
        }
    }

    private boolean toBoolean (JSONObject response) {
        try {
            return new BooleanParser().parse(response);
        }
        catch (JSONException e) {
            throw new DataAccessException(e);
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
            throw new DataAccessException(e);
        }
        catch (IOException e) {
            throw new DataAccessException(e);
        }
        catch (InterruptedException e) {
            throw new DataAccessException(e);
        }
        catch (ExecutionException e) {
            throw new DataAccessException(e);
        }
        catch (TimeoutException e) {
            throw new DataAccessException(e);
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
            WebSocketClientFactory factory = new WebSocketClientFactory();
            factory.start();
            WebSocketClient webSocketClient = factory.newWebSocketClient();
            webSocketClient.open(new URI(this.queryAPIPostUri), this.webSocket, 5, TimeUnit.SECONDS);
            this.status = ServerProcessStatus.STARTED;
        }
        catch (URISyntaxException e) {
            throw CodingException.validationFailed(OnlineComServer.class, "queryAPIPostUri");
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

    private class QueryWebSocket implements WebSocket.OnTextMessage {

        private Connection connection;

        @Override
        public void onOpen (Connection connection) {
            this.connection = connection;
        }

        @Override
        public void onClose (int closeCode, String message) {
            this.connection = null;
            webSocketClosed();
        }

        public void disconnect () {
            if (this.connection != null) {
                this.connection.close();
            }
        }

        public void post (Query query) throws IOException {
            this.connection.sendMessage(query.getSpecs().toString());
        }

        @Override
        public void onMessage (String data) {
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
        private JSONObject value;
        private boolean done = false;

        public JSONObject getValue () {
            return value;
        }

        public void setValue (JSONObject value) {
            this.value = value;
            this.done = true;
            this.notifyAll();
        }

        @Override
        public boolean isDone () {
            return this.done;
        }

        @Override
        public JSONObject get () throws InterruptedException, ExecutionException {
            this.wait();
            return this.getValue();
        }

        @Override
        public JSONObject get (long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            this.wait(unit.toMillis(timeout));
            return this.getValue();
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

}
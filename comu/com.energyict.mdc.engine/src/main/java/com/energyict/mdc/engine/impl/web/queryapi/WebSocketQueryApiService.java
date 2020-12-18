package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.JSONTypeMapperProvider;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.exceptions.NoMoreHighPriorityTasksCanBePickedUpRuntimeException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.remote.ObjectMapperFactory;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.engine.impl.core.remote.RemoteJSONTypeMapperProvider;
import com.energyict.mdc.engine.impl.core.remote.RemoteProperties;
import com.energyict.mdc.engine.impl.tools.ProtocolUtils;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Services clients of the remote query API.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-09 (11:11)
 */
@WebSocket
public class WebSocketQueryApiService {

    public static final String ERROR_PREFIX = "Error: ";
    public static final String FAILURE_TO_DECODE_REQUEST = "failure to decompress and decode request: ";
    public static final String MESSAGE_NOT_UNDERSTOOD = "message not understood: ";
    public static final int DEFAULT_MAX_BINARY_MESSAGE_SIZE = 500000;


    private final Logger logger;
    private Session connection;
    private QueryAPIStatistics queryAPIStatistics;
    private QueryMethod.ServiceProvider serviceProvider;
    private RunningOnlineComServer runningOnlineComServer;

    public WebSocketQueryApiService(RunningOnlineComServer comServer, QueryMethod.ServiceProvider serviceProvider, QueryAPIStatistics queryAPIStatistics) {
        this(comServer);
        this.serviceProvider = serviceProvider;
        this.queryAPIStatistics = queryAPIStatistics;
    }

    public WebSocketQueryApiService(RunningOnlineComServer comServer, ComServerDAO comServerDAO, EngineConfigurationService engineConfigurationService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, TransactionService transactionService,  ThreadPrincipalService threadPrincipalService, UserService userService) {
        this(comServer);
        this.serviceProvider = new ServiceProvider(comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService, threadPrincipalService, userService);
        JSONTypeMapperProvider.instance.set(new RemoteJSONTypeMapperProvider());
    }

    protected WebSocketQueryApiService(RunningOnlineComServer comServer) {
        this.runningOnlineComServer = comServer;
        this.logger = Logger.getLogger(getClass().getName());
    }

    protected WebSocketQueryApiService(RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        this.runningOnlineComServer = comServer;
        this.logger = Logger.getLogger(getClass().getName());
        this.queryAPIStatistics = queryAPIStatistics;
    }

    public OnlineComServer getOnlineComServer() {
        return runningOnlineComServer.getComServer();
    }

    /**
     * Parse the incoming data, execute the proper method.
     * In case of unexpected errors, send them to the other side so they don't have a timeout.
     */
    @OnWebSocketMessage
    public void onMessage(byte[] data, int offset, int length) {
       /* try {
            String decompressedData = DataCompressor.decompressAndDecode(ProtocolUtils.getSubArray2(data, offset, offset + length), getOnlineComServer().isCompressingEnabled());
            onMessage(decompressedData);
        } catch (IOException e) {
            sendMessage(ERROR_PREFIX + FAILURE_TO_DECODE_REQUEST + e.getMessage());
            return;
        }*/
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
      /*  JSONObject jsonQuery;
        String queryMethodName;
        String queryId;

        try {
            jsonQuery = new JSONObject(message);
            queryMethodName = (String) jsonQuery.get(RemoteComServerQueryJSonPropertyNames.METHOD);
            queryId = jsonQuery.getString(RemoteComServerQueryJSonPropertyNames.QUERY_ID);
        } catch (JSONException e) {
            sendMessage(ERROR_PREFIX + MESSAGE_NOT_UNDERSTOOD + e.getMessage());
            return;
        }
        try {
            Long userId = jsonQuery.getLong(RemoteComServerQueryJSonPropertyNames.USER_ID);
            serviceProvider.threadPrincipalService().runAs(
                    serviceProvider.userService().getUser(userId).get(),
                    () -> executeQuery(jsonQuery, queryMethodName, queryId)
            );
        } catch (JSONException e) {
            executeQuery(jsonQuery, queryMethodName, queryId);
        }*/

    }

    @OnWebSocketError
    public void onError(Throwable e) {
        sendMessage(ERROR_PREFIX + e.getLocalizedMessage());
    }

    private void executeQuery(JSONObject jsonQuery, String queryMethodName, String queryId) {
        try {
            QueryMethod queryMethod = QueryMethod.byName(queryMethodName);
            String result = this.execute(queryMethod, jsonQuery, queryId);
            sendMessage(result);
        } catch (IOException | JSONException e) {
            e.printStackTrace(System.err);
            sendSerializedErrorMessage("failure to marshall response: ", e, queryId);
        } catch (NoMoreHighPriorityTasksCanBePickedUpRuntimeException e) {
            sendSerializedErrorMessage("info: ", e, queryId);
        } catch (DataAccessException | ApplicationException | NullPointerException |
                NumberFormatException | IndexOutOfBoundsException e) {    //Note that SQL and Business exceptions will be wrapped here
            e.printStackTrace(System.err);
            sendSerializedErrorMessage("unexpected failure: ", e, queryId);
        } catch (Throwable e) {     //This causes the connection to close, so only severe exceptions should end up in this catch clause.
            e.printStackTrace(System.err);
            sendSerializedErrorMessage("unhandled exception: ", e, queryId);
        }
    }

    protected void sendSerializedErrorMessage(String message, Throwable e, String queryId) {
        String fullMessage = ERROR_PREFIX + message + e.getMessage();
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        QueryResult result = QueryResult.forResult(queryId, fullMessage);
        StringWriter writer = new StringWriter();

        try {
            mapper.writeValue(writer, result);
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
            sendMessage(ERROR_PREFIX + "failure to marshall response: " + e1.getMessage());
            return;
        }

        sendMessage(writer.toString());
    }

    private String execute(QueryMethod queryMethod, JSONObject jsonQuery, String queryId) throws JSONException, IOException {
        Map<String, Object> parameters = extractQueryParameters(jsonQuery);
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        Object queryResultValue = queryMethod.execute(parameters, getQueryMethodServiceProvider());
        if (queryMethod == QueryMethod.DisconnectRemoteComServer) {
            // A DisConnectRemoteComServer message was sent by the remote: keep statistics ok
            if (queryAPIStatistics != null) {
                queryAPIStatistics.clientUnregistered(((ComServer) queryResultValue).getName());
            }
            // No use to sent an answer the remote is disconnecting: only to keep statistics ok
            queryResultValue = null;
        }
        mapper.writeValue(writer, QueryResult.forResult(queryId, queryResultValue));
        return writer.toString();
    }

    private Map<String, Object> extractQueryParameters(JSONObject jsonQuery) throws JSONException {
        Map<String, Object> parameters = new HashMap<>();
        Iterator keys = jsonQuery.keys();
        while (keys.hasNext()) {
            Object next = keys.next();
            String key = (String) next;
            if (this.isQueryParameter(key)) {
                parameters.put(key, jsonQuery.get(key));
            }
        }
        return parameters;
    }

    private boolean isQueryParameter(String key) {
        return !(RemoteComServerQueryJSonPropertyNames.QUERY_ID.equals(key)
                || RemoteComServerQueryJSonPropertyNames.METHOD.equals(key));
    }

    @OnWebSocketConnect
    public void onOpen(Session connection) {
        //these methods commented as no security on start up to sync the data.
       /* this.connection = connection;
        this.connection.setIdleTimeout(0); //Infinite
        int maxMessageSize = getMaxMessageSize();
        logger.info("Setting max message size for binary/text messages to " + maxMessageSize);
        this.connection.getPolicy().setMaxBinaryMessageSize(maxMessageSize);
        this.connection.getPolicy().setMaxTextMessageSize(maxMessageSize);*/
    }

    private int getMaxMessageSize() {
        Properties comServerProperties = new Properties();
        try (InputStream comserverPropertiesStream = this.getComServerPropertiesStream()) {
            comServerProperties.load(comserverPropertiesStream);
        } catch (IOException e) {
            logger.severe("Could not read properties file: " + e.getMessage());
            return DEFAULT_MAX_BINARY_MESSAGE_SIZE;
        }
        RemoteProperties remoteProperties = new RemoteProperties(comServerProperties);
        return remoteProperties.getMaxMessageSize();
    }

    @OnWebSocketClose
    public void onClose(int closeCode, String message) {
        this.connection = null;
    }

    private void sendMessage(String message) {
        try {
            if (isConnected()) {
                if (getOnlineComServer().isCompressingEnabled()) {
                    byte[] compressedData = DataCompressor.encodeAndCompress(message, getOnlineComServer().isCompressingEnabled());
                    connection.getRemote().sendBytes(ByteBuffer.wrap(compressedData));//sendMessage(compressedData, 0, compressedData.length);
                } else {
                    connection.getRemote().sendString(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private boolean isConnected() {
        return connection != null;
    }

    protected QueryMethod.ServiceProvider getQueryMethodServiceProvider() {
        return serviceProvider;
    }

    /**
     * Setter for ComServerDAO, only to be used in Unit tests!
     */
    protected void setQueryMethodServiceProvider(QueryMethod.ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }


    private InputStream getComServerPropertiesStream() throws FileNotFoundException {
        return new FileInputStream("conf/comserver.properties");
    }

    private class ServiceProvider implements QueryMethod.ServiceProvider {

        private ComServerDAO comServerDAO;
        private EngineConfigurationService engineConfigurationService;
        private ConnectionTaskService connectionTaskService;
        private CommunicationTaskService communicationTaskService;
        private TransactionService transactionService;
        private ThreadPrincipalService threadPrincipalService;
        private UserService userService;

        public ServiceProvider(ComServerDAO comServerDAO, EngineConfigurationService engineConfigurationService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService, UserService userService) {
            this.comServerDAO = comServerDAO;
            this.engineConfigurationService = engineConfigurationService;
            this.connectionTaskService = connectionTaskService;
            this.communicationTaskService = communicationTaskService;
            this.transactionService = transactionService;
            this.threadPrincipalService = threadPrincipalService;
            this.userService = userService;
        }

        @Override
        public ComServerDAO comServerDAO() {
            return comServerDAO;
        }

        @Override
        public EngineConfigurationService engineConfigurationService() {
            return engineConfigurationService;
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return connectionTaskService;
        }

        @Override
        public CommunicationTaskService communicationTaskService() {
            return communicationTaskService;
        }

        @Override
        public TransactionService transactionService() {
            return transactionService;
        }

        @Override
        public ThreadPrincipalService threadPrincipalService() {
            return threadPrincipalService;
        }

        public UserService userService() {
            return userService;
        }
    }

}
package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.impl.EngineConfigurationServiceImpl;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link WebSocketQueryApiService} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (11:19)
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketQueryApiServiceTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static DataModel dataModel;
    private static EngineConfigurationService engineConfigurationService;
    private static DeviceService deviceService;
    private static TransactionService transactionService;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @BeforeClass
    public static void staticSetUp() {
        initializeMocks();
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new NlsModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new UserModule(),
                new TransactionModule(false),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(NlsService.class); // fake call to make sure component is initialized
            injector.getInstance(ProtocolPluggableService.class); // fake call to make sure component is initialized
            transactionService = injector.getInstance(TransactionService.class);
            EngineConfigurationServiceImpl engineConfigurationServiceImpl = (EngineConfigurationServiceImpl) injector.getInstance(EngineConfigurationService.class);
            dataModel = engineConfigurationServiceImpl.getDataModel();
            engineConfigurationService = engineConfigurationServiceImpl;
            ctx.commit();
        }
    }

    private static void initializeMocks() {
        deviceService = mock(DeviceService.class);
    }

    @AfterClass
    public static void staticTearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    /**
     * Executes the {@link ComServerDAO#getThisComServer()} method.
     */
    @Test
    public void testGetThisComServer() throws SQLException, BusinessException, JSONException {
        OnlineComServer comServer = this.createComServerForThisMachine();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getThisComServer()).thenReturn(comServer);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testGetThisComServer";
        String query = this.getThisComServerQueryString(queryId);

        // Business method
        queryApiService.onMessage(query);

        // Asserts
        String receivedMessage = connection.getReceivedMessage();
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).contains(queryId);
        assertThat(receivedMessage).contains(comServer.getName());
    }

    /**
     * Executes the {@link ComServerDAO#getComServer(String)} method
     * for an {@link OnlineComServer}.
     */
    @Test
    public void testGetOnlineComServer() throws SQLException, BusinessException, JSONException {
        String hostName = "online.WebSocketQueryApiServiceTest";
        OnlineComServer comServer = this.createOnlineComServer(hostName);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServer(hostName)).thenReturn(comServer);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testGetComServer";
        String query = this.getComServerQueryString(queryId, hostName);

        // Business method
        queryApiService.onMessage(query);

        // Asserts
        String receivedMessage = connection.getReceivedMessage();
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).contains(queryId);
        assertThat(receivedMessage).contains(comServer.getName());
    }

    /**
     * Executes the {@link ComServerDAO#getComServer(String)} method
     * for an {@link RemoteComServer}.
     */
    @Test
    public void testGetRemoteComServer() throws SQLException, BusinessException, JSONException {
        String onlineHostName = "online.testGetRemoteComServer";
        OnlineComServer onlineComServer = this.createOnlineComServer(onlineHostName);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServer(onlineHostName)).thenReturn(onlineComServer);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(onlineComServer);
        String remoteHostName = "remote.testGetRemoteComServer";
        RemoteComServer comServer = this.createRemoteComServer(remoteHostName, onlineComServer);
        when(comServerDAO.getComServer(remoteHostName)).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testGetRemoteComServer";
        String query = this.getComServerQueryString(queryId, remoteHostName);

        // Business method
        queryApiService.onMessage(query);

        // Asserts
        String receivedMessage = connection.getReceivedMessage();
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).contains(queryId);
        assertThat(receivedMessage).contains(comServer.getName());
    }

    /**
     * Executes the {@link ComServerDAO#getComServer(String)} method.
     */
    @Test
    public void testGetComServerThatDoesNotExist() throws SQLException, BusinessException, JSONException {
        OnlineComServer comServer = this.createOnlineComServer("testGetComServerThatDoesNotExist");
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testGetComServer";
        String query = this.getComServerQueryString(queryId, "Does.Not.Exist");

        // Business method
        queryApiService.onMessage(query);

        // Asserts
        String receivedMessage = connection.getReceivedMessage();
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).contains(queryId);
        assertThat(receivedMessage).doesNotContain(comServer.getName());
    }

    private String getThisComServerQueryString(String queryId) throws JSONException {
        JSONWriter queryWriter = new JSONStringer().object();
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.QUERY_ID).value(queryId);
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.METHOD).value(QueryMethod.GetThisComServer.name());
        queryWriter.endObject();
        return queryWriter.toString();
    }

    private String getComServerQueryString(String queryId, String hostName) throws JSONException {
        JSONWriter queryWriter = new JSONStringer().object();
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.QUERY_ID).value(queryId);
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.METHOD).value(QueryMethod.GetComServer.name());
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.HOSTNAME).value(hostName);
        queryWriter.endObject();
        return queryWriter.toString();
    }

    private OnlineComServer createComServerForThisMachine() {
        return this.createOnlineComServer(HostName.getCurrent());
    }

    private OnlineComServer createOnlineComServer(String hostName) {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            OnlineComServer onlineComServer = engineConfigurationService.newOnlineComServerInstance();
            onlineComServer.setName(hostName);
            onlineComServer.setActive(true);
            onlineComServer.setActive(true);
            onlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
            onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.ERROR);
            onlineComServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.HOURS));
            onlineComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
            onlineComServer.setStoreTaskQueueSize(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
            onlineComServer.setNumberOfStoreTaskThreads(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
            onlineComServer.setStoreTaskThreadPriority(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
            onlineComServer.save();
            ctx.commit();
            return onlineComServer;
        }
    }

    private RemoteComServer createRemoteComServer(String hostName, OnlineComServer onlineComServer) {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            RemoteComServer remoteComServer = this.doCreateRemoteComServer(hostName, onlineComServer);
            ctx.commit();
            return remoteComServer;
        }
    }

    private RemoteComServer doCreateRemoteComServer(String hostName, OnlineComServer onlineComServer) {
        RemoteComServer remoteComServer = engineConfigurationService.newRemoteComServerInstance();
        remoteComServer.setName(hostName);
        remoteComServer.setOnlineComServer(onlineComServer);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.HOURS));
        remoteComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        remoteComServer.save();
        return remoteComServer;
    }

    private RemoteComServer createRemoteComServerWithOneOutboundComPort(String hostName, OnlineComServer onlineComServer) {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            RemoteComServer remoteComServer = this.doCreateRemoteComServer(hostName, onlineComServer);
            OutboundComPort.OutboundComPortBuilder portBuilder = remoteComServer.newOutboundComPort("TCP", 1);
            portBuilder.comPortType(ComPortType.TCP);
            portBuilder.active(true);
            portBuilder.add();
            ctx.commit();
            return remoteComServer;
        }
    }

    private class TestConnection implements org.eclipse.jetty.websocket.WebSocket.Connection {

        private String receivedMessage;

        private String getReceivedMessage() {
            return receivedMessage;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public void sendMessage(String data) throws IOException {
            this.receivedMessage = data;
        }

        @Override
        public void sendMessage(byte[] data, int offset, int length) throws IOException {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void close() {
        }

        @Override
        public void close(int closeCode, String message) {
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void setMaxIdleTime(int ms) {
        }

        @Override
        public void setMaxTextMessageSize(int size) {
        }

        @Override
        public void setMaxBinaryMessageSize(int size) {
        }

        @Override
        public int getMaxIdleTime() {
            return 0;
        }

        @Override
        public int getMaxTextMessageSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxBinaryMessageSize() {
            return 0;
        }
    }

    private static class MockModule extends AbstractModule {
        private BundleContext bundleContext;
        private EventAdmin eventAdmin;
        private ProtocolPluggableService protocolPluggableService;

        private MockModule(BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = mock(EventAdmin.class);
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
        }

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(DeviceService.class).toInstance(deviceService);
        }
    }

}
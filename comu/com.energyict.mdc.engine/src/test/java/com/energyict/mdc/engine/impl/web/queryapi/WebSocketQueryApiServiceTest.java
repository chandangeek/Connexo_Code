/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.tasks.DataCompressor;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.impl.EngineConfigurationServiceImpl;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.SuspendToken;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
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
@Ignore
public class WebSocketQueryApiServiceTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static DataModel dataModel;
    private static EngineConfigurationService engineConfigurationService;
    private static DeviceService deviceService;
    private static TransactionService transactionService;
    private static ThreadPrincipalService threadPrincipalService;
    private static UserService userService;

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
                new H2OrmModule(),
                new UtilModule(),
                new NlsModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new UserModule(),
                new TransactionModule(false),
                new EngineModelModule(),
                new DataVaultModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(NlsService.class); // fake call to make sure component is initialized
            injector.getInstance(ProtocolPluggableService.class); // fake call to make sure component is initialized
            transactionService = injector.getInstance(TransactionService.class);
            EngineConfigurationServiceImpl engineConfigurationServiceImpl = (EngineConfigurationServiceImpl) injector.getInstance(EngineConfigurationService.class);
            dataModel = engineConfigurationServiceImpl.getDataModel();
            engineConfigurationService = engineConfigurationServiceImpl;
            threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
            userService = injector.getInstance(UserService.class);
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
    public void testGetThisComServer() throws SQLException, JSONException {
        OnlineComServer comServer = this.createComServerForThisMachine();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getThisComServer()).thenReturn(comServer);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService, threadPrincipalService, userService);
        TestConnection connection = new TestConnection(true);
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
    public void testGetOnlineComServer() throws SQLException, JSONException {
        String hostName = "online.WebSocketQueryApiServiceTest";
        OnlineComServer comServer = this.createOnlineComServer(hostName);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServer(hostName)).thenReturn(comServer);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService, threadPrincipalService, userService);
        TestConnection connection = new TestConnection(true);
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
    public void testGetRemoteComServer() throws SQLException, JSONException {
        String onlineHostName = "online.testGetRemoteComServer";
        OnlineComServer onlineComServer = this.createOnlineComServer(onlineHostName);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServer(onlineHostName)).thenReturn(onlineComServer);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(onlineComServer);
        String remoteHostName = "remote.testGetRemoteComServer";
        RemoteComServer comServer = this.createRemoteComServer(remoteHostName, onlineComServer);
        when(comServerDAO.getComServer(remoteHostName)).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService, threadPrincipalService, userService);
        TestConnection connection = new TestConnection(true);
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
    public void testGetComServerThatDoesNotExist() throws SQLException, JSONException {
        OnlineComServer comServer = this.createOnlineComServer("testGetComServerThatDoesNotExist");
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiService(runningComServer, comServerDAO, engineConfigurationService, connectionTaskService, communicationTaskService, transactionService, threadPrincipalService, userService);
        TestConnection connection = new TestConnection(true);
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
            OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = engineConfigurationService.newOnlineComServerBuilder();
            onlineComServerBuilder.name(hostName);
            onlineComServerBuilder.active(true);
            onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.ERROR);
            onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.ERROR);
            onlineComServerBuilder.changesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.HOURS));
            onlineComServerBuilder.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
            onlineComServerBuilder.storeTaskQueueSize(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
            onlineComServerBuilder.numberOfStoreTaskThreads(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
            onlineComServerBuilder.storeTaskThreadPriority(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
            onlineComServerBuilder.serverName(hostName);
            onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
            final OnlineComServer onlineComServer = onlineComServerBuilder.create();
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
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = engineConfigurationService.newRemoteComServerBuilder();
        remoteComServer.name(hostName);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.changesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.HOURS));
        remoteComServer.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        remoteComServer.serverName(hostName);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return remoteComServer.create();
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

    public class TestConnection implements org.eclipse.jetty.websocket.api.Session {
        private TestRemoteEndpoint remote;
        private boolean compressingEnabled;

        public TestConnection(boolean compressingEnabled) {
            remote = new TestRemoteEndpoint();
            this.compressingEnabled = compressingEnabled;
        }

        public String getReceivedMessage() {
            return remote.getReceivedMessage();
        }

        @Override
        public void close() {

        }

        @Override
        public void close(CloseStatus closeStatus) {

        }

        @Override
        public void close(int statusCode, String reason) {

        }

        @Override
        public void disconnect() throws IOException {

        }

        @Override
        public long getIdleTimeout() {
            return 0;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public WebSocketPolicy getPolicy() {
            return WebSocketPolicy.newServerPolicy();
        }

        @Override
        public String getProtocolVersion() {
            return null;
        }

        @Override
        public RemoteEndpoint getRemote() {
            return remote;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public UpgradeRequest getUpgradeRequest() {
            return null;
        }

        @Override
        public UpgradeResponse getUpgradeResponse() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        public boolean isCompressingEnabled() {
            return compressingEnabled;
        }

        @Override
        public void setIdleTimeout(long ms) {

        }

        @Override
        public SuspendToken suspend() {
            return null;
        }

        private class TestRemoteEndpoint implements RemoteEndpoint {
            private String receivedMessage;

            private String getReceivedMessage() {
                return receivedMessage;
            }

            @Override
            public void sendBytes(ByteBuffer data) throws IOException {
                byte[] buffer = data.array();
                receivedMessage = DataCompressor.decompressAndDecode(ProtocolUtils.getSubArray(buffer, 0, buffer.length - 1), isCompressingEnabled());
            }

            @Override
            public Future<Void> sendBytesByFuture(ByteBuffer data) {
                return null;
            }

            @Override
            public void sendBytes(ByteBuffer data, WriteCallback callback) {
                try {
                    sendBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void sendPartialBytes(ByteBuffer fragment, boolean isLast) throws IOException {

            }

            @Override
            public void sendPartialString(String fragment, boolean isLast) throws IOException {

            }

            @Override
            public void sendPing(ByteBuffer applicationData) throws IOException {

            }

            @Override
            public void sendPong(ByteBuffer applicationData) throws IOException {

            }

            @Override
            public void sendString(String text) {
                receivedMessage = text;
            }

            @Override
            public Future<Void> sendStringByFuture(String text) {
                return null;
            }

            @Override
            public void sendString(String text, WriteCallback callback) {
                sendString(text);
            }

            @Override
            public BatchMode getBatchMode() {
                return null;
            }

            //        @Override
            public void setBatchMode(BatchMode mode) {

            }

            //        @Override
            public InetSocketAddress getInetSocketAddress() {
                return null;
            }

            @Override
            public void flush() throws IOException {

            }
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
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

}
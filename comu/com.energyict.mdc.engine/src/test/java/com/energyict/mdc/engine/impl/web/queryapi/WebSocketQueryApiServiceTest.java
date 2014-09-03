package com.energyict.mdc.engine.impl.web.queryapi;

import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.users.impl.UserModule;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.HostName;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.engine.model.impl.EngineModelServiceImpl;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTimeConstants;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
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
    private static EngineModelService engineModelService;
    private static DeviceDataService deviceDataService;
    private static TransactionService transactionService;
    private static ServiceProvider serviceProvider;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

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
                new MdcCommonModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new UserModule(),
                new TransactionModule(false),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(EnvironmentImpl.class); // fake call to make sure component is initialized
            injector.getInstance(NlsService.class); // fake call to make sure component is initialized
            injector.getInstance(ProtocolPluggableService.class); // fake call to make sure component is initialized
            transactionService = injector.getInstance(TransactionService.class);
            EngineModelServiceImpl engineModelServiceImpl = (EngineModelServiceImpl) injector.getInstance(EngineModelService.class);
            dataModel = engineModelServiceImpl.getDataModel();
            engineModelService = engineModelServiceImpl;
            ctx.commit();
        }
        when(serviceProvider.engineModelService()).thenReturn(engineModelService);
        when(serviceProvider.transactionService()).thenReturn(transactionService);
    }

    private static void initializeMocks() {
        deviceDataService = mock(DeviceDataService.class);
        serviceProvider = mock(ServiceProvider.class);
        when(serviceProvider.deviceDataService()).thenReturn(deviceDataService);
        ServiceProvider.instance.set(serviceProvider);
    }

    @AfterClass
    public static void staticTearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    /**
     * Executes the {@link ComServerDAO#getThisComServer()} method.
     */
    @Test
    public void testGetThisComServer () throws SQLException, BusinessException, JSONException {
        OnlineComServer comServer = this.createComServerForThisMachine();
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiServiceFactoryImpl().newWebSocketQueryApiService(runningComServer);
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
    public void testGetOnlineComServer () throws SQLException, BusinessException, JSONException {
        String hostName = "online.WebSocketQueryApiServiceTest";
        OnlineComServer comServer = this.createOnlineComServer(hostName);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiServiceFactoryImpl().newWebSocketQueryApiService(runningComServer);
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
    public void testGetRemoteComServer () throws SQLException, BusinessException, JSONException {
        String onlineHostName = "online.testGetRemoteComServer";
        OnlineComServer onlineComServer = this.createOnlineComServer(onlineHostName);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(onlineComServer);
        String remoteHostName = "remote.testGetRemoteComServer";
        RemoteComServer comServer = this.createRemoteComServer(remoteHostName, onlineComServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiServiceFactoryImpl().newWebSocketQueryApiService(runningComServer);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testGetComServer";
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
    public void testGetComServerThatDoesNotExist () throws SQLException, BusinessException, JSONException {
        OnlineComServer comServer = this.createOnlineComServer("testGetComServerThatDoesNotExist");
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(comServer);
        WebSocketQueryApiService queryApiService = new WebSocketQueryApiServiceFactoryImpl().newWebSocketQueryApiService(runningComServer);
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

    /**
     * Executes the {@link ComServerDAO#refreshComPort(ComPort)} method
     * for a ComPort that was not changed.
     */
    @Test
    public void testRefreshComPortWithoutChanges () throws SQLException, BusinessException, JSONException {
        String onlineHostName = "online.testRefreshComPortWithoutChanges";
        OnlineComServer onlineComServer = this.createOnlineComServer(onlineHostName);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(onlineComServer);
        String remoteHostName = "remote.testRefreshComPortWithoutChanges";
        RemoteComServer comServer = this.createRemoteComServerWithOneOutboundComPort(remoteHostName, onlineComServer);
        OutboundComPort comPort = comServer.getOutboundComPorts().get(0);

        WebSocketQueryApiService queryApiService = new WebSocketQueryApiServiceFactoryImpl().newWebSocketQueryApiService(runningComServer);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testRefreshComPortWithoutChanges";
        String query = this.getRefreshComPortQueryString(queryId, comPort);

        // Business method
        queryApiService.onMessage(query);

        // Asserts
        String receivedMessage = connection.getReceivedMessage();
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).contains(queryId);
        assertThat(receivedMessage).doesNotContain(comPort.getName());
    }

    /**
     * Executes the {@link ComServerDAO#refreshComPort(ComPort)} method
     * for a ComPort that was changed.
     */
    @Test
    public void testRefreshComPortWithChanges () throws SQLException, BusinessException, JSONException {
        String onlineHostName = "online.testRefreshComPortWithChanges";
        OnlineComServer onlineComServer = this.createOnlineComServer(onlineHostName);
        RunningOnlineComServer runningComServer = mock(RunningOnlineComServer.class);
        when(runningComServer.getComServer()).thenReturn(onlineComServer);
        String remoteHostName = "remote.testRefreshComPortWithChanges";
        RemoteComServer comServer = this.createRemoteComServerWithOneOutboundComPort(remoteHostName, onlineComServer);
        OutboundComPort comPort = comServer.getOutboundComPorts().get(0);
        Date creationDate = comPort.getModificationDate();

        WebSocketQueryApiService queryApiService = new WebSocketQueryApiServiceFactoryImpl().newWebSocketQueryApiService(runningComServer);
        TestConnection connection = new TestConnection();
        queryApiService.onOpen(connection);
        String queryId = "testRefreshComPortWithChanges";
        String query = this.getRefreshComPortQueryString(queryId, comPort);

        // Now update the ComPort's modification date
        Date modificationDate = new Date(creationDate.getTime() + DateTimeConstants.MILLIS_PER_DAY);
        this.updateComPortModificationDate(comPort, modificationDate);

        // Business method
        queryApiService.onMessage(query);

        // Asserts
        String receivedMessage = connection.getReceivedMessage();
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage).contains(queryId);
        assertThat(receivedMessage).contains(comPort.getName());
    }

    private void updateComPortModificationDate (ComPort comPort, Date modificationDate) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("update mdc_comport set mod_date = ?");
        sqlBuilder.bindDate(modificationDate);
        sqlBuilder.append(" where id = ?");
        sqlBuilder.bindLong(comPort.getId());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            try (Connection connection = dataModel.getConnection(true)) {
                try (PreparedStatement statement = sqlBuilder.getStatement(connection)) {
                    statement.executeUpdate();
                }
            }
            ctx.commit();
        }
    }

    private String getThisComServerQueryString (String queryId) throws JSONException {
        JSONWriter queryWriter = new JSONStringer().object();
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.QUERY_ID).value(queryId);
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.METHOD).value(QueryMethod.GetThisComServer.name());
        queryWriter.endObject();
        return queryWriter.toString();
    }

    private String getComServerQueryString (String queryId, String hostName) throws JSONException {
        JSONWriter queryWriter = new JSONStringer().object();
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.QUERY_ID).value(queryId);
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.METHOD).value(QueryMethod.GetComServer.name());
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.HOSTNAME).value(hostName);
        queryWriter.endObject();
        return queryWriter.toString();
    }

    private String getRefreshComPortQueryString (String queryId, ComPort comPort) throws JSONException {
        JSONWriter queryWriter = new JSONStringer().object();
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.QUERY_ID).value(queryId);
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.METHOD).value(QueryMethod.RefreshComPort.name());
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.COMPORT).value(comPort.getId());
        queryWriter.key(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE).value(comPort.getModificationDate().getTime());
        queryWriter.endObject();
        return queryWriter.toString();
    }

    private OnlineComServer createComServerForThisMachine () {
        return this.createOnlineComServer(HostName.getCurrent());
    }

    private OnlineComServer createOnlineComServer (String hostName) {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            OnlineComServer onlineComServer = engineModelService.newOnlineComServerInstance();
            onlineComServer.setName(hostName);
            onlineComServer.setActive(true);
            onlineComServer.setActive(true);
            onlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
            onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.ERROR);
            onlineComServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.HOURS));
            onlineComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.MINUTES));
            onlineComServer.setStoreTaskQueueSize(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
            onlineComServer.setNumberOfStoreTaskThreads(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
            onlineComServer.setStoreTaskThreadPriority(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
            onlineComServer.save();
            ctx.commit();
            return onlineComServer;
        }
    }

    private RemoteComServer createRemoteComServer (String hostName, OnlineComServer onlineComServer) {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            RemoteComServer remoteComServer = this.doCreateRemoteComServer(hostName, onlineComServer);
            ctx.commit();
            return remoteComServer;
        }
    }

    private RemoteComServer doCreateRemoteComServer(String hostName, OnlineComServer onlineComServer) {
        RemoteComServer remoteComServer = engineModelService.newRemoteComServerInstance();
        remoteComServer.setName(hostName);
        remoteComServer.setOnlineComServer(onlineComServer);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.HOURS));
        remoteComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.MINUTES));
        remoteComServer.save();
        return remoteComServer;
    }

    private RemoteComServer createRemoteComServerWithOneOutboundComPort (String hostName, OnlineComServer onlineComServer) {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
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

        private String getReceivedMessage () {
            return receivedMessage;
        }

        @Override
        public String getProtocol () {
            return null;
        }

        @Override
        public void sendMessage (String data) throws IOException {
            this.receivedMessage = data;
        }

        @Override
        public void sendMessage (byte[] data, int offset, int length) throws IOException {
        }

        @Override
        public void disconnect () {
        }

        @Override
        public void close () {
        }

        @Override
        public void close (int closeCode, String message) {
        }

        @Override
        public boolean isOpen () {
            return true;
        }

        @Override
        public void setMaxIdleTime (int ms) {
        }

        @Override
        public void setMaxTextMessageSize (int size) {
        }

        @Override
        public void setMaxBinaryMessageSize (int size) {
        }

        @Override
        public int getMaxIdleTime () {
            return 0;
        }

        @Override
        public int getMaxTextMessageSize () {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxBinaryMessageSize () {
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
            this.eventAdmin =  mock(EventAdmin.class);
            this.protocolPluggableService = mock(ProtocolPluggableService.class);
        }

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(DeviceDataService.class).toInstance(deviceDataService);
        }
    }

}
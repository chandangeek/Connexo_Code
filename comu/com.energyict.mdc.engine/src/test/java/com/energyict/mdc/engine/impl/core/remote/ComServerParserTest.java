package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.engine.model.impl.EngineModelServiceImpl;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.ComServerParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:56)
 */
public class ComServerParserTest {

    private static final String ONLINE_COMSERVER_AS_QUERY_RESULT = "{\"query-id\":\"testGetThisComServer\",\"single-value\":{\"name\":\"online.comserver.energyict.com\",\"type\":\"OnlineComServerImpl\",\"active\":true,\"serverLogLevel\":\"ERROR\",\"communicationLogLevel\":\"DEBUG\",\"changesInterPollDelay\":{\"seconds\":18000},\"schedulingInterPollDelay\":{\"seconds\":60},\"eventRegistrationUri\":\"ws://online.comserver.energyict.com/events/registration\",\"storeTaskQueueSize\":50,\"numberOfStoreTaskThreads\":1,\"storeTaskThreadPriority\":5}}";
    private static final String REMOTE_COMSERVER_AS_QUERY_RESULT = "{\"query-id\":\"testGetThisComServer\",\"single-value\":{\"name\":\"remote.comserver.energyict.com\",\"active\":true,\"serverLogLevel\":\"DEBUG\",\"communicationLogLevel\":\"ERROR\",\"changesInterPollDelay\":{\"seconds\":1800},\"schedulingInterPollDelay\":{\"seconds\":600},\"eventRegistrationUri\":\"ws://remote.comserver.energyict.com/events/registration\",\"type\":\"RemoteComServerImpl\"}}";

    private static final String QUERY_API_USER_NAME = "johndoe";
    private static final String QUERY_API_PASSWORD = "doe";

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static EngineModelService engineModelService;

    @BeforeClass
    public static void initializeDatabase () {
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new NlsModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new MdcCommonModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new TransactionModule(false),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(EnvironmentImpl.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(ProtocolPluggableService.class);
            engineModelService = injector.getInstance(EngineModelService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanup(){
        inMemoryBootstrapModule.deactivate();
    }

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Test
    public void testDelegateToEngineModelService () throws JSONException {
        EngineModelService engineModelService = mock(EngineModelService.class);
        JSONObject jsonObject = new JSONObject(ONLINE_COMSERVER_AS_QUERY_RESULT);
        JSONObject comserverJsonObject = (JSONObject) jsonObject.get("single-value");
        ComServerParser comServerParser = new ComServerParser(engineModelService);

        // Business method
        comServerParser.parse(jsonObject);

        // Asserts
        verify(engineModelService).parseComServerQueryResult(comserverJsonObject);
    }

    @Test
    @Transactional
    public void testOnline () throws JSONException {
        this.createTestOnlineComServer(engineModelService);
        ComServerParser comServerParser = new ComServerParser(engineModelService);
        JSONObject jsonObject = new JSONObject(ONLINE_COMSERVER_AS_QUERY_RESULT);

        // Business method
        ComServer comServer = comServerParser.parse(jsonObject);

        // Asserts
        assertThat(comServer).isInstanceOf(OnlineComServer.class);
        OnlineComServer onlineComServer = (OnlineComServer) comServer;
        assertThat(onlineComServer.getName()).isEqualTo("online.comserver.energyict.com");
        assertThat(onlineComServer.isActive()).isTrue();
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(TimeDuration.seconds(18000));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(TimeDuration.seconds(60));
        assertThat(onlineComServer.getEventRegistrationUri()).isEqualTo("ws://online.comserver.energyict.com:" + ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER + "/events/registration");
        assertThat(onlineComServer.getQueryApiPostUri()).isEqualTo("http://online.comserver.energyict.com:" + ComServer.DEFAULT_QUERY_API_PORT_NUMBER + "/remote/queries");
        assertThat(onlineComServer.getStoreTaskQueueSize()).isEqualTo(50);
        assertThat(onlineComServer.getNumberOfStoreTaskThreads()).isEqualTo(1);
        assertThat(onlineComServer.getStoreTaskThreadPriority()).isEqualTo(5);
    }

    @Test
    @Transactional
    public void testRemote () throws JSONException {
        this.createTestRemoteComServer(engineModelService);
        ComServerParser comServerParser = new ComServerParser(engineModelService);
        JSONObject jsonObject = new JSONObject(REMOTE_COMSERVER_AS_QUERY_RESULT);

        // Business method
        ComServer comServer = comServerParser.parse(jsonObject);

        // Asserts
        assertThat(comServer).isInstanceOf(RemoteComServer.class);
        RemoteComServer remoteComServer = (RemoteComServer) comServer;
        assertThat(remoteComServer.getName()).isEqualTo("remote.comserver.energyict.com");
        assertThat(remoteComServer.isActive()).isTrue();
        assertThat(remoteComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(remoteComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(remoteComServer.getChangesInterPollDelay()).isEqualTo(TimeDuration.seconds(1800));
        assertThat(remoteComServer.getSchedulingInterPollDelay()).isEqualTo(TimeDuration.seconds(600));
        assertThat(remoteComServer.getEventRegistrationUri()).isEqualTo("ws://remote.comserver.energyict.com:" + ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER + "/events/registration");
    }

    private OnlineComServer createTestOnlineComServer(EngineModelService engineModelService) {
        OnlineComServer onlineComServer = engineModelService.newOnlineComServerInstance();
        String name = "online.comserver.energyict.com";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setChangesInterPollDelay(TimeDuration.seconds(18000));
        onlineComServer.setSchedulingInterPollDelay(TimeDuration.seconds(60));
        onlineComServer.setStoreTaskQueueSize(50);
        onlineComServer.setStoreTaskThreadPriority(Thread.NORM_PRIORITY);
        onlineComServer.setNumberOfStoreTaskThreads(1);
        onlineComServer.save();
        return onlineComServer;
    }

    private RemoteComServer createTestRemoteComServer (EngineModelService engineModelService) {
        OnlineComServer onlineComServer = this.createTestOnlineComServer(engineModelService);
        RemoteComServer remoteComServer = engineModelService.newRemoteComServerInstance();
        String name = "remote.comserver.energyict.com";
        remoteComServer.setName(name);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.setChangesInterPollDelay(TimeDuration.seconds(1800));
        remoteComServer.setSchedulingInterPollDelay(TimeDuration.seconds(600));
        remoteComServer.setOnlineComServer(onlineComServer);
        remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
        remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);
        remoteComServer.save();
        return remoteComServer;
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
            bind(BundleContext.class).toInstance(this.bundleContext);
            bind(EventAdmin.class).toInstance(this.eventAdmin);
            bind(ProtocolPluggableService.class).toInstance(this.protocolPluggableService);
        }
    }

}
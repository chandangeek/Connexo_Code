package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

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

    private static final String ONLINE_COMSERVER_AS_QUERY_RESULT = "{\"query-id\":\"testGetThisComServer\",\"single-value\":{\"name\":\"online.comserver.energyict.com\",\"type\":\"OnlineComServerImpl\",\"active\":true,\"serverLogLevel\":\"ERROR\",\"communicationLogLevel\":\"DEBUG\",\"changesInterPollDelay\":{\"seconds\":18000},\"schedulingInterPollDelay\":{\"seconds\":60},\"eventRegistrationPort\":8888,\"storeTaskQueueSize\":50,\"numberOfStoreTaskThreads\":1,\"storeTaskThreadPriority\":5}}";
    private static final String REMOTE_COMSERVER_AS_QUERY_RESULT = "{\"query-id\":\"testGetThisComServer\",\"single-value\":{\"name\":\"remote.comserver.energyict.com\",\"active\":true,\"serverLogLevel\":\"DEBUG\",\"communicationLogLevel\":\"ERROR\",\"changesInterPollDelay\":{\"seconds\":1800},\"schedulingInterPollDelay\":{\"seconds\":600},\"eventRegistrationPort\":8888,\"type\":\"RemoteComServerImpl\"}}";

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static EngineConfigurationService engineConfigurationService;

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
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new TransactionModule(false),
                new UserModule(),
                new EngineModelModule(),
                new DataVaultModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(NlsService.class);
            injector.getInstance(ProtocolPluggableService.class);
            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
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
        EngineConfigurationService engineConfigurationService = mock(EngineConfigurationService.class);
        JSONObject jsonObject = new JSONObject(ONLINE_COMSERVER_AS_QUERY_RESULT);
        JSONObject comserverJsonObject = (JSONObject) jsonObject.get("single-value");
        ComServerParser comServerParser = new ComServerParser(engineConfigurationService);

        // Business method
        comServerParser.parse(jsonObject);

        // Asserts
        verify(engineConfigurationService).parseComServerQueryResult(comserverJsonObject);
    }

    @Test
    @Transactional
    public void testOnline () throws JSONException {
        this.createTestOnlineComServer(engineConfigurationService);
        ComServerParser comServerParser = new ComServerParser(engineConfigurationService);
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
        assertThat(onlineComServer.getEventRegistrationPort()).isEqualTo(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        assertThat(onlineComServer.getEventRegistrationUri()).isEqualTo("ws://online.comserver.energyict.com:" + ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER + "/events/registration");
        assertThat(onlineComServer.getStoreTaskQueueSize()).isEqualTo(50);
        assertThat(onlineComServer.getNumberOfStoreTaskThreads()).isEqualTo(1);
        assertThat(onlineComServer.getStoreTaskThreadPriority()).isEqualTo(5);
    }

    @Test
    @Transactional
    public void testRemote () throws JSONException {
        this.createTestRemoteComServer(engineConfigurationService);
        ComServerParser comServerParser = new ComServerParser(engineConfigurationService);
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
        assertThat(remoteComServer.getEventRegistrationPort()).isEqualTo(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        assertThat(remoteComServer.getEventRegistrationUri()).isEqualTo("ws://remote.comserver.energyict.com:" + ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER + "/events/registration");
    }

    private OnlineComServer createTestOnlineComServer(EngineConfigurationService engineConfigurationService) {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = engineConfigurationService.newOnlineComServerBuilder();
        String name = "online.comserver.energyict.com";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.changesInterPollDelay(TimeDuration.seconds(18000));
        onlineComServer.schedulingInterPollDelay(TimeDuration.seconds(60));
        onlineComServer.storeTaskQueueSize(50);
        onlineComServer.storeTaskThreadPriority(Thread.NORM_PRIORITY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return onlineComServer.create();
    }

    private RemoteComServer createTestRemoteComServer (EngineConfigurationService engineConfigurationService) {
        OnlineComServer onlineComServer = this.createTestOnlineComServer(engineConfigurationService);
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = engineConfigurationService.newRemoteComServerBuilder();
        String name = "remote.comserver.energyict.com";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.changesInterPollDelay(TimeDuration.seconds(1800));
        remoteComServer.schedulingInterPollDelay(TimeDuration.seconds(600));
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return remoteComServer.create();
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
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

}
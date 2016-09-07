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
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.protocol.api.ComPortType;
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
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.ComPortParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (13:45)
 */
public class ComPortParserTest {

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
                new UserModule(),
                new TransactionModule(false),
                new DataVaultModule(),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(NlsService.class);
            injector.getInstance(ProtocolPluggableService.class);
            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            ctx.commit();
        }
    }

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @AfterClass
    public static void cleanupDatabase() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testDelegateToEngineModelService () throws JSONException {
        EngineConfigurationService engineConfigurationService = mock(EngineConfigurationService.class);
        JSONObject jsonObject = new JSONObject("{\"query-id\":\"refreshComPort\",\"single-value\":{\"id\":\"0\",\"name\":\"TCP\",\"active\":true,\"numberOfSimultaneousConnections\":5,\"type\":\"OutboundComPortImpl\"}}");
        JSONObject comPortJSon = (JSONObject) jsonObject.get("single-value");
        ComPortParser comPortParser = new ComPortParser(engineConfigurationService);

        // Business method
        comPortParser.parse(jsonObject);

        // Asserts
        verify(engineConfigurationService).parseComPortQueryResult(comPortJSon);
    }

    @Test
    @Transactional
    public void testOutbound () throws JSONException {
        OutboundComPort outboundComPort = this.createTestOnlineComServer(engineConfigurationService);
        JSONObject jsonObject = new JSONObject("{\"query-id\":\"refreshComPort\",\"single-value\":{\"id\":\"" + outboundComPort.getId() + "\",\"name\":\"TCP\",\"active\":true,\"numberOfSimultaneousConnections\":5,\"type\":\"OutboundComPortImpl\"}}");
        ComPortParser comPortParser = new ComPortParser(engineConfigurationService);

        // Business method
        ComPort parsed = comPortParser.parse(jsonObject);

        // Asserts
        assertThat(parsed).isInstanceOf(OutboundComPort.class);
        OutboundComPort parsedOutboundComPort = (OutboundComPort) parsed;
        assertThat(parsedOutboundComPort.getName()).isEqualTo("TCP");
        assertThat(parsedOutboundComPort.isActive()).isTrue();
        assertThat(parsedOutboundComPort.getNumberOfSimultaneousConnections()).isEqualTo(5);
    }

    private OutboundComPort createTestOnlineComServer(EngineConfigurationService engineConfigurationService) {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = engineConfigurationService.newOnlineComServerBuilder();
        String name = "online.comserver.energyict.com";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.ERROR);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.changesInterPollDelay(TimeDuration.seconds(18000));
        onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.seconds(60));
        onlineComServerBuilder.storeTaskQueueSize(50);
        onlineComServerBuilder.storeTaskThreadPriority(Thread.NORM_PRIORITY);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        return onlineComServer.newOutboundComPort("TCP", 5).active(true).comPortType(ComPortType.TCP).add();
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
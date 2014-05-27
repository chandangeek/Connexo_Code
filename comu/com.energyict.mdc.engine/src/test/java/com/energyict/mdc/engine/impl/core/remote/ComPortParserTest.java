package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
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

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Test
    public void testDelegateToEngineModelService () throws JSONException {
        EngineModelService engineModelService = mock(EngineModelService.class);
        JSONObject jsonObject = new JSONObject("{\"query-id\":\"refreshComPort\",\"single-value\":{\"id\":\"0\",\"name\":\"TCP\",\"active\":true,\"numberOfSimultaneousConnections\":5,\"type\":\"OutboundComPortImpl\"}}");
        JSONObject comPortJSon = (JSONObject) jsonObject.get("single-value");
        ComPortParser comPortParser = new ComPortParser(engineModelService);

        // Business method
        comPortParser.parse(jsonObject);

        // Asserts
        verify(engineModelService).parseComPortQueryResult(comPortJSon);
    }

    @Test
    @Transactional
    public void testOutbound () throws JSONException {
        OutboundComPort outboundComPort = this.createTestOnlineComServer(engineModelService);
        JSONObject jsonObject = new JSONObject("{\"query-id\":\"refreshComPort\",\"single-value\":{\"id\":\"" + outboundComPort.getId() + "\",\"name\":\"TCP\",\"active\":true,\"numberOfSimultaneousConnections\":5,\"type\":\"OutboundComPortImpl\"}}");
        ComPortParser comPortParser = new ComPortParser(engineModelService);

        // Business method
        ComPort parsed = comPortParser.parse(jsonObject);

        // Asserts
        assertThat(parsed).isInstanceOf(OutboundComPort.class);
        OutboundComPort parsedOutboundComPort = (OutboundComPort) parsed;
        assertThat(parsedOutboundComPort.getName()).isEqualTo("TCP");
        assertThat(parsedOutboundComPort.isActive()).isTrue();
        assertThat(parsedOutboundComPort.getNumberOfSimultaneousConnections()).isEqualTo(5);
    }

    private OutboundComPort createTestOnlineComServer(EngineModelService engineModelService) {
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
        }
    }

}
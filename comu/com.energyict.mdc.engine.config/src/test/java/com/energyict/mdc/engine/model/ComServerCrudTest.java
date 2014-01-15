package com.energyict.mdc.engine.model;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() throws SQLException {
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new MdcCommonModule(),
                new TransactionModule(true),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(EngineModelService.class);
        	injector.getInstance(EnvironmentImpl.class); // fake call to make sure component is initialized
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private EngineModelService getEngineModelService() {
        return injector.getInstance(EngineModelService.class);
    }

    @Test
    public void testCreateLoadOfflineComServer() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
            OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
            offlineComServer.setName("Offliner");
            offlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
            offlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
            offlineComServer.setChangesInterPollDelay(new TimeDuration(600));
            offlineComServer.setSchedulingInterPollDelay(new TimeDuration(900));
            offlineComServer.setActive(false);
            offlineComServer.save();
            context.commit();
        }

        ComServer offlineComServer = getEngineModelService().findComServer("Offliner");
        assertTrue(offlineComServer instanceof OfflineComServer);
        assertThat(offlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(600));
        assertThat(offlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(900));
        assertThat(offlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(offlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(offlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    public void testCreateLoadOnlineComServerWithDefaultUris() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
            onlineComServer.setName("Onliner");
            onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
            onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
            onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
            onlineComServer.setActive(false);
            onlineComServer.setUsesDefaultQueryAPIPostUri(true);
            onlineComServer.setStoreTaskQueueSize(10);
            onlineComServer.setStoreTaskThreadPriority(3);
            onlineComServer.setNumberOfStoreTaskThreads(6);
            onlineComServer.setUsesDefaultEventRegistrationUri(true);

            onlineComServer.save();
            context.commit();
        }

        ComServer onlineComServer = getEngineModelService().findComServer("Onliner");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) onlineComServer).usesDefaultQueryApiPostUri()).isEqualTo(true);
        assertThat(((OnlineComServer) onlineComServer).getQueryApiPostUri()).isEqualTo("http://Onliner:8889/remote/queries");
        assertThat(((OnlineComServer) onlineComServer).usesDefaultEventRegistrationUri()).isEqualTo(true);
        assertThat(((OnlineComServer) onlineComServer).getEventRegistrationUri()).isEqualTo("ws://Onliner:8888/events/registration");
        assertThat(((OnlineComServer) onlineComServer).getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(onlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    public void testCreateLoadOnlineComServerWithCustomUris() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
            onlineComServer.setName("Onliner-2");
            onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
            onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
            onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
            onlineComServer.setActive(false);
            onlineComServer.setStoreTaskQueueSize(10);
            onlineComServer.setStoreTaskThreadPriority(3);
            onlineComServer.setNumberOfStoreTaskThreads(6);
            onlineComServer.setEventRegistrationUri("/some/uri");
            onlineComServer.setQueryAPIPostUri("/another/uri");

            onlineComServer.save();
            context.commit();
        }

        ComServer onlineComServer = getEngineModelService().findComServer("Onliner-2");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) onlineComServer).usesDefaultQueryApiPostUri()).isEqualTo(false);
        assertThat(((OnlineComServer) onlineComServer).getQueryApiPostUri()).isEqualTo("/another/uri");
        assertThat(((OnlineComServer) onlineComServer).usesDefaultEventRegistrationUri()).isEqualTo(false);
        assertThat(((OnlineComServer) onlineComServer).getEventRegistrationUri()).isEqualTo("/some/uri");
        assertThat(((OnlineComServer) onlineComServer).getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(onlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    public void testCreateLoadOnlineComServerWithComPort() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
            onlineComServer.setName("Onliner-3");
            onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
            onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
            onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
            onlineComServer.setActive(false);
            onlineComServer.setUsesDefaultQueryAPIPostUri(true);
            onlineComServer.setStoreTaskQueueSize(10);
            onlineComServer.setStoreTaskThreadPriority(3);
            onlineComServer.setNumberOfStoreTaskThreads(6);
            onlineComServer.setUsesDefaultEventRegistrationUri(true);

            OutboundComPort outboundComPort = getEngineModelService().newOutbound(onlineComServer);
            outboundComPort.setName("some comport");
            outboundComPort.setComPortType(ComPortType.TCP);
            outboundComPort.setNumberOfSimultaneousConnections(4);
            List<ComPort> comPorts = new ArrayList<>();
            comPorts.add(outboundComPort);
            onlineComServer.setComPorts(comPorts);
            onlineComServer.save();
            context.commit();
        }

        ComServer onlineComServer = getEngineModelService().findComServer("Onliner-3");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getComPorts()).hasSize(1);
    }

    private static class MockModule extends AbstractModule {
        private BundleContext bundleContext;

        private MockModule(BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
        }

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
        }
    }

}

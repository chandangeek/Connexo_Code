package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15/01/14
 * Time: 15:02
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCollectedDataIntegrationTest {

    private static Injector injector;
    private static InMemoryBootstrapModule bootstrapModule;

    @Mock
    private DeviceFactory deviceFactory;

    @BeforeClass
    public static void setupEnvironment() {
        BundleContext bundleContext = mock(BundleContext.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence");
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
        EventAdmin eventAdmin = mock(EventAdmin.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext, eventAdmin),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new DomainUtilModule(),
                new UtilModule(),
                new OrmModule(),
                new MdcCommonModule(),
                new MdcDynamicModule(),
                new IdsModule(),
                new EventsModule(),
                new PartyModule(),
                new MeteringModule(),
                new UserModule(),
                new InMemoryMessagingModule());
        initializeTopModuleInATransaction();
    }

    private static void initializeTopModuleInATransaction() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                injector.getInstance(MeteringService.class);
            }
        });
    }

    @AfterClass
    public static void tearDownEnvironment() {
        bootstrapModule.deactivate();
    }

    protected static Injector getInjector(){
        return injector;
    }

    protected <T> T executeInTransaction(Transaction<T> transaction) {
        return injector.getInstance(TransactionService.class).execute(transaction);
    }

    protected ComServerDAOImpl mockComServerDAOButCallRealMethodForMeterReadingStoring() {
        final ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doCallRealMethod().when(comServerDAO).storeMeterReadings(any(DeviceIdentifier.class), any(MeterReading.class));
        return comServerDAO;
    }

    protected Device mockDevice(long deviceId) {
        DeviceImpl device = mock(DeviceImpl.class);
        when(device.getId()).thenReturn(deviceId);
        doCallRealMethod().when(device).store(any(MeterReading.class));
        when(deviceFactory.findById(deviceId)).thenReturn(device);
        return device;
    }

    private static class MockModule extends AbstractModule {

        private BundleContext bundleContext;

        private EventAdmin eventAdmin;

        private MockModule(BundleContext bundleContext, EventAdmin eventAdmin) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = eventAdmin;
        }

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
        }

    }
}

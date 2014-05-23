package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.impl.NlsModule;
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
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.EngineModule;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.history.impl.TaskHistoryModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 * Date: 15/01/14
 * Time: 15:02
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCollectedDataIntegrationTest {

    private static Injector injector;
    private static InMemoryBootstrapModule bootstrapModule;
    static MeteringService meteringService;
    private static DeviceDataService deviceDataService;
    private static SocketService socketService;
    private static SerialComponentService serialComponentService;

    @Mock
    private DeviceFactory deviceFactory;
    private static TransactionService transactionService;

    @BeforeClass
    public static void setupEnvironment() {
        BundleContext bundleContext = mock(BundleContext.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence");
        bootstrapModule = new InMemoryBootstrapModule();
        EventAdmin eventAdmin = mock(EventAdmin.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext, eventAdmin, deviceDataService, socketService, serialComponentService),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new DomainUtilModule(),
                new UtilModule(),
                new NlsModule(),
                new OrmModule(),
                new MdcCommonModule(),
                new MdcDynamicModule(),
                new IdsModule(),
                new EventsModule(),
                new PartyModule(),
                new MeteringModule(),
                new UserModule(),
                new InMemoryMessagingModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new EngineModule(),
                new ProtocolsModule(),
                new PluggableModule(),
                new TaskHistoryModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new MdcReadingTypeUtilServiceModule(),
                new SchedulingModule(),
                new TasksModule(),
                new IssuesModule());
        initializeTopModuleInATransaction();
    }

    private static void initializeTopModuleInATransaction() {
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                injector.getInstance(MeteringService.class);
                injector.getInstance(EngineService.class);
                meteringService = injector.getInstance(MeteringService.class);
            }
        });
    }

    @AfterClass
    public static void tearDownEnvironment() {
        bootstrapModule.deactivate();
        ServiceProvider.instance.set(null);
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
        when(comServerDAO.executeTransaction(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return transactionService.execute((Transaction<?>) invocation.getArguments()[0]);
            }
        });
        return comServerDAO;
    }

    protected Device mockDevice(long deviceId) {
        DeviceImpl device = mock(DeviceImpl.class, RETURNS_DEEP_STUBS);
        when(device.getId()).thenReturn(deviceId);
        doCallRealMethod().when(device).store(any(MeterReading.class));
        when(deviceDataService.findDeviceById(deviceId)).thenReturn(device);
        return device;
    }

    private static class MockModule extends AbstractModule {

        private final BundleContext bundleContext;
        private final EventAdmin eventAdmin;
        private final DeviceDataService deviceDataService;
        private final SocketService socketService;
        private final SerialComponentService serialComponentService;

        private MockModule(BundleContext bundleContext, EventAdmin eventAdmin, DeviceDataService deviceDataService, SocketService socketService, SerialComponentService serialComponentService) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = eventAdmin;
            this.deviceDataService = deviceDataService;
            this.socketService = socketService;
            this.serialComponentService = serialComponentService;
        }

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DeviceDataService.class).toInstance(deviceDataService);
            bind(SocketService.class).toInstance(socketService);
            bind(SerialComponentService.class).toInstance(serialComponentService);
        }

    }
}

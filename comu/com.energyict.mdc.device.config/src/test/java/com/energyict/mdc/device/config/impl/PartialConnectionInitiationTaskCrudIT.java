package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialConnectionInitiationTaskCrudIT {

    private OutboundComPortPool outboundComPortPool, outboundComPortPool1;
    private ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private DataModel dataModel;
    private Injector injector;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private LicensedProtocolService licensedProtocolService;
    private ConnectionTypeService connectionTypeService;
    private EngineConfigurationService engineConfigurationService;
    private InMemoryBootstrapModule bootstrapModule;
    @Mock
    private LicenseService licenseService;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }
    }

    public void initializeDatabase(boolean showSqlLogging) {
        this.initializeMocks();
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new DataVaultModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new MasterDataModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new MdcIOModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new ValidationModule(),
                new IssuesModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new SchedulingModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(MdcReadingTypeUtilService.class);
            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            protocolPluggableService = (ProtocolPluggableServiceImpl) injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            protocolPluggableService.addConnectionTypeService(connectionTypeService);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(PluggableService.class);
            injector.getInstance(ValidationService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
    }

    private void initializeMocks() {
        this.licensedProtocolService = mock(LicensedProtocolService.class);
        when(this.licensedProtocolService.isValidJavaClassName(anyString(), any(License.class))).thenReturn(true);
        this.connectionTypeService = mock(ConnectionTypeService.class);
        when(this.connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
    }

    @Before
    public void setUp() {
        when(principal.getName()).thenReturn("test");
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        initializeDatabase(false);
        engineConfigurationService = injector.getInstance(EngineConfigurationService.class);

        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", OutboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass2.save();
            outboundComPortPool = engineConfigurationService.newOutboundComPortPool("inboundComPortPool", ComPortType.TCP, TimeDuration.minutes(15));
            outboundComPortPool.setActive(true);
            outboundComPortPool.save();
            outboundComPortPool1 = engineConfigurationService.newOutboundComPortPool("inboundComPortPool2", ComPortType.TCP, TimeDuration.minutes(5));
            outboundComPortPool1.setActive(true);
            outboundComPortPool1.save();
            context.commit();
        }
    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    public void testCreate() {

        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();

            connectionInitiationTask = deviceConfiguration
                    .newPartialConnectionInitiationTask(
                            "MyInitiation",
                            connectionTypePluggableClass,
                            TimeDuration.seconds(60))
                    .comPortPool(outboundComPortPool)
                    .build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialConnectionInitiationTaskImpl.class);

        PartialConnectionInitiationTaskImpl partialConnectionInitiationTask = (PartialConnectionInitiationTaskImpl) partialConnectionTask;

        assertThat(partialConnectionInitiationTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialConnectionInitiationTask.isDefault()).isFalse();
        assertThat(partialConnectionInitiationTask.getConfiguration().getCommunicationConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialConnectionInitiationTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialConnectionInitiationTask.getName()).isEqualTo("MyInitiation");

    }

//            partialInboundConnectionTask.setName("Changed");
    @Test
    public void testUpdate() {

        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            connectionInitiationTask = deviceConfiguration
                    .newPartialConnectionInitiationTask(
                            "MyInitiation",
                            connectionTypePluggableClass,
                            TimeDuration.seconds(60))
                    .comPortPool(outboundComPortPool)
                    .build();
            deviceConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            PartialConnectionInitiationTask partialConnectionInitiationTask = deviceConfiguration.getPartialConnectionInitiationTasks().get(0);
            partialConnectionInitiationTask.setComportPool(outboundComPortPool1);
            partialConnectionInitiationTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
            partialConnectionInitiationTask.setName("Changed");
            partialConnectionInitiationTask.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialConnectionInitiationTaskImpl.class);

        PartialConnectionInitiationTaskImpl partialConnectionInitiationTask = (PartialConnectionInitiationTaskImpl) partialConnectionTask;

        assertThat(partialConnectionInitiationTask.getComPortPool().getId()).isEqualTo(outboundComPortPool1.getId());
        assertThat(partialConnectionInitiationTask.isDefault()).isFalse();
        assertThat(partialConnectionInitiationTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialConnectionInitiationTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(partialConnectionInitiationTask.getName()).isEqualTo("Changed");
    }

    @Test
    public void testDelete() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();


            connectionInitiationTask = deviceConfiguration
                    .newPartialConnectionInitiationTask(
                            "MyOutbound",
                            connectionTypePluggableClass,
                            TimeDuration.seconds(60))
                    .comPortPool(outboundComPortPool)
                    .build();
            deviceConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            DeviceCommunicationConfiguration configuration = deviceConfiguration.getCommunicationConfiguration();
            PartialConnectionInitiationTask partialOutboundConnectionTask = configuration.getPartialConnectionInitiationTasks().get(0);
            configuration.remove(partialOutboundConnectionTask);
            configuration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isFalse();

    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
    public void createWithIncorrectConnectionTypeTest() {
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            ConnectionTypePluggableClass inboundConnectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("OutboundNoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
            inboundConnectionTypePluggableClass.save();
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();

            deviceConfiguration
                    .newPartialConnectionInitiationTask(
                            "MyInitiation",
                            inboundConnectionTypePluggableClass,
                            TimeDuration.seconds(60))
                    .comPortPool(outboundComPortPool)
                    .build();
            deviceConfiguration.save();

            context.commit();
        }
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

}
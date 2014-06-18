package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
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
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.protocols.mdc.inbound.dlms.DlmsSerialNumberDiscover;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialInboundConnectiontaskCrudIT {

    private InboundComPortPool inboundComPortPool, inboundComPortPool2;
    private ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;

    @Rule
    public final TestRule rule1 = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    DeviceProtocol deviceProtocol;
    @Mock
    private LicenseService licenseService;

    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private NlsService nlsService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;
    private DataModel dataModel;
    private Injector injector;
    @Mock
    private ApplicationContext applicationContext;
    private ProtocolPluggableService protocolPluggableService;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private EngineModelService engineModelService;
    private InMemoryBootstrapModule bootstrapModule;
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    private InboundDeviceProtocolPluggableClass discoveryPluggable;
    @Mock
    private IdBusinessObjectFactory businessObjectFactory;

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

    public void initializeDatabase(boolean showSqlLogging, boolean createMasterData) {
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
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new TasksModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new SchedulingModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            ormService = injector.getInstance(OrmService.class);
            eventService = injector.getInstance(EventService.class);
            nlsService = injector.getInstance(NlsService.class);
            meteringService = injector.getInstance(MeteringService.class);
            readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
            engineModelService = injector.getInstance(EngineModelService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            inboundDeviceProtocolService = injector.getInstance(InboundDeviceProtocolService.class);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(applicationContext);
    }

    @Before
    public void setUp() {
        when(principal.getName()).thenReturn("test");
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        when(applicationContext.findFactory(5011)).thenReturn(businessObjectFactory);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>absent());
        initializeDatabase(false, false);
        protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
        engineModelService = injector.getInstance(EngineModelService.class);

        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", InboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", InboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass2.save();
            discoveryPluggable = protocolPluggableService.newInboundDeviceProtocolPluggableClass("MyDiscoveryName", DlmsSerialNumberDiscover.class.getName());
            discoveryPluggable.save();
            inboundComPortPool = engineModelService.newInboundComPortPool();
            inboundComPortPool.setActive(true);
            inboundComPortPool.setComPortType(ComPortType.TCP);
            inboundComPortPool.setName("inboundComPortPool");
            inboundComPortPool.setDiscoveryProtocolPluggableClass(discoveryPluggable);
            inboundComPortPool.save();
            inboundComPortPool2 = engineModelService.newInboundComPortPool();
            inboundComPortPool2.setActive(true);
            inboundComPortPool2.setComPortType(ComPortType.TCP);
            inboundComPortPool2.setName("inboundComPortPool2");
            inboundComPortPool2.setDiscoveryProtocolPluggableClass(discoveryPluggable);
            inboundComPortPool2.save();
            context.commit();
        }


    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    public void testCreate() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl partialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(partialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool.getId());
        assertThat(partialInboundConnectionTask.isDefault()).isTrue();
        assertThat(partialInboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialInboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("MyInbound");

    }

    @Test
    public void createDefaultWithoutDefaultTest() {
        PartialInboundConnectionTaskImpl notTheDefault;
        PartialInboundConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            notTheDefault = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(false).build();
            theDefault = deviceConfiguration.newPartialInboundConnectionTask("MyDefault", connectionTypePluggableClass2)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.getPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.getPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault).isPresent();
        assertThat(foundTheDefault).isPresent();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    public void createDefaultWithAlreadyDefaultTest() {
        PartialInboundConnectionTaskImpl notTheDefault;
        PartialInboundConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            notTheDefault = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            theDefault = deviceConfiguration.newPartialInboundConnectionTask("MyDefault", connectionTypePluggableClass2)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.getPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.getPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault).isPresent();
        assertThat(foundTheDefault).isPresent();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    public void testUpdate() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            PartialInboundConnectionTask partialInboundConnectionTask = deviceConfiguration.getPartialInboundConnectionTasks().get(0);
            partialInboundConnectionTask.setDefault(false);
            partialInboundConnectionTask.setComportPool(inboundComPortPool2);
            partialInboundConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
            partialInboundConnectionTask.setName("Changed");
            partialInboundConnectionTask.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found).isPresent();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl partialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(partialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool2.getId());
        assertThat(partialInboundConnectionTask.isDefault()).isFalse();
        assertThat(partialInboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialInboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("Changed");

    }


    @Test
    public void updateToDefaultWithoutCurrentDefaultTest() {
        PartialInboundConnectionTaskImpl notTheDefault;
        PartialInboundConnectionTaskImpl theToBeDefault;
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            notTheDefault = deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(false).build();
            theToBeDefault = deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2)
                    .comPortPool(inboundComPortPool)
                    .asDefault(false).build();
            deviceConfiguration.save();

            context.commit();
        }

        PartialInboundConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
            task.setDefault(true);
            task.save();

            context.commit();
        }

        DeviceConfiguration reloadedDeviceConfig = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());
        PartialInboundConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        Assertions.assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialInboundConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        Assertions.assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    @Test
    public void updateToDefaultWithCurrentDefaultTest() {
        PartialInboundConnectionTaskImpl temporaryDefault;
        PartialInboundConnectionTaskImpl theToBeDefault;
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            temporaryDefault = deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            theToBeDefault = deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2)
                    .comPortPool(inboundComPortPool)
                    .asDefault(false).build();
            deviceConfiguration.save();

            context.commit();
        }

        PartialInboundConnectionTask initialDefault = getConnectionTaskWithName(deviceConfiguration, connectionTaskName1);
        Assertions.assertThat(initialDefault.isDefault()).isTrue();

        PartialInboundConnectionTask task;
        try (TransactionContext context = transactionService.getContext()) {
            task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
            task.setDefault(true);
            task.save();

            context.commit();
        }

        DeviceConfiguration reloadedDeviceConfig = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());
        PartialInboundConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        Assertions.assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialInboundConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        Assertions.assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    private PartialInboundConnectionTask getConnectionTaskWithName(DeviceConfiguration deviceConfiguration, String connectionTaskName) {
        for (PartialInboundConnectionTask partialScheduledConnectionTask : deviceConfiguration.getPartialInboundConnectionTasks()) {
            if(partialScheduledConnectionTask.getName().equals(connectionTaskName)){
                return partialScheduledConnectionTask;
            }
        }
        return null;
    }

    @Test
    public void testDelete() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            PartialInboundConnectionTask partialInboundConnectionTask = deviceConfiguration.getPartialInboundConnectionTasks().get(0);
            deviceConfiguration.remove(partialInboundConnectionTask);
            deviceConfiguration.save();

            context.commit();
        }

        Optional<PartialConnectionTask> found = deviceConfigurationService.getPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found).isAbsent();

    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC + '}')
    public void testCreateWithUnspeccedProperty() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceCommunicationConfiguration communicationConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(deviceConfiguration);
            communicationConfiguration.save();

            inboundConnectionTask = communicationConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true)
                    .addProperty("unspecced", true)
                    .build();
            communicationConfiguration.save();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testCreateWithDuplicateName() {

        PartialInboundConnectionTaskImpl inboundConnectionTask;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }


    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
    public void createWithIncorrectConnectionTypeTest() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            ConnectionTypePluggableClass outboundConnectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("OutboundNoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
            outboundConnectionTypePluggableClass.save();

            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInboundWhichHasAnOutboundType", outboundConnectionTypePluggableClass)
                    .comPortPool(inboundComPortPool)
                    .asDefault(true).build();
            deviceConfiguration.save();

            context.commit();
        }
    }


    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

}

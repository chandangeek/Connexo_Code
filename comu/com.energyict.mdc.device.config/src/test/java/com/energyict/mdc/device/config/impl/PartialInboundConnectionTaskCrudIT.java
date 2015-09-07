package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
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
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialInboundConnectionTaskCrudIT {

    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);
    @Rule
    public final TestRule rule1 = new ExpectedConstraintViolationRule();

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static LicenseService licenseService;
    private static TransactionService transactionService;
    private static EngineConfigurationService engineConfigurationService;
    private static ProtocolPluggableService protocolPluggableService;
    private static DeviceConfigurationServiceImpl deviceConfigurationService;
    private static ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;
    private static LicensedProtocolService licensedProtocolService;
    private static ConnectionTypeService connectionTypeService;
    private static InboundComPortPool inboundComPortPool, inboundComPortPool2;

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
        }

    }

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(PartialInboundConnectionTaskCrudIT.class.getSimpleName());
        Injector injector = null;
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    bootstrapModule,
                    new ThreadSecurityModule(principal),
                    new EventsModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new UtilModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new ProtocolApiModule(),
                    new TasksModule(),
                    new DeviceLifeCycleConfigurationModule(),
                    new DeviceConfigurationModule(),
                    new MdcIOModule(),
                    new EngineModelModule(),
                    new ProtocolPluggableModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(MdcReadingTypeUtilService.class);
            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            injector.getInstance(InboundDeviceProtocolService.class);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
        setupMasterData();
    }

    private static void initializeStaticMocks() {
        eventAdmin = mock(EventAdmin.class);
        bundleContext = mock(BundleContext.class);
        licenseService = mock(LicenseService.class);
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        bootstrapModule = new InMemoryBootstrapModule();
        licensedProtocolService = mock(LicensedProtocolService.class);
        when(licensedProtocolService.isValidJavaClassName(anyString(), any(License.class))).thenReturn(true);
        connectionTypeService = mock(ConnectionTypeService.class);
        when(connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(InboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new InboundNoParamsConnectionTypeImpl());
    }

    private static void setupMasterData () {
        try (TransactionContext context = transactionService.getContext()) {
            ((ProtocolPluggableServiceImpl) protocolPluggableService).addInboundDeviceProtocolService(new InboundDeviceProtocolService());
            ((ProtocolPluggableServiceImpl) protocolPluggableService).addLicensedProtocolService(licensedProtocolService);
            ((ProtocolPluggableServiceImpl) protocolPluggableService).addConnectionTypeService(connectionTypeService);
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", InboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", InboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass2.save();
            InboundDeviceProtocolPluggableClass discoveryPluggable = protocolPluggableService.newInboundDeviceProtocolPluggableClass("MyDiscoveryName", DummyInboundDiscoveryProtocol.class.getName());
            discoveryPluggable.save();
            inboundComPortPool = engineConfigurationService.newInboundComPortPool("inboundComPortPool", ComPortType.TCP, discoveryPluggable);
            inboundComPortPool.setActive(true);
            inboundComPortPool.save();
            inboundComPortPool2 = engineConfigurationService.newInboundComPortPool("inboundComPortPool2", ComPortType.TCP, discoveryPluggable);
            inboundComPortPool2.setActive(true);
            inboundComPortPool2.save();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());
    }

    @Test
    @Transactional
    public void testCreate() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

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
    @Transactional
    public void createDefaultWithoutDefaultTest() {
        PartialInboundConnectionTaskImpl notTheDefault;
        PartialInboundConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
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

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.findPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.findPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault.isPresent()).isTrue();
        assertThat(foundTheDefault.isPresent()).isTrue();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void createDefaultWithAlreadyDefaultTest() {
        PartialInboundConnectionTaskImpl notTheDefault;
        PartialInboundConnectionTaskImpl theDefault;
        DeviceConfiguration deviceConfiguration;
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

        Optional<PartialConnectionTask> foundTheNotDefault = deviceConfigurationService.findPartialConnectionTask(notTheDefault.getId());
        Optional<PartialConnectionTask> foundTheDefault = deviceConfigurationService.findPartialConnectionTask(theDefault.getId());
        assertThat(foundTheNotDefault.isPresent()).isTrue();
        assertThat(foundTheDefault.isPresent()).isTrue();
        assertThat(foundTheNotDefault.get().isDefault()).isFalse();
        assertThat(foundTheDefault.get().isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdate() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        PartialInboundConnectionTask partialInboundConnectionTask = deviceConfiguration.getPartialInboundConnectionTasks().get(0);
        partialInboundConnectionTask.setDefault(false);
        partialInboundConnectionTask.setComportPool(inboundComPortPool2);
        partialInboundConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
        partialInboundConnectionTask.setName("Changed");
        partialInboundConnectionTask.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl reloadedPartialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(reloadedPartialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool2.getId());
        assertThat(reloadedPartialInboundConnectionTask.isDefault()).isFalse();
        assertThat(reloadedPartialInboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(reloadedPartialInboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass2.getConnectionType());
        assertThat(reloadedPartialInboundConnectionTask.getName()).isEqualTo("Changed");
    }

    @Test
    @Transactional
    public void updateToDefaultWithoutCurrentDefaultTest() {
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(false).build();
        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2)
                .comPortPool(inboundComPortPool)
                .asDefault(false).build();
        deviceConfiguration.save();

        PartialInboundConnectionTask task;
        task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
        task.setDefault(true);
        task.save();

        DeviceConfiguration reloadedDeviceConfig =
                deviceConfigurationService
                        .findDeviceConfiguration(deviceConfiguration.getId())
                        .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + deviceConfiguration.getId()));
        PartialInboundConnectionTask partialConnectionTask1 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName1);
        Assertions.assertThat(partialConnectionTask1.isDefault()).isFalse();
        PartialInboundConnectionTask partialConnectionTask2 = getConnectionTaskWithName(reloadedDeviceConfig, connectionTaskName2);
        Assertions.assertThat(partialConnectionTask2.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void updateToDefaultWithCurrentDefaultTest() {
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2)
                .comPortPool(inboundComPortPool)
                .asDefault(false).build();
        deviceConfiguration.save();

        PartialInboundConnectionTask initialDefault = getConnectionTaskWithName(deviceConfiguration, connectionTaskName1);
        Assertions.assertThat(initialDefault.isDefault()).isTrue();

        PartialInboundConnectionTask task;
        task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
        task.setDefault(true);
        task.save();

        DeviceConfiguration reloadedDeviceConfig =
                deviceConfigurationService
                        .findDeviceConfiguration(deviceConfiguration.getId())
                        .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + deviceConfiguration.getId()));
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
    @Transactional
    public void testDelete() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        PartialInboundConnectionTask partialInboundConnectionTask = deviceConfiguration.getPartialInboundConnectionTasks().get(0);
        deviceConfiguration.remove(partialInboundConnectionTask);
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found.isPresent()).isFalse();

    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC + '}')
    public void testCreateWithUnspecifiedProperty() {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true)
                .addProperty("unspecced", true)
                .build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testCreateWithDuplicateName() {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
    public void createWithIncorrectConnectionTypeTest() {
        DeviceConfiguration deviceConfiguration;
        ConnectionTypePluggableClass outboundConnectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("OutboundNoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
        outboundConnectionTypePluggableClass.save();

        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInboundWhichHasAnOutboundType", outboundConnectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

    private static class InboundDeviceProtocolService implements com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService {
        @Override
        public InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName) {
            if (DummyInboundDiscoveryProtocol.class.getName().equals(javaClassName)) {
                return new DummyInboundDiscoveryProtocol();
            }
            else {
                throw new RuntimeException("Class " + javaClassName + " not known or supported by this bundle");
            }
        }

        @Override
        public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
            return this.createInboundDeviceProtocolFor(pluggableClass.getJavaClassName());
        }

        @Override
        public Collection<PluggableClassDefinition> getExistingInboundDeviceProtocolPluggableClasses() {
            PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
            when(pluggableClassDefinition.getName()).thenReturn(DummyInboundDiscoveryProtocol.class.getSimpleName());
            when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(DummyInboundDiscoveryProtocol.class);
            return Arrays.asList(pluggableClassDefinition);
        }
    }

    @Test
    @Transactional
    public void cloneTest() {
        PartialInboundConnectionTaskImpl inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceConfiguration clonedDeviceConfig;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();
        clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        clonedDeviceConfig.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());

        PartialConnectionTask partialConnectionTask = ((ServerPartialConnectionTask) found.get()).cloneForDeviceConfig(clonedDeviceConfig);

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl partialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(partialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool.getId());
        assertThat(partialInboundConnectionTask.isDefault()).isTrue();
        assertThat(partialInboundConnectionTask.getConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
        assertThat(partialInboundConnectionTask.getConnectionType()).isEqualTo(connectionTypePluggableClass.getConnectionType());
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("MyInbound");

    }
}
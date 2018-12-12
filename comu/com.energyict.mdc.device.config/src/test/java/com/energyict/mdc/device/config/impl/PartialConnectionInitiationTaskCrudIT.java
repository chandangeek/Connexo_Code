/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
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
import com.elster.jupiter.kpi.impl.KpiModule;
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
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartialConnectionInitiationTaskCrudIT {

    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static LicenseService licenseService;
    private static TransactionService transactionService;
    private static EngineConfigurationService engineConfigurationService;
    private static ProtocolPluggableServiceImpl protocolPluggableService;
    private static DeviceConfigurationServiceImpl deviceConfigurationService;
    private static LicensedProtocolService licensedProtocolService;
    private static ConnectionTypeService connectionTypeService;
    private static OutboundComPortPool outboundComPortPool, outboundComPortPool1;
    private static ConnectionTypePluggableClass connectionTypePluggableClass, connectionTypePluggableClass2;

    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocolDialect deviceProtocolDialect1, deviceProtocolDialect2, deviceProtocolDialect3;
    @Mock
    private PropertySpec deviceProtocolDialectSpec1, deviceProtocolDialectSpec2, deviceProtocolDialectSpec3, deviceProtocolDialectSpec4;


    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(IdentificationService.class).toInstance(mock(IdentificationService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
        }
    }

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(PartialConnectionInitiationTaskCrudIT.class.getSimpleName());
        bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = null;
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    bootstrapModule,
                    new CustomPropertySetsModule(),
                    new ThreadSecurityModule(principal),
                    new EventsModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new UtilModule(),
                    new NlsModule(),
                    new PkiModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
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
                    new DeviceLifeCycleConfigurationModule(),
                    new DeviceConfigurationModule(),
                    new EngineModelModule(),
                    new ProtocolPluggableModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule(),
                    new CalendarModule(),
                    new WebServicesModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(OrmService.class);
            injector.getInstance(EventService.class);
            injector.getInstance(NlsService.class);
            injector.getInstance(FiniteStateMachineService.class);
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
        licensedProtocolService = mock(LicensedProtocolService.class);
        when(licensedProtocolService.isValidJavaClassName(anyString(), any(License.class))).thenReturn(true);
        connectionTypeService = mock(ConnectionTypeService.class);
        when(connectionTypeService.createConnectionType(OutboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new OutboundNoParamsConnectionTypeImpl());
        when(connectionTypeService.createConnectionType(InboundNoParamsConnectionTypeImpl.class.getName())).thenReturn(new InboundNoParamsConnectionTypeImpl());
    }

    private static void setupMasterData() {
        try (TransactionContext context = transactionService.getContext()) {
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", OutboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass2.save();
            outboundComPortPool = engineConfigurationService.newOutboundComPortPool("inboundComPortPool", ComPortType.TCP, TimeDuration.minutes(15));
            outboundComPortPool.setActive(true);
            outboundComPortPool.update();
            outboundComPortPool1 = engineConfigurationService.newOutboundComPortPool("inboundComPortPool2", ComPortType.TCP, TimeDuration.minutes(5));
            outboundComPortPool1.setActive(true);
            outboundComPortPool1.update();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolDialectSpec1.getName()).thenReturn("deviceProtocolDialectSpec1");
        when(deviceProtocolDialectSpec1.getValueFactory()).thenReturn(new StringFactory());

        when(deviceProtocolDialectSpec2.getName()).thenReturn("deviceProtocolDialectSpec2");
        when(deviceProtocolDialectSpec2.getValueFactory()).thenReturn(new StringFactory());

        when(deviceProtocolDialectSpec3.getName()).thenReturn("deviceProtocolDialectSpec3");
        when(deviceProtocolDialectSpec3.getValueFactory()).thenReturn(new StringFactory());

        when(deviceProtocolDialectSpec4.getName()).thenReturn("deviceProtocolDialectSpec4");
        when(deviceProtocolDialectSpec4.getValueFactory()).thenReturn(new StringFactory());

        when(deviceProtocolDialect1.getDeviceProtocolDialectName()).thenReturn("Device Protocol Dialect 1");
        when(deviceProtocolDialect1.getPropertySpecs()).thenReturn(Arrays.asList(deviceProtocolDialectSpec1, deviceProtocolDialectSpec2, deviceProtocolDialectSpec3, deviceProtocolDialectSpec4));
        when(deviceProtocolDialect2.getDeviceProtocolDialectName()).thenReturn("Device Protocol Dialect 2");
        when(deviceProtocolDialect3.getDeviceProtocolDialectName()).thenReturn("Device Protocol Dialect 3");
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(deviceProtocolDialect1, deviceProtocolDialect2, deviceProtocolDialect3));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
    }

    @Test
    @Transactional
    public void testCreate() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        connectionInitiationTask = deviceConfiguration
                .newPartialConnectionInitiationTask(
                        "MyInitiation",
                        connectionTypePluggableClass,
                        TimeDuration.seconds(60),
                        deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(outboundComPortPool)
                .build();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialConnectionInitiationTaskImpl.class);

        PartialConnectionInitiationTaskImpl partialConnectionInitiationTask = (PartialConnectionInitiationTaskImpl) partialConnectionTask;

        assertThat(partialConnectionInitiationTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialConnectionInitiationTask.isDefault()).isFalse();
        assertThat(partialConnectionInitiationTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(partialConnectionInitiationTask.getName()).isEqualTo("MyInitiation");
        assertThat(partialConnectionInitiationTask.getProtocolDialectConfigurationProperties()).isNotNull();
        assertThat(partialConnectionInitiationTask.getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName()).isEqualTo("Device Protocol Dialect 1");
    }

    /**
     * Tests that saving a {@link PartialConnectionTask} without protocol dialect properties, produces a constraint violation.
     */
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}" , property = "protocolDialectConfigurationProperties")
    public void testCreateWithoutProtocolDialect() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration.newPartialConnectionInitiationTask(
                        "MyInitiation",
                        connectionTypePluggableClass,
                        TimeDuration.seconds(60),
                        null)     // No protocoldialect
                .comPortPool(outboundComPortPool)
                .build();
    }

    @Test
    @Transactional
    public void testUpdate() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        connectionInitiationTask = deviceConfiguration
                .newPartialConnectionInitiationTask(
                        "MyInitiation",
                        connectionTypePluggableClass,
                        TimeDuration.seconds(60), deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(outboundComPortPool)
                .build();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        PartialConnectionInitiationTask partialConnectionInitiationTask = deviceConfiguration.getPartialConnectionInitiationTasks().get(0);
        partialConnectionInitiationTask.setComportPool(outboundComPortPool1);
        partialConnectionInitiationTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
        partialConnectionInitiationTask.setName("Changed");
        partialConnectionInitiationTask.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialConnectionInitiationTaskImpl.class);

        PartialConnectionInitiationTaskImpl reloadedPartialConnectionInitiationTask = (PartialConnectionInitiationTaskImpl) partialConnectionTask;

        assertThat(reloadedPartialConnectionInitiationTask.getComPortPool().getId()).isEqualTo(outboundComPortPool1.getId());
        assertThat(reloadedPartialConnectionInitiationTask.isDefault()).isFalse();
        assertThat(reloadedPartialConnectionInitiationTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(reloadedPartialConnectionInitiationTask.getName()).isEqualTo("Changed");
    }

    @Test
    @Transactional
    public void testUpdateProtocolDialectProperties() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        connectionInitiationTask = deviceConfiguration
                .newPartialConnectionInitiationTask(
                        "MyInitiation",
                        connectionTypePluggableClass,
                        TimeDuration.seconds(60),
                        deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(outboundComPortPool)
                .build();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec1","test property 1");
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec2","test property 2");
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec3","test property 3");
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec4","test property 4");

        PartialConnectionInitiationTask partialConnectionInitiationTask = deviceConfiguration.getPartialConnectionInitiationTasks().get(0);
        partialConnectionInitiationTask.setComportPool(outboundComPortPool1);
        partialConnectionInitiationTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
        partialConnectionInitiationTask.setName("Changed");
        partialConnectionInitiationTask.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
        partialConnectionInitiationTask.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialConnectionInitiationTaskImpl.class);

        PartialConnectionInitiationTaskImpl reloadedPartialConnectionInitiationTask = (PartialConnectionInitiationTaskImpl) partialConnectionTask;

        assertThat(reloadedPartialConnectionInitiationTask.getComPortPool().getId()).isEqualTo(outboundComPortPool1.getId());
        assertThat(reloadedPartialConnectionInitiationTask.isDefault()).isFalse();
        assertThat(reloadedPartialConnectionInitiationTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(reloadedPartialConnectionInitiationTask.getName()).isEqualTo("Changed");
        ProtocolDialectConfigurationProperties dialectConfigurationProperties = reloadedPartialConnectionInitiationTask.getProtocolDialectConfigurationProperties();
        ProtocolDialectConfigurationProperties spyDialectConfigurationProperties = spy(dialectConfigurationProperties);
        when(spyDialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect1); // Need to force this
        assertThat(spyDialectConfigurationProperties.getPropertySpecs()).hasSize(4);
        assertThat(spyDialectConfigurationProperties.getProperty("deviceProtocolDialectSpec1")).isEqualTo("test property 1");
        assertThat(spyDialectConfigurationProperties.getProperty("deviceProtocolDialectSpec2")).isEqualTo("test property 2");
        assertThat(spyDialectConfigurationProperties.getProperty("deviceProtocolDialectSpec3")).isEqualTo("test property 3");
        assertThat(spyDialectConfigurationProperties.getProperty("deviceProtocolDialectSpec4")).isEqualTo("test property 4");
    }

    @Test
    @Transactional
    public void testDelete() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        connectionInitiationTask = deviceConfiguration
                .newPartialConnectionInitiationTask(
                        "MyOutbound",
                        connectionTypePluggableClass,
                        TimeDuration.seconds(60), deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(outboundComPortPool)
                .build();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        PartialConnectionInitiationTask partialOutboundConnectionTask = deviceConfiguration.getPartialConnectionInitiationTasks().get(0);
        deviceConfiguration.remove(partialOutboundConnectionTask);
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(connectionInitiationTask.getId());
        assertThat(found.isPresent()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
    public void createWithIncorrectConnectionTypeTest() {
        DeviceConfiguration deviceConfiguration;
        ConnectionTypePluggableClass inboundConnectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("InboundNoParamsConnectionType", InboundNoParamsConnectionTypeImpl.class.getName());

        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        deviceConfiguration
                .newPartialConnectionInitiationTask(
                        "MyInitiation",
                        inboundConnectionTypePluggableClass,
                        TimeDuration.seconds(60), deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(outboundComPortPool)
                .build();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }


    @Test
    @Transactional
    public void cloneTest() {
        PartialConnectionInitiationTaskImpl connectionInitiationTask;
        DeviceConfiguration deviceConfiguration;
        DeviceConfiguration clonedDeviceConfig;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();
        clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        clonedDeviceConfig.setDirectlyAddressable(true);
        clonedDeviceConfig.save();

        connectionInitiationTask = deviceConfiguration
                .newPartialConnectionInitiationTask(
                        "MyInitiation",
                        connectionTypePluggableClass,
                        TimeDuration.seconds(60), deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(outboundComPortPool)
                .build();
        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(connectionInitiationTask.getId());
        PartialConnectionTask partialConnectionTask = ((ServerPartialConnectionTask) found.get()).cloneForDeviceConfig(clonedDeviceConfig);

        assertThat(partialConnectionTask).isInstanceOf(PartialConnectionInitiationTaskImpl.class);

        PartialConnectionInitiationTaskImpl partialConnectionInitiationTask = (PartialConnectionInitiationTaskImpl) partialConnectionTask;

        assertThat(partialConnectionInitiationTask.getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(partialConnectionInitiationTask.isDefault()).isFalse();
        assertThat(partialConnectionInitiationTask.getConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
        assertThat(partialConnectionInitiationTask.getName()).isEqualTo("MyInitiation");
    }
}

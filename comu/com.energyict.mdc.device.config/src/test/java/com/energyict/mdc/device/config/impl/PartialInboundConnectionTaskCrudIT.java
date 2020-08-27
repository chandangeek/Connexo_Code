/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
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
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.http.whiteboard.TokenService;
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
import com.elster.jupiter.orm.h2.H2OrmModule;
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
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.ServerPartialConnectionTask;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.assertj.core.api.Assertions;
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
    private static long myDeviceProtocolPluggableClassID;

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

    private ConnectionFunction connectionFunction_1, connectionFunction_2;

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
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(TokenService.class).toInstance(mock(TokenService.class));
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
                    new CustomPropertySetsModule(),
                    new EventsModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new UtilModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new PkiModule(),
                    new IdsModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new H2OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new ProtocolApiModule(),
                    new TasksModule(),
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
                    new WebServicesModule(),
                    new AuditServiceModule(),
                    new FileImportModule()
            );
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

    private static void setupMasterData() {
        try (TransactionContext context = transactionService.getContext()) {
            protocolPluggableService.addDeviceProtocolService(new DeviceProtocolService());
            protocolPluggableService.addInboundDeviceProtocolService(new InboundDeviceProtocolService());
            protocolPluggableService.addLicensedProtocolService(licensedProtocolService);
            protocolPluggableService.addConnectionTypeService(connectionTypeService);
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType", InboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass.save();
            connectionTypePluggableClass2 = protocolPluggableService.newConnectionTypePluggableClass("NoParamsConnectionType2", InboundNoParamsConnectionTypeImpl.class.getName());
            connectionTypePluggableClass2.save();
            InboundDeviceProtocolPluggableClass discoveryPluggable = protocolPluggableService.newInboundDeviceProtocolPluggableClass("MyDiscoveryName", DummyInboundDiscoveryProtocol.class.getName());
            myDeviceProtocolPluggableClassID = protocolPluggableService.newDeviceProtocolPluggableClass("MyDeviceProtocol", DummyDeviceProtocolPluggableClassHavingConnectionFunctions.class.getName()).getId();
            discoveryPluggable.save();
            inboundComPortPool = engineConfigurationService.newInboundComPortPool("inboundComPortPool", ComPortType.TCP, discoveryPluggable, Collections.emptyMap());
            inboundComPortPool.setActive(true);
            inboundComPortPool.update();
            inboundComPortPool2 = engineConfigurationService.newInboundComPortPool("inboundComPortPool2", ComPortType.TCP, discoveryPluggable, Collections.emptyMap());
            inboundComPortPool2.setActive(true);
            inboundComPortPool2.update();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        connectionFunction_1 = mockConnectionFunction(1, "CF_1", "CF 1");
        connectionFunction_2 = mockConnectionFunction(2, "CF_2", "CF 2");

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
        when(deviceProtocolPluggableClass.getId()).thenReturn(myDeviceProtocolPluggableClassID);
        when(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction_1, connectionFunction_2));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Arrays.asList(deviceProtocolDialect1, deviceProtocolDialect2, deviceProtocolDialect3));
    }

    @Test
    @Transactional
    public void testCreate() {
        PartialInboundConnectionTask inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
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
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("MyInbound");

    }

    @Test
    @Transactional
    public void createDefaultWithoutDefaultTest() {
        PartialInboundConnectionTask notTheDefault;
        PartialInboundConnectionTask theDefault;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        notTheDefault = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(false).build();
        theDefault = deviceConfiguration.newPartialInboundConnectionTask("MyDefault", connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
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
        PartialInboundConnectionTask notTheDefault;
        PartialInboundConnectionTask theDefault;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        notTheDefault = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        theDefault = deviceConfiguration.newPartialInboundConnectionTask("MyDefault", connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
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
    public void createHavingConnectionFunctionNotAlreadyInUseTest() {
        PartialInboundConnectionTask havingConnectionFunction1;
        PartialInboundConnectionTask havingConnectionFunction2;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        havingConnectionFunction1 = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .connectionFunction(connectionFunction_1).build();
        havingConnectionFunction2 = deviceConfiguration.newPartialInboundConnectionTask("MyDefault", connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .connectionFunction(connectionFunction_2).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> havingCF1 = deviceConfigurationService.findPartialConnectionTask(havingConnectionFunction1.getId());
        Optional<PartialConnectionTask> havingCF2 = deviceConfigurationService.findPartialConnectionTask(havingConnectionFunction2.getId());
        assertThat(havingCF1.isPresent()).isTrue();
        assertThat(havingCF1.get().getConnectionFunction().isPresent()).isTrue();
        assertThat(havingCF1.get().getConnectionFunction().get().getId()).isEqualTo(connectionFunction_1.getId());
        assertThat(havingCF1.get().isDefault()).isFalse();
        assertThat(havingCF2.isPresent()).isTrue();
        assertThat(havingCF2.get().isDefault()).isFalse();
        assertThat(havingCF2.get().getConnectionFunction().isPresent()).isTrue();
        assertThat(havingCF2.get().getConnectionFunction().get().getId()).isEqualTo(connectionFunction_2.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.CONNECTION_FUNCTION_UNIQUE + '}')
    public void createHavingConnectionFunctionAlreadyInUseTest() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .connectionFunction(connectionFunction_1).build();
        deviceConfiguration.newPartialInboundConnectionTask("MyDefault", connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .connectionFunction(connectionFunction_1).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.CONNECTION_FUNCTION_NOT_SUPPORTED_BY_DEVICE_PROTOCOL + '}')
    public void createHavingUnsupportedConnectionFunctionTest() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        ConnectionFunction unsupportedConnectionFunction = mockConnectionFunction(100, "Unsupported_CF", "Unsupported CF");
        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                        .comPortPool(inboundComPortPool)
                        .connectionFunction(unsupportedConnectionFunction).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void testUpdate() {
        PartialInboundConnectionTask inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        PartialInboundConnectionTask partialInboundConnectionTask = deviceConfiguration.getPartialInboundConnectionTasks().get(0);
        partialInboundConnectionTask.setDefault(false);
        partialInboundConnectionTask.setConnectionFunction(connectionFunction_1);
        partialInboundConnectionTask.setComportPool(inboundComPortPool2);
        partialInboundConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
        partialInboundConnectionTask.setName("Changed");
        partialInboundConnectionTask.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTask.class);

        PartialInboundConnectionTask reloadedPartialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(reloadedPartialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool2.getId());
        assertThat(reloadedPartialInboundConnectionTask.isDefault()).isFalse();
        assertThat(reloadedPartialInboundConnectionTask.getConnectionFunction().isPresent()).isTrue();
        assertThat(reloadedPartialInboundConnectionTask.getConnectionFunction().get().getId()).isEqualTo(connectionFunction_1.getId());
        assertThat(reloadedPartialInboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(reloadedPartialInboundConnectionTask.getName()).isEqualTo("Changed");
    }


    @Test
    @Transactional
    public void testUpdateProtocolDialectProperties() {
        PartialInboundConnectionTask inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec1", "test property 1");
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec2", "test property 2");
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec3", "test property 3");
        protocolDialectConfigurationProperties.setProperty("deviceProtocolDialectSpec4", "test property 4");

        PartialInboundConnectionTask partialInboundConnectionTask = deviceConfiguration.getPartialInboundConnectionTasks().get(0);
        partialInboundConnectionTask.setDefault(false);
        partialInboundConnectionTask.setComportPool(inboundComPortPool2);
        partialInboundConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass2);
        partialInboundConnectionTask.setName("Changed");
        partialInboundConnectionTask.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
        partialInboundConnectionTask.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());
        assertThat(found.isPresent()).isTrue();

        PartialConnectionTask partialConnectionTask = found.get();

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl reloadedPartialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(reloadedPartialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool2.getId());
        assertThat(reloadedPartialInboundConnectionTask.isDefault()).isFalse();
        assertThat(reloadedPartialInboundConnectionTask.getConfiguration().getId()).isEqualTo(deviceConfiguration.getId());
        assertThat(reloadedPartialInboundConnectionTask.getName()).isEqualTo("Changed");
        ProtocolDialectConfigurationProperties dialectConfigurationProperties = reloadedPartialInboundConnectionTask.getProtocolDialectConfigurationProperties();
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
    public void updateToDefaultWithoutCurrentDefaultTest() {
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(false).build();
        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
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

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
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

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.CONNECTION_FUNCTION_UNIQUE + '}')
    public void updateToConnectionFunctionAlreadyInUseTest() {
        DeviceConfiguration deviceConfiguration;
        final String connectionTaskName1 = "MyOutbound";
        final String connectionTaskName2 = "MyDefault";
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName1, connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true)
                .connectionFunction(connectionFunction_1).build();
        deviceConfiguration.newPartialInboundConnectionTask(connectionTaskName2, connectionTypePluggableClass2, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(false)
                .connectionFunction(connectionFunction_2).build();
        deviceConfiguration.save();

        PartialInboundConnectionTask initialWithConnectionFunction = getConnectionTaskWithName(deviceConfiguration, connectionTaskName1);
        assertThat(initialWithConnectionFunction.getConnectionFunction().isPresent()).isTrue();
        assertThat(initialWithConnectionFunction.getConnectionFunction().get().getId()).isEqualTo(1);

        PartialInboundConnectionTask task;
        task = getConnectionTaskWithName(deviceConfiguration, connectionTaskName2);
        task.setConnectionFunction(connectionFunction_1);
        task.save();
    }

    private PartialInboundConnectionTask getConnectionTaskWithName(DeviceConfiguration deviceConfiguration, String connectionTaskName) {
        for (PartialInboundConnectionTask partialScheduledConnectionTask : deviceConfiguration.getPartialInboundConnectionTasks()) {
            if (partialScheduledConnectionTask.getName().equals(connectionTaskName)) {
                return partialScheduledConnectionTask;
            }
        }
        return null;
    }

    @Test
    @Transactional
    public void testDelete() {
        PartialInboundConnectionTask inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
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

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
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

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD + "}")
    public void createWithIncorrectConnectionTypeTest() {
        DeviceConfiguration deviceConfiguration;
        ConnectionTypePluggableClass outboundConnectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass("OutboundNoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class
                .getName());
        outboundConnectionTypePluggableClass.save();

        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.newPartialInboundConnectionTask("MyInboundWhichHasAnOutboundType", outboundConnectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(true).build();
        deviceConfiguration.save();
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

    private static class DeviceProtocolService implements com.energyict.mdc.protocol.api.services.DeviceProtocolService {

        @Override
        public Object createProtocol(String className) {
            if (DummyDeviceProtocolPluggableClassHavingConnectionFunctions.class.getName().equals(className)) {
                return new DummyDeviceProtocolPluggableClassHavingConnectionFunctions();
            } else {
                throw new RuntimeException("Class " + className + " not known or supported by this bundle");
            }
        }
    }

    private static class InboundDeviceProtocolService implements com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService {
        @Override
        public InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName) {
            if (DummyInboundDiscoveryProtocol.class.getName().equals(javaClassName)) {
                return new DummyInboundDiscoveryProtocol();
            } else {
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
        PartialInboundConnectionTask inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceConfiguration clonedDeviceConfig;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();
        clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        clonedDeviceConfig.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
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
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("MyInbound");
    }

    @Test
    @Transactional
    public void cloneHavingConnectionFunctionTest() {
        PartialInboundConnectionTask inboundConnectionTask;
        DeviceConfiguration deviceConfiguration;
        DeviceConfiguration clonedDeviceConfig;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();
        clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        clonedDeviceConfig.save();

        inboundConnectionTask = deviceConfiguration.newPartialInboundConnectionTask("MyInbound", connectionTypePluggableClass, deviceConfiguration.getProtocolDialectConfigurationPropertiesList()
                .get(0))
                .comPortPool(inboundComPortPool)
                .asDefault(false)
                .connectionFunction(connectionFunction_1).build();
        deviceConfiguration.save();

        Optional<PartialConnectionTask> found = deviceConfigurationService.findPartialConnectionTask(inboundConnectionTask.getId());

        PartialConnectionTask partialConnectionTask = ((ServerPartialConnectionTask) found.get()).cloneForDeviceConfig(clonedDeviceConfig);

        assertThat(partialConnectionTask).isInstanceOf(PartialInboundConnectionTaskImpl.class);

        PartialInboundConnectionTaskImpl partialInboundConnectionTask = (PartialInboundConnectionTaskImpl) partialConnectionTask;

        assertThat(partialInboundConnectionTask.getComPortPool().getId()).isEqualTo(inboundComPortPool.getId());
        assertThat(partialInboundConnectionTask.isDefault()).isFalse();
        assertThat(partialInboundConnectionTask.getConnectionFunction().isPresent()).isTrue();
        assertThat(partialInboundConnectionTask.getConnectionFunction().get().getId()).isEqualTo(connectionFunction_1.getId());
        assertThat(partialInboundConnectionTask.getConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
        assertThat(partialInboundConnectionTask.getName()).isEqualTo("MyInbound");
    }

    private ConnectionFunction mockConnectionFunction(int id, String name, String displayName) {
            return new ConnectionFunction() {
                @Override
                public long getId() {
                    return id;
                }

                @Override
                public String getConnectionFunctionName() {
                    return name;
                }

                @Override
                public String getConnectionFunctionDisplayName() {
                    return displayName;
                }
            };
    }
}

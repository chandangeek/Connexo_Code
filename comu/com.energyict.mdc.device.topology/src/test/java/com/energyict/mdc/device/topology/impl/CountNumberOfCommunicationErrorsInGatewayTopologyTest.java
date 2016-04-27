package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the method of the {@link TopologyServiceImpl} component
 * that counts the number of communication errors in a device communication topology.
 */
@RunWith(MockitoJUnitRunner.class)
public class CountNumberOfCommunicationErrorsInGatewayTopologyTest {

    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final String DEVICE_TYPE_NAME = "CountComErrorsInTopology";
    private static final String DEVICE_CONFIGURATION_NAME = "conf";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private LicenseService licenseService;
    @Mock
    private KpiService kpiService;

    private InMemoryBootstrapModule bootstrapModule;
    private TransactionService transactionService;
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private DataVaultService dataVaultService;
    private DeviceConfigurationService deviceConfigurationService;
    private TopologyService topologyService;
    private DeviceService deviceService;

    private Device device;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(Thesaurus.class).toInstance(mock(Thesaurus.class, RETURNS_DEEP_STUBS));
        }

    }

    public void initializeDatabase(boolean showSqlLogging) {
        when(this.connectionTypeService.createConnectionType(NoParamsConnectionType.class.getName())).thenReturn(new NoParamsConnectionType());
        this.bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
                new ThreadSecurityModule(principal),
                new CustomPropertySetsModule(),
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
                new MeteringGroupsModule(),
                new SearchModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new KpiModule(),
                new TaskModule(),
                new TasksModule(),
                new MdcIOModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new TopologyModule(),
                new IssuesModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolApiModule(),
                new PluggableModule(),
                new SchedulingModule());
        this.transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(EventService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(OrmService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MasterDataService.class);
            this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            this.protocolPluggableService.addConnectionTypeService(this.connectionTypeService);
            injector.getInstance(MeteringGroupsService.class);
            DeviceDataModelServiceImpl deviceDataModelService = injector.getInstance(DeviceDataModelServiceImpl.class);
            deviceDataModelService.communicationTaskReportService();
            this.deviceService = deviceDataModelService.deviceService();
            this.topologyService = injector.getInstance(TopologyService.class);
            ctx.commit();
        }
    }

    @Before
    public void setUp() {
        when(this.principal.getName()).thenReturn(CountNumberOfCommunicationErrorsInGatewayTopologyTest.class.getSimpleName());
        this.initializeDatabase(false);
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encryptionAccessLevel));

        try (TransactionContext ctx = this.transactionService.getContext()) {
            DeviceType deviceType = this.deviceConfigurationService.newDeviceType(DEVICE_TYPE_NAME, this.deviceProtocolPluggableClass);
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
            deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new TestDialect());
            deviceConfiguration.save();
            deviceConfiguration.activate();
            this.device = this.deviceService.newDevice(deviceConfiguration, "SimpleDevice", "mrid", Instant.now());
            this.device.save();
            ConnectionTypePluggableClass connectionTypePluggableClass = this.protocolPluggableService.newConnectionTypePluggableClass(NoParamsConnectionType.class.getSimpleName(), NoParamsConnectionType.class
                    .getName());
            connectionTypePluggableClass.save();
            ctx.commit();
        }
    }

    @After
    public void tearDown() {
        this.bootstrapModule.deactivate();
    }

    @Test
    public void testConnectionSetupFailure() {
        // Business method
        int numberOfDevices = this.topologyService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.CONNECTION_SETUP_FAILURE, this.device, Interval.sinceEpoch());

        // Asserts: validates mostly that no SQLExceptions have occurred
        assertThat(numberOfDevices).isZero();
    }

    @Test
    public void testCommunicationFailure() {
        // Business method
        int numberOfDevices = this.topologyService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.COMMUNICATION_FAILURE, this.device, Interval.sinceEpoch());

        // Asserts: validates mostly that no SQLExceptions have occurred
        assertThat(numberOfDevices).isZero();
    }

    @Test
    public void testConnectionFailure() {
        // Business method
        int numberOfDevices = this.topologyService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.CONNECTION_FAILURE, this.device, Interval.sinceEpoch());

        // Asserts: validates mostly that no SQLExceptions have occurred
        assertThat(numberOfDevices).isZero();
    }

    private class TestDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return CountNumberOfCommunicationErrorsInGatewayTopologyTest.class.getSimpleName();
        }

        @Override
        public String getDisplayName() {
            return "For testing purposes only";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

    }

}
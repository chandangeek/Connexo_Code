package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the method of the {@link DeviceDataServiceImpl} component
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

    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    private TransactionService transactionService;
    private OrmService ormService;
    private EventService eventService;
    private ProtocolPluggableService protocolPluggableService;
    private DeviceConfigurationService deviceConfigurationService;
    private DeviceDataService deviceDataService;

    private ConnectionTypePluggableClass connectionTypePluggableClass;
    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;
    private ProtocolDialectConfigurationProperties configDialectProps;
    private Device device;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
        }

    }

    public void initializeDatabase(boolean showSqlLogging) {
        this.bootstrapModule = new InMemoryBootstrapModule();
        this.injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
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
                new MdcCommonModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new ValidationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new SchedulingModule());
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.eventService = injector.getInstance(EventService.class);
            this.ormService = this.injector.getInstance(OrmService.class);
            this.deviceDataService = this.injector.getInstance(DeviceDataService.class);
            this.deviceConfigurationService = this.injector.getInstance(DeviceConfigurationService.class);
            this.protocolPluggableService = this.injector.getInstance(ProtocolPluggableService.class);
            this.deviceConfigurationService = this.injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
    }

    @Before
    public void setUp() {
        when(this.principal.getName()).thenReturn(CountNumberOfCommunicationErrorsInGatewayTopologyTest.class.getSimpleName());
        this.initializeDatabase(false);
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));

        try (TransactionContext ctx = this.transactionService.getContext()) {
            this.deviceType = this.deviceConfigurationService.newDeviceType(DEVICE_TYPE_NAME, this.deviceProtocolPluggableClass);
            this.deviceType.save();
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            this.deviceConfiguration = deviceConfigurationBuilder.add();
            this.configDialectProps = this.deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new TestDialect());
            this.deviceConfiguration.save();
            this.deviceConfiguration.activate();
            this.device = this.deviceDataService.newDevice(this.deviceConfiguration, "SimpleDevice", "mrid");
            this.device.save();
            this.connectionTypePluggableClass = this.protocolPluggableService.newConnectionTypePluggableClass(NoParamsConnectionType.class.getSimpleName(), NoParamsConnectionType.class.getName());
            this.connectionTypePluggableClass.save();
            ctx.commit();
        }
    }

    @After
    public void tearDown() {
        this.bootstrapModule.deactivate();
    }

    @Test
    public void testConnectionSetupFailure () {
        // Business method
        int numberOfDevices = this.deviceDataService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.CONNECTION_SETUP_FAILURE, this.device, Interval.sinceEpoch());

        // Asserts: validates mostly that no SQLExceptions have occurred
        assertThat(numberOfDevices).isZero();
    }

    @Test
    public void testCommunicationFailure () {
        // Business method
        int numberOfDevices = this.deviceDataService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.COMMUNICATION_FAILURE, this.device, Interval.sinceEpoch());

        // Asserts: validates mostly that no SQLExceptions have occurred
        assertThat(numberOfDevices).isZero();
    }

    @Test
    public void testConnectionFailure () {
        // Business method
        int numberOfDevices = this.deviceDataService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType.CONNECTION_FAILURE, this.device, Interval.sinceEpoch());

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
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }
    }

}
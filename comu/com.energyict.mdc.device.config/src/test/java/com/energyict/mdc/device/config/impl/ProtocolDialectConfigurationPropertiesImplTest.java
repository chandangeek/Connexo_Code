package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
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
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the peristence aspects of the {@link ProtocolDialectConfigurationPropertiesImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolDialectConfigurationPropertiesImplTest {

    private static final String MY_PROPERTY = "myProperty";
    public static final String PROTOCOL_DIALECT = "protocolDialect";
    public static final String VERY_LARGE_STRING = Strings.repeat("0123456789", 10000); // String containing 100_000 characters which >> 4K
    private SharedData sharedData;
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceType myDeviceType;
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private BundleContext bundleContext;
    private InMemoryBootstrapModule bootstrapModule;
    private TransactionService transactionService;
    @Mock
    private Principal principal;
    private ProtocolPluggableService protocolPluggableService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    @Mock
    private LicenseService licenseService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private License license;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
        }
    }

    public void initializeDatabase(boolean showSqlLogging) {
        bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
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
                new FiniteStateMachineModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new DataVaultModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new SchedulingModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new MdcIOModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new IssuesModule(),
                new ValidationModule(),
                new EstimationModule(),
                new MeteringGroupsModule(),
                new TaskModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new TimeModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(EngineConfigurationService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addLicensedProtocolService(this.licensedProtocolService);
            when(this.deviceProtocolService.createProtocol(MyDeviceProtocolPluggableClass.class.getName())).thenReturn(new MyDeviceProtocolPluggableClass());
            protocolPluggableService.addDeviceProtocolService(this.deviceProtocolService);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(SchedulingService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
    }


    @Before
    public void setUp() {
        sharedData = new SharedData();
        when(principal.getName()).thenReturn("test");
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(this.license));
        when(this.licensedProtocolService.isValidJavaClassName(anyString(), eq(this.license))).thenReturn(true);
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(this.license.getLicensedValues()).thenReturn(properties);
        initializeDatabase(false);
        try (TransactionContext context = transactionService.getContext()) {
            DeviceProtocolPluggableClass protocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("protocolPluggableClass", MyDeviceProtocolPluggableClass.class.getName());
            protocolPluggableClass.save();

            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", protocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            context.commit();
        }
    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
        sharedData.invalidate();
    }

    @Test
    public void testCreateWithoutViolations() {
        long id;

        try (TransactionContext context = transactionService.getContext()) {
            ProtocolDialectConfigurationProperties properties = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(sharedData.getProtocolDialect());
            properties.setProperty(MY_PROPERTY, 15);
            properties.save();

            id = properties.getId();
            context.commit();
        }

        // Asserts
        DeviceConfiguration reloadedConfiguration = deviceConfigurationService.findDeviceConfiguration(this.deviceConfiguration.getId()).get();
        List<ProtocolDialectConfigurationProperties> list = reloadedConfiguration.getProtocolDialectConfigurationPropertiesList();
        assertThat(list).isNotEmpty();

        ProtocolDialectConfigurationProperties found = list.get(0);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getProperty("myProperty")).isEqualTo(15);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "properties." + MY_PROPERTY)
    public void testCreateWithTooLargePropertyValue() {
        try (TransactionContext context = transactionService.getContext()) {
            ProtocolDialectConfigurationProperties properties = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(sharedData.getProtocolDialect());
            properties.setProperty(MY_PROPERTY, VERY_LARGE_STRING);
            properties.save();

            context.commit();
        }

        // Asserts: see expected constraint violation rule
    }

    public static class SharedData {
        private static PropertySpec propertySpec;
        private static DeviceProtocolDialect protocolDialect;
        private static ValueFactory valueFactory;

        private static interface State {
            PropertySpec getPropertySpec();
            DeviceProtocolDialect getProtocolDialect();
            ValueFactory getValueFactory();
        }

        private static State actual;

        public SharedData() {
            if (actual == null) {
                actual = new State() {
                    @Override
                    public PropertySpec getPropertySpec() {
                        return propertySpec;
                    }

                    @Override
                    public DeviceProtocolDialect getProtocolDialect() {
                        return protocolDialect;
                    }

                    @Override
                    public ValueFactory getValueFactory() {
                        return valueFactory;
                    }
                };
                propertySpec = mock(PropertySpec.class);
                protocolDialect = mock(DeviceProtocolDialect.class);
                when(protocolDialect.getDisplayName()).thenReturn(PROTOCOL_DIALECT);
                valueFactory = mock(ValueFactory.class);
                when(getProtocolDialect().getPropertySpec(MY_PROPERTY)).thenReturn(getPropertySpec());
                when(getPropertySpec().getValueFactory()).thenReturn(getValueFactory());
                when(getProtocolDialect().getDeviceProtocolDialectName()).thenReturn(PROTOCOL_DIALECT);
                when(getValueFactory().fromStringValue("15")).thenReturn(15);
                when(getValueFactory().toStringValue(15)).thenReturn("15");
                when(getValueFactory().fromStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
                when(getValueFactory().toStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
            }
        }

        PropertySpec getPropertySpec() {
            return actual.getPropertySpec();
        }

        DeviceProtocolDialect getProtocolDialect() {
            return actual.getProtocolDialect();
        }

        ValueFactory getValueFactory() {
            return actual.getValueFactory();
        }

        void invalidate() {
            actual = null;
        }
    }

    public static class MyDeviceProtocolPluggableClass implements DeviceProtocol {

        private final SharedData sharedData = new SharedData();

        public MyDeviceProtocolPluggableClass() {
        }

        @Override
        public List<PropertySpec> getSecurityPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public String getSecurityRelationTypeName() {
            return null;
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return null;
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return null;
        }

        @Override
        public PropertySpec getSecurityPropertySpec(String name) {
            return null;
        }

        @Override
        public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

        }

        @Override
        public void terminate() {

        }

        @Override
        public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
            return null;
        }

        @Override
        public String getProtocolDescription() {
            return null;
        }

        @Override
        public DeviceFunction getDeviceFunction() {
            return null;
        }

        @Override
        public ManufacturerInformation getManufacturerInformation() {
            return null;
        }

        @Override
        public List<ConnectionType> getSupportedConnectionTypes() {
            return null;
        }

        @Override
        public void logOn() {

        }

        @Override
        public void daisyChainedLogOn() {

        }

        @Override
        public void logOff() {

        }

        @Override
        public void daisyChainedLogOff() {

        }

        @Override
        public String getSerialNumber() {
            return null;
        }

        @Override
        public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

        }

        @Override
        public DeviceProtocolCache getDeviceCache() {
            return null;
        }

        @Override
        public void setTime(Date timeToSet) {

        }

        @Override
        public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
            return null;
        }

        @Override
        public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
            return null;
        }

        @Override
        public Date getTime() {
            return null;
        }

        @Override
        public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
            return null;
        }

        @Override
        public Set<DeviceMessageId> getSupportedMessages() {
            return EnumSet.noneOf(DeviceMessageId.class);
        }

        @Override
        public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
            return null;
        }

        @Override
        public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
            return null;
        }

        @Override
        public String format(PropertySpec propertySpec, Object messageAttribute) {
            return null;
        }

        @Override
        public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
            return Arrays.asList(sharedData.getProtocolDialect());
        }

        @Override
        public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

        }

        @Override
        public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
            return null;
        }

        @Override
        public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

        }

        @Override
        public CollectedTopology getDeviceTopology() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public void copyProperties(TypedProperties properties) {

        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(sharedData.getPropertySpec());
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }

        @Override
        public CollectedFirmwareVersion getFirmwareVersions() {
            return null;
        }
    }

}
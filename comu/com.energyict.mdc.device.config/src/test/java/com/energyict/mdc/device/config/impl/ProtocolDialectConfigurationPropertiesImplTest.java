package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
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

import javax.validation.constraints.Size;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

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

    public static final String PROTOCOL_DIALECT = "protocolDialect";
    public static final String VERY_LARGE_STRING = Strings.repeat("0123456789", 10000); // String containing 100_000 characters which >> 4K
    private static final String MY_PROPERTY = "myProperty";
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    private SharedData sharedData;
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

    public void initializeDatabase(boolean showSqlLogging) throws SQLException {
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
                new UsagePointLifeCycleConfigurationModule(),
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
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new TaskModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new TimeModule(),
                new CustomPropertySetsModule(),
                new CalendarModule());
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
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(SchedulingService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            registerDeviceProtocolDialectPropertyProviderTable(injector.getInstance(OrmService.class));
            ctx.commit();
        }
    }

    private void registerDeviceProtocolDialectPropertyProviderTable(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel("TST", "For testing purposes only");
        Table<DeviceProtocolDialectPropertyProvider> table = dataModel.addTable("TST_DEVPROTDIALECT", DeviceProtocolDialectPropertyProvider.class);
        table.map(Whatever.class);
        Column id = table.addAutoIdColumn();
        table
                .primaryKey("PK_TST_DEVPROTDIALECT").on(id)
                .add();
        dataModel.install(true, false);
        dataModel.register();
    }

    @Before
    public void setUp() throws SQLException {
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

    private static class DeviceProtocolDialectTestImpl implements HasId {
        @Override
        public long getId() {
            return 0;
        }
    }

    private static class Whatever implements DeviceProtocolDialectPropertyProvider {
        private long id;

        @Override
        public List<DeviceProtocolDialectProperty> getProperties() {
            return Collections.emptyList();
        }
    }

    public static class MyDialectProperties extends CommonDeviceProtocolDialectProperties {
        @Size(max = Table.MAX_STRING_LENGTH)
        private String myProperty;

        @Override
        protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
            this.myProperty = (String) propertyValues.getProperty(MY_PROPERTY);
        }

        @Override
        protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
            this.setPropertyIfNotNull(propertySetValues, MY_PROPERTY, this.myProperty);
        }

        @Override
        public void validateDelete() {

        }
    }

    public static class SharedData {
        private static DeviceProtocolDialect protocolDialect;
        private static PropertySpec propertySpec;
        private static ValueFactory valueFactory;
        private static CustomPropertySet<DeviceProtocolDialectPropertyProvider, MyDialectProperties> customPropertySet;
        private static PersistenceSupport<DeviceProtocolDialectPropertyProvider, MyDialectProperties> persistenceSupport;
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
                valueFactory = mock(ValueFactory.class);
                when(valueFactory.fromStringValue("15")).thenReturn(15);
                when(valueFactory.fromStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
                when(valueFactory.toStringValue(15)).thenReturn("15");
                when(valueFactory.toStringValue(VERY_LARGE_STRING)).thenReturn(VERY_LARGE_STRING);
                propertySpec = mock(PropertySpec.class);
                when(propertySpec.getName()).thenReturn(MY_PROPERTY);
                persistenceSupport = mock(PersistenceSupport.class);
                when(persistenceSupport.application()).thenReturn("Example");
                when(persistenceSupport.componentName()).thenReturn("DDD");
                when(persistenceSupport.tableName()).thenReturn("TST_MYPROPS");
                when(persistenceSupport.journalTableName()).thenReturn("TST_MYPROPSJRNL");
                when(persistenceSupport.domainColumnName()).thenReturn(CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.databaseName());
                when(persistenceSupport.domainFieldName()).thenReturn(CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.javaName());
                when(persistenceSupport.domainForeignKeyName()).thenReturn("FK_TST_MYPROPS");
                when(persistenceSupport.persistenceClass()).thenReturn(MyDialectProperties.class);
                when(persistenceSupport.module()).thenReturn(Optional.empty());
                customPropertySet = mock(CustomPropertySet.class);
                when(customPropertySet.getId()).thenReturn(ProtocolDialectConfigurationPropertiesImplTest.class.getSimpleName());
                when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
                when(customPropertySet.getPersistenceSupport()).thenReturn(persistenceSupport);
                when(customPropertySet.getDomainClass()).thenReturn(DeviceProtocolDialectPropertyProvider.class);
                protocolDialect = mock(DeviceProtocolDialect.class);
                when(protocolDialect.getDeviceProtocolDialectDisplayName()).thenReturn(PROTOCOL_DIALECT);
                when(protocolDialect.getCustomPropertySet()).thenReturn(Optional.of(customPropertySet));
                when(protocolDialect.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
                when(propertySpec.getValueFactory()).thenReturn(valueFactory);
                when(protocolDialect.getDeviceProtocolDialectName()).thenReturn(PROTOCOL_DIALECT);
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

        private interface State {
            DeviceProtocolDialect getProtocolDialect();

            PropertySpec getPropertySpec();

            ValueFactory getValueFactory();
        }
    }

    public static class MyDeviceProtocolPluggableClass implements DeviceProtocol {

        private final SharedData sharedData = new SharedData();

        public MyDeviceProtocolPluggableClass() {
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

        @Override
        public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
            return Optional.empty();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

        }

        @Override
        public List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return null;
        }

        @Override
        public List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
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
            return Collections.emptyList();
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
        public DeviceProtocolCache getDeviceCache() {
            return null;
        }

        @Override
        public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
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
        public void setTime(Date timeToSet) {

        }

        @Override
        public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
            return null;
        }

        @Override
        public List<DeviceMessageSpec> getSupportedMessages() {
            return Collections.emptyList();
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
        public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
            return null;
        }

        @Override
        public String prepareMessageContext(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
            return null;
        }

        @Override
        public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
            return Collections.singletonList(sharedData.getProtocolDialect());
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
            return Collections.singletonList(sharedData.getPropertySpec());
        }

        @Override
        public CollectedFirmwareVersion getFirmwareVersions() {
            return null;
        }

        @Override
        public CollectedBreakerStatus getBreakerStatus() {
            return null;
        }

        @Override
        public CollectedCalendar getCollectedCalendar() {
            return null;
        }
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

}
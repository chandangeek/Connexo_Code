/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.NonExistingMessageConverter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.mocks.DeviceMessageTestSpec;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocolHavingSupportForConnectionFunctions;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocolWithTestPropertySpecs;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.SDKDeviceProtocolTestWithMandatoryProperty;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.UPLConnectionFunction;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.obis.ObisCode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateMidnight;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassImplTest {

    public static final String DEVICE_PROTOCOL_NAME = "DeviceProtocolPluggableClassName";
    public static final String MOCK_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol";
    public static final String MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES = "com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocolWithTestPropertySpecs";
    public static final String MOCK_DEVICE_PROTOCOL_WITH_CONNECTION_FUNCTIONS = "com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocolHavingSupportForConnectionFunctions";
    public static final String MOCK_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol";
    public static final String MOCK_SMART_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol";
    public static final String MOCK_NOT_A_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol";

    private static final String SDK_DEVICE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY = "com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithMandatoryProperty";

    @Rule
    public ExpectedConstraintViolationRule rule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private UserService userService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private LicenseService licenseService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private License license;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;

    private DataModel dataModel;
    private InMemoryBootstrapModule bootstrapModule;
    private TransactionService transactionService;
    private ProtocolPluggableServiceImpl protocolPluggableService;
    private PropertySpecServiceImpl propertySpecService;

    @Before
    public void initializeDatabase() throws SQLException {
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdmin eventAdmin = mock(EventAdmin.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence.mdc.protocol.pluggable");
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
        bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(bundleContext, eventAdmin, deviceProtocolService),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new H2OrmModule(),
                new DataVaultModule(),
                new IssuesModule(),
                new PluggableModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolPluggableModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(OrmService.class);
            transactionService = injector.getInstance(TransactionService.class);
            protocolPluggableService = (ProtocolPluggableServiceImpl) injector.getInstance(ProtocolPluggableService.class);
            protocolPluggableService.addDeviceProtocolService(this.deviceProtocolService);
            protocolPluggableService.addLicensedProtocolService(this.licensedProtocolService);
            protocolPluggableService.addDeviceProtocolSecurityService(deviceProtocolSecurityService);
            protocolPluggableService.addDeviceProtocolMessageService(deviceProtocolMessageService);
            dataModel = protocolPluggableService.getDataModel();
            propertySpecService = (PropertySpecServiceImpl) injector.getInstance(PropertySpecService.class);
            this.initializeSecuritySupport();
            this.initializeMessageSupport();
            ctx.commit();
        }
    }

    @Before
    public void initializeLicenseService() {
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(this.license));
        when(this.licensedProtocolService.isValidJavaClassName(anyString(), eq(this.license))).thenReturn(true);
    }

    @Before
    public void initializeDeviceProtocolService() {
        when(deviceProtocolService.createProtocol(MOCK_DEVICE_PROTOCOL)).thenReturn(new MockDeviceProtocol());
        when(deviceProtocolService.createProtocol(MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES)).thenReturn(new MockDeviceProtocolWithTestPropertySpecs(propertySpecService));
        when(deviceProtocolService.createProtocol(MOCK_DEVICE_PROTOCOL_WITH_CONNECTION_FUNCTIONS)).thenReturn(new MockDeviceProtocolHavingSupportForConnectionFunctions());
        when(deviceProtocolService.createProtocol(MOCK_METER_PROTOCOL)).thenReturn(new MockMeterProtocol(propertySpecService));
        when(deviceProtocolService.createProtocol(MOCK_SMART_METER_PROTOCOL)).thenReturn(new MockSmartMeterProtocol());
        when(deviceProtocolService.createProtocol(MOCK_NOT_A_DEVICE_PROTOCOL)).thenReturn(new NotADeviceProtocol());
        when(deviceProtocolService.createProtocol(SDK_DEVICE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY)).thenReturn(new SDKDeviceProtocolTestWithMandatoryProperty(this.propertySpecService, mock(CollectedDataFactory.class)));
    }

    private void initializeSecuritySupport() throws SQLException {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(insertSecuritySupportAdapterMappingSql())) {
                statement.setString(1, "com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol");
                statement.setString(2, NoSecuritySupport.class.getName());
                statement.addBatch();
                statement.setString(1, "com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol");
                statement.setString(2, NoSecuritySupport.class.getName());
                statement.addBatch();
                statement.executeBatch();
            }
        }
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(NoSecuritySupport.class.getName())).thenReturn(new NoSecuritySupport());
    }

    private String insertSecuritySupportAdapterMappingSql() {
        return "insert into " + TableSpecs.PPC_SECSUPPORTADAPTERMAPPING.name() + " (deviceprotocoljavaclassname, securitysupportclassname) values(?, ?)";
    }

    private void initializeMessageSupport() throws SQLException {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(insertMessageSupportAdapterMappingSql())) {
                statement.setString(1, "com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol");
                statement.setString(2, NonExistingMessageConverter.class.getName());
                statement.addBatch();
                statement.setString(1, "com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol");
                statement.setString(2, NonExistingMessageConverter.class.getName());
                statement.addBatch();
                statement.executeBatch();
            }
        }
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(NonExistingMessageConverter.class.getName())).thenReturn(new NonExistingMessageConverter());
    }

    private String insertMessageSupportAdapterMappingSql() {
        return "insert into " + TableSpecs.PPC_MESSAGEADAPTERMAPPING.name() + " (deviceprotocoljavaclassname, messageadapterjavaclassname) values(?, ?)";
    }

    @After
    public void cleanUp() throws SQLException {
        for (final DeviceProtocolPluggableClass pluggableClass : protocolPluggableService.findAllDeviceProtocolPluggableClasses().find()) {
            transactionService.execute(() -> {
                pluggableClass.delete();
                return null;
            });
        }
        bootstrapModule.deactivate();
    }

    @Test
    public void newInstanceDeviceProtocolTest() throws SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL));

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(DeviceProtocol.class);
    }

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with TypedProperties
     * specified @ construction time.
     */
    @Test
    public void newDeviceProtocolWithProperties() {
        final TypedProperties creationProperties = TypedProperties.empty();
        Date activationDate = new DateMidnight().toDate();
        creationProperties.setProperty(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME, activationDate);

        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = transactionService.
                execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES, creationProperties));

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(DeviceProtocol.class);
        assertThat(deviceProtocol.getPropertySpecs()).hasSize(1);
        assertThat(deviceProtocol.getPropertySpec(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME)).isPresent();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties();
        assertThat(properties.getProperty(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME)).isEqualTo(activationDate);
    }

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} and then
     * updates it with TypedProperties.
     */
    @Test
    public void saveDeviceProtocolProperties() {
        final TypedProperties creationProperties = TypedProperties.empty();
        final Date activationDate = new DateMidnight().toDate();
        creationProperties.setProperty(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME, activationDate);

        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = transactionService.
                execute(() -> {
                    DeviceProtocolPluggableClass deviceProtocolPluggableClass1 = protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
                    deviceProtocolPluggableClass1.setProperty(
                            DeviceMessageTestSpec.extendedSpecs(propertySpecService).getPropertySpec(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME).get(),
                            activationDate);
                    deviceProtocolPluggableClass1.save();
                    return deviceProtocolPluggableClass1;

                });

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(DeviceProtocol.class);
        assertThat(deviceProtocol.getPropertySpecs()).hasSize(1);
        assertThat(deviceProtocol.getPropertySpec(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME)).isPresent();
        TypedProperties properties = deviceProtocolPluggableClass.getProperties();
        assertThat(properties.getProperty(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME)).isEqualTo(activationDate);
    }

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} with TypedProperties
     * specified @ construction time that do not actually exist on the protocol class.
     */
    @Test(expected = UnknownPluggableClassPropertiesException.class)
    public void newDeviceProtocolWithNonExistingProperties() {
        final TypedProperties creationProperties = TypedProperties.empty();
        creationProperties.setProperty("foo", "bar");

        // Business method
        transactionService.
                execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES, creationProperties));

        // Expected UnknownPluggableClassPropertiesException
    }

    /**
     * Creates a new {@link DeviceProtocolPluggableClass} and then
     * updates it with TypedProperties that do not actually exist on the protocol class.
     */
    @Test(expected = UnknownPluggableClassPropertiesException.class)
    public void saveDeviceProtocolWithNonExistingProperties() {
        final PropertySpec foo = mock(PropertySpec.class);
        when(foo.getName()).thenReturn("foo");
        when(foo.getValueFactory()).thenReturn(new StringFactory());
        when(foo.isReference()).thenReturn(false);
        when(foo.isRequired()).thenReturn(true);

        // Business method
        transactionService.
                execute(() -> {
                    DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
                    deviceProtocolPluggableClass.setProperty(foo, "bar");
                    deviceProtocolPluggableClass.save();
                    return deviceProtocolPluggableClass;
                });

        // Expected UnknownPluggableClassPropertiesException
    }

    @Test
    public void newDeviceProtocolSupportingConnectionFunctionsTest() throws SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_CONNECTION_FUNCTIONS));

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL_WITH_CONNECTION_FUNCTIONS);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(DeviceProtocol.class);
        assertThat(deviceProtocol.getProvidedConnectionFunctions()).hasSize(2);
        assertThat(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).hasSize(2);
        assertConnectionFunction(deviceProtocol.getProvidedConnectionFunctions(), deviceProtocolPluggableClass.getProvidedConnectionFunctions(), MockDeviceProtocolHavingSupportForConnectionFunctions.PROVIDED_CF_1_ID, MockDeviceProtocolHavingSupportForConnectionFunctions.PROVIDED_CF_1_NAME);
        assertConnectionFunction(deviceProtocol.getProvidedConnectionFunctions(), deviceProtocolPluggableClass.getProvidedConnectionFunctions(), MockDeviceProtocolHavingSupportForConnectionFunctions.PROVIDED_CF_2_ID, MockDeviceProtocolHavingSupportForConnectionFunctions.PROVIDED_CF_2_NAME);

        assertThat(deviceProtocol.getConsumableConnectionFunctions()).hasSize(2);
        assertThat(deviceProtocolPluggableClass.getConsumableConnectionFunctions()).hasSize(2);
        assertConnectionFunction(deviceProtocol.getConsumableConnectionFunctions(), deviceProtocolPluggableClass.getConsumableConnectionFunctions(), MockDeviceProtocolHavingSupportForConnectionFunctions.CONSUMABLE_CF_3_ID, MockDeviceProtocolHavingSupportForConnectionFunctions.CONSUMABLE_CF_3_NAME);
        assertConnectionFunction(deviceProtocol.getConsumableConnectionFunctions(), deviceProtocolPluggableClass.getConsumableConnectionFunctions(), MockDeviceProtocolHavingSupportForConnectionFunctions.CONSUMABLE_CF_4_ID, MockDeviceProtocolHavingSupportForConnectionFunctions.CONSUMABLE_CF_4_NAME);
    }

    private void assertConnectionFunction(List<UPLConnectionFunction> protocolConnectionFunctions, List<ConnectionFunction> pluggableClassConnectionFunctions, int id, String name) {
        Optional<UPLConnectionFunction> protocolConnectionFunction = protocolConnectionFunctions
                .stream()
                .filter(cf -> cf.getId() == id)
                .findFirst();
        assertThat(protocolConnectionFunction).isPresent();
        assertThat(protocolConnectionFunction.get().getConnectionFunctionName()).isEqualTo(name);

        Optional<ConnectionFunction> pluggableClassConnectionFunction = pluggableClassConnectionFunctions
                .stream()
                .filter(cf -> cf.getId() == id)
                .findFirst();
        assertThat(pluggableClassConnectionFunction).isPresent();
        assertThat(pluggableClassConnectionFunction.get().getConnectionFunctionName()).isEqualTo(name);
        assertThat(pluggableClassConnectionFunction.get().getConnectionFunctionDisplayName()).isEqualTo(name);
    }

    @Test
    public void newInstanceMeterProtocolTest() throws SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_METER_PROTOCOL));

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_METER_PROTOCOL);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(MeterProtocolAdapter.class);
    }

    @Test
    public void newInstanceSmartMeterProtocolTest() throws SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_SMART_METER_PROTOCOL));

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_SMART_METER_PROTOCOL);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(SmartMeterProtocolAdapterImpl.class);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "The value is not listed as a possible value for this property", property = "properties.SDKObisCodeProperty")
    public void newInstanceSmartMeterProtocolIllegalPropertyTest() throws SQLException {
        transactionService.execute(() -> {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("SDKDeviceProtocolTestWithMandatoryProperty", SDK_DEVICE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY);
            PropertySpec deviceTimeZone = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpec("SDKObisCodeProperty").get();
            deviceProtocolPluggableClass.setProperty(deviceTimeZone, new ObisCode(1, 1, 1, 1, 1, 1));
            deviceProtocolPluggableClass.save();
            return deviceProtocolPluggableClass;
        });
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE + "}")
    public void newInstanceNotADeviceProtocolTest() throws SQLException {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(() -> protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_NOT_A_DEVICE_PROTOCOL));

        // Business method
        deviceProtocolPluggableClass.getDeviceProtocol();
    }


    private class MockModule extends AbstractModule {

        private BundleContext bundleContext;
        private EventAdmin eventAdmin;
        private DeviceProtocolService deviceProtocolService;

        private MockModule(BundleContext bundleContext, EventAdmin eventAdmin, DeviceProtocolService deviceProtocolService) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = eventAdmin;
            this.deviceProtocolService = deviceProtocolService;
        }

        @Override
        protected void configure() {
            bind(TimeService.class).toInstance(mock(TimeService.class));
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(UserService.class).toInstance(userService);
            bind(MeteringService.class).toInstance(meteringService);
            bind(InboundDeviceProtocolService.class).toInstance(mock(InboundDeviceProtocolService.class));
            bind(CustomPropertySetService.class).toInstance(mock(CustomPropertySetService.class));
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(ConnectionTypeService.class).toInstance(mock(ConnectionTypeService.class));
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(DeviceProtocolMessageService.class).toInstance(mock(DeviceProtocolMessageService.class));
            bind(DeviceProtocolSecurityService.class).toInstance(mock(DeviceProtocolSecurityService.class));
            bind(DeviceCacheMarshallingService.class).toInstance(mock(DeviceCacheMarshallingService.class));
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(IdentificationService.class).toInstance(mock(IdentificationService.class));
        }

    }

    public interface ProtocolDialectProperties {
    }

}
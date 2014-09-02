package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.users.impl.UserModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;
import com.energyict.mdc.protocol.pluggable.mocks.DeviceMessageTestSpec;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocolWithTestPropertySpecs;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithMandatoryProperty;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import org.joda.time.DateMidnight;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link DeviceProtocolPluggableClassImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 11:39
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassImplTest {

    public static final String DEVICE_PROTOCOL_NAME = "DeviceProtocolPluggableClassName";
    public static final String MOCK_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol";
    public static final String MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES = "com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocolWithTestPropertySpecs";
    public static final String MOCK_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol";
    public static final String MOCK_SMART_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol";
    public static final String MOCK_NOT_A_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol";

    private static final String SDK_DEVICE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY = "com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithMandatoryProperty";

    private TransactionService transactionService;
    private ProtocolPluggableService protocolPluggableService;
    private DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
    private PropertySpecServiceImpl propertySpecService;

    @Rule
    public ExpectedConstraintViolationRule rule = new ExpectedConstraintViolationRule();

    @Mock
    private LicenseService licenseService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private License license;

    private DataModel dataModel;
    private InMemoryBootstrapModule bootstrapModule;

    @Before
    public void initializeDatabase() throws SQLException {
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdmin eventAdmin = mock(EventAdmin.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence.mdc.protocol.pluggable");
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>absent());
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
                new OrmModule(),
                new IssuesModule(),
                new PluggableModule(),
                new MdcCommonModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new ProtocolPluggableModule(),
                new UserModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(OrmService.class);
            transactionService = injector.getInstance(TransactionService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            propertySpecService = (PropertySpecServiceImpl) injector.getInstance(PropertySpecService.class);
            dataModel = ((ProtocolPluggableServiceImpl) protocolPluggableService).getDataModel();
            createOracleMetaDataTables(dataModel.getConnection(true));
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        IdBusinessObjectFactory deviceProtocolDialectFactory = mock(IdBusinessObjectFactory.class);
        when(deviceProtocolDialectFactory.getInstanceType()).thenReturn(DeviceProtocolDialect.class);
        when(applicationContext.findFactory(FactoryIds.DEVICE_PROTOCOL_DIALECT.id())).thenReturn(deviceProtocolDialectFactory);

        if (Environment.DEFAULT.get().getApplicationContext() != null) {
            fail("Application context was not cleaned up properly by previous test");
            if (Environment.DEFAULT.get().getApplicationContext().findFactory(FactoryIds.CODE.id()) != null) {
                fail("Code Factory was not cleaned up properly by previous test");
            }
        }
        propertySpecService.addFactoryProvider(new ReferencePropertySpecFinderProvider() {
            @Override
            public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
                List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
                finders.add(new ProtocolDialectPropertiesFinder());
                finders.add(new CodeFinder());
                return finders;
            }
        });

        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        environment.setApplicationContext(applicationContext);
    }

    private static void createOracleMetaDataTables(Connection connection) throws SQLException {
        executeDDL(connection, "create table if not exists user_tables (table_name varchar2(30) not null)");
        executeDDL(connection, "create table if not exists user_tab_columns (table_name varchar2(30) not null, column_name varchar2(30) not null)");
        executeDDL(connection, "create table if not exists user_sequences (sequence_name varchar2(30) not null)");
        executeDDL(connection, "create table if not exists user_ind_columns (index_name varchar2(30) not null, table_name varchar2(30), column_name varchar2(30), column_position number)");
    }

    private static void executeDDL(Connection connection, String ddl) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(ddl)) {
            preparedStatement.execute();
        }
    }

    @Before
    public void initializeLicenseService () {
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(this.license));
        when(this.licensedProtocolService.isValidJavaClassName(anyString(), eq(this.license))).thenReturn(true);
    }

    @Before
    public void initializeDeviceProtocolService() {
        when(deviceProtocolService.loadProtocolClass(MOCK_DEVICE_PROTOCOL)).thenReturn(MockDeviceProtocol.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES)).thenReturn(MockDeviceProtocolWithTestPropertySpecs.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_METER_PROTOCOL)).thenReturn(MockMeterProtocol.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_SMART_METER_PROTOCOL)).thenReturn(MockSmartMeterProtocol.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_NOT_A_DEVICE_PROTOCOL)).thenReturn(NotADeviceProtocol.class);
        when(deviceProtocolService.loadProtocolClass(SDK_DEVICE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY)).thenReturn(SDKDeviceProtocolTestWithMandatoryProperty.class);
    }

    @After
    public void cleanUp() throws BusinessException, SQLException {
        for (final DeviceProtocolPluggableClass pluggableClass : protocolPluggableService.findAllDeviceProtocolPluggableClasses().find()) {
            transactionService.execute(new Transaction<Object>() {
                @Override
                public Object perform() {
                    pluggableClass.delete();
                    return null;
                }
            });
        }
        bootstrapModule.deactivate();
    }

    @Test
    public void newInstanceDeviceProtocolTest() throws BusinessException, SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        return protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL);
                    }
                });

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
                execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        return protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES, creationProperties);
                    }
                });

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(DeviceProtocol.class);
        assertThat(deviceProtocol.getPropertySpecs()).hasSize(1);
        assertThat(deviceProtocol.getPropertySpec(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME)).isNotNull();
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
                execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
                        deviceProtocolPluggableClass.setProperty(
                                DeviceMessageTestSpec.extendedSpecs(propertySpecService).getPropertySpec(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME),
                                activationDate);
                        deviceProtocolPluggableClass.save();
                        return deviceProtocolPluggableClass;

                    }
                });

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(DeviceProtocol.class);
        assertThat(deviceProtocol.getPropertySpecs()).hasSize(1);
        assertThat(deviceProtocol.getPropertySpec(DeviceMessageTestSpec.ACTIVATIONDATE_PROPERTY_SPEC_NAME)).isNotNull();
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
                execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        return protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES, creationProperties);
                    }
                });

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
                execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL_WITH_PROPERTIES);
                        deviceProtocolPluggableClass.setProperty(foo, "bar");
                        deviceProtocolPluggableClass.save();
                        return deviceProtocolPluggableClass;
                    }
                });

        // Expected UnknownPluggableClassPropertiesException
    }

    @Test
    public void newInstanceMeterProtocolTest() throws BusinessException, SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        return protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_METER_PROTOCOL);
                    }
                });

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_METER_PROTOCOL);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(MeterProtocolAdapter.class);
    }

    @Test
    public void newInstanceSmartMeterProtocolTest() throws BusinessException, SQLException {
        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        return protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_SMART_METER_PROTOCOL);
                    }
                });

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_SMART_METER_PROTOCOL);
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        assertThat(deviceProtocol).isNotNull();
        assertThat(deviceProtocol).isInstanceOf(SmartMeterProtocolAdapter.class);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY+"}")
    public void newInstanceSmartMeterProtocolIllegalPropertyTest() throws BusinessException, SQLException {
        transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
            @Override
            public DeviceProtocolPluggableClass perform() {
                DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass("SDKDeviceProtocolTestWithMandatoryProperty", SDK_DEVICE_PROTOCOL_TEST_WITH_MANDATORY_PROPERTY);
                PropertySpec<?> deviceTimeZone = deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpec("SDKObisCodeProperty");
                deviceProtocolPluggableClass.setProperty(deviceTimeZone, new ObisCode(1,1,1,1,1,1));
                deviceProtocolPluggableClass.save();
                return deviceProtocolPluggableClass;
            }

        });
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE + "}")
    public void newInstanceNotADeviceProtocolTest() throws BusinessException, SQLException {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        return protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_NOT_A_DEVICE_PROTOCOL);
                    }
                });

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
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(InboundDeviceProtocolService.class).toInstance(mock(InboundDeviceProtocolService.class));
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(ConnectionTypeService.class).toInstance(mock(ConnectionTypeService.class));
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(DeviceProtocolMessageService.class).toInstance(mock(DeviceProtocolMessageService.class));
            bind(DeviceProtocolSecurityService.class).toInstance(mock(DeviceProtocolSecurityService.class));
            bind(DeviceCacheMarshallingService.class).toInstance(mock(DeviceCacheMarshallingService.class));
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

    public interface ProtocolDialectProperties {}

    private class ProtocolDialectPropertiesFinder implements CanFindByLongPrimaryKey {
        @Override
        public FactoryIds factoryId() {
            return FactoryIds.DEVICE_PROTOCOL_DIALECT;
        }

        @Override
        public Class<?> valueDomain() {
            return ProtocolDialectProperties.class;
        }

        @Override
        public Optional<?> findByPrimaryKey(long id) {
            return null;
        }

    }

    private class CodeFinder implements CanFindByLongPrimaryKey {
        @Override
        public FactoryIds factoryId() {
            return FactoryIds.CODE;
        }

        @Override
        public Class<Code> valueDomain() {
            return Code.class;
        }

        @Override
        public Optional<Code> findByPrimaryKey(long id) {
            return Optional.absent();
        }
    }

}
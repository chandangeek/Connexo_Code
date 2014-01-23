package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.LicenseServer;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
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
    public static final String MOCK_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol";
    public static final String MOCK_SMART_METER_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.MockSmartMeterProtocol";
    public static final String MOCK_NOT_A_DEVICE_PROTOCOL = "com.energyict.mdc.protocol.pluggable.mocks.NotADeviceProtocol";

    private static TransactionService transactionService;
    private static ProtocolPluggableService protocolPluggableService;
    private static DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);

    private static DataModel dataModel;
    @Mock
    private License license;

    @BeforeClass
    public static void initializeDatabase() throws SQLException {
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdmin eventAdmin = mock(EventAdmin.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence.mdc.protocol.pluggable");
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
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
                new MdcDynamicModule(),
                new ProtocolPluggableModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(OrmService.class);
            transactionService = injector.getInstance(TransactionService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            dataModel = ((ProtocolPluggableServiceImpl) protocolPluggableService).getDataModel();
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.findFactory(FactoryIds.DEVICE_PROTOCOL_DIALECT.id())).thenReturn(mock(IdBusinessObjectFactory.class));
        when(applicationContext.findFactory(FactoryIds.TIMEZONE_IN_USE.id())).thenReturn(mock(IdBusinessObjectFactory.class));
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        environment.setApplicationContext(applicationContext);
        createOracleMetaDataTables(environment.getConnection());
    }

    private static void createOracleMetaDataTables(Connection connection) throws SQLException {
        executeDDL(connection, "create table user_tables (table_name varchar2(30) not null)");
        executeDDL(connection, "create table user_tab_columns (table_name varchar2(30) not null, column_name varchar2(30) not null)");
        executeDDL(connection, "create table user_sequences (sequence_name varchar2(30) not null)");
        executeDDL(connection, "create table user_ind_columns (index_name varchar2(30) not null, table_name varchar2(30), column_name varchar2(30), column_position number)");
    }

    private static void executeDDL(Connection connection, String ddl) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(ddl)) {
            preparedStatement.execute();
        }
    }

    @BeforeClass
    public static void initializeDeviceProtocolService () {
        when(deviceProtocolService.loadProtocolClass(MOCK_DEVICE_PROTOCOL)).thenReturn(MockDeviceProtocol.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_METER_PROTOCOL)).thenReturn(MockMeterProtocol.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_SMART_METER_PROTOCOL)).thenReturn(MockSmartMeterProtocol.class);
        when(deviceProtocolService.loadProtocolClass(MOCK_NOT_A_DEVICE_PROTOCOL)).thenReturn(NotADeviceProtocol.class);
    }

    @AfterClass
    public static void cleanupDatabase () throws SQLException {
        new InMemoryPersistence().cleanUpDataBase();
    }

    @Before
    public void initializeLicense () {
        when(this.license.hasAllProtocols()).thenReturn(true);
        LicenseServer.licenseHolder.set(this.license);
    }

    @After
    public void cleanUp() throws BusinessException, SQLException {
        for (final DeviceProtocolPluggableClass pluggableClass : protocolPluggableService.findAllDeviceProtocolPluggableClasses()) {
            transactionService.execute(new Transaction<Object>() {
                @Override
                public Object perform() {
                    pluggableClass.delete();
                    return null;
                }
            });
        }
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
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
    }

    @Test
    public void saveDeviceProtocolTest() throws BusinessException, SQLException {

        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = transactionService.
                execute(new Transaction<DeviceProtocolPluggableClass>() {
                    @Override
                    public DeviceProtocolPluggableClass perform() {
                        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(DEVICE_PROTOCOL_NAME, MOCK_DEVICE_PROTOCOL);
                        deviceProtocolPluggableClass.save();
                        return deviceProtocolPluggableClass;
                    }
                });

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
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
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(MeterProtocolAdapter.class);
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
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(SmartMeterProtocolAdapter.class);
    }

    @Test(expected = ProtocolCreationException.class)
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


    private static class MockModule extends AbstractModule {

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
            bind(InboundDeviceProtocolService.class).toInstance(mock(InboundDeviceProtocolService.class));
            bind(LicensedProtocolService.class).toInstance(mock(LicensedProtocolService.class));
            bind(ConnectionTypeService.class).toInstance(mock(ConnectionTypeService.class));
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(DeviceProtocolMessageService.class).toInstance(mock(DeviceProtocolMessageService.class));
            bind(DeviceProtocolSecurityService.class).toInstance(mock(DeviceProtocolSecurityService.class));
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

}
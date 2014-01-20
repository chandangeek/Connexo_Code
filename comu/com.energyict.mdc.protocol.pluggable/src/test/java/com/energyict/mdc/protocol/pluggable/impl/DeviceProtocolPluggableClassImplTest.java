package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
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
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.LicenseServer;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolAdapter;
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
import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
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

    private static ProtocolPluggableService protocolPluggableService;
    private static DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
    private static PluggableService pluggableService = mock(PluggableService.class);

    private static DataModel dataModel;
    @Mock
    private License license;

    @BeforeClass
    public static void initializeDatabase() {
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdmin eventAdmin = mock(EventAdmin.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("InMemoryPersistence.mdc.protocol.pluggable");
        InMemoryBootstrapModule bootstrapModule = new InMemoryBootstrapModule();
        Injector injector = Guice.createInjector(
                new MockModule(bundleContext, eventAdmin, deviceProtocolService, pluggableService),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new DomainUtilModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new IssuesModule(),
                new MdcCommonModule(),
                new MdcDynamicModule(),
                new ProtocolPluggableModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(OrmService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            dataModel = ((ProtocolPluggableServiceImpl) protocolPluggableService).getDataModel();
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.findFactory(FactoryIds.TIMEZONE_IN_USE.id())).thenReturn(mock(IdBusinessObjectFactory.class));
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        environment.setApplicationContext(applicationContext);
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
        for (DeviceProtocolPluggableClass pluggableClass : protocolPluggableService.findAllDeviceProtocolPluggableClasses()) {
            pluggableClass.delete();
        }
    }

    @Test
    public void newInstanceDeviceProtocolTest() throws BusinessException, SQLException {
        PluggableClass pluggableClass = mock(PluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn(MOCK_DEVICE_PROTOCOL);
        when(pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, MOCK_DEVICE_PROTOCOL)).thenReturn(Arrays.asList(pluggableClass));

        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(MOCK_DEVICE_PROTOCOL);

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
    }

    @Test
    public void saveDeviceProtocolTest() throws BusinessException, SQLException {
        PluggableClass pluggableClass = mock(PluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn(MOCK_DEVICE_PROTOCOL);
        when(pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, MOCK_DEVICE_PROTOCOL)).thenReturn(Arrays.asList(pluggableClass));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(MOCK_DEVICE_PROTOCOL);

        // Business method
        deviceProtocolPluggableClass.save();

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_DEVICE_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(DeviceProtocol.class);
    }

    @Test
    public void newInstanceMeterProtocolTest() throws BusinessException, SQLException {
        PluggableClass pluggableClass = mock(PluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn(MOCK_METER_PROTOCOL);
        when(pluggableClass.getProperties(anyList())).thenReturn(TypedProperties.empty());
        when(pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, MOCK_METER_PROTOCOL)).thenReturn(Arrays.asList(pluggableClass));

        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(MOCK_METER_PROTOCOL);

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_METER_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(MeterProtocolAdapter.class);
    }

    @Test
    public void newInstanceSmartMeterProtocolTest() throws BusinessException, SQLException {
        PluggableClass pluggableClass = mock(PluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn(MOCK_SMART_METER_PROTOCOL);
        when(pluggableClass.getProperties(anyList())).thenReturn(TypedProperties.empty());
        when(pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, MOCK_SMART_METER_PROTOCOL)).thenReturn(Arrays.asList(pluggableClass));

        // Business method
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(MOCK_SMART_METER_PROTOCOL);

        // asserts
        assertThat(deviceProtocolPluggableClass).isNotNull();
        assertThat(deviceProtocolPluggableClass.getJavaClassName()).isEqualTo(MOCK_SMART_METER_PROTOCOL);
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isNotNull();
        assertThat(deviceProtocolPluggableClass.getDeviceProtocol()).isInstanceOf(SmartMeterProtocolAdapter.class);
    }

    @Test(expected = ProtocolCreationException.class)
    public void newInstanceNotADeviceProtocolTest() throws BusinessException, SQLException {
        PluggableClass pluggableClass = mock(PluggableClass.class);
        when(pluggableClass.getJavaClassName()).thenReturn(MOCK_NOT_A_DEVICE_PROTOCOL);
        when(pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, MOCK_NOT_A_DEVICE_PROTOCOL)).thenReturn(Arrays.asList(pluggableClass));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.newDeviceProtocolPluggableClass(MOCK_NOT_A_DEVICE_PROTOCOL);

        // Business method
        deviceProtocolPluggableClass.getDeviceProtocol();
    }


    private static class MockModule extends AbstractModule {

        private BundleContext bundleContext;
        private EventAdmin eventAdmin;
        private DeviceProtocolService deviceProtocolService;
        private PluggableService pluggableService;

        private MockModule(BundleContext bundleContext, EventAdmin eventAdmin, DeviceProtocolService deviceProtocolService, PluggableService pluggableService) {
            super();
            this.bundleContext = bundleContext;
            this.eventAdmin = eventAdmin;
            this.deviceProtocolService = deviceProtocolService;
            this.pluggableService = pluggableService;
        }

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(InboundDeviceProtocolService.class).toInstance(mock(InboundDeviceProtocolService.class));
            bind(LicensedProtocolService.class).toInstance(mock(LicensedProtocolService.class));
            bind(ConnectionTypeService.class).toInstance(mock(ConnectionTypeService.class));
            bind(PluggableService.class).toInstance(pluggableService);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }

    }

}
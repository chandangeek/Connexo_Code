package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.BusinessEventManager;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.finders.ConnectionMethodFinder;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.LicenseServer;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SecurityPropertyServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-15 (09:010)
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertyServiceImplTest {

    @Mock
    private PluggableService pluggableService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;
    @Mock
    private RelationType securityPropertyRelationType;
    @Mock
    private Clock clock;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private DeviceCacheMarshallingService deviceCacheMarshallingService;

    private InMemoryPersistence inMemoryPersistence;

    @Before
    public void initializeMocks () {
        when(this.clock.now()).thenReturn(new Date());
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(this.securityPropertyRelationType);
        when(this.deviceProtocolPluggableClass.getProperties(anyListOf(PropertySpec.class))).thenReturn(TypedProperties.empty());

        when(this.securityPropertySet.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.securityPropertySet.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
    }

    @Test
    public void getSecurityPropertiesForUserThatIsNotAllowedToView () {
        when(this.securityPropertySet.currentUserIsAllowedToEditDeviceProperties()).thenReturn(false);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, new Date(), this.securityPropertySet);

        // Asserts
        verify(this.securityPropertySet).currentUserIsAllowedToViewDeviceProperties();
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityPropertiesWithoutRelationType () {
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(null);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, new Date(), this.securityPropertySet);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityProperties () throws SQLException {

        try {
            this.initializeDatabase();
            // Business method
            List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, new Date(), this.securityPropertySet);

            // Asserts
            verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
            assertThat(securityProperties).isEmpty();
        } finally {
            if (this.inMemoryPersistence != null) {
                this.inMemoryPersistence.cleanUpDataBase();
            }
        }
    }

    private SecurityPropertyService testService () {
        return new SecurityPropertyServiceImpl(this.protocolPluggableService);
    }

    private void initializeDatabase () throws SQLException {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase();
        when(this.pluggableService.newPluggableClass(PluggableClassType.DeviceProtocol, "SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName())).
        thenReturn(this.deviceProtocolPluggableClass);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(new TestProtocolWithOnlySecurityProperties());
        when(this.deviceProtocolPluggableClass.getJavaClassName()).thenReturn(TestProtocolWithOnlySecurityProperties.class.getName());
        this.inMemoryPersistence.newDeviceProtocolPluggableClass("SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName());
    }

    private class InMemoryPersistence {
        private BundleContext bundleContext;
        private Principal principal;
        private EventAdmin eventAdmin;
        private TransactionService transactionService;
        private OrmService ormService;
        private EventService eventService;
        private NlsService nlsService;
        private RelationService relationService;
        private ProtocolPluggableService protocolPluggableService;
        private InMemoryBootstrapModule bootstrapModule;
        private Environment environment;
        private ApplicationContext applicationContext;
        private PropertySpecService propertySpecService;

        public RelationService getRelationService() {
            return relationService;
        }

        public ProtocolPluggableService getProtocolPluggableService() {
            return protocolPluggableService;
        }

        private void initializeDatabase () throws SQLException {
            this.initializeMocks();
            this.bootstrapModule = new InMemoryBootstrapModule();
            Injector injector = Guice.createInjector(
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(JsonService.class).toInstance(new JsonServiceImpl());
                            bind(BeanService.class).toInstance(new BeanServiceImpl());
                            bind(Clock.class).toInstance(clock);
                            bind(EventAdmin.class).toInstance(eventAdmin);
                            bind(BundleContext.class).toInstance(bundleContext);
                            bind(PluggableService.class).toInstance(pluggableService);
                            bind(PluggableService.class).toInstance(pluggableService);
                            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
                            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
                            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
                            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
                            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
                            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
                            bind(DeviceCacheMarshallingService.class).toInstance(deviceCacheMarshallingService);
                        }
                    },
                    this.bootstrapModule,
                    new ThreadSecurityModule(this.principal),
                    new EventsModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new InMemoryMessagingModule(),
                    new OrmModule(),
                    new IssuesModule(),
                    new MdcCommonModule(),
                    new MdcDynamicModule(),
                    new ProtocolPluggableModule());
            this.transactionService = injector.getInstance(TransactionService.class);
            this.environment = injector.getInstance(Environment.class);
            this.environment.setApplicationContext(this.applicationContext);
            try (TransactionContext ctx = this.transactionService.getContext()) {
                this.ormService = injector.getInstance(OrmService.class);
                this.transactionService = injector.getInstance(TransactionService.class);
                this.eventService = injector.getInstance(EventService.class);
                this.nlsService = injector.getInstance(NlsService.class);
                this.relationService = injector.getInstance(RelationService.class);
                this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
                this.ormService = injector.getInstance(OrmService.class);
                this.propertySpecService = injector.getInstance(PropertySpecService.class);
                createOracleAliases((OrmServiceImpl) this.ormService);
                initializeFactoryProviders();
                ctx.commit();
            }
        }

        private void initializeFactoryProviders() {
            ((PropertySpecServiceImpl) this.propertySpecService).addFactoryProvider(new ReferencePropertySpecFinderProvider() {
                @Override
                public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
                    List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
                    finders.add(new ConnectionMethodFinder(ormService.getDataModels().get(0)));
                    finders.add(new DeviceFinder(ormService.getDataModels().get(0)));
                    finders.add(new SecuritySetFinder(ormService.getDataModels().get(0)));
                    return finders;
                }
            });
        }

        private void initializeMocks() {
            this.applicationContext = mock(ApplicationContext.class);
            BusinessEventManager eventManager = mock(BusinessEventManager.class);
            when(this.applicationContext.createEventManager()).thenReturn(eventManager);
            Translator translator = mock(Translator.class);
            when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
            when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
            when(this.applicationContext.getTranslator()).thenReturn(translator);
            when(this.applicationContext.findFactory(FactoryIds.DEVICE.id())).thenReturn(mock(IdBusinessObjectFactory.class));
            when(this.applicationContext.findFactory(FactoryIds.SECURITY_SET.id())).thenReturn(mock(IdBusinessObjectFactory.class));
            this.bundleContext = mock(BundleContext.class);
            this.eventAdmin = mock(EventAdmin.class);
            this.principal = mock(Principal.class);
            when(this.principal.getName()).thenReturn("SecurityPropertyServiceImplTest");
            when(deviceProtocolService.loadProtocolClass(TestProtocolWithOnlySecurityProperties.class.getName())).thenReturn(TestProtocolWithOnlySecurityProperties.class);
        }

        private void createOracleAliases(OrmServiceImpl ormService) throws SQLException {
            try (PreparedStatement preparedStatement = ormService.getConnection(true).prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_TABLES AS select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = ormService.getConnection(true).prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_IND_COLUMNS AS select index_name, table_name, column_name, ordinal_position AS column_position from INFORMATION_SCHEMA.INDEXES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = ormService.getConnection(true).prepareStatement(
                    "CREATE TABLE IF NOT EXISTS USER_SEQUENCES ( SEQUENCE_NAME VARCHAR2 (30) NOT NULL, MIN_VALUE NUMBER, MAX_VALUE NUMBER, INCREMENT_BY NUMBER NOT NULL, CYCLE_FLAG VARCHAR2 (1), ORDER_FLAG VARCHAR2 (1), CACHE_SIZE NUMBER NOT NULL, LAST_NUMBER NUMBER NOT NULL)"
            )) {
                preparedStatement.execute();
            }
        }

        public void cleanUpDataBase() throws SQLException {
            this.bootstrapModule.deactivate();
        }

        public void newDeviceProtocolPluggableClass(String name, String javaClassName) {
            try (TransactionContext ctx = this.transactionService.getContext()) {
                protocolPluggableService.newDeviceProtocolPluggableClass(name, javaClassName);
                ctx.commit();
            }

        }
    }
}
package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.finders.ConnectionTaskFinder;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
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
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(clock.instant()).thenReturn(Instant.now());
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.pluggableService.newPluggableClass(PluggableClassType.DeviceProtocol, "SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName())).
                thenReturn(this.deviceProtocolPluggableClass);
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(this.securityPropertyRelationType);
        when(this.protocolPluggableService.isLicensedProtocolClassName(anyString())).thenReturn(true);

        when(this.securityPropertySet.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.securityPropertySet.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
    }

    @Test
    public void getSecurityPropertiesForUserThatIsNotAllowedToView () {
        when(this.securityPropertySet.currentUserIsAllowedToEditDeviceProperties()).thenReturn(false);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet);

        // Asserts
        verify(this.securityPropertySet).currentUserIsAllowedToViewDeviceProperties();
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityPropertiesWithoutRelationType () {
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(null);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityProperties () throws SQLException {
        try {
            this.initializeDatabase();

            // Business method
            List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet);

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
        this.inMemoryPersistence.newDeviceProtocolPluggableClass("SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName());
        when(this.deviceProtocolPluggableClass.getProperties(anyListOf(PropertySpec.class))).thenReturn(TypedProperties.empty());
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(new TestProtocolWithOnlySecurityProperties(inMemoryPersistence.propertySpecService));
        when(this.deviceProtocolPluggableClass.getJavaClassName()).thenReturn(TestProtocolWithOnlySecurityProperties.class.getName());
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
        private InMemoryBootstrapModule bootstrapModule;
        private PropertySpecService propertySpecService;
        private LicenseService licenseService;
        private DeviceConfigurationService deviceConfigurationService;

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
                            bind(LicenseService.class).toInstance(licenseService);
                            bind(BundleContext.class).toInstance(bundleContext);
                            bind(PluggableService.class).toInstance(pluggableService);
                            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
                            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
                            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
                            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
                            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
                            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
                            bind(DeviceCacheMarshallingService.class).toInstance(deviceCacheMarshallingService);
                            bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
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
                    new DataVaultModule(),
                    new IssuesModule(),
                    new MdcCommonModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule());
            this.transactionService = injector.getInstance(TransactionService.class);
            try (TransactionContext ctx = this.transactionService.getContext()) {
                this.ormService = injector.getInstance(OrmService.class);
                this.transactionService = injector.getInstance(TransactionService.class);
                this.eventService = injector.getInstance(EventService.class);
                this.nlsService = injector.getInstance(NlsService.class);
                this.relationService = injector.getInstance(RelationService.class);
                this.ormService = injector.getInstance(OrmService.class);
                this.propertySpecService = injector.getInstance(PropertySpecService.class);
                this.deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
                createOracleAliases((OrmServiceImpl) this.ormService);
                initializeFactoryProviders();
                ctx.commit();
            }
        }

        private void initializeFactoryProviders() {
            this.propertySpecService.addFactoryProvider(() -> {
                List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
                finders.add(new ConnectionTaskFinder(ormService.getDataModels().get(0)));
                finders.add(new DeviceFinder(ormService.getDataModels().get(0)));
                finders.add(new SecuritySetFinder(deviceConfigurationService));
                return finders;
            });
        }

        private void initializeMocks() {
            this.bundleContext = mock(BundleContext.class);
            this.eventAdmin = mock(EventAdmin.class);
            this.principal = mock(Principal.class);
            this.deviceConfigurationService = mock(DeviceConfigurationService.class);
            when(this.principal.getName()).thenReturn("SecurityPropertyServiceImplTest");
            this.licenseService = mock(LicenseService.class);
            when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.<License>empty());
            when(deviceProtocolService.createProtocol(TestProtocolWithOnlySecurityProperties.class.getName())).thenReturn(new TestProtocolWithOnlySecurityProperties(propertySpecService));
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
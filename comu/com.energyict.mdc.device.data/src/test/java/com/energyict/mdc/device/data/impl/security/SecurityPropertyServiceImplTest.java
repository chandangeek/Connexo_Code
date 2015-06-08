package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;

import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.finders.ConnectionTaskFinder;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
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
import com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames;

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
import com.elster.jupiter.properties.StringFactory;
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
import com.google.common.collect.Range;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    private static final long SECURITY_PROPERTY_SET1_ID = 97L;
    private static final long SECURITY_PROPERTY_SET2_ID = 103L;
    private static final long ETERNITY = 1_000_000_000_000_000_000L;
    private static final String USERNAME_SECURITY_PROPERTY_NAME = "username";
    private static final String PASSWORD_SECURITY_PROPERTY_NAME = "password";
    private static final String SOME_KEY_SECURITY_PROPERTY_NAME = "someKey";

    @Mock
    private PluggableService pluggableService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private SecurityPropertySet securityPropertySet1;
    @Mock
    private SecurityPropertySet securityPropertySet2;
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
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));
        ComTaskEnablement cte1 = mock(ComTaskEnablement.class);
        when(cte1.getSecurityPropertySet()).thenReturn(this.securityPropertySet1);
        ComTaskEnablement cte2 = mock(ComTaskEnablement.class);
        when(cte2.getSecurityPropertySet()).thenReturn(this.securityPropertySet2);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(cte1, cte2));
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.pluggableService
                .newPluggableClass(PluggableClassType.DeviceProtocol, "SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName()))
                .thenReturn(this.deviceProtocolPluggableClass);
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(this.securityPropertyRelationType);
        when(this.protocolPluggableService.isLicensedProtocolClassName(anyString())).thenReturn(true);

        when(this.securityPropertySet1.getId()).thenReturn(SECURITY_PROPERTY_SET1_ID);
        when(this.securityPropertySet1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        PropertySpec userName = mock(PropertySpec.class);
        when(userName.getName()).thenReturn(USERNAME_SECURITY_PROPERTY_NAME);
        when(userName.isRequired()).thenReturn(true);
        when(userName.getValueFactory()).thenReturn(new StringFactory());
        PropertySpec password = mock(PropertySpec.class);
        when(password.getName()).thenReturn(PASSWORD_SECURITY_PROPERTY_NAME);
        when(password.isRequired()).thenReturn(true);
        when(password.getValueFactory()).thenReturn(new StringFactory());
        when(this.securityPropertySet1.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(userName, password)));
        when(this.securityPropertySet2.getId()).thenReturn(SECURITY_PROPERTY_SET2_ID);
        when(this.securityPropertySet2.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(this.securityPropertySet2.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        PropertySpec someKey = mock(PropertySpec.class);
        when(someKey.getName()).thenReturn(SOME_KEY_SECURITY_PROPERTY_NAME);
        when(someKey.getValueFactory()).thenReturn(new StringFactory());
        when(someKey.isRequired()).thenReturn(true);
        when(this.securityPropertySet2.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(someKey)));
    }

    @Test
    public void getSecurityPropertiesForUserThatIsNotAllowedToView () {
        when(this.securityPropertySet1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(false);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet1);

        // Asserts
        verify(this.securityPropertySet1).currentUserIsAllowedToViewDeviceProperties();
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityPropertiesWithoutRelationType () {
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(null);

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet1);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        assertThat(securityProperties).isEmpty();
    }

    @Test
    public void getSecurityProperties () throws SQLException {
        try {
            this.initializeDatabase();

            // Business method
            List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, Instant.now(), this.securityPropertySet1);

            // Asserts
            verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
            assertThat(securityProperties).isEmpty();
        } finally {
            if (this.inMemoryPersistence != null) {
                this.inMemoryPersistence.cleanUpDataBase();
            }
        }
    }

    @Test
    public void securityPropertiesAreNotValidWhenAllAreMissing() {
        SecurityPropertyService service = this.testService();
        when(this.securityPropertyRelationType.findByFilter(any(RelationSearchFilter.class))).thenReturn(Collections.emptyList());

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreNotValidWhenOneIsMissing() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Collections.emptyList());

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreValidWhenUnusedOneIsMissing() {
        SecurityPropertySet unused = mock(SecurityPropertySet.class);
        when(unused.getId()).thenReturn(SECURITY_PROPERTY_SET2_ID + 1);
        PropertySpec otherKey = mock(PropertySpec.class);
        when(otherKey.getName()).thenReturn("Other");
        when(otherKey.getValueFactory()).thenReturn(new StringFactory());
        when(otherKey.isRequired()).thenReturn(true);
        when(unused.getPropertySpecs()).thenReturn(new HashSet<>(Arrays.asList(otherKey)));
        when(this.deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet1, this.securityPropertySet2));
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn("something");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isTrue();
    }

    @Test
    public void securityPropertiesAreNotValidWhenAllAreInComplete() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(false);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn(null);
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(false);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn(null);
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreNotValidWhenSomeAreInComplete() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(false);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn(null);
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isFalse();
    }

    @Test
    public void securityPropertiesAreValid() {
        Instant now = Instant.ofEpochSecond(1430523600L);
        when(this.clock.instant()).thenReturn(now);
        Range<Instant> period = Range.closedOpen(now, Instant.ofEpochMilli(ETERNITY));
        SecurityPropertyService service = this.testService();
        Relation relationForSecurityPropertySet1 = mock(Relation.class);
        when(relationForSecurityPropertySet1.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet1.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet1.get(USERNAME_SECURITY_PROPERTY_NAME)).thenReturn("test");
        when(relationForSecurityPropertySet1.get(PASSWORD_SECURITY_PROPERTY_NAME)).thenReturn("pass");
        Relation relationForSecurityPropertySet2 = mock(Relation.class);
        when(relationForSecurityPropertySet2.getPeriod()).thenReturn(period);
        when(relationForSecurityPropertySet2.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME)).thenReturn(true);
        when(relationForSecurityPropertySet2.get(SOME_KEY_SECURITY_PROPERTY_NAME)).thenReturn("something");
        when(this.securityPropertyRelationType
                .findByFilter(any(RelationSearchFilter.class)))
                .thenReturn(
                        Arrays.asList(relationForSecurityPropertySet1),
                        Arrays.asList(relationForSecurityPropertySet2));

        // Business method
        boolean securityPropertiesAreValid = service.securityPropertiesAreValid(this.device);

        // Asserts
        assertThat(securityPropertiesAreValid).isTrue();
    }

    private SecurityPropertyService testService () {
        return new SecurityPropertyServiceImpl(this.clock, this.protocolPluggableService);
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
        private DeviceService deviceService;
        private RelationService relationService;
        private InMemoryBootstrapModule bootstrapModule;
        private PropertySpecService propertySpecService;
        private LicenseService licenseService;
        private DeviceConfigurationService deviceConfigurationService;
        private TimeService timeService;

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
                            bind(TimeService.class).toInstance(timeService);
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
                            bind(DeviceService.class).toInstance(deviceService);
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
                    new BasicPropertiesModule(),
                    new MdcDynamicModule());
            this.transactionService = injector.getInstance(TransactionService.class);
            try (TransactionContext ctx = this.transactionService.getContext()) {
                this.ormService = injector.getInstance(OrmService.class);
                this.transactionService = injector.getInstance(TransactionService.class);
                injector.getInstance(EventService.class);
                injector.getInstance(NlsService.class);
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
                finders.add(new ConnectionTaskFinder(this.ormService.getDataModels().get(0)));
                finders.add(new DeviceFinder(this.deviceService));
                finders.add(new SecuritySetFinder(deviceConfigurationService));
                return finders;
            });
        }

        private void initializeMocks() {
            this.timeService = mock(TimeService.class);
            this.bundleContext = mock(BundleContext.class);
            this.eventAdmin = mock(EventAdmin.class);
            this.principal = mock(Principal.class);
            this.deviceConfigurationService = mock(DeviceConfigurationService.class);
            this.deviceService = mock(DeviceService.class);
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
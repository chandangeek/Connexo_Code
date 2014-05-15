package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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

    private InMemoryPersistence inMemoryPersistence;

    @Before
    public void initializeMocks () {
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.protocolPluggableService.findSecurityPropertyRelationType(this.deviceProtocolPluggableClass)).thenReturn(this.securityPropertyRelationType);

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
        this.initializeDatabase();

        // Business method
        List<SecurityProperty> securityProperties = this.testService().getSecurityProperties(this.device, new Date(), this.securityPropertySet);

        // Asserts
        verify(this.protocolPluggableService).findSecurityPropertyRelationType(this.deviceProtocolPluggableClass);
        assertThat(securityProperties).isEmpty();
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
        this.inMemoryPersistence.getProtocolPluggableService()
                    .newDeviceProtocolPluggableClass("SecurityPropertyServiceImplTest", TestProtocolWithOnlySecurityProperties.class.getName());
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
                    new MdcDynamicModule());
            this.transactionService = injector.getInstance(TransactionService.class);
            try (TransactionContext ctx = this.transactionService.getContext()) {
                this.ormService = injector.getInstance(OrmService.class);
                this.transactionService = injector.getInstance(TransactionService.class);
                this.eventService = injector.getInstance(EventService.class);
                this.nlsService = injector.getInstance(NlsService.class);
                this.relationService = injector.getInstance(RelationService.class);
                this.protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
                ctx.commit();
            }
            createOracleAliases();
        }

        private void initializeMocks() {
            this.bundleContext = mock(BundleContext.class);
            this.eventAdmin = mock(EventAdmin.class);
            this.principal = mock(Principal.class);
            when(this.principal.getName()).thenReturn("SecurityPropertyServiceImplTest");
        }

        private void createOracleAliases() throws SQLException {
            try (PreparedStatement preparedStatement = Environment.DEFAULT.get().getConnection().prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_TABLES AS select table_name from INFORMATION_SCHEMA.TABLES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = Environment.DEFAULT.get().getConnection().prepareStatement(
                    "CREATE VIEW IF NOT EXISTS USER_IND_COLUMNS AS select index_name, table_name, column_name, ordinal_position AS column_position from INFORMATION_SCHEMA.INDEXES where table_schema = 'PUBLIC'"
            )) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = Environment.DEFAULT.get().getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS USER_SEQUENCES ( SEQUENCE_NAME VARCHAR2 (30) NOT NULL, MIN_VALUE NUMBER, MAX_VALUE NUMBER, INCREMENT_BY NUMBER NOT NULL, CYCLE_FLAG VARCHAR2 (1), ORDER_FLAG VARCHAR2 (1), CACHE_SIZE NUMBER NOT NULL, LAST_NUMBER NUMBER NOT NULL)"
            )) {
                preparedStatement.execute();
            }
            Environment.DEFAULT.get().closeConnection();
        }

        public void cleanUpDataBase() throws SQLException {
            this.bootstrapModule.deactivate();
        }

    }
}
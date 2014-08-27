package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsServiceImpl;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.MeteringServiceImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyServiceImpl;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.*;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceGroupTest {

    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    private static final String ED_MRID = " ( ";
    private Injector injector;
    private InMemoryBootstrapModule bootstrapModule;

    static final String DEVICE_TYPE_NAME = "DeviceTypeName";
    protected static InMemoryIntegrationPersistence inMemoryPersistence;

    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    @Mock
    DeviceProtocol deviceProtocol;


    @Mock
    private Principal principal;
    @Mock
    private Thesaurus thesaurus;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LicenseService licenseService;
    @Mock
    private IssueService issueService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private RelationService relationService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private DeviceCacheMarshallingService deviceCacheMarshallingService;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private UserService userService;
    @Mock
    private SecurityPropertyService securityPropertyService;



    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(IssueService.class).toInstance(issueService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(RelationService.class).toInstance(relationService);
            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
            bind(DeviceCacheMarshallingService.class).toInstance(deviceCacheMarshallingService);
            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(UserService.class).toInstance(userService);
            bind(SecurityPropertyService.class).toInstance(securityPropertyService);
        }
    }

    @Before
    public void setUp() throws SQLException {
        when(principal.getName()).thenReturn("Ernie");
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new ThreadSecurityModule(this.principal),
                this.bootstrapModule,
                new OrmModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new MasterDataModule(),
                new PartyModule(),
                new IdsModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new ValidationModule(),
                new SchedulingModule(),
                new ProtocolPluggableModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new MdcReadingTypeUtilServiceModule(),
                new PluggableModule(),
                new DeviceConfigurationModule(),
                new TasksModule(),
                new MockModule(),
                new MeteringGroupsModule()
        );
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(MeteringGroupsService.class);
                injector.getInstance(ThreadPrincipalService.class);
                injector.getInstance(ValidationService.class);
                injector.getInstance(MasterDataService.class);
                injector.getInstance(DeviceConfigurationService.class);
                injector.getInstance(DeviceDataServiceImpl.class);
                injector.getInstance(SecurityPropertyServiceImpl.class);

                MeteringGroupsService meteringGroupsService = (MeteringGroupsServiceImpl) injector.getInstance(MeteringGroupsService.class);
                MeteringService meteringService = (MeteringServiceImpl) injector.getInstance(MeteringService.class);
                DeviceDataService deviceDataService = (DeviceDataServiceImpl) injector.getInstance(DeviceDataServiceImpl.class);

                DeviceEndDeviceQueryProvider endDeviceQueryProvider = new DeviceEndDeviceQueryProvider();
                endDeviceQueryProvider.setMeteringService(meteringService);
                endDeviceQueryProvider.setDeviceDataService(deviceDataService);
                meteringGroupsService.addEndDeviceQueryProvider(endDeviceQueryProvider);

                return null;
            }
        });

    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistence() {
        EndDevice endDevice = null;
        try(TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);

            DeviceDataService deviceDataService = injector.getInstance(DeviceDataServiceImpl.class);
            Device device = deviceDataService.newDevice(getDeviceConfiguration(), "name", ED_MRID);
            device.save();
            endDevice = meteringService.findAmrSystem(1).get().newMeter(String.valueOf(device.getId()), ED_MRID);
            endDevice.save();
            ctx.commit();
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {

            Condition conditionDevice =
                    Condition.TRUE.and(where("deviceConfiguration.deviceType.name").isEqualTo(DEVICE_TYPE_NAME));


            QueryEndDeviceGroup queryEndDeviceGroup =
                    meteringGroupsService.createQueryEndDeviceGroup(conditionDevice);
            queryEndDeviceGroup.setMRID("mine");
            queryEndDeviceGroup.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPRVIDER);
            queryEndDeviceGroup.save();
            ctx.commit();
        }

        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("mine");
        assertThat(found).isPresent();
        assertThat(found.get()).isInstanceOf(QueryEndDeviceGroup.class);
        QueryEndDeviceGroup group = (QueryEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(new DateTime(2014, 1, 23, 14, 54).toDate());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(endDevice.getId());
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }

    private DeviceConfiguration getDeviceConfiguration() {
        DeviceType deviceType = injector.getInstance(DeviceConfigurationService.class).newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("DeviceConfiguration");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
    }

}

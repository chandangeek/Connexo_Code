package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.events.EndDeviceGroupDeletionVetoEventHandler;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyServiceImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceGroupTest {

    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    private static final String ED_MRID = "mrid1";
    private static final String ED_MRID2 = "mrid2";
    private static final String DEVICE_NAME1 = "devicename1";
    private static final String DEVICE_NAME2 = "devicename2";
    private Injector injector;

    private static final String DEVICE_TYPE_NAME = "DeviceTypeName";

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private static Principal principal;
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
            bind(com.elster.jupiter.issue.share.service.IssueService.class).toInstance(mock(com.elster.jupiter.issue.share.service.IssueService.class, RETURNS_DEEP_STUBS));
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(RelationService.class).toInstance(relationService);
            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
            bind(DeviceCacheMarshallingService.class).toInstance(deviceCacheMarshallingService);
            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(LogService.class).toInstance(mock(LogService.class));
        }
    }

    @Before
    public void setUp() throws SQLException {
        when(principal.getName()).thenReturn("Ernie");
        this.inMemoryBootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new ThreadSecurityModule(principal),
                this.inMemoryBootstrapModule,
                new OrmModule(),
                new DataVaultModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new MasterDataModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(false),
                new InMemoryMessagingModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new SchedulingModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MdcReadingTypeUtilServiceModule(),
                new PluggableModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new KpiModule(),
                new TaskModule(),
                new TasksModule(),
                new MeteringGroupsModule(),
                new DeviceDataModule(),
                new MockModule()
        );
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(DeviceConfigurationService.class);
            injector.getInstance(SecurityPropertyServiceImpl.class);

            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            DeviceServiceImpl deviceService = injector.getInstance(DeviceServiceImpl.class);

            DeviceEndDeviceQueryProvider endDeviceQueryProvider = new DeviceEndDeviceQueryProvider();
            endDeviceQueryProvider.setMeteringService(meteringService);
            endDeviceQueryProvider.setDeviceService(deviceService);
            meteringGroupsService.addEndDeviceQueryProvider(endDeviceQueryProvider);

            return null;
        });

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

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistenceDynamicGroup() {
        EndDevice endDevice;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            DeviceService deviceService = injector.getInstance(DeviceServiceImpl.class);
            Device device = deviceService.newDevice(getDeviceConfiguration(), DEVICE_NAME1, ED_MRID);
            device.save();
            endDevice = meteringService.findAmrSystem(1).get().findMeter(String.valueOf(device.getId())).orElseThrow(() -> new RuntimeException("MDC bundle failed to create Kore meter in AMR system"));
            ctx.commit();
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {

            Condition conditionDevice =
                    Condition.TRUE.and(where("deviceConfiguration.deviceType.name").isEqualTo(DEVICE_TYPE_NAME));


            QueryEndDeviceGroup queryEndDeviceGroup =
                    meteringGroupsService.createQueryEndDeviceGroup(conditionDevice);
            queryEndDeviceGroup.setMRID("dynamic");
            queryEndDeviceGroup.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER);
            queryEndDeviceGroup.save();
            ctx.commit();
        }

        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("dynamic");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(QueryEndDeviceGroup.class);
        QueryEndDeviceGroup group = (QueryEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(new DateTime(2014, 1, 23, 14, 54).toDate().toInstant());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(endDevice.getId());
    }

    @Test
    public void testPersistenceStaticGroup() {
        EndDevice endDevice;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            DeviceService deviceService = injector.getInstance(DeviceServiceImpl.class);
            Device device = deviceService.newDevice(getDeviceConfiguration(), DEVICE_NAME2, ED_MRID2);
            device.save();
            endDevice = meteringService.findAmrSystem(1).get().findMeter(String.valueOf(device.getId())).orElseThrow(() -> new RuntimeException("MDC bundle failed to create Kore meter in AMR system"));
            ctx.commit();
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup("enumerated");
            enumeratedEndDeviceGroup.setMRID("enumerated");
            enumeratedEndDeviceGroup.add(endDevice, Interval.sinceEpoch().toClosedRange());
            enumeratedEndDeviceGroup.save();
            ctx.commit();
        }

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {

            Condition conditionDevice =
                    Condition.TRUE.and(where("deviceConfiguration.deviceType.name").isEqualTo(DEVICE_TYPE_NAME));


            QueryEndDeviceGroup queryEndDeviceGroup =
                    meteringGroupsService.createQueryEndDeviceGroup(conditionDevice);
            queryEndDeviceGroup.setMRID("dynamic");
            queryEndDeviceGroup.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER);
            queryEndDeviceGroup.save();
            ctx.commit();
        }

        List<EndDeviceGroup> allDevicesGroups = meteringGroupsService.findEndDeviceGroups();
        Assertions.assertThat(allDevicesGroups).hasSize(2);

        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("enumerated");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(EnumeratedEndDeviceGroup.class);
        EnumeratedEndDeviceGroup group = (EnumeratedEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(new DateTime(2014, 1, 23, 14, 54).toDate().toInstant());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(endDevice.getId());
    }

    @Test
    @Ignore // Can't get the TopicHandler registration right
    public void testVetoDeletionOfDeviceGroupInKpi() throws Exception {
        injector.getInstance(EventServiceImpl.class).addTopicHandler(injector.getInstance(EndDeviceGroupDeletionVetoEventHandler.class));
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        DataCollectionKpiService kpiService = injector.getInstance(DataCollectionKpiService.class);
        QueryEndDeviceGroup queryEndDeviceGroup;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            queryEndDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(Operator.EQUAL.compare("id", 15).or(Operator.EQUAL.compare("mRID", ED_MRID)));
            queryEndDeviceGroup.setMRID("mine");
            queryEndDeviceGroup.setQueryProviderName(SimpleEndDeviceQueryProvider.SIMPLE_ENDDEVICE_QUERYPRVIDER);
            queryEndDeviceGroup.save();
            ctx.commit();
        }

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            DataCollectionKpiService.DataCollectionKpiBuilder dataCollectionKpiBuilder = kpiService.newDataCollectionKpi(queryEndDeviceGroup);
            dataCollectionKpiBuilder.frequency(Duration.ofMinutes(15)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.TEN);
            dataCollectionKpiBuilder.save();
            ctx.commit();
        }

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            queryEndDeviceGroup.delete();
        }
    }


    private DeviceConfiguration getDeviceConfiguration() {

        DeviceType deviceType = injector.getInstance(DeviceConfigurationService.class).newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("DeviceConfiguration");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

}
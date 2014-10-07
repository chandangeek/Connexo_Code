package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.DeviceServiceImpl;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
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
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataCollectionKpiImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (10:25)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionKpiImplTest {

    private static final String DEVICE_TYPE_NAME = "DeviceTypeName";

    private static LicenseService licenseService;
    @Mock
    private static SecurityPropertyService securityPropertyService;

    private static TransactionService transactionService;

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static KpiService kpiService;
    private static DeviceDataModelServiceImpl deviceDataModelService;
    private static EndDeviceGroup endDeviceGroup;

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(transactionService);

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).toInstance(licenseService);
            bind(IssueService.class).toInstance(mock(IssueService.class));
            bind(PropertySpecService.class).toInstance(mock(PropertySpecService.class));
            bind(RelationService.class).toInstance(mock(RelationService.class));
            bind(ConnectionTypeService.class).toInstance(mock(ConnectionTypeService.class));
            bind(DeviceCacheMarshallingService.class).toInstance(mock(DeviceCacheMarshallingService.class));
            bind(DeviceProtocolMessageService.class).toInstance(mock(DeviceProtocolMessageService.class));
            bind(DeviceProtocolSecurityService.class).toInstance(mock(DeviceProtocolSecurityService.class));
            bind(DeviceProtocolService.class).toInstance(mock(DeviceProtocolService.class));
            bind(InboundDeviceProtocolService.class).toInstance(mock(InboundDeviceProtocolService.class));
            bind(LicensedProtocolService.class).toInstance(mock(LicensedProtocolService.class));
            bind(UserService.class).toInstance(mock(UserService.class));
        }
    }

    @BeforeClass
    public static void setUp() {
        licenseService = mock(LicenseService.class);
        when(licenseService.getLicenseForApplication("MDC")).thenReturn(Optional.absent());
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("DataCollectionKpiImplTest");
        inMemoryBootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new ThreadSecurityModule(principal),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
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
                new EventsModule(),
                new ValidationModule(),
                new SchedulingModule(),
                new ProtocolPluggableModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new MdcReadingTypeUtilServiceModule(),
                new PluggableModule(),
                new DeviceConfigurationModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new KpiModule(),
                new DeviceDataModule(),
                new MockModule(),
                new MeteringGroupsModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        endDeviceGroup = transactionService.execute(() -> {
            kpiService = injector.getInstance(KpiService.class);
            deviceDataModelService = injector.getInstance(DeviceDataModelServiceImpl.class);
            injector.getInstance(MeteringGroupsService.class);
            MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            DeviceService deviceService = injector.getInstance(DeviceServiceImpl.class);

            DeviceEndDeviceQueryProvider endDeviceQueryProvider = new DeviceEndDeviceQueryProvider();
            endDeviceQueryProvider.setMeteringService(meteringService);
            endDeviceQueryProvider.setDeviceService(deviceService);
            meteringGroupsService.addEndDeviceQueryProvider(endDeviceQueryProvider);

            Condition conditionDevice = where("deviceConfiguration.deviceType.name").isEqualTo(DEVICE_TYPE_NAME);
            return meteringGroupsService.createQueryEndDeviceGroup(conditionDevice);
        });

    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.EMPTY_DATA_COLLECTION_KPI + "}")
    public void testCreateEmptyKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);

        // Business method
        builder.save();

        // Asserts: see expected ConstraintViolationsRule
    }

    @Test
    @Transactional
    public void testCreatedKpiIsReturnedByFindById() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateConnectionSetupKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.save();

        // Business method
        java.util.Optional<DataCollectionKpi> found = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId());

        // Asserts
        assertThat(found.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testFindByAllReturnsEmptyCollectionWhenNoneCreated() {
        // Business method
        List<DataCollectionKpi> allKPIs = deviceDataModelService.dataCollectionKpiService().findAllDataCollectionKpis();

        // Asserts
        assertThat(allKPIs).isEmpty();
    }

    @Test
    @Transactional
    public void testCreatedKpiIsReturnedByFindByAll() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateConnectionSetupKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.save();

        // Business method
        List<DataCollectionKpi> allKPIs = deviceDataModelService.dataCollectionKpiService().findAllDataCollectionKpis();
        Set<Long> allKpiIDs = allKPIs.stream().map(DataCollectionKpi::getId).collect(Collectors.toSet());

        // Asserts
        assertThat(allKpiIDs).contains(kpi.getId());
    }

    @Test
    @Transactional
    public void testCreateWithConnectionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateConnectionSetupKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpi kpi = builder.save();

        // Asserts
        assertThat(kpi).isNotNull();
        assertThat(kpi.getDeviceGroup()).isNotNull();
        assertThat(kpi.getDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(kpi.calculatesConnectionSetupKpi()).isTrue();
        assertThat(kpi.connectionSetupKpiCalculationIntervalLength().isPresent()).isTrue();
        assertThat(kpi.calculatesComTaskExecutionKpi()).isFalse();
        assertThat(kpi.comTaskExecutionKpiCalculationIntervalLength().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testCreateWithComTaskExecutionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateComTaskExecutionKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpi kpi = builder.save();

        // Asserts
        assertThat(kpi).isNotNull();
        assertThat(kpi.getDeviceGroup()).isNotNull();
        assertThat(kpi.getDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(kpi.calculatesComTaskExecutionKpi()).isTrue();
        assertThat(kpi.comTaskExecutionKpiCalculationIntervalLength().isPresent()).isTrue();
        assertThat(kpi.calculatesConnectionSetupKpi()).isFalse();
        assertThat(kpi.connectionSetupKpiCalculationIntervalLength().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testCreateWithConnectionAndComTaskExecutionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateConnectionSetupKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi(Duration.ofHours(2)).expectingAsMaximum(BigDecimal.TEN);

        // Business method
        DataCollectionKpi kpi = builder.save();

        // Asserts
        assertThat(kpi).isNotNull();
        assertThat(kpi.getDeviceGroup()).isNotNull();
        assertThat(kpi.getDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(kpi.calculatesConnectionSetupKpi()).isTrue();
        assertThat(kpi.connectionSetupKpiCalculationIntervalLength().isPresent()).isTrue();
        assertThat(kpi.calculatesComTaskExecutionKpi()).isTrue();
        assertThat(kpi.comTaskExecutionKpiCalculationIntervalLength().isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testConnectionKpiKoreSettings() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofHours(1);
        builder.calculateConnectionSetupKpi(expectedIntervalLength).expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.save();

        // Asserts
        Kpi connectionKpi = kpi.connectionKpi().get();
        assertThat(connectionKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(connectionKpi.getMembers()).hasSize(DataCollectionKpiService.MONITORED_STATUSSES.size());
    }

    @Test
    @Transactional
    public void testComTaskExecutionKpiKoreSettings() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofHours(1);
        builder.calculateComTaskExecutionKpi(expectedIntervalLength).expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.save();

        // Asserts
        Kpi communicationKpi = kpi.communicationKpi().get();
        assertThat(communicationKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(communicationKpi.getMembers()).hasSize(DataCollectionKpiService.MONITORED_STATUSSES.size());
    }

    @Test
    @Transactional
    public void testDelete() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateConnectionSetupKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi(Duration.ofHours(2)).expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpi kpi = builder.save();
        long kpiId = kpi.getId();

        // Business method
        kpi.delete();

        // Asserts
        assertThat(deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpiId).isPresent()).isFalse();
    }

    @Ignore
    @Test
    @Transactional
    public void testDeleteAlsoDeletesKoreKPIs() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.calculateConnectionSetupKpi(Duration.ofHours(1)).expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi(Duration.ofHours(2)).expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.save();
        long connectionKpiId = kpi.connectionKpi().get().getId();
        long communicationKpiId = kpi.communicationKpi().get().getId();


        // Business method
        kpi.delete();

        // Asserts
        assertThat(kpiService.getKpi(connectionKpiId).isPresent()).isFalse();
        assertThat(kpiService.getKpi(communicationKpiId).isPresent()).isFalse();
    }

}
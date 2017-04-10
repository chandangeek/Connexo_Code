/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.ExecutionTimerService;
import com.elster.jupiter.util.time.impl.ExecutionTimerServiceImpl;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.MasterDataService;
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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.Principal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
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

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(transactionService);

    @Mock
    private static SecurityPropertyService securityPropertyService;

    private static LicenseService licenseService;
    private static TransactionService transactionService;
    private static Clock clock = Clock.systemDefaultZone();
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static TaskService taskService;
    private static KpiService kpiService;
    private static DeviceDataModelServiceImpl deviceDataModelService;
    private static EndDeviceGroup endDeviceGroup;
    private static CronExpressionParser cronExpressionParser;
    private static MeteringGroupsService meteringGroupsService;
    private static MeteringService meteringService;
    private static ServerDeviceService deviceService;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).toInstance(licenseService);
            bind(IssueService.class).toInstance(mock(IssueService.class));
            bind(com.elster.jupiter.issue.share.service.IssueService.class).toInstance(mock(com.elster.jupiter.issue.share.service.IssueService.class, RETURNS_DEEP_STUBS));
            bind(ConnectionTypeService.class).toInstance(mock(ConnectionTypeService.class));
            bind(DeviceCacheMarshallingService.class).toInstance(mock(DeviceCacheMarshallingService.class));
            bind(DeviceProtocolMessageService.class).toInstance(mock(DeviceProtocolMessageService.class));
            bind(DeviceProtocolSecurityService.class).toInstance(mock(DeviceProtocolSecurityService.class));
            bind(DeviceProtocolService.class).toInstance(mock(DeviceProtocolService.class));
            bind(InboundDeviceProtocolService.class).toInstance(mock(InboundDeviceProtocolService.class));
            bind(LicensedProtocolService.class).toInstance(mock(LicensedProtocolService.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(CronExpressionParser.class).toInstance(cronExpressionParser);
            bind(Clock.class).toInstance(clock);
            bind(JsonService.class).to(JsonServiceImpl.class).in(Scopes.SINGLETON);
            bind(BeanService.class).to(BeanServiceImpl.class).in(Scopes.SINGLETON);
            bind(ExecutionTimerService.class).to(ExecutionTimerServiceImpl.class).in(Scopes.SINGLETON);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        cronExpressionParser = mock(CronExpressionParser.class, RETURNS_DEEP_STUBS);
        CronExpression cronExpression = mock(CronExpression.class);
        when(cronExpression.encoded()).thenReturn("0 0 0/1 * * ? *");
        when(cronExpressionParser.parse(anyString())).thenReturn(Optional.of(cronExpression));
        doReturn(Optional.of(ZonedDateTime.now())).when(cronExpression).nextOccurrence(any());
        taskService = mock(TaskService.class);
        licenseService = mock(LicenseService.class);
        when(licenseService.getLicenseForApplication("MDC")).thenReturn(Optional.empty());
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("DataCollectionKpiImplTest");
        inMemoryBootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new ThreadSecurityModule(principal),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new OrmModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new DataVaultModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new MasterDataModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new PkiModule(),
                new CalendarModule(),
                new MeteringModule(),
                new EventsModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new BasicPropertiesModule(),
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
                new TaskModule(),
                new TasksModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new DeviceDataModule(),
                new MockModule(),
                new CalendarModule(),
                new MdcDynamicModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        endDeviceGroup = transactionService.execute(() -> {
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
            injector.getInstance(MessageService.class);
            taskService = injector.getInstance(TaskService.class);
            kpiService = injector.getInstance(KpiService.class);
            injector.getInstance(FiniteStateMachineService.class);
            meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
            meteringService = injector.getInstance(MeteringService.class);
            injector.getInstance(MasterDataService.class);
            deviceDataModelService = injector.getInstance(DeviceDataModelServiceImpl.class);
            deviceService = deviceDataModelService.deviceService();
            return meteringGroupsService.createEnumeratedEndDeviceGroup().setName("static device group").create();
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
        builder.frequency(Duration.ofMinutes(15));
        builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts: see expected ConstraintViolationsRule
    }

    @Test
    @Transactional
    public void testCreatedKpiIsReturnedByFindById() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

        // Business method
        java.util.Optional<DataCollectionKpi> found = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId());

        // Asserts
        assertThat(found.isPresent()).isTrue();
    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void testCreateKpiWithoutEndDeviceGroup() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(null);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

        // Business method
        deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId());
    }

    @Test
    @Transactional
    public void testDisplayPeriodIsPersisted() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

        // Business method
        java.util.Optional<DataCollectionKpi> found = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId());

        // Asserts
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getDisplayRange()).isEqualTo(TimeDuration.days(1));
    }

    @Test
    @Transactional
    public void testNonExistingKpiIsNotReturnedByFindById() {
        // Business method
        java.util.Optional<DataCollectionKpi> found = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(0);

        // Asserts
        assertThat(found.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testCreatedKpiIsReturnedByFindByGroup() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.displayPeriod(TimeDuration.days(1)).save();

        // Business method
        java.util.Optional<DataCollectionKpi> found = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(endDeviceGroup);

        // Asserts
        assertThat(found.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testFindByGroupForDifferentGroup() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.displayPeriod(TimeDuration.days(1)).save();

        EndDeviceGroup otherEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup().setName("Another static device group").create();

        // Business method
        java.util.Optional<DataCollectionKpi> found = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(otherEndDeviceGroup);

        // Asserts
        assertThat(found.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testCreateConnectionKpiAlsoCreatesRecurrentTasks() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Optional<RecurrentTask> kpiTask = kpi.connectionKpiTask();
        assertThat(kpiTask.isPresent()).isTrue();
        Optional<RecurrentTask> recurrentTask = taskService.getRecurrentTask(kpiTask.get().getId());
        assertThat(recurrentTask.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCreateCommunicationKpiAlsoCreatesRecurrentTasks() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Optional<RecurrentTask> kpiTask = kpi.communicationKpiTask();
        assertThat(kpiTask.isPresent()).isTrue();
        Optional<RecurrentTask> recurrentTask = taskService.getRecurrentTask(kpiTask.get().getId());
        assertThat(recurrentTask.isPresent()).isTrue();
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
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

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
        builder.displayPeriod(TimeDuration.days(1)).frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

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
    public void testCreateWithConnectionKpiAndAddCommunicationKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).displayPeriod(TimeDuration.days(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpi kpi = builder.save();

        kpi = deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();
        kpi.calculateComTaskExecutionKpi(BigDecimal.valueOf(99.9));

        // Asserts
        assertThat(kpi).isNotNull();
        assertThat(kpi.getDeviceGroup()).isNotNull();
        assertThat(kpi.getDeviceGroup().getId()).isEqualTo(endDeviceGroup.getId());
        assertThat(kpi.calculatesConnectionSetupKpi()).isTrue();
        assertThat(kpi.calculatesComTaskExecutionKpi()).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithComTaskExecutionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

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
        builder.frequency(Duration.ofMinutes(15)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);

        // Business method
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

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
    public void testConnectionKpiKoreSettingsOneHour() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofHours(1);
        builder.frequency(expectedIntervalLength).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Kpi connectionKpi = kpi.connectionKpi().get();
        assertThat(connectionKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(connectionKpi.getMembers()).hasSize(MonitoredTaskStatus.values().length);
    }

    @Test
    @Transactional
    public void testConnectionKpiKoreSettings15Minutes() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofMinutes(15);
        builder.frequency(expectedIntervalLength).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Kpi connectionKpi = kpi.connectionKpi().get();
        assertThat(connectionKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(connectionKpi.getMembers()).hasSize(MonitoredTaskStatus.values().length);
    }

    @Test
    @Transactional
    public void testConnectionKpiKoreSettings60Minutes() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofMinutes(60);
        builder.frequency(expectedIntervalLength).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Kpi connectionKpi = kpi.connectionKpi().get();
        assertThat(connectionKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(connectionKpi.getMembers()).hasSize(MonitoredTaskStatus.values().length);
    }

    @Test
    @Transactional
    public void testConnectionKpiKoreSettings3600Seconds() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofSeconds(3600);
        builder.frequency(expectedIntervalLength).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Kpi connectionKpi = kpi.connectionKpi().get();
        assertThat(connectionKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(connectionKpi.getMembers()).hasSize(MonitoredTaskStatus.values().length);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    @org.junit.Ignore // Ignore until COPL-384 is fixed
    public void testConnectionKpi1Year() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Period unsupported = Period.ofDays(1);
        builder.frequency(unsupported).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testConnectionKpiZeroSeconds() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration unsupported = Duration.ofSeconds(0);
        builder.frequency(unsupported).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testConnectionKpiInYearsMonthsAndDays() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Period unsupported = Period.of(1, 1, 1);
        builder.frequency(unsupported).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testComTaskExecutionKpiKoreSettings() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Duration expectedIntervalLength = Duration.ofHours(1);
        builder.frequency(expectedIntervalLength).calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts
        Kpi communicationKpi = kpi.communicationKpi().get();
        assertThat(communicationKpi.getIntervalLength()).isEqualTo(expectedIntervalLength);
        assertThat(communicationKpi.getMembers()).hasSize(MonitoredTaskStatus.values().length);
    }

    @Test
    @Transactional
    public void testNoConnectionScoresForOnlyComTaskExecutionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

        // Business method
        List<DataCollectionKpiScore> scores = kpi.getConnectionSetupKpiScores(Ranges.closed(Instant.EPOCH, Instant.now()));

        // Asserts
        assertThat(scores).isEmpty();
    }

    @Test
    @Transactional
    public void testNoComTaskExecutionScoresForOnlyConnectionSetupKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();

        // Business method
        List<DataCollectionKpiScore> scores = kpi.getConnectionSetupKpiScores(Ranges.closed(Instant.EPOCH, Instant.now()));

        // Asserts
        assertThat(scores).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testComTaskExecutionKpiInYearsMonthsAndDays() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        Period unsupported = Period.of(1, 1, 1);
        builder.frequency(unsupported).calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.ONE);

        // Business method
        builder.displayPeriod(TimeDuration.days(1)).save();

        // Asserts: see expected exception
    }

    @Test
    @Transactional
    public void testDelete() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofMinutes(15)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpi kpi = builder.displayPeriod(TimeDuration.days(1)).save();
        long kpiId = kpi.getId();

        // Business method
        kpi.delete();

        // Asserts
        assertThat(deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpiId).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testDeleteAlsoDeletesKoreKPIs() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();
        long connectionKpiId = kpi.connectionKpi().get().getId();
        long communicationKpiId = kpi.communicationKpi().get().getId();


        // Business method
        kpi.delete();

        // Asserts
        assertThat(kpiService.getKpi(connectionKpiId).isPresent()).isFalse();
        assertThat(kpiService.getKpi(communicationKpiId).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRemoveExistingComTaskExecutionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // must reload to trigger postLoad and init strategies
        kpi = (DataCollectionKpiImpl) deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();

        long connectionKpiId = kpi.connectionKpi().get().getId();
        long communicationKpiId = kpi.communicationKpi().get().getId();
        long connectionTaskId = kpi.connectionKpiTask().get().getId();
        long communicationTaskId = kpi.communicationKpiTask().get().getId();


        // Business method
        kpi.dropComTaskExecutionKpi();

        // Asserts
        assertThat(kpiService.getKpi(connectionKpiId).isPresent()).isTrue();
        assertThat(kpiService.getKpi(communicationKpiId).isPresent()).isFalse();
        assertThat(taskService.getRecurrentTask(connectionTaskId).isPresent()).isTrue();
        assertThat(taskService.getRecurrentTask(communicationTaskId).isPresent()).isFalse();
    }

    @Test
    @Transactional // COMU-305
    public void testAddCommunicationTargetToKpiWithConnectionTarget() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // must reload to trigger postLoad and init strategies
        kpi = (DataCollectionKpiImpl) deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();


        // Business method
        kpi.calculateComTaskExecutionKpi(BigDecimal.TEN);

    }

    @Test
    @Transactional
    public void testUpdateExistingComTaskExecutionKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.displayPeriod(TimeDuration.days(1));
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.save();

        // must reload to trigger postLoad and init strategies
        kpi = (DataCollectionKpiImpl) deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();

        long connectionKpiId = kpi.connectionKpi().get().getId();
        long communicationKpiId = kpi.communicationKpi().get().getId();
        long connectionTaskId = kpi.connectionKpiTask().get().getId();
        long communicationTaskId = kpi.communicationKpiTask().get().getId();


        // Business method
        kpi.calculateComTaskExecutionKpi(BigDecimal.valueOf(99.99));

        // Asserts
        assertThat(kpiService.getKpi(connectionKpiId).isPresent()).isTrue();
        assertThat(kpiService.getKpi(communicationKpiId).isPresent()).isTrue();
        kpiService.getKpi(communicationKpiId).get().
                getMembers().stream().
                forEach(member -> assertThat(member.getTarget(Instant.now())).isEqualTo(BigDecimal.valueOf(99.99)));
        assertThat(taskService.getRecurrentTask(connectionTaskId).isPresent()).isTrue();
        assertThat(taskService.getRecurrentTask(communicationTaskId).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateExistingConnectionTaskKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.displayPeriod(TimeDuration.days(1));
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.save();

        // must reload to trigger postLoad and init strategies
        kpi = (DataCollectionKpiImpl) deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();

        long connectionKpiId = kpi.connectionKpi().get().getId();
        long communicationKpiId = kpi.communicationKpi().get().getId();
        long connectionTaskId = kpi.connectionKpiTask().get().getId();
        long communicationTaskId = kpi.communicationKpiTask().get().getId();


        // Business method
        kpi.calculateConnectionKpi(BigDecimal.valueOf(99.99));

        // Asserts
        assertThat(kpiService.getKpi(connectionKpiId).isPresent()).isTrue();
        assertThat(kpiService.getKpi(communicationKpiId).isPresent()).isTrue();
        kpiService.getKpi(connectionKpiId).get().
                getMembers().stream().
                forEach(member -> assertThat(member.getTarget(Instant.now())).isEqualTo(BigDecimal.valueOf(99.99)));
        assertThat(taskService.getRecurrentTask(connectionTaskId).isPresent()).isTrue();
        assertThat(taskService.getRecurrentTask(communicationTaskId).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testRemoveExistingConnectionTaskKpi() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofHours(1)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // must reload to trigger postLoad and init strategies
        kpi = (DataCollectionKpiImpl) deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();

        long connectionKpiId = kpi.connectionKpi().get().getId();
        long communicationKpiId = kpi.communicationKpi().get().getId();
        long connectionTaskId = kpi.connectionKpiTask().get().getId();
        long communicationTaskId = kpi.communicationKpiTask().get().getId();


        // Business method
        kpi.dropConnectionSetupKpi();

        // Asserts
        assertThat(kpiService.getKpi(connectionKpiId).isPresent()).isFalse();
        assertThat(kpiService.getKpi(communicationKpiId).isPresent()).isTrue();
        assertThat(taskService.getRecurrentTask(connectionTaskId).isPresent()).isFalse();
        assertThat(taskService.getRecurrentTask(communicationTaskId).isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testSwitchKpis() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofMinutes(15)).calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();

        // must reload to trigger postLoad and init strategies
        kpi = (DataCollectionKpiImpl) deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();
        kpi.calculateConnectionKpi(BigDecimal.ONE);
        kpi.dropComTaskExecutionKpi();

        // must reload to trigger postLoad and init strategies
        deviceDataModelService.dataCollectionKpiService().findDataCollectionKpi(kpi.getId()).get();
    }

    @Test
    @Transactional
    public void testDeleteAlsoDeletesRecurrentTasks() {
        DataCollectionKpiService.DataCollectionKpiBuilder builder = deviceDataModelService.dataCollectionKpiService().newDataCollectionKpi(endDeviceGroup);
        builder.frequency(Duration.ofMinutes(30)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.ONE);
        builder.calculateComTaskExecutionKpi().expectingAsMaximum(BigDecimal.TEN);
        DataCollectionKpiImpl kpi = (DataCollectionKpiImpl) builder.displayPeriod(TimeDuration.days(1)).save();
        long connectionTaskId = kpi.connectionKpiTask().get().getId();
        long communicationTaskId = kpi.communicationKpiTask().get().getId();

        // Business method
        kpi.delete();

        // Asserts
        assertThat(taskService.getRecurrentTask(connectionTaskId).isPresent()).isFalse();
        assertThat(taskService.getRecurrentTask(communicationTaskId).isPresent()).isFalse();
    }

}
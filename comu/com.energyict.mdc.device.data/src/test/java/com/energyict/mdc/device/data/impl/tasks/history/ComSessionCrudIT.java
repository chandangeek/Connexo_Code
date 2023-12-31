/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.impl.ZoneModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.common.tasks.history.ComStatistics;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommunicationTestServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComSessionCrudIT {
    private static final String DEVICE_TYPE_NAME = "DeviceType";
    private static final String DEVICE_CONFIGURATION_NAME = "conf";
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    private static TransactionService transactionService;
    private static TaskService taskService;
    private static DeviceDataModelService deviceDataModelService;
    private static InMemoryBootstrapModule bootstrapModule;
    private static DeviceConfigurationService deviceConfigurationService;
    private static ProtocolPluggableService protocolPluggableService;
    private static EngineConfigurationService engineConfigurationService;
    private static Device device;
    private static OutboundComPortPool outboundTcpipComPortPool;
    private static ScheduledConnectionTask connectionTask;
    private static OutboundComPort comport;
    private static ComTask comTask;
    private static ComTaskExecution comTaskExecution;

    @Rule
    public final TestRule duraLexSedLex = new ExpectedConstraintViolationRule();
    @Rule
    public final TestRule transactionalRule = new TransactionalRule(transactionService);

    public static void initializeDatabase(boolean showSqlLogging) {
        bootstrapModule = new InMemoryBootstrapModule();
        Injector injector;
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    bootstrapModule,
                    new ThreadSecurityModule(() -> "test"),
                    new ServiceCallModule(),
                    new CustomPropertySetsModule(),
                    new EventsModule(),
                    new PubSubModule(),
                    new PkiModule(),
                    new TransactionModule(showSqlLogging),
                    new UtilModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new BpmModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CalendarModule(),
                    new MeteringModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new H2OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new ProtocolApiModule(),
                    new KpiModule(),
                    new TasksModule(),
                    new EngineModelModule(),
                    new ProtocolPluggableModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new TimeModule(),
                    new DeviceLifeCycleConfigurationModule(),
                    new DeviceConfigurationModule(),
                    new DeviceDataModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TaskModule(),
                    new CalendarModule(),
                    new WebServicesModule(),
                    new AuditServiceModule(),
                    new FileImportModule(),
                    new ZoneModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommandCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CompletionOptionsCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new OnDemandReadServiceCallCustomPropertySet());
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(new CommunicationTestServiceCallCustomPropertySet());
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(MasterDataService.class);
            deviceDataModelService = injector.getInstance(DeviceDataModelServiceImpl.class);
            deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);

            ConnectionTypeService connectionTypeService = mock(ConnectionTypeService.class);
            when(connectionTypeService.createConnectionType(NoParamsConnectionType.class.getName())).thenReturn(new NoParamsConnectionType());
            protocolPluggableService.addConnectionTypeService(connectionTypeService);

            engineConfigurationService = injector.getInstance(EngineConfigurationService.class);
            deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            taskService = injector.getInstance(TaskService.class);
            injector.getInstance(AuditService.class);
            ctx.commit();
        }
    }

    @BeforeClass
    public static void setUp() {
        initializeDatabase(false);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encryptionAccessLevel));
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.values()));
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.empty());

        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfigurationBuilder.isDirectlyAddressable(true);
            DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
            ProtocolDialectConfigurationProperties configDialectProps = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
            deviceConfiguration.save();
            deviceConfiguration.activate();
            device = deviceDataModelService.deviceService()
                    .newDevice(deviceConfiguration, "SimpleDevice", "mrid", Instant.now());
            device.save();
            ConnectionTypePluggableClass connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass(
                    NoParamsConnectionType.class.getSimpleName(),
                    NoParamsConnectionType.class.getName());
            connectionTypePluggableClass.save();

            PartialScheduledConnectionTask partialScheduledConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("Outbound (1)",
                    connectionTypePluggableClass,
                    TimeDuration.minutes(5),
                    ConnectionStrategy.AS_SOON_AS_POSSIBLE, configDialectProps)
                    .comWindow(new ComWindow(0, 7200))
                    .build();
            partialScheduledConnectionTask.save();

            outboundTcpipComPortPool = engineConfigurationService.newOutboundComPortPool("outTCPIPPool", ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES), 0);
            outboundTcpipComPortPool.setActive(true);
            outboundTcpipComPortPool.update();

            connectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask)
                    .setComPortPool(outboundTcpipComPortPool)
                    .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .add();

            OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = engineConfigurationService.newOnlineComServerBuilder();
            String name = "ComServer";
            onlineComServerBuilder.name(name);
            onlineComServerBuilder.storeTaskQueueSize(1);
            onlineComServerBuilder.storeTaskThreadPriority(1);
            onlineComServerBuilder.changesInterPollDelay(TimeDuration.minutes(5));
            onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.minutes(1));
            onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServerBuilder.numberOfStoreTaskThreads(2);
            onlineComServerBuilder.serverName(name);
            onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
            final OnlineComServer onlineComServer = onlineComServerBuilder.create();
            comport = onlineComServer.newOutboundComPort("comport", 1)
                    .comPortType(ComPortType.TCP).add();

            comTask = taskService.newComTask("comtask");
            comTask.setStoreData(true);
            comTask.setMaxNrOfTries(1);
            comTask.createBasicCheckTask().add();
            comTask.save();

            SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet("sec").encryptionLevel(0).authenticationLevel(0).build();

            ComTaskEnablement comTaskEnablement = deviceConfiguration.enableComTask(comTask, securityPropertySet)
                    .useDefaultConnectionTask(true)
                    .add();

            comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement)
                    .useDefaultConnectionTask(true)
                    .connectionTask(connectionTask)
                    .add();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testCreationOfComSession() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        assertThat(foundSession.getStartDate()).isEqualTo(startTime);
        assertThat(foundSession.getStopDate()).isEqualTo(stopTime);
        assertThat(foundSession.getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.Success);
        Assertions.assertThat(EqualById.byId(foundSession.getConnectionTask())).isEqualTo(EqualById.byId(connectionTask));
        Assertions.assertThat(EqualById.byId(foundSession.getComPortPool())).isEqualTo(EqualById.byId(outboundTcpipComPortPool));
        Assertions.assertThat(EqualById.byId(foundSession.getComPort())).isEqualTo(EqualById.byId(comport));
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithStatistics() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSession comSession = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .incrementSuccessFulTasks(3)
                .incrementFailedTasks(2)
                .incrementNotExecutedTasks(1)
                .addReceivedBytes(128)
                .addSentBytes(64)
                .addReceivedPackets(32)
                .addSentPackets(16)
                .endSession(stopTime, ComSession.SuccessIndicator.Success).create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        ComStatistics statistics = foundSession.getStatistics();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getNumberOfBytesReceived()).isEqualTo(128);
        assertThat(statistics.getNumberOfBytesSent()).isEqualTo(64);
        assertThat(statistics.getNumberOfPacketsReceived()).isEqualTo(32);
        assertThat(statistics.getNumberOfPacketsSent()).isEqualTo(16);

        assertThat(foundSession.getNumberOfSuccessFulTasks()).isEqualTo(3);
        assertThat(foundSession.getNumberOfFailedTasks()).isEqualTo(2);
        assertThat(foundSession.getNumberOfPlannedButNotExecutedTasks()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithJournalEntries() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant entryTime1 = ZonedDateTime.of(2011, 5, 14, 4, 15, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant entryTime2 = ZonedDateTime.of(2011, 5, 14, 5, 15, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Throwable cause = new RuntimeException();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .addJournalEntry(entryTime1, ComServer.LogLevel.INFO, "entry1")
                .addJournalEntry(entryTime2, ComServer.LogLevel.INFO, "entry2", cause)
                .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);
        assertThat(found.isPresent()).isTrue();
        ComSession foundSession = found.get();
        assertThat(foundSession.getJournalEntries()).hasSize(2);
        ComSessionJournalEntry entry1 = foundSession.getJournalEntries().get(0);
        assertThat(entry1.getTimestamp()).isEqualTo(entryTime1);
        assertThat(entry1.getComSession()).isEqualTo(foundSession);
        assertThat(entry1.getMessage()).isEqualTo("entry1");
        assertThat(entry1.getStackTrace()).isNull();

        ComSessionJournalEntry entry2 = foundSession.getJournalEntries().get(1);
        assertThat(entry2.getTimestamp()).isEqualTo(entryTime2);
        assertThat(entry2.getComSession()).isEqualTo(foundSession);
        assertThat(entry2.getMessage()).isEqualTo("entry2");
        assertThat(entry2.getStackTrace()).isNotNull();
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithComTaskExecutionJournals() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant task1StartTime = ZonedDateTime.of(2011, 5, 14, 0, 5, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant task1StopTime = ZonedDateTime.of(2011, 5, 14, 0, 6, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant task2StartTime = ZonedDateTime.of(2011, 5, 14, 0, 6, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant task2StopTime = ZonedDateTime.of(2011, 5, 14, 0, 7, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder =
                connectionTaskService
                        .buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                        .addComTaskExecutionSession(comTaskExecution, comTask, task1StartTime)
                        .add(task1StopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                        .addComTaskExecutionSession(comTaskExecution, comTask, task2StartTime)
                        .add(task2StopTime, ComTaskExecutionSession.SuccessIndicator.Success)
                        .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(2);

        ComTaskExecutionSession comTaskExecutionSession1 = foundSession.getComTaskExecutionSessions().get(0);
        assertThat(EqualById.byId(comTaskExecutionSession1.getComSession())).isEqualTo(EqualById.byId(foundSession));
        assertThat(EqualById.byId(comTaskExecutionSession1.getComTaskExecution())).isEqualTo(EqualById.byId(comTaskExecution));
        assertThat(EqualById.byId(comTaskExecutionSession1.getDevice())).isEqualTo(EqualById.byId(device));
        assertThat(comTaskExecutionSession1.getHighestPriorityCompletionCode()).isEqualTo(CompletionCode.Ok);
        assertThat(comTaskExecutionSession1.getHighestPriorityErrorDescription()).isNull();
        assertThat(comTaskExecutionSession1.getSuccessIndicator()).isEqualTo(ComTaskExecutionSession.SuccessIndicator.Failure);

        ComTaskExecutionSession comTaskExecutionSession2 = foundSession.getComTaskExecutionSessions().get(1);
        assertThat(EqualById.byId(comTaskExecutionSession2.getComSession())).isEqualTo(EqualById.byId(foundSession));
        assertThat(EqualById.byId(comTaskExecutionSession2.getComTaskExecution())).isEqualTo(EqualById.byId(comTaskExecution));
        assertThat(EqualById.byId(comTaskExecutionSession2.getDevice())).isEqualTo(EqualById.byId(device));
        assertThat(comTaskExecutionSession2.getHighestPriorityCompletionCode()).isEqualTo(CompletionCode.Ok);
        assertThat(comTaskExecutionSession2.getHighestPriorityErrorDescription()).isNull();
        assertThat(comTaskExecutionSession2.getSuccessIndicator()).isEqualTo(ComTaskExecutionSession.SuccessIndicator.Success);

        ServerCommunicationTaskService communicationTaskService = deviceDataModelService.communicationTaskService();

        ComTaskExecution reloaded = communicationTaskService.findComTaskExecution(comTaskExecution.getId()).get();
        Optional<ComTaskExecutionSession> lastSession = reloaded.getLastSession();
        assertThat(lastSession.isPresent()).isTrue();
        assertThat(lastSession.get().getId()).isEqualTo(comTaskExecutionSession2.getId());
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithComTaskExecutionJournalWithStats() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStartTime = ZonedDateTime.of(2011, 5, 14, 0, 5, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStopTime = ZonedDateTime.of(2011, 5, 14, 0, 10, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .addComTaskExecutionSession(comTaskExecution, comTask, taskStartTime)
                .addSentBytes(128)
                .addReceivedBytes(64)
                .addSentPackets(32)
                .addReceivedPackets(16)
                .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        ComStatistics comStatistics = comTaskExecutionSession.getStatistics();
        assertThat(comStatistics).isNotNull();

        assertThat(comStatistics.getNumberOfBytesSent()).isEqualTo(128);
        assertThat(comStatistics.getNumberOfBytesReceived()).isEqualTo(64);
        assertThat(comStatistics.getNumberOfPacketsSent()).isEqualTo(32);
        assertThat(comStatistics.getNumberOfPacketsReceived()).isEqualTo(16);
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithComTaskExecutionJournalWithComCommandJournalEntry() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStartTime = ZonedDateTime.of(2011, 5, 14, 0, 5, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant journalEntryTime = ZonedDateTime.of(2011, 5, 14, 0, 6, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStopTime = ZonedDateTime.of(2011, 5, 14, 0, 10, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .addComTaskExecutionSession(comTaskExecution, comTask, taskStartTime)
                .addComCommandJournalEntry(journalEntryTime, CompletionCode.Ok, "AOK", "OpenValve")
                .addComCommandJournalEntry(journalEntryTime, CompletionCode.NotExecuted, "Whatever")
                .addComCommandJournalEntry(journalEntryTime, CompletionCode.ConfigurationWarning, "Just a warning", "ConfigurationWarning")
                .addComCommandJournalEntry(journalEntryTime, CompletionCode.ConfigurationError, "This is an error", "ConfigurationError")
                .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        assertThat(comTaskExecutionSession.getComTaskExecutionJournalEntries()).hasSize(4);
        assertThat(comTaskExecutionSession.findComTaskExecutionJournalEntries(EnumSet.noneOf(ComServer.LogLevel.class)).find()).isEmpty();
        assertThat(comTaskExecutionSession.findComTaskExecutionJournalEntries(EnumSet.of(ComServer.LogLevel.INFO)).find()).hasSize(2);
        assertThat(comTaskExecutionSession.findComTaskExecutionJournalEntries(EnumSet.of(ComServer.LogLevel.WARN)).find()).hasSize(1);
        assertThat(comTaskExecutionSession.findComTaskExecutionJournalEntries(EnumSet.of(ComServer.LogLevel.ERROR)).find()).hasSize(1);

        ComTaskExecutionJournalEntry journalEntry = comTaskExecutionSession.getComTaskExecutionJournalEntries().get(0);

        assertThat(journalEntry).isInstanceOf(ComCommandJournalEntry.class);

        ComCommandJournalEntry comCommandJournalEntry = ((ComCommandJournalEntry) journalEntry);

        assertThat(comCommandJournalEntry.getTimestamp()).isEqualTo(journalEntryTime);
        assertThat(comCommandJournalEntry.getCommandDescription()).isEqualTo("OpenValve");
        assertThat(comCommandJournalEntry.getCompletionCode()).isEqualTo(CompletionCode.Ok);
        assertThat(comCommandJournalEntry.getErrorDescription()).isEqualTo("AOK");
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithComTaskExecutionJournalWithComTaskExecutionMessageJournalEntry() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStartTime = ZonedDateTime.of(2011, 5, 14, 0, 5, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant journalEntryTime = ZonedDateTime.of(2011, 5, 14, 0, 6, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStopTime = ZonedDateTime.of(2011, 5, 14, 0, 10, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .addComTaskExecutionSession(comTaskExecution, comTask, taskStartTime)
                .addComTaskExecutionMessageJournalEntry(journalEntryTime, ComServer.LogLevel.INFO, "All is well", "Aok")
                .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        assertThat(comTaskExecutionSession.findComTaskExecutionJournalEntries(EnumSet.noneOf(ComServer.LogLevel.class)).find()).isEmpty();
        assertThat(comTaskExecutionSession.findComTaskExecutionJournalEntries(EnumSet.of(ComServer.LogLevel.INFO)).find()).hasSize(1);
        assertThat(comTaskExecutionSession.getComTaskExecutionJournalEntries()).hasSize(1);

        ComTaskExecutionJournalEntry journalEntry = comTaskExecutionSession.getComTaskExecutionJournalEntries().get(0);

        assertThat(journalEntry).isInstanceOf(ComTaskExecutionMessageJournalEntry.class);

        ComTaskExecutionMessageJournalEntry comCommandJournalEntry = ((ComTaskExecutionMessageJournalEntry) journalEntry);

        assertThat(comCommandJournalEntry.getTimestamp()).isEqualTo(journalEntryTime);
        assertThat(comCommandJournalEntry.getErrorDescription()).isEqualTo("Aok");
        assertThat(comCommandJournalEntry.getMessage()).isEqualTo("All is well");
    }

    @Test
    @Transactional
    public void testCreationOfComSessionWithComTaskExecutionJournalWithMoreThanOneJournalEntry() {
        long id;
        Instant startTime = ZonedDateTime.of(2011, 5, 14, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStartTime = ZonedDateTime.of(2011, 5, 14, 0, 5, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant journalEntryTime = ZonedDateTime.of(2011, 5, 14, 0, 6, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant taskStopTime = ZonedDateTime.of(2011, 5, 14, 0, 10, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant stopTime = ZonedDateTime.of(2011, 5, 14, 7, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ServerConnectionTaskService connectionTaskService = deviceDataModelService.connectionTaskService();
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = connectionTaskService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                .addComTaskExecutionSession(comTaskExecution, comTask, taskStartTime)
                .addComCommandJournalEntry(journalEntryTime, CompletionCode.Ok, "AOK", "OpenValve")
                .addComTaskExecutionMessageJournalEntry(journalEntryTime, ComServer.LogLevel.INFO, "All is well", "Aok")
                .addComCommandJournalEntry(journalEntryTime, CompletionCode.ConnectionError, "Oops", "CloseValve")
                .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                .endSession(stopTime, ComSession.SuccessIndicator.Success);
        ComSession comSession = endedComSessionBuilder.create();
        id = comSession.getId();

        Optional<ComSession> found = connectionTaskService.findComSession(id);

        assertThat(found.isPresent()).isTrue();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        assertThat(comTaskExecutionSession.getComTaskExecutionJournalEntries()).hasSize(3);

        assertThat(comTaskExecutionSession.getHighestPriorityCompletionCode()).isEqualTo(CompletionCode.ConnectionError);
        assertThat(comTaskExecutionSession.getHighestPriorityErrorDescription()).isEqualTo("Oops");
    }

    private static class ComTaskExecutionDialect implements DeviceProtocolDialect {
        @Override
        public String getDeviceProtocolDialectName() {
            return "dialect";
        }

        @Override
        public List<PropertySpec> getUPLPropertySpecs() {
            return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::adaptTo).collect(Collectors.toList());
        }

        @Override
        public String getDeviceProtocolDialectDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(IssueService.class).toInstance(mock(IssueService.class, RETURNS_DEEP_STUBS));
            bind(Thesaurus.class).toInstance(NlsModule.FakeThesaurus.INSTANCE);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(AppService.class).toInstance(mock(AppService.class));
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }
}

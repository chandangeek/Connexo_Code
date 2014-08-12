package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateTime;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComSessionCrudIT {

    private static final String DEVICE_TYPE_NAME = "DeviceType";
    private static final String DEVICE_CONFIGURATION_NAME = "conf";
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    private InboundComPortPool inboundComPortPool, inboundComPortPool2;
    private ConnectionTypePluggableClass connectionTypePluggableClass;

    @Rule
    public final TestRule duraLexSedLex = new ExpectedConstraintViolationRule();


    public static final String JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME = "jupiter.bootstrap.module";

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LicenseService licenseService;
    private TransactionService transactionService;
    private OrmService ormService;
    private PartialScheduledConnectionTask partialScheduledConnectionTask;
    private DeviceDataService deviceDataService;
    private OutboundComPortPool outboundTcpipComPortPool;
    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;

    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private DeviceConfigurationService deviceConfigurationService;
    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;
    private Device device;
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    private ProtocolPluggableService protocolPluggableService;
    private EngineModelService engineModelService;
    private ScheduledConnectionTask connectionTask;
    private OutboundComPort comport;
    @Mock
    private DeviceProtocol deviceProtocol;
    private TaskService taskService;
    private ComTask comTask;
    private ComTaskExecution comTaskExecution;
    private ProtocolDialectConfigurationProperties configDialectProps;

    private class ComTaskExecutionDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return "dialect";
        }

        @Override
        public String getDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }
    }


    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
        }

    }

    public void initializeDatabase(boolean showSqlLogging) {
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new PartyModule(),
                new UserModule(),
                new IdsModule(),
                new MeteringModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new OrmModule(),
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new TasksModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new ProtocolPluggableModule(),
                new ValidationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new SchedulingModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            ormService = injector.getInstance(OrmService.class);
//            eventService = injector.getInstance(EventService.class);
//            nlsService = injector.getInstance(NlsService.class);
//            meteringService = injector.getInstance(MeteringService.class);
//            readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
//            engineModelService = injector.getInstance(EngineModelService.class);
//            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
//            inboundDeviceProtocolService = injector.getInstance(InboundDeviceProtocolService.class);
            deviceDataService = injector.getInstance(DeviceDataService.class);
            deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
            engineModelService = injector.getInstance(EngineModelService.class);
            deviceConfigurationService = injector.getInstance(DeviceConfigurationService.class);
            taskService = injector.getInstance(TaskService.class);
            ctx.commit();
        }
    }

    @Before
    public void setUp() {
        when(principal.getName()).thenReturn("test");
        initializeDatabase(false);
        when(principal.getName()).thenReturn("test");
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));

        try (TransactionContext ctx = transactionService.getContext()) {
            deviceType = deviceConfigurationService.newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
            deviceType.save();
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfiguration = deviceConfigurationBuilder.add();
            configDialectProps = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
            deviceConfiguration.save();
            deviceConfiguration.activate();
            device = deviceDataService.newDevice(deviceConfiguration, "SimpleDevice", "mrid");
            device.save();
            connectionTypePluggableClass = protocolPluggableService.newConnectionTypePluggableClass(NoParamsConnectionType.class.getSimpleName(), NoParamsConnectionType.class.getName());
            connectionTypePluggableClass.save();

            partialScheduledConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("Outbound (1)", connectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                    comWindow(new ComWindow(0, 7200)).
                    build();
            partialScheduledConnectionTask.save();

            outboundTcpipComPortPool = engineModelService.newOutboundComPortPool();
            outboundTcpipComPortPool.setActive(true);
            outboundTcpipComPortPool.setComPortType(ComPortType.TCP);
            outboundTcpipComPortPool.setName("outTCPIPPool");
            outboundTcpipComPortPool.setTaskExecutionTimeout(new TimeDuration(1, TimeDuration.MINUTES));
            outboundTcpipComPortPool.save();

            connectionTask = this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                    .setComPortPool(outboundTcpipComPortPool)
                    .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                    .add();
            device.save();
            connectionTask.save();

            OnlineComServer onlineComServer = engineModelService.newOnlineComServerInstance();
            onlineComServer.setName("ComServer");
            onlineComServer.setStoreTaskQueueSize(1);
            onlineComServer.setStoreTaskThreadPriority(1);
            onlineComServer.setChangesInterPollDelay(TimeDuration.minutes(5));
            onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServer.setSchedulingInterPollDelay(TimeDuration.minutes(1));
            onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
            onlineComServer.setNumberOfStoreTaskThreads(2);
            comport = onlineComServer.newOutboundComPort("comport", 1)
                .comPortType(ComPortType.TCP).add();
            onlineComServer.save();

            comTask = taskService.newComTask("comtask");
            comTask.setStoreData(true);
            comTask.setMaxNrOfTries(1);
            comTask.createBasicCheckTask().add();
            comTask.save();

            SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet("sec").encryptionLevel(0).authenticationLevel(0).build();

            ComTaskEnablement comTaskEnablement = deviceConfiguration.enableComTask(comTask, securityPropertySet)
                    .useDefaultConnectionTask(true)
                    .add();

            comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement, configDialectProps)
                    .useDefaultConnectionTask(true)
                    .connectionTask(connectionTask)
                    .add();
            device.save();

            ctx.commit();
        }


    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    public void testCreationOfComSession() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getStartDate()).isEqualTo(startTime);
        assertThat(foundSession.getStopDate()).isEqualTo(stopTime);
        assertThat(foundSession.getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.Success);
        Assertions.assertThat(EqualById.byId(foundSession.getConnectionTask())).isEqualTo(EqualById.byId(connectionTask));
        Assertions.assertThat(EqualById.byId(foundSession.getComPortPool())).isEqualTo(EqualById.byId(outboundTcpipComPortPool));
        Assertions.assertThat(EqualById.byId(foundSession.getComPort())).isEqualTo(EqualById.byId(comport));

    }

    @Test
    public void testCreationOfComSessionWithStatistics() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSession comSession = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .incrementSuccessFulTasks()
                    .incrementSuccessFulTasks()
                    .incrementSuccessFulTasks()
                    .incrementFailedTasks()
                    .incrementFailedTasks()
                    .incrementNotExecutedTasks()
                    .addReceivedBytes(128)
                    .addSentBytes(64)
                    .addReceivedPackets(32)
                    .addSentPackets(16)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success).create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getStatistics().getNrOfBytesReceived()).isEqualTo(128);
        assertThat(foundSession.getStatistics().getNrOfBytesSent()).isEqualTo(64);
        assertThat(foundSession.getStatistics().getNrOfPacketsReceived()).isEqualTo(32);
        assertThat(foundSession.getStatistics().getNrOfPacketsSent()).isEqualTo(16);

        assertThat(foundSession.getNumberOfSuccessFulTasks()).isEqualTo(3);
        assertThat(foundSession.getNumberOfFailedTasks()).isEqualTo(2);
        assertThat(foundSession.getNumberOfPlannedButNotExecutedTasks()).isEqualTo(1);

    }

    @Test
    public void testCreationOfComSessionWithJournalEntries() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date entryTime1 = new DateTime(2011, 5, 14, 4, 15).toDate();
        Date entryTime2 = new DateTime(2011, 5, 14, 5, 15).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        Throwable cause = new RuntimeException();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .addJournalEntry(entryTime1, "entry1", null)
                    .addJournalEntry(entryTime2, "entry2", cause)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

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
    public void testCreationOfComSessionWithComTaskExecutionJournals() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date taskStartTime = new DateTime(2011, 5, 14, 0, 5).toDate();
        Date taskStopTime = new DateTime(2011, 5, 14, 0, 10).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .addComTaskExecutionSession(comTaskExecution, device, taskStartTime)
                    .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                    .addComTaskExecutionSession(comTaskExecution, device, taskStartTime)
                    .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Success)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(2);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        Assertions.assertThat(EqualById.byId(comTaskExecutionSession.getComSession())).isEqualTo(EqualById.byId(foundSession));
        Assertions.assertThat(EqualById.byId(comTaskExecutionSession.getComTaskExecution())).isEqualTo(EqualById.byId(comTaskExecution));
        Assertions.assertThat(EqualById.byId(comTaskExecutionSession.getDevice())).isEqualTo(EqualById.byId(device));
        assertThat(comTaskExecutionSession.getHighestPriorityCompletionCode()).isNull();
        assertThat(comTaskExecutionSession.getHighestPriorityErrorDescription()).isNull();
        assertThat(comTaskExecutionSession.getSuccessIndicator()).isEqualTo(ComTaskExecutionSession.SuccessIndicator.Failure);

        comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(1);

        Assertions.assertThat(EqualById.byId(comTaskExecutionSession.getComSession())).isEqualTo(EqualById.byId(foundSession));
        Assertions.assertThat(EqualById.byId(comTaskExecutionSession.getComTaskExecution())).isEqualTo(EqualById.byId(comTaskExecution));
        Assertions.assertThat(EqualById.byId(comTaskExecutionSession.getDevice())).isEqualTo(EqualById.byId(device));
        assertThat(comTaskExecutionSession.getHighestPriorityCompletionCode()).isNull();
        assertThat(comTaskExecutionSession.getHighestPriorityErrorDescription()).isNull();
        assertThat(comTaskExecutionSession.getSuccessIndicator()).isEqualTo(ComTaskExecutionSession.SuccessIndicator.Success);
    }

    @Test
    public void testCreationOfComSessionWithComTaskExecutionJournalWithStats() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date taskStartTime = new DateTime(2011, 5, 14, 0, 5).toDate();
        Date taskStopTime = new DateTime(2011, 5, 14, 0, 10).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .addComTaskExecutionSession(comTaskExecution, device, taskStartTime)
                    .addSentBytes(128)
                    .addReceivedBytes(64)
                    .addSentPackets(32)
                    .addReceivedPackets(16)
                    .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        ComStatistics comStatistics = comTaskExecutionSession.getStatistics();
        assertThat(comStatistics).isNotNull();

        assertThat(comStatistics.getNrOfBytesSent()).isEqualTo(128);
        assertThat(comStatistics.getNrOfBytesReceived()).isEqualTo(64);
        assertThat(comStatistics.getNrOfPacketsSent()).isEqualTo(32);
        assertThat(comStatistics.getNrOfPacketsReceived()).isEqualTo(16);
    }

    @Test
    public void testCreationOfComSessionWithComTaskExecutionJournalWithComCommandJournalEntry() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date taskStartTime = new DateTime(2011, 5, 14, 0, 5).toDate();
        Date journalEntryTime = new DateTime(2011, 5, 14, 0, 6).toDate();
        Date taskStopTime = new DateTime(2011, 5, 14, 0, 10).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .addComTaskExecutionSession(comTaskExecution, device, taskStartTime)
                    .addComCommandJournalEntry(journalEntryTime, CompletionCode.Ok, "AOK", "OpenValve")
                    .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        assertThat(comTaskExecutionSession.getComTaskExecutionJournalEntries()).hasSize(1);

        ComTaskExecutionJournalEntry journalEntry = comTaskExecutionSession.getComTaskExecutionJournalEntries().get(0);

        assertThat(journalEntry).isInstanceOf(ComCommandJournalEntry.class);

        ComCommandJournalEntry comCommandJournalEntry = ((ComCommandJournalEntry) journalEntry);

        assertThat(comCommandJournalEntry.getTimestamp()).isEqualTo(journalEntryTime);
        assertThat(comCommandJournalEntry.getCommandDescription()).isEqualTo("OpenValve");
        assertThat(comCommandJournalEntry.getCompletionCode()).isEqualTo(CompletionCode.Ok);
        assertThat(comCommandJournalEntry.getErrorDescription()).isEqualTo("AOK");
    }

    @Test
    public void testCreationOfComSessionWithComTaskExecutionJournalWithComTaskExecutionMessageJournalEntry() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date taskStartTime = new DateTime(2011, 5, 14, 0, 5).toDate();
        Date journalEntryTime = new DateTime(2011, 5, 14, 0, 6).toDate();
        Date taskStopTime = new DateTime(2011, 5, 14, 0, 10).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .addComTaskExecutionSession(comTaskExecution, device, taskStartTime)
                    .addComTaskExecutionMessageJournalEntry(journalEntryTime, "Aok", "All is well")
                    .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        assertThat(comTaskExecutionSession.getComTaskExecutionJournalEntries()).hasSize(1);

        ComTaskExecutionJournalEntry journalEntry = comTaskExecutionSession.getComTaskExecutionJournalEntries().get(0);

        assertThat(journalEntry).isInstanceOf(ComTaskExecutionMessageJournalEntry.class);

        ComTaskExecutionMessageJournalEntry comCommandJournalEntry = ((ComTaskExecutionMessageJournalEntry) journalEntry);

        assertThat(comCommandJournalEntry.getTimestamp()).isEqualTo(journalEntryTime);
        assertThat(comCommandJournalEntry.getErrorDescription()).isEqualTo("Aok");
        assertThat(comCommandJournalEntry.getMessage()).isEqualTo("All is well");
    }

    @Test
    public void testCreationOfComSessionWithComTaskExecutionJournalWithMoreThanOneJournalEntry() {
        long id;
        Date startTime = new DateTime(2011, 5, 14, 0, 0).toDate();
        Date taskStartTime = new DateTime(2011, 5, 14, 0, 5).toDate();
        Date journalEntryTime = new DateTime(2011, 5, 14, 0, 6).toDate();
        Date taskStopTime = new DateTime(2011, 5, 14, 0, 10).toDate();
        Date stopTime = new DateTime(2011, 5, 14, 7, 0).toDate();
        try (TransactionContext ctx = transactionService.getContext()) {
            ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = deviceDataService.buildComSession(connectionTask, outboundTcpipComPortPool, comport, startTime)
                    .addComTaskExecutionSession(comTaskExecution, device, taskStartTime)
                    .addComCommandJournalEntry(journalEntryTime, CompletionCode.Ok, "AOK", "OpenValve")
                    .addComTaskExecutionMessageJournalEntry(journalEntryTime, "Aok", "All is well")
                    .addComCommandJournalEntry(journalEntryTime, CompletionCode.ConnectionError, "Oops", "CloseValve")
                    .add(taskStopTime, ComTaskExecutionSession.SuccessIndicator.Failure)
                    .endSession(stopTime, ComSession.SuccessIndicator.Success);
            ComSession comSession = endedComSessionBuilder.create();
            id = comSession.getId();
            ctx.commit();
        }

        Optional<ComSession> found = deviceDataService.findComSession(id);

        assertThat(found).isPresent();

        ComSession foundSession = found.get();

        assertThat(foundSession.getComTaskExecutionSessions()).hasSize(1);

        ComTaskExecutionSession comTaskExecutionSession = foundSession.getComTaskExecutionSessions().get(0);

        assertThat(comTaskExecutionSession.getComTaskExecutionJournalEntries()).hasSize(3);

        assertThat(comTaskExecutionSession.getHighestPriorityCompletionCode()).isEqualTo(CompletionCode.ConnectionError);
        assertThat(comTaskExecutionSession.getHighestPriorityErrorDescription()).isEqualTo("Oops");
    }

}

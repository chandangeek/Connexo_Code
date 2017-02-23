package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.properties.PropertySpec;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the aspects of the ScheduledConnectionTask component
 * that relate to or rely on the topology feature.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-10 (11:06)
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledConnectionTaskInTopologyIT extends PersistenceIntegrationTest {

    protected static final TimeDuration EVERY_HOUR = new TimeDuration(1, TimeDuration.TimeUnit.HOURS);
    private static final String DEVICE_PROTOCOL_DIALECT_NAME = "Limbueregs";
    protected static OutboundComPortPool outboundTcpipComPortPool;
    protected static ConnectionTypePluggableClass outboundNoParamsConnectionTypePluggableClass;
    protected int comTaskEnablementPriority = 213;
    protected Device device;
    protected ComTaskEnablement comTaskEnablement1;
    protected ComTaskEnablement comTaskEnablement2;
    protected ComTaskEnablement comTaskEnablement3;
    protected PartialScheduledConnectionTask partialScheduledConnectionTask;
    private OnlineComServer onlineComServer;
    private OnlineComServer otherOnlineComServer;
    private String COM_TASK_NAME = "TheNameOfMyComTask";
    private int maxNrOfTries = 5;
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;

    @BeforeClass
    public static void registerConnectionTypePluggableClasses() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                outboundNoParamsConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundNoParamsConnectionTypeImpl.class);
            }
        });
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass registerConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(connectionTypeClass.getSimpleName(), connectionTypeClass.getName());
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    @BeforeClass
    public static void createComPortPools() {
        createIpComPortPools();
    }

    private static void createIpComPortPools() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                outboundTcpipComPortPool = createOutboundIpComPortPool("TCP/IP out(1)");
            }
        });
    }

    private static OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        ipComPortPool.setActive(true);
        ipComPortPool.update();
        return ipComPortPool;
    }

    @Before
    public void getFirstProtocolDialectConfigurationPropertiesFromDeviceConfiguration() {
        this.protocolDialectConfigurationProperties = this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
    }

    @Before
    public void initializeMocks() {
        super.initializeMocks();
        this.device = createSimpleDevice(this.getClass().getSimpleName());
        ProtocolDialectConfigurationProperties configDialect = createDialectConfigProperties();
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck();
        ComTask comTaskWithLogBooks = createComTaskWithLogBooks();
        ComTask comTaskWithRegisters = createComTaskWithRegisters();

        this.comTaskEnablement1 = enableComTask(true, configDialect, comTaskWithBasicCheck);
        this.comTaskEnablement2 = enableComTask(true, configDialect, comTaskWithLogBooks);
        this.comTaskEnablement3 = enableComTask(true, configDialect, comTaskWithRegisters);

        partialScheduledConnectionTask = deviceConfiguration.newPartialScheduledConnectionTask("Outbound (1)", outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();
        deviceConfiguration.save();
    }

    @Before
    public void addEventHandlers() {
        ServerTopologyService topologyService = inMemoryPersistence.getTopologyService();
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new DefaultConnectionTaskCreateEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ComTaskExecutionCreateEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ComTaskExecutionUpdateEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ComTaskExecutionObsoleteEventHandler(topologyService, mock(Thesaurus.class))));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new SetDefaultConnectionTaskEventHandler(topologyService)));
        inMemoryPersistence.getPublisher()
                .addSubscriber(new SubscriberForTopicHandler(new ClearDefaultConnectionTaskEventHandler(topologyService)));
    }

    @Test
    @Transactional
    public void testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp() {
        Instant earliestNextExecutionTimestamp = LocalDateTime.of(2013, 2, 14, 0, 0).toInstant(ZoneOffset.UTC);
        ComTaskExecution comTaskExecution = createComTaskExecution();
        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.forceNextExecutionTimeStampAndPriority(earliestNextExecutionTimestamp, comTaskEnablementPriority);
        comTaskExecutionUpdater.update();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp");
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(earliestNextExecutionTimestamp, TaskPriorityConstants.DEFAULT_PRIORITY);

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        // Asserts
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device, comTaskExecution);
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(earliestNextExecutionTimestamp);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void updateToAsapDefaultTestNextExecutionTimeStamp() throws SQLException {
        Instant comTaskNextExecutionTimeStamp = freezeClock(2013, Calendar.FEBRUARY, 13);

        freezeClock(2013, Calendar.FEBRUARY, 13, 10, 53, 20, 0);    // anything, as long as it's different from comTaskNextExecutionTimeStamp

        ScheduledConnectionTaskImpl notDefaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations("updateToDefaultTestNextExecutionTimeStamp");

        createComTaskExecutionAndSetNextExecutionTimeStamp(comTaskNextExecutionTimeStamp);
        ServerConnectionTaskService connectionTaskService = inMemoryPersistence.getConnectionTaskService();

        // Business method
        connectionTaskService.setDefaultConnectionTask(notDefaultConnectionTask);
        ScheduledConnectionTask reloaded = connectionTaskService.findScheduledConnectionTask(notDefaultConnectionTask.getId()).get();

        // Asserts after update
        assertThat(reloaded.getNextExecutionTimestamp()).isEqualTo(comTaskNextExecutionTimeStamp);
    }

    @Test
    @Transactional
    public void updateToMinimizeDefaultTestNextExecutionTimeStamp() throws SQLException {
        Instant comTaskNextExecutionTimeStamp = freezeClock(2013, Calendar.FEBRUARY, 13);

        freezeClock(2013, Calendar.FEBRUARY, 17, 10, 53, 20, 0);    // anything, as long as it's different from comTaskNextExecutionTimeStamp

        ScheduledConnectionTaskImpl theOneThatMinimizesConnections = this.createMinimizeWithNoPropertiesWithoutViolations("updateToDefaultTestNextExecutionTimeStamp", new TemporalExpression(EVERY_HOUR));
        ConnectionTaskService connectionTaskService = inMemoryPersistence.getConnectionTaskService();

        ComTaskExecution comTaskExecution = createComTaskExecutionAndSetNextExecutionTimeStamp(comTaskNextExecutionTimeStamp);

        // Business method
        connectionTaskService.setDefaultConnectionTask(theOneThatMinimizesConnections);
        Instant nextExecutionTimestamp = theOneThatMinimizesConnections.getNextExecutionTimestamp();
        ScheduledConnectionTask reloaded = connectionTaskService.findScheduledConnectionTask(theOneThatMinimizesConnections.getId()).get();
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device, comTaskExecution);


        // Asserts after update
        assertThat(reloaded.getNextExecutionTimestamp()).isEqualTo(nextExecutionTimestamp);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(nextExecutionTimestamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(nextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void clearDefaultConnectionWithObsoleteComTaskExecutionsTest() {
        ComTaskExecution comTaskExecution = createComTaskExecution();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("clearDefaultConnectionWithObsoleteComTaskExecutionsTest");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        device = getReloadedDevice(device);
        // We need to reload the comTaskExecution because ConnectionTaskService#setDefaultConnectionTask updates all executions
        ComTaskExecution reloaded = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        device.removeComTaskExecution(reloaded);

        inMemoryPersistence.getConnectionTaskService().clearDefaultConnectionTask(getReloadedDevice(device));

        ComTaskExecution reloadedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId()); // should not be updated
    }

    @Test
    @Transactional
    public void creatingDefaultFromPartialShouldAlsoUpdateComTaskExecutionsWhichUseTheDefaultTest() {
        this.partialScheduledConnectionTask.setDefault(true);
        this.partialScheduledConnectionTask.save();
        ComTaskExecution comTaskExecution = createComTaskExecution();

        // Prologue asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getConnectionTask()).isEmpty();

        // Business method
        ScheduledConnectionTaskImpl myDefaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations("MyDefaultConnectionTask", this.partialScheduledConnectionTask);

        // Asserts
        ComTaskExecution reloadedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isPresent();
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(myDefaultConnectionTask.getId());
    }

    @Test
    @Transactional
    public void testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault() {
        ComTaskExecution comTaskExecution = createComTaskExecution();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault");

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device, comTaskExecution);

        // Asserts
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void testTriggerWithAsapStrategyAndOnlyOnHoldAndWaitingTasks() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testTriggerWithAsapStrategyAndOnlyOnHoldAndWaitingTasks");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        final Instant futureDate = freezeClock(2013, Calendar.JULY, 4);
        Instant triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
        ComTaskExecution comTaskExecution1 = createComTaskExecutionAndSetNextExecutionTimeStamp(futureDate, comTaskEnablement1);
        ((ServerComTaskExecution) comTaskExecution1).executionCompleted();
        comTaskExecution1.getDevice().getComTaskExecutionUpdater(comTaskExecution1).forceNextExecutionTimeStampAndPriority(futureDate, 100).update();

        ComTaskExecution comTaskExecution2 = createComTaskExecution(comTaskEnablement2);
        comTaskExecution2.putOnHold();

        // Business method
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        Instant nextExecutionTimstamp = connectionTask.trigger(triggerDate);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        List<ComTaskExecution> comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureDate.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return comTaskExecution.getNextExecutionTimestamp() == null;
            }
        });
        assertThat(nextExecutionTimstamp).isEqualTo(triggerDate);
    }

    @Test
    @Transactional
    public void createWithComTaskUsingDefaultTestNextExecutionTimeStamp() throws SQLException {
        Instant febFirst = freezeClock(2013, Calendar.FEBRUARY, 1);
        ComTaskExecution comTaskExecution = createComTaskExecutionAndSetNextExecutionTimeStamp(febFirst);

        ScheduledConnectionTaskImpl defaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations("createWithComTaskUsingDefaultTestNextExecutionTimeStamp");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(defaultConnectionTask);

        // asserts
        assertThat(defaultConnectionTask.getNextExecutionTimestamp()).isEqualTo(febFirst);
    }

    @Test
    @Transactional
    public void testTriggerWithAsapStrategyAndOnlyPendingTasks() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testTriggerWithAsapStrategyAndOnlyPendingTasks");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        final Instant pastDate = freezeClock(2013, Calendar.JUNE, 3);

        ComTaskExecution comTaskExecution1 = createComTaskExecutionAndSetNextExecutionTimeStamp(pastDate, comTaskEnablement1);
        ComTaskExecution comTaskExecution2 = createComTaskExecutionAndSetNextExecutionTimeStamp(pastDate, comTaskEnablement2);

        Instant triggerDate = freezeClock(2013, Calendar.JULY, 5);

        // Business method
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        Instant nextExecutionTimstamp = connectionTask.trigger(triggerDate);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        List<ComTaskExecution> comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).is(new Condition<List<? extends ComTaskExecution>>() {
            @Override
            public boolean matches(List<? extends ComTaskExecution> comTaskExecutions) {
                for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                    if (!comTaskExecution.getNextExecutionTimestamp().equals(triggerDate)) {
                        return false;
                    }
                }
                return true;
            }
        });
        assertThat(nextExecutionTimstamp).isEqualTo(triggerDate);
    }

    @Test
    @Transactional
    public void testCreateWithExistingComTaskExecutions() {
        ComTaskExecution comTaskExecution = createComTaskExecution();

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateWithExistingComTaskExecutions");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        Device reloadedDevice = getReloadedDevice(device);
        // Asserts
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            assertThat(taskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
        }
    }

    @Test
    @Transactional
    public void testTriggerWithAsapStrategyAllComTaskStatusses() throws SQLException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testTriggerWithAsapStrategyAllComTaskStatusses");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        final Instant futureDate = freezeClock(2013, Calendar.JULY, 4);
        final Instant triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        final Instant lastExecutionStartTimestamp = freezeClock(2013, Calendar.MAY, 3);

        ComTaskExecution comTaskExecution = createComTaskExecutionAndSetNextExecutionTimeStamp(futureDate, lastExecutionStartTimestamp, comTaskEnablement1);
        assertThat(comTaskExecution.getStatus()).isEqualTo(TaskStatus.NeverCompleted);
        // Reload task because entity was changed during comTaskExecution creation process
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        connectionTask.trigger(triggerDate); // never completed
        Device reloadedDevice = getReloadedDevice(device);
        List<ComTaskExecution> comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return triggerDate.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        // Reload comTaskExecution because entity was changed during ConnectionTask#trigger(...) method call
        comTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        ((ServerComTaskExecution) comTaskExecution).executionCompleted();
        comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).forceNextExecutionTimeStampAndPriority(futureDate, 100).update(); // waiting task
        assertThat(getReloadedComTaskExecution(device, comTaskExecution).getStatus()).isEqualTo(TaskStatus.Waiting);
        // Reload connectionTask because comTaskExecution#executionCompleted() calls doReschedule which changes the task
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        connectionTask.trigger(triggerDate);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureDate.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        OutboundComPort outboundComPort = createOutboundComPort();
        ((ServerComTaskExecution) comTaskExecution).setLockedComPort(outboundComPort); // busy task
        setCurrentlyExecutionComServerOnConnectionTask(connectionTask, outboundComPort.getComServer()); // set busy task
        assertThat(getReloadedComTaskExecution(device, comTaskExecution).getStatus()).isEqualTo(TaskStatus.Busy);
        connectionTask.trigger(triggerDate);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureDate.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        setCurrentlyExecutionComServerOnConnectionTask(connectionTask, null);
        comTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        ((ServerComTaskExecution) comTaskExecution).setLockedComPort(null);
        comTaskExecution.putOnHold(); // on hold task
        assertThat(getReloadedComTaskExecution(device, comTaskExecution).getStatus()).isEqualTo(TaskStatus.OnHold);
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        connectionTask.trigger(triggerDate);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return comTaskExecution.getNextExecutionTimestamp() == null;
            }
        });

        comTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        comTaskExecution.resume();
        comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).forceNextExecutionTimeStampAndPriority(futureDate, 100).update();
        final Instant futureTrigger = freezeClock(2013, Calendar.AUGUST, 5); // pending task
        assertThat(getReloadedComTaskExecution(device, comTaskExecution).getStatus()).isEqualTo(TaskStatus.Pending);
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        connectionTask.trigger(futureTrigger);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureTrigger.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        comTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();  // make it retry
        assertThat(getReloadedComTaskExecution(device, comTaskExecution).getStatus()).isEqualTo(TaskStatus.Retrying);
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureTrigger.isBefore(getReloadedComTaskExecution(device, comTaskExecution).getNextExecutionTimestamp());
            }
        });

        connectionTask.trigger(futureTrigger);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureTrigger.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        comTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).forceNextExecutionTimeStampAndPriority(futureDate, 100).update();
        ((ServerComTaskExecution) comTaskExecution).executionCompleted();   // Resets any failures/retries

        freezeClock(2013, Calendar.AUGUST, 6);
        ((ServerComTaskExecution) comTaskExecution).executionStarted(outboundComPort);
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();  // make it fail
        assertThat(getReloadedComTaskExecution(device, comTaskExecution).getStatus()).isEqualTo(TaskStatus.Failed);
        connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findDefaultConnectionTaskForDevice(device).get();
        connectionTask.trigger(futureTrigger);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureTrigger.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
    }

    private void setCurrentlyExecutionComServerOnConnectionTask(ScheduledConnectionTaskImpl connectionTask, ComServer comServer) {
//        connectionTask.setExecutingComServer(comServer);
//        connectionTask.save();
        connectionTask.executionStarted(comServer);
    }

    private OutboundComPort createOutboundComPort() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
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
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        OutboundComPort outboundComPort = outboundComPortBuilder.add();
        return outboundComPort;
    }

    protected Instant freezeClock(int year, int month, int day) {
        return freezeClock(year, month, day, 0, 0, 0, 0);
    }

    protected Instant freezeClock(int year, int month, int day, TimeZone timeZone) {
        return freezeClock(year, month, day, 0, 0, 0, 0, timeZone);
    }

    protected Instant freezeClock(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        return freezeClock(year, month, day, hour, minute, second, millisecond, utcTimeZone);
    }

    protected Instant freezeClock(int year, int month, int day, int hour, int minute, int second, int millisecond, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        when(clock.getZone()).thenReturn(timeZone.toZoneId());
        Instant frozenClockValue = calendar.getTime().toInstant();
        when(clock.instant()).thenReturn(frozenClockValue);
        return frozenClockValue;
    }

    protected ComTaskExecution createComTaskExecution() {
        return createComTaskExecution(comTaskEnablement1);
    }

    protected ComTaskExecution createComTaskExecution(ComTaskEnablement comTaskEnablement) {
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        return device.newScheduledComTaskExecution(comSchedule).add();
    }

    protected ComTaskExecution createComTaskExecutionAndSetNextExecutionTimeStamp(Instant nextExecutionTimeStamp) {
        return createComTaskExecutionAndSetNextExecutionTimeStamp(nextExecutionTimeStamp, comTaskEnablement1);
    }

    protected ComTaskExecution createComTaskExecutionAndSetNextExecutionTimeStamp(Instant nextExecutionTimeStamp, ComTaskEnablement comTaskEnablement) {
        return createComTaskExecutionAndSetNextExecutionTimeStamp(nextExecutionTimeStamp, null, comTaskEnablement);
    }

    protected ComTaskExecution createComTaskExecutionAndSetNextExecutionTimeStamp(Instant nextExecutionTimeStamp, Instant lastExecutionStartTimestamp, ComTaskEnablement comTaskEnablement) {
        ComTaskExecution comTaskExecution = createComTaskExecution(comTaskEnablement);
        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.forceNextExecutionTimeStampAndPriority(nextExecutionTimeStamp, 100);
        if (lastExecutionStartTimestamp != null) {
            comTaskExecutionUpdater.forceLastExecutionStartTimestamp(lastExecutionStartTimestamp);
        }
        comTaskExecutionUpdater.update();
        return comTaskExecution;
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name) {
        return createAsapWithNoPropertiesWithoutViolations(name, this.partialScheduledConnectionTask);
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, PartialScheduledConnectionTask partialConnectionTask) {
        partialConnectionTask.setName(name);
        partialConnectionTask.save();
        return (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
    }

    private ScheduledConnectionTaskImpl createMinimizeWithNoPropertiesWithoutViolations(String name, TemporalExpression temporalExpression) {
        return (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(temporalExpression)
                .add();
    }

    private ProtocolDialectConfigurationProperties createDialectConfigProperties() {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        return configDialect;
    }

    private Device createSimpleDevice(String mRID) {
        Device simpleDevice = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "SimpleDevice", mRID, Instant.now());
        simpleDevice.save();
        return simpleDevice;
    }

    private ComSchedule createComSchedule(ComTask comTask) {
        return inMemoryPersistence.getSchedulingService()
                .newComSchedule(comTask.getName(), new TemporalExpression(TimeDuration.days(1)), Instant.now())
                .addComTask(comTask)
                .build();
    }

    protected ComTaskExecution getReloadedComTaskExecution(Device device, ComTaskExecution toReload) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getComTaskExecutions().stream().filter(x -> x.getId() == toReload.getId()).findAny().get();
    }

    private ComTask createComTaskWithBasicCheck() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    private ComTask createComTaskWithLogBooks() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME + 2);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createLogbooksTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    private ComTask createComTaskWithRegisters() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(COM_TASK_NAME + 3);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createRegistersTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    private ComTaskEnablement enableComTask(boolean useDefault, ProtocolDialectConfigurationProperties configDialect, ComTask comTask) {
        ComTaskEnablementBuilder builder = this.deviceConfiguration.enableComTask(comTask, this.securityPropertySet, configDialect);
        builder.useDefaultConnectionTask(useDefault);
        builder.setPriority(this.comTaskEnablementPriority);
        return builder.add();
    }

    private class ComTaskExecutionDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return Property.DEVICE_PROTOCOL_DIALECT.getName();
        }

        @Override
        public List<PropertySpec> getUPLPropertySpecs() {
            return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList());
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

    private class SubscriberForTopicHandler implements Subscriber {
        private final TopicHandler topicHandler;

        private SubscriberForTopicHandler(TopicHandler topicHandler) {
            super();
            this.topicHandler = topicHandler;
        }

        @Override
        public void handle(Object notification, Object... notificationDetails) {
            LocalEvent event = (LocalEvent) notification;
            if (event.getType().getTopic().equals(this.topicHandler.getTopicMatcher())) {
                this.topicHandler.handle(event);
            }
        }

        @Override
        public Class<?>[] getClasses() {
            return new Class<?>[]{LocalEvent.class};
        }

    }

}
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.DuplicateException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.ValueRequiredException;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.ComTaskExecutionFactory;
import com.energyict.mdc.device.data.PartialConnectionTaskFactory;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.pluggable.impl.ConnectionTypePluggableClassImpl;
import org.assertj.core.api.Assertions;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.*;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ScheduledConnectionTaskImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (14:12)
 */
public class ScheduledConnectionTaskImplIT extends ConnectionTaskImplIT {

    private static final TimeDuration EVERY_DAY = new TimeDuration(1, TimeDuration.DAYS);
    private static final ComWindow FROM_ONE_AM_TO_TWO_AM = new ComWindow(DateTimeConstants.SECONDS_PER_HOUR, 2 * DateTimeConstants.SECONDS_PER_HOUR);
    private static final ComWindow FROM_TEN_PM_TO_TWO_AM = new ComWindow(22 * DateTimeConstants.SECONDS_PER_HOUR, 2 * DateTimeConstants.SECONDS_PER_HOUR);

    private TimeZone toRestore;

    @Before
    public void useNoParamsForAllOutboundConnectionTasks() {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);
    }

    @After
    public void restoreTimeZone() {
        if (this.toRestore != null) {
            TimeZone.setDefault(this.toRestore);
        }
    }

    @After
    public void refreshAllConnectionTypePluggableClasses() throws BusinessException {
        refreshConnectionTypePluggableClasses();
    }

    @Test
    public void testCreateWithNoPropertiesWithoutViolations() throws BusinessException, SQLException {
        String name = "testCreateWithNoPropertiesWithoutViolations";
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations(name);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getName()).isEqualTo(name);
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getInitiatorTask()).isNull();
        assertThat(connectionTask.getProperties()).isEmpty();
        assertThat(connectionTask.isDefault()).isTrue();
        assertThat(connectionTask.isPaused()).isFalse();
        assertThat(connectionTask.getCurrentRetryCount()).isEqualTo(0);
        assertThat(connectionTask.getRescheduleDelay()).isNull();
        assertThat(connectionTask.lastExecutionFailed()).isEqualTo(false);
        assertThat(connectionTask.getExecutingComServer()).isNull();
    }

    @Test
    public void testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault() throws BusinessException, SQLException {
        ComTaskExecutionFactory comTaskExecutionFactory = mock(ComTaskExecutionFactory.class);
        when(inMemoryPersistence.getApplicationContext().getModulesImplementing(ComTaskExecutionFactory.class)).thenReturn(Arrays.asList(comTaskExecutionFactory));
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        List<ComTaskExecution> comTasksThatRelyOnTheDefault = new ArrayList<>(1);
        comTasksThatRelyOnTheDefault.add(comTask);
        when(comTaskExecutionFactory.findComTaskExecutionsForDefaultOutboundConnectionTask(this.device)).thenReturn(comTasksThatRelyOnTheDefault);
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault", false);

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        verify(comTask).updateToUseDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp() throws BusinessException, SQLException {
        ComTaskExecutionFactory comTaskExecutionFactory = mock(ComTaskExecutionFactory.class);
        when(inMemoryPersistence.getApplicationContext().getModulesImplementing(ComTaskExecutionFactory.class)).thenReturn(Arrays.asList(comTaskExecutionFactory));
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        List<ComTaskExecution> comTasksThatRelyOnTheDefault = new ArrayList<>(1);
        comTasksThatRelyOnTheDefault.add(comTask);
        when(comTaskExecutionFactory.findComTaskExecutionsForDefaultOutboundConnectionTask(this.device)).thenReturn(comTasksThatRelyOnTheDefault);
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp", false);
        Date earliestNextExecutionTimestamp = new DateMidnight(2013, 2, 14).toDate();
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(earliestNextExecutionTimestamp, ComTaskExecutionShadow.DEFAULT_PRIORITY);
        //when(this.manager.defaultConnectionTaskChanged(this.device, connectionTask)).thenReturn(earliestNextExecutionTimestampAndPriority);

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        verify(comTask).updateToUseDefaultConnectionTask(connectionTask);
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(earliestNextExecutionTimestamp);
    }

    @Test
    public void testCreatePaused() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreatePaused", false, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        connectionTask.save();

        //Business method
        connectionTask.pause();

        // Asserts
        assertThat(connectionTask.isPaused()).isTrue();
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    // Todo (JP-1122): enable this test when done
    @Ignore
    public void testCreateOfDifferentConfig() {
        DeviceCommunicationConfiguration mockCommunicationConfig = mock(DeviceCommunicationConfiguration.class);
        when(mockCommunicationConfig.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        PartialScheduledConnectionTask partialScheduledConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialScheduledConnectionTask.getId()).thenReturn(PARTIAL_SCHEDULED_CONNECTION_TASK3_ID);
        when(partialScheduledConnectionTask.getName()).thenReturn("testCreateOfDifferentConfig");
        when(partialScheduledConnectionTask.getConfiguration()).thenReturn(mockCommunicationConfig);
        when(partialScheduledConnectionTask.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);
        PartialConnectionTaskFactory partialConnectionTaskFactory = mock(PartialConnectionTaskFactory.class);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_SCHEDULED_CONNECTION_TASK1_ID)).thenReturn(this.partialScheduledConnectionTask);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_SCHEDULED_CONNECTION_TASK2_ID)).thenReturn(this.partialScheduledConnectionTask2);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_SCHEDULED_CONNECTION_TASK3_ID)).thenReturn(partialScheduledConnectionTask);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_SCHEDULED_CONNECTION_TASK1_ID)).thenReturn(this.partialScheduledConnectionTask);
        when(partialConnectionTaskFactory.findPartialConnectionTask(PARTIAL_SCHEDULED_CONNECTION_TASK2_ID)).thenReturn(this.partialScheduledConnectionTask2);
        List<PartialConnectionTaskFactory> partialConnectionTaskFactories = Arrays.asList(partialConnectionTaskFactory);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(PartialConnectionTaskFactory.class)).thenReturn(partialConnectionTaskFactories);

        // Business method
        ScheduledConnectionTask connectionTask;
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(ConnectionStrategy.AS_SOON_AS_POSSIBLE)) {
            connectionTask = deviceDataService.newAsapConnectionTask(this.device, partialScheduledConnectionTask, outboundTcpipComPortPool);
        }
        else {
            connectionTask = deviceDataService.
                    newMinimizeConnectionTask(
                            this.device,
                            partialScheduledConnectionTask,
                            outboundTcpipComPortPool,
                            new TemporalExpression(EVERY_HOUR));
        }

        // Asserts: see expected exception rule
    }

    @Test(expected = BusinessException.class)
    public void testCreateWithOffsetBiggerThanFrequency() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(new TimeDuration(1, TimeDuration.DAYS));
        nextExecutionSpecsShadow.setOffset(new TimeDuration(36, TimeDuration.HOURS));
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Expected a BusinessException because the offset is outside a day boundary
            assertThat(e.getMessageId()).isEqualTo("OffsetXIsBiggerThanFrequencyY");
            throw e;
        }

    }

    @Test
    public void testCreateWithOffsetBiggerThanDayButWithinFrequency() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        String name = "testCreateWithOffsetBiggerThanDayButWithinFrequency";
        when(this.partialOutboundConnectionTask1.getName()).thenReturn(name);    // name will be inherited from the partial connectionTask
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        // Set frequency and offset so that it executes every week, at noon of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        nextExecutionSpecsShadow.setFrequency(frequency);
        TimeDuration offset = new TimeDuration(36, TimeDuration.HOURS);
        nextExecutionSpecsShadow.setOffset(offset);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getName()).isEqualTo(name);
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getEvery()).isEqualTo(frequency);
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getOffset()).isEqualTo(offset);
    }

    @Test
    public void testPause() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testPause");

        OutboundConnectionTaskShadow updateShadow = connectionTask.getShadow();
        updateShadow.setPaused(true);

        // Business method
        connectionTask.update(updateShadow);

        // Asserts
        assertThat(connectionTask.isPaused()).isTrue();
    }

    @Test(expected = DuplicateException.class)
    public void testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask() throws BusinessException, SQLException {
        OutboundConnectionTask firstTask = this.createWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask");
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(firstTask.getDevice().getId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        try {
            // Business method
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (DuplicateException e) {
            // Asserts
            assertThat(e.getMessageId()).isEqualTo("duplicateConnectionTaskX");
            throw e;
        }
    }

    @Test
    public void testCreateSecondTaskAgainstTheSameDevice() throws BusinessException, SQLException {
        OutboundConnectionTask firstTask = this.createWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDevice-1");
        int firstDeviceId = firstTask.getDevice().getId();

        when(this.partialScheduledConnectionTask2.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask2.getName()).thenReturn("testCreateSecondTaskAgainstTheSameDevice-2");
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask2);
        shadow.setDeviceId(firstDeviceId);
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        OutboundConnectionTask secondTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Both tasks should be successfully created with each a separate ConnectionMethod
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
        ConnectionMethod firstTaskConnectionMethod = firstTask.getConnectionMethod();
        ConnectionMethod secondTaskConnectionMethod = secondTask.getConnectionMethod();
        assertThat(firstTaskConnectionMethod).isNotNull();
        assertThat(secondTaskConnectionMethod).isNotNull();
        assertNotSame(firstTaskConnectionMethod, secondTaskConnectionMethod);
        assertNotSame(firstTaskConnectionMethod.getId(), secondTaskConnectionMethod.getId());
    }

    @Test
    public void testCreateAgainstAnotherDeviceBasedOnTheSamePartialConnectionTask() throws BusinessException, SQLException {
        OutboundConnectionTask firstTask = this.createWithNoPropertiesWithoutViolations("testCreateAgainstAnotherDeviceBasedOnSamePartialConnectionTask-1");

        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        OutboundConnectionTask secondTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Both tasks should be successfully created with the same name
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
        assertThat(secondTask.getName()).isEqualTo(firstTask.getName());
    }

    @Test(expected = BusinessException.class)
    public void testCreateMinimizeConnectionsWithSimultaneous() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        shadow.setAllowSimultaneousConnections(true);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn("testCreateMinimizeConnectionsWithSimultaneous-1");
        shadow.setDeviceId(this.nextDeviceId());
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        // Business method
        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Asserts
            assertThat(e.getMessageId()).isEqualTo("simultaneousConnectionsNotAllowedForMinimizeConnectionStrategy");
            throw e;
        }
    }

    @Test
    public void testCreateWithAllIpProperties() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.empty());
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getConnectionMethod().getComPortPool()).isEqualTo(this.ipComPortPool);
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    public void testCreateWithOnlyRequiredIpPropertiesAndNoDefaultsOnPluggableClass() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.empty());
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getConnectionMethod().getComPortPool()).isEqualTo(this.ipComPortPool);
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(1);   // Only 1 property is locally defined and higher levels do not specify any property values
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(1);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
        assertThat(connectionTask.getShadow().getProperties().size()).isEqualTo(1);
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test
    public void testCreateWithOnlyRequiredIpPropertiesAndSomeDefaultsOnPluggableClass() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        PluggableClassShadow pluggableClassShadow = ((ConnectionTypePluggableClassImpl) ipConnectionTypePluggableClass).getShadow();
        pluggableClassShadow.set(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.update(pluggableClassShadow);

        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties()));
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getConnectionMethod().getComPortPool()).isEqualTo(this.ipComPortPool);
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);   // 1 property is locally defined, 1 is inherited and the third is not specified at any level
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
        assertThat(connectionTask.getShadow().getProperties().size()).isEqualTo(2);
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClass() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        PluggableClassShadow pluggableClassShadow = ((ConnectionTypePluggableClassImpl) ipConnectionTypePluggableClass).getShadow();
        pluggableClassShadow.set(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS_PROPERTY_VALUE);
        pluggableClassShadow.set(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        pluggableClassShadow.set(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        ipConnectionTypePluggableClass.update(pluggableClassShadow);

        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties()));
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        shadow.setComPortPoolId(IP_COMPORT_POOL_ID);

        // Do not add any properties to the shadow

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getConnectionMethod().getComPortPool()).isEqualTo(this.ipComPortPool);
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // no properties are locally defined, all 3 are inherited from the connection type pluggable class
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().size()).isEqualTo(3);
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClassAndPartialConnectionTask() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        PluggableClassShadow pluggableClassShadow = ((ConnectionTypePluggableClassImpl) ipConnectionTypePluggableClass).getShadow();
        pluggableClassShadow.set(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS_PROPERTY_VALUE);
        pluggableClassShadow.set(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        pluggableClassShadow.set(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        ipConnectionTypePluggableClass.update(pluggableClassShadow);

        TypedProperties partialConnectionTaskProperties = TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties());
        partialConnectionTaskProperties.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskProperties.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(partialConnectionTaskProperties);
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        shadow.setComPortPoolId(IP_COMPORT_POOL_ID);
        // Do not add any properties to the shadow

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getConnectionMethod().getComPortPool()).isEqualTo(this.ipComPortPool);
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // 2 properties are inherited from the partial connection task and 1 is inherited from the connection type pluggable class
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    public void testCreateWithAllPropertiesInheritedFromPartialConnectionTask() throws BusinessException, SQLException {
        TypedProperties partialConnectionTaskTypedProperties = TypedProperties.empty();
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(partialConnectionTaskTypedProperties);
        PartialConnectionTaskProperty partialIpAddressProperty = mock(PartialConnectionTaskProperty.class);
        when(partialIpAddressProperty.getName()).thenReturn(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        when(partialIpAddressProperty.getValue()).thenReturn(IP_ADDRESS_PROPERTY_VALUE);
        when(partialIpAddressProperty.getPartialConnectionTask()).thenReturn(this.partialOutboundConnectionTask1);
        PartialConnectionTaskProperty partialPortProperty = mock(PartialConnectionTaskProperty.class);
        when(partialPortProperty.getName()).thenReturn(IpConnectionType.PORT_PROPERTY_NAME);
        when(partialPortProperty.getValue()).thenReturn(PORT_PROPERTY_VALUE);
        when(partialPortProperty.getPartialConnectionTask()).thenReturn(this.partialOutboundConnectionTask1);
        PartialConnectionTaskProperty partialCodeTableProperty = mock(PartialConnectionTaskProperty.class);
        when(partialCodeTableProperty.getName()).thenReturn(IpConnectionType.CODE_TABLE_PROPERTY_NAME);
        when(partialCodeTableProperty.getValue()).thenReturn(codeTable);
        when(partialCodeTableProperty.getPartialConnectionTask()).thenReturn(this.partialOutboundConnectionTask1);
        List<PartialConnectionTaskProperty> partialConnectionTaskProperties = Arrays.asList(partialIpAddressProperty, partialPortProperty, partialCodeTableProperty);
        when(this.partialOutboundConnectionTask1.getProperties()).thenReturn(partialConnectionTaskProperties);
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        shadow.setComPortPoolId(IP_COMPORT_POOL_ID);
        // Do not add any properties to the shadow

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getConnectionMethod().getComPortPool()).isEqualTo(this.ipComPortPool);
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // no properties are locally defined, all 3 are inherited from the partial connection task
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().size()).isEqualTo(3);
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getShadow().getProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(connectionTask.getShadow().getProperties().hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    public void testUpdateIpConnectionTypeProperty() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.empty());
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(creationShadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);

        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.set(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);

        // Business method
        connectionTask.update(shadow);

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(3);  // Ip is default and has 3 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    public void testAddIpConnectionTypeProperty() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.empty());
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(creationShadow, IP_ADDRESS_PROPERTY_VALUE, null, codeTable);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);

        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.set(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);

        // Business method
        connectionTask.update(shadow);

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(3);  // Ip is default and has 3 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    public void testRemoveIpConnectionTypeProperty() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.empty());
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(creationShadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);

        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.remove(IpConnectionType.PORT_PROPERTY_NAME);

        // Business method
        connectionTask.update(shadow);

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(2);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    public void testReturnToInheritedProperty() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        PluggableClassShadow pluggableClassShadow = ((ConnectionTypePluggableClassImpl) ipConnectionTypePluggableClass).getShadow();
        pluggableClassShadow.set(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.update(pluggableClassShadow);

        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties()));
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(creationShadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);

        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.remove(IpConnectionType.PORT_PROPERTY_NAME);

        // Business method
        connectionTask.update(shadow);

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(3);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test(expected = BusinessException.class)
    public void testCreateWithIpWithModemComPortPool() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable, MODEM_COMPORT_POOL_ID);

        // Business method
        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Expected BusinessException because the ComPortType of the ComPortPool is not supported by the ConnectionType
            assertThat(e.getMessageId()).isEqualTo("comPortTypeXOfComPortPoolYIsNotSupportedByConnectionTypePluggableClassZ");
            throw e;
        }
    }

    @Test(expected = InvalidReferenceException.class)
    public void testCreateWithInboundComPortPool() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setComPortPool(shadow, INBOUND_COMPORT_POOL1_ID);

        // Business method
        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Expected InvalidReferenceException because the NoParams connection type usage is created with an InboundComPortPool
    }

    @Test
    public void testGetPropertiesOnMultipleDates() throws BusinessException, SQLException {
        FrozenClock mayFirst2011 = FrozenClock.frozenOn(2011, Calendar.MAY, 1);
        FrozenClock mayFirst2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 1);

        // Create task with properties on may first 2011
        Clocks.setDatabaseServerClock(mayFirst2011);
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(creationShadow, IP_ADDRESS_PROPERTY_VALUE, null, null);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);

        Clocks.setDatabaseServerClock(mayFirst2012);
        OutboundConnectionTaskShadow updateShadow = connectionTask.getShadow();
        updateShadow.set(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        updateShadow.set(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);

        // Business method
        connectionTask.update(updateShadow);

        // Asserts
        assertThat(connectionTask.getConnectionMethod().getPluggableClass().getConnectionType()).isInstanceOf(IpConnectionType.class);
        List<ConnectionTaskProperty> allPropertiesOnMayFirst2011 = connectionTask.getProperties(mayFirst2011.now());
        assertThat(allPropertiesOnMayFirst2011).hasSize(1); // On May 1st, 2011 only the ip address was specified
        ConnectionTaskProperty property = allPropertiesOnMayFirst2011.get(0);
        assertThat(property.getName()).isEqualTo(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        assertThat(property.getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        ConnectionMethod ipConnectionMethod = connectionTask.getConnectionMethod();
        assertThat(ipConnectionMethod.getAllProperties(Clocks.getAppServerClock().now())).hasSize(2);
        assertThat(ipConnectionMethod.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME).getValue()).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(ipConnectionMethod.getProperty(IpConnectionType.PORT_PROPERTY_NAME).getValue()).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(ipConnectionMethod.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test
    public void testGetPropertiesOnMultipleDatesAfterReload() throws BusinessException, SQLException {
        FrozenClock mayFirst2011 = FrozenClock.frozenOn(2011, Calendar.MAY, 1);
        FrozenClock mayFirst2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 1);

        // Create task with properties on may first 2011
        Clocks.setDatabaseServerClock(mayFirst2011);
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(creationShadow, IP_ADDRESS_PROPERTY_VALUE, null, null);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);

        Clocks.setDatabaseServerClock(mayFirst2012);
        OutboundConnectionTaskShadow updateShadow = connectionTask.getShadow();
        updateShadow.set(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        updateShadow.set(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);

        connectionTask.update(updateShadow);

        // Business method
        OutboundConnectionTask reloaded = (OutboundConnectionTask) new ConnectionTaskFactoryImpl().find(connectionTask.getId());

        // Asserts
        assertThat(reloaded.getConnectionMethod().getPluggableClass().getConnectionType()).isInstanceOf(IpConnectionType.class);
        List<ConnectionTaskProperty> allPropertiesOnMayFirst2011 = reloaded.getProperties(mayFirst2011.now());
        assertThat(allPropertiesOnMayFirst2011).hasSize(1); // On May 1st, 2011 only the ip address was specified
        ConnectionTaskProperty property = allPropertiesOnMayFirst2011.get(0);
        assertThat(property.getName()).isEqualTo(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        assertThat(property.getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        ConnectionMethod ipConnectionMethod = connectionTask.getConnectionMethod();
        assertThat(ipConnectionMethod.getAllProperties(Clocks.getAppServerClock().now())).hasSize(2);
        assertThat(ipConnectionMethod.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME).getValue()).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(ipConnectionMethod.getProperty(IpConnectionType.PORT_PROPERTY_NAME).getValue()).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(ipConnectionMethod.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test(expected = ValueRequiredException.class)
    public void testCreateWithMissingRequiredProperty() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialOutboundConnectionTask1.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties()));
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, null, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Expected ValueRequiredException because no value is specified for the required ipAddress property
    }

    @Test(expected = BusinessException.class)
    public void testCreateWithNonExistingProperty() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        // Add values for non existing property
        shadow.set("doesNotExist", "I don't care");

        // Business method
        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Expected BusinessException because one of the values relates to a non existing attribute of the IpConnectionType
            assertThat(e.getMessageId()).isEqualTo("connectionTaskPropertyXIsNotInConnectionTypeSpec");
            throw e;
        }
    }

    @Test
    public void testCreateWithCommunicationWindowWithoutViolations() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithCommunicationWindowWithoutViolations("testCreateWithCommunicationWindowWithoutViolations");

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getCommunicationWindow()).isNotNull();
        assertTrue("Was not expecting any properties on the Outbound Connection Task", connectionTask.getProperties().isEmpty());
    }

    @Test(expected = BusinessException.class)
    public void createWithoutOffsetAndCommunicationWindow() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_DAY);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        // Business method
        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Expecting a BusinessException because the offset is outside the communication window.
            assertThat(e.getMessageId()).isEqualTo("OffsetXIsNotWithinComWindowY");
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void createWithOffsetWithinDayButOutsideCommunicationWindow() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_DAY);
        nextExecutionSpecsShadow.setOffset(new TimeDuration(12, TimeDuration.HOURS));
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        // Business method
        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Expecting a BusinessException because the offset is outside the communication window.
            assertThat(e.getMessageId()).isEqualTo("OffsetXIsNotWithinComWindowY");
            throw e;
        }
    }

    @Test
    public void createWithOffsetOutsideDayButWithinCommunicationWindow() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        String name = "createWithOffsetOutsideDayButWithinCommunicationWindow";
        when(this.partialOutboundConnectionTask1.getName()).thenReturn(name);    // name will be inherited from the partial connectionTask
        shadow.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        // Set it to execute every week, at 01:30 (am) of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        nextExecutionSpecsShadow.setFrequency(frequency);
        TimeDuration offset = new TimeDuration(TimeConstants.SECONDS_IN_HOUR * 25 + TimeConstants.SECONDS_IN_MINUTE * 30, TimeDuration.SECONDS);
        nextExecutionSpecsShadow.setOffset(offset);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getName()).isEqualTo(name);
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getEvery()).isEqualTo(frequency);
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getOffset()).isEqualTo(offset);
    }

    @Test(expected = BusinessException.class)
    public void createWithOffsetOutsideDayAndOutsideCommunicationWindow() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        // Set it to execute every week, at 00:30 (am) of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        nextExecutionSpecsShadow.setFrequency(frequency);
        TimeDuration offset = new TimeDuration(TimeConstants.SECONDS_IN_HOUR * 24 + TimeConstants.SECONDS_IN_MINUTE * 30, TimeDuration.SECONDS);
        nextExecutionSpecsShadow.setOffset(offset);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        // Business method
        try {
            new ConnectionTaskFactoryImpl().createScheduled(shadow);
        } catch (BusinessException e) {
            // Expecting a BusinessException because the offset is outside the communication window.
            assertThat(e.getMessageId()).isEqualTo("LongOffsetXIsNotWithinComWindowY");
            throw e;
        }
    }

    @Test
    public void testCreateWithExistingComPorts() throws BusinessException, SQLException {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.add(mock(ComTaskExecution.class));
        comTaskExecutions.add(mock(ComTaskExecution.class));
        when(this.comTaskExecutionFactory.findComTaskExecutionsByTopology(this.device)).thenReturn(comTaskExecutions);
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreateWithExistingComPorts");

        // Asserts
        for (ComTaskExecution ComTaskExecution : comTaskExecutions) {
            ComTaskExecution serverComTask = (ComTaskExecution) ComTaskExecution;
            verify(serverComTask).connectionTaskCreated(this.device, connectionTask);
        }
    }

    @Test
    public void createWithComTaskUsingDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        FrozenClock febFirst = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 1);
        Clocks.setAppServerClock(febFirst);
        Date nextExecutionTimeStamp = Clocks.getAppServerClock().now();

        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(Matchers.<OutboundConnectionTask>any())).thenReturn(new Pair<>(nextExecutionTimeStamp, 100));
        OutboundConnectionTask defaultConnectionTask = this.createWithNoPropertiesWithoutViolations("testDefaultNextExec", true);

        // asserts
        assertThat(defaultConnectionTask.getNextExecutionTimestamp()).isEqualTo(nextExecutionTimeStamp);
    }

    @Test
    public void updateToDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        FrozenClock febFirst = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 13, 10, 53, 20, 0);
        Clocks.setAppServerClock(febFirst);
        FrozenClock nextConnectionTaskCalculated = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 13, 11, 0, 0, 0);   // 1 hour later according to the executionSpec
        OutboundConnectionTask notDefaultConnectionTask = this.createWithNoPropertiesWithoutViolations("testNotDefaultYetNextExec", false);
        OutboundConnectionTask reloaded = (OutboundConnectionTask) this.connectionTaskFactory.find(notDefaultConnectionTask.getId());
        Date comTaskNextExecutionTimeStamp = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 13).now();
        Pair<Date, Integer> earliestNextExecutionTimeStampAndPriority = new Pair<>(comTaskNextExecutionTimeStamp, 100);
        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(Matchers.<OutboundConnectionTask>any())).thenReturn(earliestNextExecutionTimeStampAndPriority);
        when(this.manager.defaultConnectionTaskChanged(this.device, notDefaultConnectionTask)).thenReturn(earliestNextExecutionTimeStampAndPriority);

        // Assert
        assertThat(reloaded.getNextExecutionTimestamp()).isNotNull();
        assertThat(reloaded.getNextExecutionTimestamp()).isEqualTo(nextConnectionTaskCalculated.now());

        // Business method
        reloaded.setAsDefault();
        OutboundConnectionTask secondReload = (OutboundConnectionTask) this.connectionTaskFactory.find(reloaded.getId());

        // Asserts after update
        assertThat(secondReload.getNextExecutionTimestamp()).isNotEqualTo(nextConnectionTaskCalculated.now());
        assertThat(secondReload.getNextExecutionTimestamp()).isEqualTo(comTaskNextExecutionTimeStamp);
    }

    @Test(expected = InvalidValueException.class)
    public void testCreateWithoutConnectionStrategy() throws BusinessException, SQLException {
        this.createWithoutConnectionStrategy("createWithoutConnectionStrategy");

        // Nothing to assert as we are expecting an InvalidValueException
    }

    @Test
    public void testCreateWithAsSoonAsPossibleButWithoutNextExecutionSpecs() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = this.createWithoutNextExecutionSpecs(ConnectionStrategy.AS_SOON_AS_POSSIBLE, "testCreateWithAsSoonAsPossibleButWithoutNextExecutionSpecs");
        this.setIpComPortPool(shadow);

        // Business method
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNull();
    }

    @Test(expected = InvalidValueException.class)
    public void testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = this.createWithoutNextExecutionSpecs(ConnectionStrategy.MINIMIZE_CONNECTIONS, "testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs");
        this.setIpComPortPool(shadow);

        // Busines method
        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Nothing to assert as we are expecting an InvalidValueException
    }

    @Test(expected = InvalidValueException.class)
    public void testCreateWithoutDevice() throws BusinessException, SQLException {
        this.createWithoutDevice();

        // Nothing to assert as we are expecting an InvalidValueException
    }

    @Test(expected = InvalidReferenceException.class)
    public void testCreateWithNonExistingDevice() throws BusinessException, SQLException {
        this.createWithNonExistingDevice();

        // Nothing to assert as we are expecting an InvalidReferenceException
    }

    @Test(expected = InvalidReferenceException.class)
    public void testCreateWithNonExistingComPortPool() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable, NON_EXISTING_COMPORT_POOL_ID);

        // Business method
        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Expected an InvalidReferenceException
    }

    @Test(expected = InvalidValueException.class)
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        int compPortPoolId = 0; // This equals setting the property to null
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable, compPortPoolId);

        // Business method
        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Expected an InvalidValueException
    }

    @Test(expected = InvalidReferenceException.class)
    public void testCreateWithNonExistingInitiator() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionInitiationTaskId(NON_EXISTING_INITIATOR_ID);
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Nothing to assert as we are expecting an InvalidReferenceException
    }

    @Test
    public void testGetShadowWithMinimumProperties() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetShadowWithMinimumProperties");

        // Business method
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();

        // Asserts
        assertEquals(connectionTask.getDevice().getId(), shadow.getDeviceId());
        assertEquals(connectionTask.getConnectionStrategy(), shadow.getConnectionStrategy());
        assertEquals(connectionTask.getCommunicationWindow(), shadow.getCommunicationWindow());
        assertEquals(connectionTask.getNextExecutionSpecs().getShadow().getDialCalendarId(), shadow.getNextExecutionSpecs().getDialCalendarId());
        assertEquals(connectionTask.getNextExecutionSpecs().getShadow().getFrequency(), shadow.getNextExecutionSpecs().getFrequency());
        assertEquals(connectionTask.getNextExecutionSpecs().getShadow().getOffset(), shadow.getNextExecutionSpecs().getOffset());
    }

    @Test
    public void testAllowSimultaneousConnections() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        shadow.setAllowSimultaneousConnections(true);
        this.setIpComPortPool(shadow);

        // First one - allow simultaneous connections
        OutboundConnectionTask outboundTrue = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // second one - deny simultaneous connections
        shadow.setAllowSimultaneousConnections(false);
        shadow.setPartialConnectionTask(partialScheduledConnectionTask2);
        OutboundConnectionTask outboundFalse = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        assertTrue(outboundTrue.isSimultaneousConnectionsAllowed());
        assertFalse(outboundFalse.isSimultaneousConnectionsAllowed());
    }

    @Test
    public void testLoad() throws BusinessException, SQLException {
        OutboundConnectionTask created = this.createWithNoPropertiesWithoutViolations("testLoad");

        // Business method
        OutboundConnectionTask loaded = (OutboundConnectionTask) this.connectionTaskFactory.find(created.getId());

        // Asserts
        assertEquals(created.getDevice().getId(), loaded.getDevice().getId());
        assertEquals(created.getConnectionMethod().getComPortPool().getId(), loaded.getConnectionMethod().getComPortPool().getId());
        assertEquals(created.getConnectionStrategy(), loaded.getConnectionStrategy());
        assertEquals(created.getCommunicationWindow(), loaded.getCommunicationWindow());
        assertEquals(created.getNextExecutionSpecs(), loaded.getNextExecutionSpecs());
        assertEquals(created.getInitiatorTask(), loaded.getInitiatorTask());
        assertEquals(created.isSimultaneousConnectionsAllowed(), loaded.isSimultaneousConnectionsAllowed());
        assertEquals(created.getNextExecutionTimestamp(), loaded.getNextExecutionTimestamp());
        assertEquals(created.getPlannedNextExecutionTimestamp(), loaded.getPlannedNextExecutionTimestamp());
    }

    @Test
    public void testGetShadow() throws BusinessException, SQLException {
        OutboundConnectionTask created = this.createWithNoPropertiesWithoutViolations("testGetShadow");

        // Business method
        OutboundConnectionTaskShadow shadow = created.getShadow();

        // Asserts
        assertThat(shadow.getDeviceId()).isEqualTo(created.getDevice().getId());
        assertThat(shadow.getConnectionStrategy()).isEqualTo(created.getConnectionStrategy());
        assertThat(shadow.getCommunicationWindow()).isEqualTo(created.getCommunicationWindow());
        assertThat(shadow.getNextExecutionSpecs().getFrequency()).isEqualTo(created.getNextExecutionSpecs().getTemporalExpression().getEvery());
        assertThat(shadow.getNextExecutionSpecs().getOffset()).isEqualTo(created.getNextExecutionSpecs().getTemporalExpression().getOffset());
        assertThat(shadow.isAllowSimultaneousConnections()).isEqualTo(created.isSimultaneousConnectionsAllowed());
    }

    @Test
    public void testUpdate() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUpdate");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setCommunicationWindow(FROM_TEN_PM_TO_TWO_AM);
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_DAY);
        TimeDuration elevenPM = new TimeDuration(23, TimeDuration.HOURS);
        nextExecutionSpecsShadow.setOffset(elevenPM);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);

        // Business method
        connectionTask.update(shadow);
        OutboundConnectionTask updated = (OutboundConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.MINIMIZE_CONNECTIONS, updated.getConnectionStrategy());
        assertEquals(FROM_TEN_PM_TO_TWO_AM, updated.getCommunicationWindow());
        assertEquals(EVERY_DAY, updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
        assertEquals(elevenPM, updated.getNextExecutionSpecs().getTemporalExpression().getOffset());
        assertThat(updated.getNextExecutionSpecs().getDialCalendar()).isNull();
    }

    @Test
    public void testUpdateWithDialCalendar() throws BusinessException, SQLException {
        int DIALCALENDAR_ID = 105;
        DialCalendar dialCalendar = mock(DialCalendar.class);
        when(dialCalendar.getId()).thenReturn(DIALCALENDAR_ID);
        when(dialCalendar.getNextDate(any(Date.class))).thenReturn(Clocks.getAppServerClock().now());
        DialCalendarFactory dialCalendarFactory = mock(DialCalendarFactory.class);
        when(dialCalendarFactory.find(DIALCALENDAR_ID)).thenReturn(dialCalendar);
        when(this.mdwInterface.getDialCalendarFactory()).thenReturn(dialCalendarFactory);

        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUpdate");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setCommunicationWindow(FROM_TEN_PM_TO_TWO_AM);
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setDialCalendarId(DIALCALENDAR_ID);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);

        // Business method
        connectionTask.update(shadow);
        OutboundConnectionTask updated = (OutboundConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.MINIMIZE_CONNECTIONS, updated.getConnectionStrategy());
        assertEquals(FROM_TEN_PM_TO_TWO_AM, updated.getCommunicationWindow());
        assertThat(updated.getNextExecutionSpecs().getTemporalExpression()).isNull(); // EISERVERSG-1888
        assertEquals(dialCalendar, updated.getNextExecutionSpecs().getDialCalendar());
    }

    @Test
    public void testSwitchToAsapStrategyAndRemoveNextExecSpec() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecs = new NextExecutionSpecsShadow();
        nextExecutionSpecs.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecs);
        this.setIpComPortPool(creationShadow);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        shadow.setNextExecutionSpecs(null);

        // Business method
        connectionTask.update(shadow);
        OutboundConnectionTask updated = (OutboundConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.AS_SOON_AS_POSSIBLE, updated.getConnectionStrategy());
        assertThat(updated.getNextExecutionSpecs()).isNull();
    }

    @Test
    public void testSwitchToMinimizeConnectionStrategy() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSwitchToMinimizeConnectionStrategy");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecs = new NextExecutionSpecsShadow();
        nextExecutionSpecs.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecs);

        // Business method
        connectionTask.update(shadow);
        OutboundConnectionTask updated = (OutboundConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.MINIMIZE_CONNECTIONS, updated.getConnectionStrategy());
        assertThat(updated.getNextExecutionSpecs()).isNotNull();
        assertEquals(EVERY_HOUR, updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
    }

    @Test
    public void testScheduleNow() throws BusinessException, SQLException {
        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);
        Clocks.setAppServerClock(mayLast2012);

        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testScheduleNow");

        // Business method
        connectionTask.scheduleNow();

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(Clocks.getAppServerClock().now());
    }

    @Test
    public void testScheduleOnDate() throws BusinessException, SQLException {
        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);
        Clocks.setAppServerClock(mayLast2012);

        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testScheduleOnDate");

        // Business method
        connectionTask.schedule(Clocks.getAppServerClock().now());

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(Clocks.getAppServerClock().now());
    }

    @Test
    public void testUpdateNextExecutionTimestampForUTCDevice() throws BusinessException, SQLException {
        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);
        Date expectedNextExecutionTimestamp = FrozenClock.frozenOn(2012, Calendar.MAY, 31, 1, 0, 0, 0).now();     // Frequency of rescheduling is 1 hour
        Clocks.setAppServerClock(mayLast2012);
//        when(this.device.getDeviceTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    public void testUpdateNextExecutionTimestampForUSPacificDevice() throws BusinessException, SQLException {
        // US/Pacific timezone has offset to UTC of -8 and when DST is applied, it has offset to UTC of -7
        TimeZone usPacific = TimeZone.getTimeZone("US/Pacific");

        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);    // This is in UTC
        Clocks.setAppServerClock(mayLast2012);

        Calendar calendar = Calendar.getInstance(usPacific);
        calendar.setTime(mayLast2012.now());
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Date expectedNextExecutionTimestamp = calendar.getTime();
//        when(this.device.getDeviceTimeZone()).thenReturn(usPacific);

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    public void testUpdateNextExecutionTimestampOnDSTFromWinterToSummer() throws BusinessException, SQLException {
        // Europe/Brussels timezone has offset to UTC of +1 and when DST is applied, it has offset to UTC of +2
        TimeZone brussels = TimeZone.getTimeZone("Europe/Brussels");

        // This is in UTC are corresponds to 02:00am in Brussels, exactly when DST applies
        FrozenClock oneMinuteBeforeDST = FrozenClock.frozenOn(2011, Calendar.MARCH, 27, 1, 0, 0, 0);
        Clocks.setAppServerClock(oneMinuteBeforeDST);

        Calendar calendar = Calendar.getInstance(brussels);
        calendar.set(2011, Calendar.MARCH, 27, 2, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Date expectedNextExecutionTimestamp = calendar.getTime();
//        when(this.device.getDeviceTimeZone()).thenReturn(brussels);

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    public void testUpdateNextExecutionTimestampOnDSTFromSummerToWinter() throws BusinessException, SQLException {
        // Europe/Brussels timezone has offset to UTC of +1 and when DST is applied, it has offset to UTC of +2
        TimeZone brussels = TimeZone.getTimeZone("Europe/Brussels");

        // This is in UTC are corresponds to 03:00am in Brussels, exactly when DST is switched off
        FrozenClock oneMinuteBeforeDST = FrozenClock.frozenOn(2011, Calendar.OCTOBER, 30, 1, 0, 0, 0);
        Clocks.setAppServerClock(oneMinuteBeforeDST);

        Calendar calendar = Calendar.getInstance(brussels);
        calendar.set(2011, Calendar.OCTOBER, 30, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR, 3);     // The calendar is now at 3 am
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Date expectedNextExecutionTimestamp = calendar.getTime();
        //TODO check to update these tests!
//        when(this.device.getDeviceTimeZone()).thenReturn(brussels);

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    public void testDeleteWithNoProperties() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testDeleteWithNoProperties");
        int id = connectionTask.getId();
        int nextExecutionSpecsId = connectionTask.getNextExecutionSpecs().getId();

        // Business method
        connectionTask.delete();

        // Asserts
        assertThat(this.connectionTaskFactory.find(id)).isNull();
        assertThat(new NextExecutionSpecsFactory().find(nextExecutionSpecsId)).isNull();
    }

    @Test
    public void testDeleteWithProperties() throws BusinessException, SQLException {
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testDeleteWithProperties");
        int id = connectionTask.getId();
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.delete();

        // Asserts
        assertThat(this.connectionTaskFactory.find(id)).isNull();
        RelationAttributeType connectionMethodAttributeType = (RelationAttributeType) ipConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test(expected = BusinessException.class)
    public void makeObsoleteWhenComServerIsExecutingTest() throws SQLException, BusinessException {
        mockMdwInterfaceTransactionExecutor();
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("makeObsoleteWhenComServerIsExecutingByComPortCheckTest");
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        ((ServerScheduledConnectionTask) connectionTask).attemptLock(comServer);

        // Business method
        try {
            connectionTask.makeObsolete();
        } catch (BusinessException e) {
            if (!e.getMessageId().equals("connectionTaskExecutingCantBeDeleted")) {
                Assertions.fail("Exception should indicate that the connectionTask couldn't be deleted because the ComServer is currently" +
                        "executing it, but was: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    public void testMakeObsoleteWithNoProperties() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteWithNoProperties");

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
    }

    @Test
    public void testMakeObsoleteAlsoMakesRelationsObsolete() throws BusinessException, SQLException {
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testMakeObsoleteAlsoMakesRelationsObsolete");
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getConnectionMethod()).isNotNull();
        RelationAttributeType connectionMethodAttributeType = (RelationAttributeType) ipConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).hasSize(1);
    }

    @Test
    public void testIsObsoleteAfterReload() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testIsObsoleteAfterReload");
        int id = connectionTask.getId();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        ConnectionTask obsolete = this.connectionTaskFactory.find(id);
        assertThat("The ConnectionTask should be marked for delete, but still present in DB", obsolete).isNotNull();
        assertTrue(obsolete.isObsolete());
        assertThat(obsolete.getObsoleteDate()).isNotNull();
    }

    @Test(expected = BusinessException.class)
    public void testUpdateAfterMakeObsolete() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUpdateAfterMakeObsolete");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        connectionTask.makeObsolete();

        // Business method
        connectionTask.update(shadow);

        // Expected BusinessException because updates are not allowed on obsolete ConnectionTasks
    }

    public void testMakeObsoleteLogsAuditTrail() throws BusinessException, SQLException {
        this.enableAuditTrailing();
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteTwice");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();

        // Business method
        connectionTask.makeObsolete();

        verify(this.getAuditTrailFactory()).create(shadow, this.connectionTaskFactory.getId(), AuditTrail.ACTION_DELETE);
    }

    @Test(expected = BusinessException.class)
    public void testMakeObsoleteTwice() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteTwice");
        connectionTask.makeObsolete();

        // Business method
        connectionTask.makeObsolete();

        // Expected BusinessException because a ConnectionTask cannot be made obsolete twice
    }

    @Test(expected = BusinessException.class)
    public void makeObsoleteWhenSomeOneElseMadeItObsoleteTest() throws SQLException, BusinessException {
        final OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("makeObsoleteWhenSomeOneElseMadeItObsoleteTest");
        MeteringWarehouse.getCurrent().execute(new Transaction<Object>() {
            @Override
            public Object doExecute() {
                try {
                    try (PreparedStatement statement = getUpdateObsoleteDateSqlBuilder(connectionTask.getId()).getStatement(Environment.DEFAULT.get().getConnection())) {
                        int updateCount = statement.executeUpdate();
                        if (updateCount != 1) {
                            throw new SQLException("updated zero rows");
                        }
                    }
                } catch (SQLException ex) {
                    throw new DatabaseException(ex);
                }
                return null;
            }
        });

        try {
            // Business method
            connectionTask.makeObsolete();
        } catch (BusinessException e) {
            if (!e.getMessageId().equals("connectionTaskIsAlreadyObsolete")) {
                Assertions.fail("Exception should have indicated that the connectionTask is already made obsolete, but was : " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private SqlBuilder getUpdateObsoleteDateSqlBuilder(int id) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(ConnectionTaskFactoryImpl.TABLENAME);
        sqlBuilder.append(" set ");
        sqlBuilder.append(ConnectionTaskFactoryImpl.OBSOLETE_DATE_COLUMN_NAME);
        sqlBuilder.append(" = ? where id = ?");
        sqlBuilder.bindDate(Clocks.getAppServerClock().now());
        sqlBuilder.bindInt(id);
        return sqlBuilder;
    }

    /**
     * Making a ConnectionTask obsolete results in updating the ComTaskExecutions with an empty ConnectionTask
     */
    @Test
    public void testMakeObsoleteWithActiveComTasks() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteWithActiveComTasks");
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.isObsolete()).thenReturn(false);
        comTaskExecutions.add(comTask);
        when(this.comTaskExecutionFactory.findAllByConnectionTask(connectionTask)).thenReturn(comTaskExecutions);

        // Business method
        connectionTask.makeObsolete();

        verify(comTask).connectionTaskRemoved();
    }

    @Test(expected = BusinessException.class)
    public void testCannotDeleteDefaultTaskThatIsInUse() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCannotDeleteDefaultTaskThatIsInUse");
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.add(mock(ComTaskExecution.class));
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).thenReturn(comTaskExecutions);

        try {
            // Business method
            connectionTask.delete();
        } catch (BusinessException e) {
            // Asserts
            assertThat(e.getMessageId()).isEqualTo("connectionTask.remainingDependentComTaskExecutions");
            throw e;
        }
    }

    @Test
    public void testDeletedAndSetComTaskToNoConnectionTask() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testDeletedAndSetComTaskToNoConnectionTask", false);
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        ComTaskExecution obsoleteComTask = mock(ComTaskExecution.class);
        comTaskExecutions.add(obsoleteComTask);
        when(this.comTaskExecutionFactory.findAllByConnectionTask(connectionTask)).thenReturn(comTaskExecutions);

        // Business method
        connectionTask.delete();

        // Asserts
        verify(obsoleteComTask).connectionTaskRemoved();
        assertThat(new ConnectionTaskFactoryImpl().find(connectionTask.getId())).isNull();
    }

    @Test
    public void testDeleteWithComSessions() throws BusinessException, SQLException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testDeleteWithComSessions");
        List<ComSession> comSessions = new ArrayList<>();
        ComSession comSession = mock(ComSession.class);
        comSessions.add(comSession);
        when(this.comSessionFactory.findByTask(connectionTask)).thenReturn(comSessions);

        // Business method
        connectionTask.delete();

        // Asserts
        verify(comSession).delete();
        assertThat(new ConnectionTaskFactoryImpl().find(connectionTask.getId())).isNull();
    }

    @Test
    public void testFindOutboundByDeviceAfterDelete() throws BusinessException, SQLException {
        when(this.partialOutboundConnectionTask1.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        OutboundConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);
        List<OutboundConnectionTask> outboundByDeviceBeforeDelete = connectionTaskFactory.findOutboundByDevice(connectionTask.getDevice());

        // Business methods
        connectionTask.delete();
        List<OutboundConnectionTask> outboundByDeviceAfterDelete = connectionTaskFactory.findOutboundByDevice(connectionTask.getDevice());

        // Asserts
        assertThat(outboundByDeviceBeforeDelete).contains(connectionTask);
        assertThat(outboundByDeviceAfterDelete).isEmpty();
    }

    @Test
    public void testDeleteWithDependentConnectionMethod() throws BusinessException, SQLException {
        OutboundConnectionTask outboundConnectionTask = this.createWithNoPropertiesWithoutViolations("testDeleteWithDependentConnectionMethod");
        ConnectionMethod connectionMethod = outboundConnectionTask.getConnectionMethod();

        outboundConnectionTask.delete();
        ConnectionTask deletedConnectionTask = this.connectionTaskFactory.find(outboundConnectionTask.getId());
        assertThat(deletedConnectionTask).isNull();
        ServerConnectionMethod deletedConnectionMethod = new ConnectionMethodFactoryImpl().find(connectionMethod.getId(), outboundConnectionTask);
        assertThat(deletedConnectionMethod).isNull();
        ServerNextExecutionSpecs deletedNextExecutionSpecs = new NextExecutionSpecsFactory().find(outboundConnectionTask.getNextExecutionSpecs().getId());
        assertThat(deletedNextExecutionSpecs).isNull();
    }

    @Test
    public void createMultipleOutboundsForSpecificDeviceWithoutViolations() throws BusinessException, SQLException {
        OutboundConnectionTask oct1 = this.createWithNoPropertiesWithoutViolations("createMultipleOutboundsForSpecificDeviceWithoutViolations");
        OutboundConnectionTaskShadow shadow = oct1.getShadow();
        shadow.setPartialConnectionTask(partialScheduledConnectionTask2);
        OutboundConnectionTask oct2 = new ConnectionTaskFactoryImpl().createScheduled(shadow);
        List<OutboundConnectionTask> outboundConnectionTasks = new ConnectionTaskFactoryImpl().findOutboundByDeviceId(shadow.getDeviceId());

        // asserts
        assertThat(oct1).isNotNull();
        assertThat(oct2).isNotNull();
        assertThat(outboundConnectionTasks).hasSize(2);
        assertThat(outboundConnectionTasks).contains(oct1, oct2);
    }

    @Test
    public void updateWithDefaultWhenNoDefaultYetExistsTest() throws BusinessException, SQLException {
        OutboundConnectionTask task1 = this.createWithNoPropertiesWithoutViolations("updateWithDefaultWhenNoDefaultYetExistsTest", false);
        OutboundConnectionTaskShadow shadow = task1.getShadow();
        shadow.setPartialConnectionTask(partialScheduledConnectionTask2);
        OutboundConnectionTask task2 = new ConnectionTaskFactoryImpl().createScheduled(shadow);  // creating a new task with same settings, but based on different partial
        int deviceId = task1.getDevice().getId();

        // Business method
        List<OutboundConnectionTask> outboundByDeviceId = this.connectionTaskFactory.findOutboundByDeviceId(deviceId);
        ConnectionTask defaultConnectionTaskForDevice = this.connectionTaskFactory.findDefaultConnectionTaskForDevice(device);

        // prologue asserts
        assertThat(outboundByDeviceId).isNotNull();
        assertThat(outboundByDeviceId).isNotEmpty();
        assertThat(outboundByDeviceId).hasSize(2);
        assertThat(outboundByDeviceId).contains(task1, task2);
        assertThat(defaultConnectionTaskForDevice).isNull();

        // update to one task to the default task
        task2.setAsDefault();

        defaultConnectionTaskForDevice = this.connectionTaskFactory.findDefaultConnectionTaskForDevice(device);

        // asserts
        assertThat(defaultConnectionTaskForDevice).isNotNull();
        assertThat(defaultConnectionTaskForDevice).isEqualTo(task2);
    }

    @Test
    public void testApplyComWindowWhenTaskDoesNotHaveAComWindow() throws SQLException, BusinessException {
        Date nextExecutionTimestamp = Clocks.getAppServerClock().now();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithoutNextExecutionSpecs", null);

        // Business method
        Date modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(nextExecutionTimestamp);
    }

    @Test
    public void testApplyComWindowWithNextExecutionTimeStampThatImmediatelyFallsWithinComWindow() throws SQLException, BusinessException {
        this.toRestore = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampThatImmediatelyFallsWithinComWindow", FROM_ONE_AM_TO_TWO_AM);
        Date nextExecutionTimestamp = FrozenClock.frozenOn(2013, Calendar.JANUARY, 9, 1, 30, 0, 0).now();   // UTC

        // Business method
        Date modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts; 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(nextExecutionTimestamp);
    }

    @Test
    public void testApplyComWindowWithNextExecutionTimeStampBeforeComWindow() throws SQLException, BusinessException {
        this.toRestore = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampBeforeComWindow", FROM_ONE_AM_TO_TWO_AM);
        Date nextExecutionTimestamp = FrozenClock.frozenOn(2013, Calendar.JANUARY, 9, 0, 30, 0, 0).now();
        Date expectedModifiedNextExecutionTimestamp = FrozenClock.frozenOn(2013, Calendar.JANUARY, 9, 1, 0, 0, 0).now();

        // Business method
        Date modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts; 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(expectedModifiedNextExecutionTimestamp);
    }

    @Test
    public void testApplyComWindowWithNextExecutionTimeStampAfterComWindow() throws SQLException, BusinessException {
        this.toRestore = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampAfterComWindow", FROM_ONE_AM_TO_TWO_AM);
        Date nextExecutionTimestamp = FrozenClock.frozenOn(2013, Calendar.JANUARY, 9, 2, 30, 0, 0).now();
        Date expectedModifiedNextExecutionTimestamp = FrozenClock.frozenOn(2013, Calendar.JANUARY, 10, 1, 0, 0, 0).now();

        // Business method
        Date modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts: 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(expectedModifiedNextExecutionTimestamp);
    }

    @Test
    public void testAttemptLock() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLock";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        boolean lockResult = connectionTask.attemptLock(this.comServer);

        // Asserts
        assertThat(lockResult).isTrue();
        assertThat(connectionTask.getExecutingComServer()).isEqualTo(this.comServer);
    }

    @Test
    public void testUnlock() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLock";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);
        connectionTask.attemptLock(this.comServer);

        // Business method
        connectionTask.unlock();

        // Asserts
        assertThat(connectionTask.getExecutingComServer()).isNull();
    }

    @Test(expected = CodingException.class)
    public void testAttemptLockWithUnexpectedBusinessException() throws BusinessException, SQLException {
        doThrow(BusinessException.class).when(this.mdwInterface).execute(any(Transaction.class));
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLockWithUnexpectedBusinessException";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        connectionTask.attemptLock(this.comServer);
    }

    @Test(expected = PersistenceCodingException.class)
    public void testAttemptLockWithUnexpectedSqlException() throws BusinessException, SQLException {
        doThrow(SQLException.class).when(this.mdwInterface).execute(any(Transaction.class));
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLockWithUnexpectedSqlException";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        connectionTask.attemptLock(this.comServer);
    }

    @Test
    public void testAttemptLockInTransaction() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        String name = "testAttemptLockInTransaction";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        final AtomicBoolean lockResult = new AtomicBoolean();
        MeteringWarehouse.getCurrent().execute(new Transaction<Object>() {
            @Override
            public Object doExecute() {
                lockResult.set(connectionTask.attemptLock(comServer));
                return null;
            }
        });

        // Asserts
        assertThat(lockResult.get()).isTrue();
        assertThat(connectionTask.getExecutingComServer()).isEqualTo(this.comServer);
    }

    @Test
    public void testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLockWhenAlreadyLockedByTheSameComServer";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);
        connectionTask.attemptLock(this.comServer);

        // Business method
        boolean lockResult = connectionTask.attemptLock(this.comServer);

        // Asserts
        assertThat(lockResult).isFalse();
        assertThat(connectionTask.getExecutingComServer()).isEqualTo(this.comServer);
    }

    @Test
    public void testAttemptLockWillFailWhenAlreadyLockedByTheSameComServerInTransaction() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        String name = "testAttemptLockWhenAlreadyLockedByTheSameComServerInTransaction";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        final AtomicBoolean lockResult = new AtomicBoolean();
        MeteringWarehouse.getCurrent().execute(new Transaction<Object>() {
            @Override
            public Object doExecute() {
                connectionTask.attemptLock(comServer);
                lockResult.set(connectionTask.attemptLock(comServer));
                return null;
            }
        });

        // Asserts
        assertThat(lockResult.get()).isFalse();
        assertThat(connectionTask.getExecutingComServer()).isEqualTo(this.comServer);
    }

    @Test
    public void testAttemptLockWhenAlreadyLockedByAnotherComServer() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        ServerOnlineComServer otherComServer = mock(ServerOnlineComServer.class);

        String name = "testAttemptLockWhenAlreadyLockedByAnotherComServer";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);
        connectionTask.attemptLock(this.comServer);

        // Business method
        boolean lockResult = connectionTask.attemptLock(otherComServer);

        // Asserts
        assertThat(lockResult).isFalse();
        assertThat(connectionTask.getExecutingComServer()).isEqualTo(this.comServer);
    }

    @Test
    public void testAttemptLockWhenAlreadyLockedByAnotherComServerInTransaction() throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        final ServerOnlineComServer otherComServer = mock(ServerOnlineComServer.class);

        String name = "testAttemptLockWhenAlreadyLockedByAnotherComServerInTransaction";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);
        connectionTask.attemptLock(this.comServer);

        // Business method
        final AtomicBoolean lockResult = new AtomicBoolean();
        MeteringWarehouse.getCurrent().execute(new Transaction<Object>() {
            @Override
            public Object doExecute() {
                connectionTask.attemptLock(comServer);
                lockResult.set(connectionTask.attemptLock(otherComServer));
                return null;
            }
        });

        // Asserts
        assertThat(lockResult.get()).isFalse();
        assertThat(connectionTask.getExecutingComServer()).isEqualTo(this.comServer);
    }

    @Test
    public void pauseIfNotPausedTest() throws SQLException, BusinessException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        String name = "pauseIfNotPausedTest";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        assertThat(connectionTask.isPaused()).isFalse();

        // business method
        connectionTask.pause();
        final ConnectionTask reloadedConnectionTask = this.connectionTaskFactory.find(connectionTask.getId());

        assertThat(reloadedConnectionTask.isPaused()).isTrue();
    }

    @Test
    public void pauseWhenAlreadyPausedTest() throws SQLException, BusinessException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        String name = "pauseWhenAlreadyPausedTest";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // business method
        connectionTask.pause();
        connectionTask.pause();
        connectionTask.pause();
        final ConnectionTask reloadedConnectionTask = this.connectionTaskFactory.find(connectionTask.getId());

        assertThat(reloadedConnectionTask.isPaused()).isTrue();
    }

    @Test
    public void resumeWhenPausedTest() throws SQLException, BusinessException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        String name = "resumeWhenPausedTest";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // business method
        connectionTask.pause();
        ConnectionTask reloadedConnectionTask = this.connectionTaskFactory.find(connectionTask.getId());
        reloadedConnectionTask.resume();
        reloadedConnectionTask = this.connectionTaskFactory.find(connectionTask.getId());

        assertThat(reloadedConnectionTask.isPaused()).isFalse();
    }

    @Test
    public void resumeWhenAlreadyResumedTest() throws SQLException, BusinessException {
        this.mockMdwInterfaceTransactionExecutor();
        when(this.mdwInterface.isInTransaction()).thenReturn(true);
        String name = "resumeWhenAlreadyResumedTest";
        final ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // business method
        connectionTask.resume();
        ConnectionTask reloadedConnectionTask = this.connectionTaskFactory.find(connectionTask.getId());

        assertThat(reloadedConnectionTask.isPaused()).isFalse();
    }

    @Test
    public void testGetLastComSessionWithoutComSessions() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetLastComSessionWithoutComSessions(connectionTask);
    }

    @Test
    public void testGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    public void testGetLastComSessionWithoutComSessionsWithActiveComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    public void testGetLastComSessionWithComSessions() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithComSessions");
        this.doTestGetLastComSessionWithComSessions(connectionTask);
    }

    @Test
    public void testGetLastComSessionWithComSessionsWithActiveLastComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    public void testGetLastComSessionWithComSessionsWithActiveComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    public void testGetSuccessIndicatorWithoutComSessions() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    public void testGetSuccessIndicatorWithSuccessfulLastComSession() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, true);
    }

    @Test
    public void testGetSuccessIndicatorWithFailedLastComSession() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithFailedLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, false);
    }

    @Test
    public void testGetLastSuccessIndicatorWithoutComSessions() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithoutComSessions");
        this.doTestGetLastSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    public void testGetLastSuccessIndicatorWithSuccessfulLastComSession() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Success);
    }

    @Test
    public void testGetLastSuccessIndicatorWithSetupErrorLastComSession() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSetupErrorLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.SetupError);
    }

    @Test
    public void testGetLastSuccessIndicatorWithBrokenLastComSession() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithBrokenLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Broken);
    }

    @Test
    public void testGetLastTaskSummaryWithoutComSessions() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithoutComSessions(connectionTask);
    }

    @Test
    public void testGetLastTaskSummaryWithComSessions() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithComSessions(connectionTask, 4, 1, 1);
    }

    @Test
    public void testTriggerWithMinimizeStrategy() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn("testTriggerWithMinimizeStrategy");
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) new ConnectionTaskFactoryImpl().createScheduled(shadow);
        connectionTask.setAsDefault();
        Date triggerDate = FrozenClock.frozenOn(2013, Calendar.JUNE, 3).now();
        Pair<Date, Integer> earliestNextExecutionTimestampAndPriority = new Pair<>(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(connectionTask)).thenReturn(earliestNextExecutionTimestampAndPriority);

        // Business method
        connectionTask.trigger(triggerDate);

        // Asserts
        verify(this.comTaskExecutionFactory).synchronizeNextExecutionAndPriorityToMinimizeConnections(connectionTask, triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
    }

    @Test
    public void testTriggerWithAsapStrategyAndOnlyPendingTasks() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn("testTriggerWithAsapStrategy");
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) new ConnectionTaskFactoryImpl().createScheduled(shadow);
        connectionTask.setAsDefault();
        Date triggerDate = FrozenClock.frozenOn(2013, Calendar.JUNE, 3).now();
        Pair<Date, Integer> earliestNextExecutionTimestampAndPriority = new Pair<>(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
        ComTaskExecution ComTaskExecution1 = mock(ComTaskExecution.class);
        when(ComTaskExecution1.getStatus()).thenReturn(TaskStatus.Pending);
        ComTaskExecution ComTaskExecution2 = mock(ComTaskExecution.class);
        when(ComTaskExecution2.getStatus()).thenReturn(TaskStatus.Pending);
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).thenReturn(Arrays.asList(ComTaskExecution1, ComTaskExecution2));
        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(connectionTask)).thenReturn(earliestNextExecutionTimestampAndPriority);

        // Business method
        Date nextExecutionTimstamp = connectionTask.trigger(triggerDate);

        // Asserts
        verify(ComTaskExecution1).schedule(triggerDate);
        verify(ComTaskExecution2).schedule(triggerDate);
        assertThat(nextExecutionTimstamp).isEqualTo(triggerDate);
    }

    @Test
    public void testTriggerWithAsapStrategyAndOnlyOnHoldAndWaitingTasks() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn("testTriggerWithAsapStrategy");
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) new ConnectionTaskFactoryImpl().createScheduled(shadow);
        connectionTask.setAsDefault();
        Date triggerDate = FrozenClock.frozenOn(2013, Calendar.JUNE, 3).now();
        Pair<Date, Integer> earliestNextExecutionTimestampAndPriority = new Pair<>(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
        ComTaskExecution ComTaskExecution1 = mock(ComTaskExecution.class);
        when(ComTaskExecution1.getStatus()).thenReturn(TaskStatus.Waiting);
        ComTaskExecution ComTaskExecution2 = mock(ComTaskExecution.class);
        when(ComTaskExecution2.getStatus()).thenReturn(TaskStatus.OnHold);
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).thenReturn(Arrays.asList(ComTaskExecution1, ComTaskExecution2));
        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(connectionTask)).thenReturn(earliestNextExecutionTimestampAndPriority);

        // Business method
        Date nextExecutionTimstamp = connectionTask.trigger(triggerDate);

        // Asserts
        verify(ComTaskExecution1, never()).schedule(triggerDate);
        verify(ComTaskExecution2, never()).schedule(triggerDate);
        assertThat(nextExecutionTimstamp).isEqualTo(triggerDate);
    }

    @Test
    public void testTriggerWithAsapStrategyAllComTaskStatusses() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn("testTriggerWithAsapStrategy");
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) new ConnectionTaskFactoryImpl().createScheduled(shadow);
        connectionTask.setAsDefault();
        Date triggerDate = FrozenClock.frozenOn(2013, Calendar.JUNE, 3).now();
        Pair<Date, Integer> earliestNextExecutionTimestampAndPriority = new Pair<>(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
        ComTaskExecution neverCompleted = mock(ComTaskExecution.class);
        when(neverCompleted.getStatus()).thenReturn(TaskStatus.NeverCompleted);
        ComTaskExecution waiting = mock(ComTaskExecution.class);
        when(waiting.getStatus()).thenReturn(TaskStatus.Waiting);
        ComTaskExecution pending = mock(ComTaskExecution.class);
        when(pending.getStatus()).thenReturn(TaskStatus.Pending);
        ComTaskExecution busy = mock(ComTaskExecution.class);
        when(busy.getStatus()).thenReturn(TaskStatus.Busy);
        ComTaskExecution retrying = mock(ComTaskExecution.class);
        when(retrying.getStatus()).thenReturn(TaskStatus.Retrying);
        ComTaskExecution failed = mock(ComTaskExecution.class);
        when(failed.getStatus()).thenReturn(TaskStatus.Failed);
        ComTaskExecution onHold = mock(ComTaskExecution.class);
        when(onHold.getStatus()).thenReturn(TaskStatus.OnHold);
        when(this.comTaskExecutionFactory.findByConnectionTask(connectionTask)).
                thenReturn(Arrays.asList(
                        neverCompleted,
                        waiting,
                        pending,
                        busy,
                        retrying,
                        failed,
                        onHold));
        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(connectionTask)).thenReturn(earliestNextExecutionTimestampAndPriority);

        // Business method
        Date nextExecutionTimstamp = connectionTask.trigger(triggerDate);

        // Asserts
        verify(neverCompleted).schedule(triggerDate);
        verify(waiting, never()).schedule(triggerDate);
        verify(pending).schedule(triggerDate);
        verify(busy).schedule(triggerDate);
        verify(retrying).schedule(triggerDate);
        verify(failed).schedule(triggerDate);
        verify(onHold, never()).schedule(triggerDate);
        assertThat(nextExecutionTimstamp).isEqualTo(triggerDate);
    }

    @Test
    public void testSwitchFromOutboundDefault() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSwitchFromOutboundDefault", false);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(this.manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ServerInboundConnectionTask inboundDefault = mock(ServerInboundConnectionTask.class);
        when(inboundDefault.isDefault()).thenReturn(true);
        when(connectionTaskFactory.findByDevice(this.device)).thenReturn(Arrays.asList(connectionTask, inboundDefault));

        // Business method
        connectionTask.setAsDefault();

        // Asserts
        verify(inboundDefault).clearDefault();
        verify(this.manager).defaultConnectionTaskChanged(this.device, connectionTask);
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    public void testSetAsDefaultWithoutOtherDefaults() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSetAsDefaultWithoutOtherDefaults", false);

        // Business method
        connectionTask.setAsDefault();

        // Asserts
        verify(this.manager).defaultConnectionTaskChanged(this.device, connectionTask);
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    public void testSetAsDefaultWithoutMinimizeConnectionStrategy() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSetAsDefaultWithoutOtherDefaults", false, ConnectionStrategy.MINIMIZE_CONNECTIONS);
        Date comTaskNextExecutionTimeStamp = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 13).now();
        Pair<Date, Integer> earliestNextExecutionTimeStampAndPriority = new Pair<>(comTaskNextExecutionTimeStamp, 100);
        when(this.manager.defaultConnectionTaskChanged(this.device, connectionTask)).thenReturn(earliestNextExecutionTimeStampAndPriority);
        reset(this.comTaskExecutionFactory);

        // Business method
        connectionTask.setAsDefault();

        // Asserts
        verify(this.manager).defaultConnectionTaskChanged(this.device, connectionTask);
        verify(this.comTaskExecutionFactory).synchronizeNextExecutionAndPriorityToMinimizeConnections(connectionTask, connectionTask.getNextExecutionTimestamp(), 100);
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    public void testUnsetAsDefaultWithOtherConnectionTasks() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUnsetAsDefaultWithOtherConnectionTasks", true);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(this.manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ServerInboundConnectionTask inboundDefault = mock(ServerInboundConnectionTask.class);
        when(inboundDefault.isDefault()).thenReturn(false);
        ServerScheduledConnectionTask otherOutbound = mock(ServerScheduledConnectionTask.class);
        when(otherOutbound.isDefault()).thenReturn(false);
        when(connectionTaskFactory.findByDevice(this.device)).thenReturn(Arrays.asList(connectionTask, otherOutbound, inboundDefault));

        // Business method
        connectionTask.unsetAsDefault();

        // Asserts
        verify(this.manager).defaultConnectionTaskChanged(this.device, null);
        assertThat(connectionTask.isDefault()).isFalse();
    }

    @Test
    public void testUnsetAsDefaultWithoutOtherDefaults() throws SQLException, BusinessException {
        OutboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUnsetAsDefaultWithoutOtherDefaults", true);

        // Business method
        connectionTask.unsetAsDefault();

        // Asserts
        verify(this.manager).defaultConnectionTaskChanged(this.device, null);
        assertThat(connectionTask.isDefault()).isFalse();
    }

    private ScheduledConnectionTask createWithNoPropertiesWithoutViolations(String name) {
        return createWithNoPropertiesWithoutViolations(name, true);
    }

    private ScheduledConnectionTask createWithNoPropertiesWithoutViolations(String name, boolean defaultState) {
        return this.createWithNoPropertiesWithoutViolations(name, defaultState, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }

    private ScheduledConnectionTask createWithNoPropertiesWithoutViolations(String name, boolean defaultState, ConnectionStrategy connectionStrategy) {
        ScheduledConnectionTask connectionTask;
        when(this.partialScheduledConnectionTask.getName()).thenReturn(name);
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(connectionStrategy)) {
            connectionTask = deviceDataService.newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        }
        else {
            connectionTask = deviceDataService.
                    newMinimizeConnectionTask(
                            this.device,
                            this.partialScheduledConnectionTask,
                            outboundTcpipComPortPool,
                            new TemporalExpression(EVERY_HOUR));
        }

        if (defaultState) {
            deviceDataService.setDefaultConnectionTask(connectionTask);
        }
        return connectionTask;
    }

    private OutboundConnectionTask createWithCommunicationWindowWithoutViolations(String name) throws BusinessException, SQLException {
        return this.createWithCommunicationWindowWithoutViolations(name, FROM_ONE_AM_TO_TWO_AM);
    }

    private OutboundConnectionTask createWithCommunicationWindowWithoutViolations(String name, ComWindow comWindow) throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn(name);
        shadow.setCommunicationWindow(comWindow);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private OutboundConnectionTask createWithoutConnectionStrategy(String name) throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn(name);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(null);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private OutboundConnectionTaskShadow createWithoutNextExecutionSpecs(ConnectionStrategy connectionStrategy, String name) {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        when(this.partialOutboundConnectionTask1.getName()).thenReturn(name);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(connectionStrategy);
        shadow.setNextExecutionSpecs(null);
        return shadow;
    }

    private OutboundConnectionTask createWithoutDevice() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private OutboundConnectionTask createWithNonExistingDevice() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialOutboundConnectionTask1);
        shadow.setDeviceId(NON_EXISTING_DEVICE_ID);
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private void mockMdwInterfaceTransactionExecutor() throws BusinessException, SQLException {
        when(this.mdwInterface.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                return transaction.doExecute();
            }
        });
    }

}
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
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
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.ComTaskExecutionFactory;
import com.energyict.mdc.device.data.PartialConnectionTaskFactory;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp() throws BusinessException, SQLException {
        ComTaskExecutionFactory comTaskExecutionFactory = mock(ComTaskExecutionFactory.class);
        when(inMemoryPersistence.getApplicationContext().getModulesImplementing(ComTaskExecutionFactory.class)).thenReturn(Arrays.asList(comTaskExecutionFactory));
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        List<ComTaskExecution> comTasksThatRelyOnTheDefault = new ArrayList<>(1);
        comTasksThatRelyOnTheDefault.add(comTask);
        when(comTaskExecutionFactory.findComTaskExecutionsForDefaultOutboundConnectionTask(this.device)).thenReturn(comTasksThatRelyOnTheDefault);
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp", false);
        Date earliestNextExecutionTimestamp = new DateMidnight(2013, 2, 14).toDate();
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(earliestNextExecutionTimestamp, TaskPriorityConstants.DEFAULT_PRIORITY);
        //when(this.manager.defaultConnectionTaskChanged(this.device, connectionTask)).thenReturn(earliestNextExecutionTimestampAndPriority);

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        verify(comTask).updateToUseDefaultConnectionTask(connectionTask);
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(earliestNextExecutionTimestamp);
    }

    @Test
    @Transactional
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
            deviceDataService.newAsapConnectionTask(this.device, partialScheduledConnectionTask, outboundTcpipComPortPool);
        }
        else {
            deviceDataService.
                    newMinimizeConnectionTask(
                            this.device,
                            partialScheduledConnectionTask,
                            outboundTcpipComPortPool,
                            new TemporalExpression(EVERY_HOUR));
        }

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testPause() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testPause");

        // Business method
        connectionTask.pause();

        // Asserts
        assertThat(connectionTask.isPaused()).isTrue();
    }

    @Test(expected = DuplicateException.class)
    @Transactional
    public void testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask() throws BusinessException, SQLException {
        ScheduledConnectionTask firstTask = this.createWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask");
        firstTask.save();
        ScheduledConnectionTask secondTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);

        // Business method
        secondTask.save();

        // Asserts
        //assertThat(e.getMessageId()).isEqualTo("duplicateConnectionTaskX");
    }

    @Test
    @Transactional
    public void testCreateSecondTaskAgainstTheSameDevice() throws BusinessException, SQLException {
        ScheduledConnectionTaskImpl firstTask = (ScheduledConnectionTaskImpl) this.createWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDevice-1");

        when(this.partialScheduledConnectionTask2.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask2.getName()).thenReturn("testCreateSecondTaskAgainstTheSameDevice-2");
        ScheduledConnectionTaskImpl secondTask = (ScheduledConnectionTaskImpl) this.createWithNoPropertiesWithoutViolations(this.partialScheduledConnectionTask2, false, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        this.addIpConnectionProperties(secondTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        secondTask.save();

        // Both tasks should be successfully created with each a separate ConnectionMethod
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
        ConnectionMethod firstTaskConnectionMethod = firstTask.getConnectionMethod();
        ConnectionMethod secondTaskConnectionMethod = secondTask.getConnectionMethod();
        assertThat(firstTaskConnectionMethod).isNotNull();
        assertThat(secondTaskConnectionMethod).isNotNull();
        assertThat(firstTaskConnectionMethod.getId()).isNotEqualTo(secondTaskConnectionMethod.getId());
    }

    @Test
    @Transactional
    public void testCreateAgainstAnotherDeviceBasedOnTheSamePartialConnectionTask() throws BusinessException, SQLException {
        ScheduledConnectionTask firstTask = this.createWithNoPropertiesWithoutViolations("testCreateAgainstAnotherDeviceBasedOnSamePartialConnectionTask-1");
        ScheduledConnectionTask secondTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.otherDevice, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(secondTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        secondTask.save();

        // Both tasks should be successfully created with the same name
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
        assertThat(secondTask.getName()).isEqualTo(firstTask.getName());
    }

    @Test(expected = BusinessException.class)
    @Transactional
    public void testCreateMinimizeConnectionsWithSimultaneous() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreateMinimizeConnectionsWithSimultaneous", false, ConnectionStrategy.MINIMIZE_CONNECTIONS);
        connectionTask.setSimultaneousConnectionsAllowed(true);

        // Business method
        connectionTask.save();

        // Asserts
        // assertThat(e.getMessageId()).isEqualTo("simultaneousConnectionsNotAllowedForMinimizeConnectionStrategy");
    }

    @Test
    @Transactional
    public void testCreateWithAllIpProperties() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod).isNotNull();
        ComPortPool comPortPool = connectionMethod.getComPortPool();
        assertThat(comPortPool).isNotNull();
        assertThat(comPortPool.getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndNoDefaultsOnPluggableClass() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod.getComPortPool()).isNotNull();
        assertThat(connectionMethod.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(1);   // Only 1 property is locally defined and higher levels do not specify any property values
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(1);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndSomeDefaultsOnPluggableClass() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME));
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME));
        ipConnectionTypePluggableClass.save();

        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(allIpPropertySpecs)));
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, outboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod.getComPortPool()).isNotNull();
        assertThat(connectionMethod.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);   // 1 property is locally defined, 1 is inherited and the third is not specified at any level
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClass() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(allIpPropertySpecs)));
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        // Do not set any properties on the ScheduledConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod.getComPortPool()).isNotNull();
        assertThat(connectionMethod.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // no properties are locally defined, all 3 are inherited from the connection type pluggable class
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClassAndPartialConnectionTask() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        TypedProperties partialConnectionTaskProperties = TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs()));
        partialConnectionTaskProperties.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskProperties.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(partialConnectionTaskProperties);
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        // Do not set any properties on the ScheduledConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod.getComPortPool()).isNotNull();
        assertThat(connectionMethod.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // 2 properties are inherited from the partial connection task and 1 is inherited from the connection type pluggable class
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromPartialConnectionTask() throws BusinessException, SQLException {
        TypedProperties partialConnectionTaskTypedProperties = TypedProperties.empty();
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(partialConnectionTaskTypedProperties);
        PartialConnectionTaskProperty partialIpAddressProperty = mock(PartialConnectionTaskProperty.class);
        when(partialIpAddressProperty.getName()).thenReturn(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        when(partialIpAddressProperty.getValue()).thenReturn(IP_ADDRESS_PROPERTY_VALUE);
        when(partialIpAddressProperty.getPartialConnectionTask()).thenReturn(this.partialScheduledConnectionTask);
        PartialConnectionTaskProperty partialPortProperty = mock(PartialConnectionTaskProperty.class);
        when(partialPortProperty.getName()).thenReturn(IpConnectionType.PORT_PROPERTY_NAME);
        when(partialPortProperty.getValue()).thenReturn(PORT_PROPERTY_VALUE);
        when(partialPortProperty.getPartialConnectionTask()).thenReturn(this.partialScheduledConnectionTask);
        PartialConnectionTaskProperty partialCodeTableProperty = mock(PartialConnectionTaskProperty.class);
        when(partialCodeTableProperty.getName()).thenReturn(IpConnectionType.CODE_TABLE_PROPERTY_NAME);
        when(partialCodeTableProperty.getValue()).thenReturn(codeTable);
        when(partialCodeTableProperty.getPartialConnectionTask()).thenReturn(this.partialScheduledConnectionTask);
        List<PartialConnectionTaskProperty> partialConnectionTaskProperties = Arrays.asList(partialIpAddressProperty, partialPortProperty, partialCodeTableProperty);
        when(this.partialScheduledConnectionTask.getProperties()).thenReturn(partialConnectionTaskProperties);
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        // Do not set any properties on the ScheduledConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod.getComPortPool()).isNotNull();
        assertThat(connectionMethod.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // no properties are locally defined, all 3 are inherited from the partial connection task
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateIpConnectionTypeProperty() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();

        connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(3);  // Ip is default and has 3 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    @Transactional
    public void testAddIpConnectionTypeProperty() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, codeTable);
        connectionTask.save();

        connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(3);  // Ip is default and has 3 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    @Transactional
    public void testRemoveIpConnectionTypeProperty() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();

        connectionTask.removeProperty(IpConnectionType.PORT_PROPERTY_NAME);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(2);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    @Transactional
    public void testReturnToInheritedProperty() throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME));
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), UPDATED_PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME));
        ipConnectionTypePluggableClass.save();

        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs())));
        ScheduledConnectionTask connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();

        connectionTask.removeProperty(IpConnectionType.PORT_PROPERTY_NAME);

        // Business method
        connectionTask.save();

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
    @Transactional
    public void testCreateWithIpWithModemComPortPool() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        ScheduledConnectionTask connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundModemComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Expected BusinessException because the ComPortType of the ComPortPool is not supported by the ConnectionType
        //assertThat(e.getMessageId()).isEqualTo("comPortTypeXOfComPortPoolYIsNotSupportedByConnectionTypePluggableClassZ");
    }

    @Test
    @Transactional
    public void testGetPropertiesOnMultipleDates() throws BusinessException, SQLException {
        // Create task with properties on may first 2011
        freezeClock(2011, Calendar.MAY, 1);
        Date mayFirst2011 = clock.now();
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        ScheduledConnectionTask connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, null);
        connectionTask.save();

        freezeClock(2012, Calendar.MAY, 1);
        connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);

        // Business method
        connectionTask.save();

        // Asserts
        List<ConnectionTaskProperty> allPropertiesOnMayFirst2011 = connectionTask.getProperties(mayFirst2011);
        assertThat(allPropertiesOnMayFirst2011).hasSize(1); // On May 1st, 2011 only the ip address was specified
        ConnectionTaskProperty property = allPropertiesOnMayFirst2011.get(0);
        assertThat(property.getName()).isEqualTo(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        assertThat(property.getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testGetPropertiesOnMultipleDatesAfterReload() throws BusinessException, SQLException {
        // Create task with properties on may first 2011
        freezeClock(2011, Calendar.MAY, 1);
        Date mayFirst2011 = clock.now();
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        ScheduledConnectionTask connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, null);
        connectionTask.save();

        freezeClock(2012, Calendar.MAY, 1);
        connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        connectionTask.save();

        // Business method
        ScheduledConnectionTask reloaded = (ScheduledConnectionTask) inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId());

        // Asserts
        List<ConnectionTaskProperty> allPropertiesOnMayFirst2011 = reloaded.getProperties(mayFirst2011);
        assertThat(allPropertiesOnMayFirst2011).hasSize(1); // On May 1st, 2011 only the ip address was specified
        ConnectionTaskProperty property = allPropertiesOnMayFirst2011.get(0);
        assertThat(property.getName()).isEqualTo(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        assertThat(property.getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY + "}")
    public void testCreateWithMissingRequiredProperty() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialScheduledConnectionTask.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs())));
        ScheduledConnectionTask connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, null, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY + "}")
    public void testCreateWithNonExistingProperty() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        ScheduledConnectionTask connectionTask = inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.addIpConnectionProperties(connectionTask, null, PORT_PROPERTY_VALUE, codeTable);
        // Add values for non existing property
        connectionTask.setProperty("doesNotExist", "I don't care");

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testCreateWithCommunicationWindowWithoutViolations() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithCommunicationWindowWithoutViolations("testCreateWithCommunicationWindowWithoutViolations");

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getCommunicationWindow()).isNotNull();
        assertThat(connectionTask.getProperties().isEmpty()).as("Was not expecting any properties on the Outbound Connection Task").isTrue();
    }

    @Test(expected = BusinessException.class)
    @Transactional
    public void createWithoutOffsetAndCommunicationWindow() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask =
                inMemoryPersistence.getDeviceDataService().
                        newMinimizeConnectionTask(
                                this.device,
                                this.partialScheduledConnectionTask,
                                outboundTcpipComPortPool,
                                new TemporalExpression(EVERY_DAY));
        connectionTask.setComWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        connectionTask.save();

        // Expecting a BusinessException because the offset is outside the communication window.
        //assertThat(e.getMessageId()).isEqualTo("OffsetXIsNotWithinComWindowY");
    }

    @Test(expected = BusinessException.class)
    @Transactional
    public void createWithOffsetWithinDayButOutsideCommunicationWindow() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask =
                inMemoryPersistence.getDeviceDataService().
                        newMinimizeConnectionTask(
                                this.device,
                                this.partialScheduledConnectionTask,
                                outboundTcpipComPortPool,
                                new TemporalExpression(
                                        EVERY_DAY,
                                        new TimeDuration(12, TimeDuration.HOURS))
                        );

        // Business method
        connectionTask.save();

        // Expecting a BusinessException because the offset is outside the communication window.
        // assertThat(e.getMessageId()).isEqualTo("OffsetXIsNotWithinComWindowY");
    }

    @Test
    @Transactional
    public void createWithOffsetOutsideDayButWithinCommunicationWindow() throws BusinessException, SQLException {
        String name = "createWithOffsetOutsideDayButWithinCommunicationWindow";
        when(this.partialScheduledConnectionTask.getName()).thenReturn(name);    // name will be inherited from the partial connectionTask
        // Set it to execute every week, at 01:30 (am) of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_HOUR * 25 + DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.SECONDS);
        ScheduledConnectionTask connectionTask =
                inMemoryPersistence.getDeviceDataService().
                        newMinimizeConnectionTask(
                                this.device,
                                this.partialScheduledConnectionTask,
                                outboundTcpipComPortPool,
                                new TemporalExpression(frequency, offset));

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getName()).isEqualTo(name);
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getEvery()).isEqualTo(frequency);
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getOffset()).isEqualTo(offset);
    }

    @Test(expected = BusinessException.class)
    @Transactional
    public void createWithOffsetOutsideDayAndOutsideCommunicationWindow() throws BusinessException, SQLException {
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_HOUR * 24 + DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.SECONDS);
        ScheduledConnectionTask connectionTask =
                inMemoryPersistence.getDeviceDataService().
                        newMinimizeConnectionTask(
                                this.device,
                                this.partialScheduledConnectionTask,
                                outboundTcpipComPortPool,
                                new TemporalExpression(frequency, offset));

        // Business method
        connectionTask.save();

        // Expecting a BusinessException because the offset is outside the communication window.
        // assertThat(e.getMessageId()).isEqualTo("LongOffsetXIsNotWithinComWindowY");
    }

    @Test
    @Transactional
    // Todo (JP-1125): Enable when ComTaskExecution has been moved to this bundle
    @Ignore
    public void testCreateWithExistingComPorts() throws BusinessException, SQLException {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.add(mock(ComTaskExecution.class));
        comTaskExecutions.add(mock(ComTaskExecution.class));
        ComTaskExecutionFactory comTaskExecutionFactory = mock(ComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.findComTaskExecutionsByTopology(this.device)).thenReturn(comTaskExecutions);
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCreateWithExistingComPorts");

        // Asserts
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            verify(comTaskExecution).connectionTaskCreated(this.device, connectionTask);
        }
    }

    @Test
    @Transactional
    public void createWithComTaskUsingDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        freezeClock(2013, Calendar.FEBRUARY, 1);
        Date febFirst = clock.now();
        Date nextExecutionTimeStamp = febFirst;

        ScheduledConnectionTask defaultConnectionTask = this.createWithNoPropertiesWithoutViolations("createWithComTaskUsingDefaultTestNextExecutionTimeStamp", true);
        ComTaskExecutionFactory comTaskExecutionFactory = mock(ComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(Matchers.<ScheduledConnectionTask>any())).thenReturn(new EarliestNextExecutionTimeStampAndPriority(nextExecutionTimeStamp, 100));

        // asserts
        assertThat(defaultConnectionTask.getNextExecutionTimestamp()).isEqualTo(nextExecutionTimeStamp);
    }

    @Test
    @Transactional
    public void updateToDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        freezeClock(2013, Calendar.FEBRUARY, 13, 10, 53, 20, 0);
        Date febFirst = clock.now();

        freezeClock(2013, Calendar.FEBRUARY, 13, 11, 0, 0, 0);   // 1 hour later according to the executionSpec
        FrozenClock nextConnectionTaskCalculated = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 13, 11, 0, 0, 0);   // 1 hour later according to the executionSpec
        ScheduledConnectionTask notDefaultConnectionTask = this.createWithNoPropertiesWithoutViolations("testNotDefaultYetNextExec", false);
        ScheduledConnectionTask reloaded = (ScheduledConnectionTask) this.connectionTaskFactory.find(notDefaultConnectionTask.getId());
        Date comTaskNextExecutionTimeStamp = FrozenClock.frozenOn(2013, Calendar.FEBRUARY, 13).now();
        Pair<Date, Integer> earliestNextExecutionTimeStampAndPriority = new Pair<>(comTaskNextExecutionTimeStamp, 100);
        when(this.comTaskExecutionFactory.getEarliestNextExecutionTimeStampAndPriority(Matchers.<ScheduledConnectionTask>any())).thenReturn(earliestNextExecutionTimeStampAndPriority);
        when(this.manager.defaultConnectionTaskChanged(this.device, notDefaultConnectionTask)).thenReturn(earliestNextExecutionTimeStampAndPriority);

        // Assert
        assertThat(reloaded.getNextExecutionTimestamp()).isNotNull();
        assertThat(reloaded.getNextExecutionTimestamp()).isEqualTo(nextConnectionTaskCalculated.now());

        // Business method
        reloaded.setAsDefault();
        ScheduledConnectionTask secondReload = (ScheduledConnectionTask) this.connectionTaskFactory.find(reloaded.getId());

        // Asserts after update
        assertThat(secondReload.getNextExecutionTimestamp()).isNotEqualTo(nextConnectionTaskCalculated.now());
        assertThat(secondReload.getNextExecutionTimestamp()).isEqualTo(comTaskNextExecutionTimeStamp);
    }

    @Test(expected = InvalidValueException.class)
    @Transactional
    public void testCreateWithoutConnectionStrategy() throws BusinessException, SQLException {
        this.createWithoutConnectionStrategy("createWithoutConnectionStrategy");

        // Nothing to assert as we are expecting an InvalidValueException
    }

    @Test
    @Transactional
    public void testCreateWithAsSoonAsPossibleButWithoutNextExecutionSpecs() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = this.createWithoutNextExecutionSpecs(ConnectionStrategy.AS_SOON_AS_POSSIBLE, "testCreateWithAsSoonAsPossibleButWithoutNextExecutionSpecs");
        this.setIpComPortPool(shadow);

        // Business method
        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNull();
    }

    @Test(expected = InvalidValueException.class)
    @Transactional
    public void testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = this.createWithoutNextExecutionSpecs(ConnectionStrategy.MINIMIZE_CONNECTIONS, "testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs");
        this.setIpComPortPool(shadow);

        // Busines method
        new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Nothing to assert as we are expecting an InvalidValueException
    }

    @Test(expected = InvalidValueException.class)
    @Transactional
    public void testCreateWithoutDevice() throws BusinessException, SQLException {
        this.createWithoutDevice();

        // Nothing to assert as we are expecting an InvalidValueException
    }

    @Test(expected = InvalidReferenceException.class)
    @Transactional
    public void testCreateWithNonExistingDevice() throws BusinessException, SQLException {
        this.createWithNonExistingDevice();

        // Nothing to assert as we are expecting an InvalidReferenceException
    }

    @Test(expected = InvalidReferenceException.class)
    @Transactional
    public void testCreateWithNonExistingComPortPool() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
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
    @Transactional
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
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
    @Transactional
    public void testCreateWithNonExistingInitiator() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
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
    @Transactional
    public void testGetShadowWithMinimumProperties() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetShadowWithMinimumProperties");

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
    @Transactional
    public void testAllowSimultaneousConnections() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        shadow.setAllowSimultaneousConnections(true);
        this.setIpComPortPool(shadow);

        // First one - allow simultaneous connections
        ScheduledConnectionTask outboundTrue = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // second one - deny simultaneous connections
        shadow.setAllowSimultaneousConnections(false);
        shadow.setPartialConnectionTask(partialScheduledConnectionTask2);
        ScheduledConnectionTask outboundFalse = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        assertTrue(outboundTrue.isSimultaneousConnectionsAllowed());
        assertFalse(outboundFalse.isSimultaneousConnectionsAllowed());
    }

    @Test
    @Transactional
    public void testLoad() throws BusinessException, SQLException {
        ScheduledConnectionTask created = this.createWithNoPropertiesWithoutViolations("testLoad");

        // Business method
        ScheduledConnectionTask loaded = (ScheduledConnectionTask) this.connectionTaskFactory.find(created.getId());

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
    @Transactional
    public void testGetShadow() throws BusinessException, SQLException {
        ScheduledConnectionTask created = this.createWithNoPropertiesWithoutViolations("testGetShadow");

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
    @Transactional
    public void testUpdate() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUpdate");
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
        ScheduledConnectionTask updated = (ScheduledConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.MINIMIZE_CONNECTIONS, updated.getConnectionStrategy());
        assertEquals(FROM_TEN_PM_TO_TWO_AM, updated.getCommunicationWindow());
        assertEquals(EVERY_DAY, updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
        assertEquals(elevenPM, updated.getNextExecutionSpecs().getTemporalExpression().getOffset());
        assertThat(updated.getNextExecutionSpecs().getDialCalendar()).isNull();
    }

    @Test
    @Transactional
    public void testUpdateWithDialCalendar() throws BusinessException, SQLException {
        int DIALCALENDAR_ID = 105;
        DialCalendar dialCalendar = mock(DialCalendar.class);
        when(dialCalendar.getId()).thenReturn(DIALCALENDAR_ID);
        when(dialCalendar.getNextDate(any(Date.class))).thenReturn(Clocks.getAppServerClock().now());
        DialCalendarFactory dialCalendarFactory = mock(DialCalendarFactory.class);
        when(dialCalendarFactory.find(DIALCALENDAR_ID)).thenReturn(dialCalendar);
        when(this.mdwInterface.getDialCalendarFactory()).thenReturn(dialCalendarFactory);

        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUpdate");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setCommunicationWindow(FROM_TEN_PM_TO_TWO_AM);
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setDialCalendarId(DIALCALENDAR_ID);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);

        // Business method
        connectionTask.update(shadow);
        ScheduledConnectionTask updated = (ScheduledConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.MINIMIZE_CONNECTIONS, updated.getConnectionStrategy());
        assertEquals(FROM_TEN_PM_TO_TWO_AM, updated.getCommunicationWindow());
        assertThat(updated.getNextExecutionSpecs().getTemporalExpression()).isNull(); // EISERVERSG-1888
        assertEquals(dialCalendar, updated.getNextExecutionSpecs().getDialCalendar());
    }

    @Test
    @Transactional
    public void testSwitchToAsapStrategyAndRemoveNextExecSpec() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow creationShadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        creationShadow.setDeviceId(this.nextDeviceId());
        creationShadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecs = new NextExecutionSpecsShadow();
        nextExecutionSpecs.setFrequency(EVERY_HOUR);
        creationShadow.setNextExecutionSpecs(nextExecutionSpecs);
        this.setIpComPortPool(creationShadow);
        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(creationShadow);
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        shadow.setNextExecutionSpecs(null);

        // Business method
        connectionTask.update(shadow);
        ScheduledConnectionTask updated = (ScheduledConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.AS_SOON_AS_POSSIBLE, updated.getConnectionStrategy());
        assertThat(updated.getNextExecutionSpecs()).isNull();
    }

    @Test
    @Transactional
    public void testSwitchToMinimizeConnectionStrategy() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSwitchToMinimizeConnectionStrategy");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        shadow.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        NextExecutionSpecsShadow nextExecutionSpecs = new NextExecutionSpecsShadow();
        nextExecutionSpecs.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecs);

        // Business method
        connectionTask.update(shadow);
        ScheduledConnectionTask updated = (ScheduledConnectionTask) this.connectionTaskFactory.find(connectionTask.getId());

        // Asserts
        assertEquals(ConnectionStrategy.MINIMIZE_CONNECTIONS, updated.getConnectionStrategy());
        assertThat(updated.getNextExecutionSpecs()).isNotNull();
        assertEquals(EVERY_HOUR, updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
    }

    @Test
    @Transactional
    public void testScheduleNow() throws BusinessException, SQLException {
        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);
        Clocks.setAppServerClock(mayLast2012);

        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testScheduleNow");

        // Business method
        connectionTask.scheduleNow();

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(Clocks.getAppServerClock().now());
    }

    @Test
    @Transactional
    public void testScheduleOnDate() throws BusinessException, SQLException {
        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);
        Clocks.setAppServerClock(mayLast2012);

        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testScheduleOnDate");

        // Business method
        connectionTask.schedule(Clocks.getAppServerClock().now());

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(Clocks.getAppServerClock().now());
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionTimestampForUTCDevice() throws BusinessException, SQLException {
        FrozenClock mayLast2012 = FrozenClock.frozenOn(2012, Calendar.MAY, 31);
        Date expectedNextExecutionTimestamp = FrozenClock.frozenOn(2012, Calendar.MAY, 31, 1, 0, 0, 0).now();     // Frequency of rescheduling is 1 hour
        Clocks.setAppServerClock(mayLast2012);
//        when(this.device.getDeviceTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
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

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
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

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
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

        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testDeleteWithNoProperties() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testDeleteWithNoProperties");
        int id = connectionTask.getId();
        int nextExecutionSpecsId = connectionTask.getNextExecutionSpecs().getId();

        // Business method
        connectionTask.delete();

        // Asserts
        assertThat(this.connectionTaskFactory.find(id)).isNull();
        assertThat(new NextExecutionSpecsFactory().find(nextExecutionSpecsId)).isNull();
    }

    @Test
    @Transactional
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
    @Transactional
    public void makeObsoleteWhenComServerIsExecutingTest() throws SQLException, BusinessException {
        mockMdwInterfaceTransactionExecutor();
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("makeObsoleteWhenComServerIsExecutingByComPortCheckTest");
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
    @Transactional
    public void testMakeObsoleteWithNoProperties() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteWithNoProperties");

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
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
    @Transactional
    public void testIsObsoleteAfterReload() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testIsObsoleteAfterReload");
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
    @Transactional
    public void testUpdateAfterMakeObsolete() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUpdateAfterMakeObsolete");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();
        connectionTask.makeObsolete();

        // Business method
        connectionTask.update(shadow);

        // Expected BusinessException because updates are not allowed on obsolete ConnectionTasks
    }

    public void testMakeObsoleteLogsAuditTrail() throws BusinessException, SQLException {
        this.enableAuditTrailing();
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteTwice");
        OutboundConnectionTaskShadow shadow = connectionTask.getShadow();

        // Business method
        connectionTask.makeObsolete();

        verify(this.getAuditTrailFactory()).create(shadow, this.connectionTaskFactory.getId(), AuditTrail.ACTION_DELETE);
    }

    @Test(expected = BusinessException.class)
    @Transactional
    public void testMakeObsoleteTwice() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteTwice");
        connectionTask.makeObsolete();

        // Business method
        connectionTask.makeObsolete();

        // Expected BusinessException because a ConnectionTask cannot be made obsolete twice
    }

    @Test(expected = BusinessException.class)
    @Transactional
    public void makeObsoleteWhenSomeOneElseMadeItObsoleteTest() throws SQLException, BusinessException {
        final ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("makeObsoleteWhenSomeOneElseMadeItObsoleteTest");
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
                }
                catch (SQLException ex) {
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
    @Transactional
    public void testMakeObsoleteWithActiveComTasks() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testMakeObsoleteWithActiveComTasks");
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
    @Transactional
    public void testCannotDeleteDefaultTaskThatIsInUse() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testCannotDeleteDefaultTaskThatIsInUse");
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
    @Transactional
    public void testDeletedAndSetComTaskToNoConnectionTask() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testDeletedAndSetComTaskToNoConnectionTask", false);
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
    @Transactional
    public void testDeleteWithComSessions() throws BusinessException, SQLException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testDeleteWithComSessions");
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
    @Transactional
    public void testFindOutboundByDeviceAfterDelete() throws BusinessException, SQLException {
        when(this.partialScheduledConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.addIpConnectionProperties(shadow, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        ScheduledConnectionTask connectionTask = new ConnectionTaskFactoryImpl().createScheduled(shadow);
        List<ScheduledConnectionTask> outboundByDeviceBeforeDelete = connectionTaskFactory.findOutboundByDevice(connectionTask.getDevice());

        // Business methods
        connectionTask.delete();
        List<ScheduledConnectionTask> outboundByDeviceAfterDelete = connectionTaskFactory.findOutboundByDevice(connectionTask.getDevice());

        // Asserts
        assertThat(outboundByDeviceBeforeDelete).contains(connectionTask);
        assertThat(outboundByDeviceAfterDelete).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteWithDependentConnectionMethod() throws BusinessException, SQLException {
        ScheduledConnectionTask outboundConnectionTask = this.createWithNoPropertiesWithoutViolations("testDeleteWithDependentConnectionMethod");
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
    @Transactional
    public void createMultipleOutboundsForSpecificDeviceWithoutViolations() throws BusinessException, SQLException {
        ScheduledConnectionTask oct1 = this.createWithNoPropertiesWithoutViolations("createMultipleOutboundsForSpecificDeviceWithoutViolations");
        OutboundConnectionTaskShadow shadow = oct1.getShadow();
        shadow.setPartialConnectionTask(partialScheduledConnectionTask2);
        ScheduledConnectionTask oct2 = new ConnectionTaskFactoryImpl().createScheduled(shadow);
        List<ScheduledConnectionTask> outboundConnectionTasks = new ConnectionTaskFactoryImpl().findOutboundByDeviceId(shadow.getDeviceId());

        // asserts
        assertThat(oct1).isNotNull();
        assertThat(oct2).isNotNull();
        assertThat(outboundConnectionTasks).hasSize(2);
        assertThat(outboundConnectionTasks).contains(oct1, oct2);
    }

    @Test
    @Transactional
    public void updateWithDefaultWhenNoDefaultYetExistsTest() throws BusinessException, SQLException {
        ScheduledConnectionTask task1 = this.createWithNoPropertiesWithoutViolations("updateWithDefaultWhenNoDefaultYetExistsTest", false);
        OutboundConnectionTaskShadow shadow = task1.getShadow();
        shadow.setPartialConnectionTask(partialScheduledConnectionTask2);
        ScheduledConnectionTask task2 = new ConnectionTaskFactoryImpl().createScheduled(shadow);  // creating a new task with same settings, but based on different partial
        int deviceId = task1.getDevice().getId();

        // Business method
        List<ScheduledConnectionTask> outboundByDeviceId = this.connectionTaskFactory.findOutboundByDeviceId(deviceId);
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
    @Transactional
    public void testApplyComWindowWhenTaskDoesNotHaveAComWindow() throws SQLException, BusinessException {
        Date nextExecutionTimestamp = Clocks.getAppServerClock().now();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithoutNextExecutionSpecs", null);

        // Business method
        Date modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(nextExecutionTimestamp);
    }

    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void testAttemptLockWithUnexpectedBusinessException() throws BusinessException, SQLException {
        doThrow(BusinessException.class).when(this.mdwInterface).execute(any(Transaction.class));
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLockWithUnexpectedBusinessException";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        connectionTask.attemptLock(this.comServer);
    }

    @Test(expected = PersistenceCodingException.class)
    @Transactional
    public void testAttemptLockWithUnexpectedSqlException() throws BusinessException, SQLException {
        doThrow(SQLException.class).when(this.mdwInterface).execute(any(Transaction.class));
        when(this.mdwInterface.isInTransaction()).thenReturn(false);
        String name = "testAttemptLockWithUnexpectedSqlException";
        ServerScheduledConnectionTask connectionTask = (ServerScheduledConnectionTask) this.createWithNoPropertiesWithoutViolations(name);

        // Business method
        connectionTask.attemptLock(this.comServer);
    }

    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void testGetLastComSessionWithoutComSessions() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetLastComSessionWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessionsWithActiveComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessions() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithComSessions");
        this.doTestGetLastComSessionWithComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessionsWithActiveLastComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessionsWithActiveComSessionCache() throws SQLException, BusinessException {
        ServerConnectionTask connectionTask = (ServerConnectionTask) this.createWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithoutComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithSuccessfulLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, true);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithFailedLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithFailedLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, false);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithoutComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithoutComSessions");
        this.doTestGetLastSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithSuccessfulLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Success);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithSetupErrorLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSetupErrorLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.SetupError);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithBrokenLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithBrokenLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Broken);
    }

    @Test
    @Transactional
    public void testGetLastTaskSummaryWithoutComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastTaskSummaryWithComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithComSessions(connectionTask, 4, 1, 1);
    }

    @Test
    @Transactional
    public void testTriggerWithMinimizeStrategy() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn("testTriggerWithMinimizeStrategy");
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
    @Transactional
    public void testTriggerWithAsapStrategyAndOnlyPendingTasks() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn("testTriggerWithAsapStrategy");
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
    @Transactional
    public void testTriggerWithAsapStrategyAndOnlyOnHoldAndWaitingTasks() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn("testTriggerWithAsapStrategy");
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
    @Transactional
    public void testTriggerWithAsapStrategyAllComTaskStatusses() throws SQLException, BusinessException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn("testTriggerWithAsapStrategy");
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
    @Transactional
    public void testSwitchFromOutboundDefault() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSwitchFromOutboundDefault", false);
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
    @Transactional
    public void testSetAsDefaultWithoutOtherDefaults() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSetAsDefaultWithoutOtherDefaults", false);

        // Business method
        connectionTask.setAsDefault();

        // Asserts
        verify(this.manager).defaultConnectionTaskChanged(this.device, connectionTask);
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testSetAsDefaultWithoutMinimizeConnectionStrategy() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testSetAsDefaultWithoutOtherDefaults", false, ConnectionStrategy.MINIMIZE_CONNECTIONS);
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
    @Transactional
    public void testUnsetAsDefaultWithOtherConnectionTasks() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUnsetAsDefaultWithOtherConnectionTasks", true);
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
    @Transactional
    public void testUnsetAsDefaultWithoutOtherDefaults() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations("testUnsetAsDefaultWithoutOtherDefaults", true);

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
        when(this.partialScheduledConnectionTask.getName()).thenReturn(name);
        return this.createWithNoPropertiesWithoutViolations(this.partialScheduledConnectionTask, defaultState, connectionStrategy);
    }

    private ScheduledConnectionTask createWithNoPropertiesWithoutViolations(PartialOutboundConnectionTask partialConnectionTask, boolean defaultState, ConnectionStrategy connectionStrategy) {
        ScheduledConnectionTask connectionTask;
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(connectionStrategy)) {
            connectionTask = deviceDataService.newAsapConnectionTask(this.device, partialConnectionTask, outboundTcpipComPortPool);
        }
        else {
            connectionTask = deviceDataService.
                    newMinimizeConnectionTask(
                            this.device,
                            partialConnectionTask,
                            outboundTcpipComPortPool,
                            new TemporalExpression(EVERY_HOUR));
        }

        if (defaultState) {
            deviceDataService.setDefaultConnectionTask(connectionTask);
        }
        return connectionTask;
    }

    private ScheduledConnectionTask createWithCommunicationWindowWithoutViolations(String name) throws BusinessException, SQLException {
        return this.createWithCommunicationWindowWithoutViolations(name, FROM_ONE_AM_TO_TWO_AM);
    }

    private ScheduledConnectionTask createWithCommunicationWindowWithoutViolations(String name, ComWindow comWindow) throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn(name);
        shadow.setCommunicationWindow(comWindow);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private ScheduledConnectionTask createWithoutConnectionStrategy(String name) throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn(name);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(null);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private OutboundConnectionTaskShadow createWithoutNextExecutionSpecs(ConnectionStrategy connectionStrategy, String name) {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        when(this.partialScheduledConnectionTask.getName()).thenReturn(name);
        shadow.setDeviceId(this.nextDeviceId());
        shadow.setConnectionStrategy(connectionStrategy);
        shadow.setNextExecutionSpecs(null);
        return shadow;
    }

    private ScheduledConnectionTask createWithoutDevice() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
        shadow.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        NextExecutionSpecsShadow nextExecutionSpecsShadow = new NextExecutionSpecsShadow();
        nextExecutionSpecsShadow.setFrequency(EVERY_HOUR);
        shadow.setNextExecutionSpecs(nextExecutionSpecsShadow);
        this.setIpComPortPool(shadow);

        return new ConnectionTaskFactoryImpl().createScheduled(shadow);
    }

    private ScheduledConnectionTask createWithNonExistingDevice() throws BusinessException, SQLException {
        OutboundConnectionTaskShadow shadow = new OutboundConnectionTaskShadow(this.partialScheduledConnectionTask);
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
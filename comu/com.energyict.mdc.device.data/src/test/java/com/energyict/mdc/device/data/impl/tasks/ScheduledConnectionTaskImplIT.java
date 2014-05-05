
package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.exceptions.CannotDeleteUsedDefaultConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.DuplicateConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.scheduling.TemporalExpression;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.assertj.core.api.Condition;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    public void testCreateWithNoPropertiesWithoutViolations() {
        String name = "testCreateWithNoPropertiesWithoutViolations";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getName()).isEqualTo(name);
        assertThat(connectionTask.getNextExecutionSpecs()).isNull();
        assertThat(connectionTask.getInitiatorTask()).isNull();
        assertThat(connectionTask.getProperties()).isEmpty();
        assertThat(connectionTask.isDefault()).isFalse();
        assertThat(connectionTask.isPaused()).isFalse();
        assertThat(connectionTask.getCurrentRetryCount()).isEqualTo(0);
        assertThat(connectionTask.getRescheduleDelay()).isEqualTo(TimeDuration.minutes(5));
        assertThat(connectionTask.lastExecutionFailed()).isEqualTo(false);
        assertThat(connectionTask.getExecutingComServer()).isNull();
    }

    @Test
    @Transactional
    public void testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault() {
        ComTaskExecution comTaskExecution = createComTaskExecution();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault");
        connectionTask.save();

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);

        // Asserts
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp() {
        Date earliestNextExecutionTimestamp = new DateMidnight(2013, 2, 14).toDate();
        ComTaskExecution comTaskExecution = createComTaskExecution();
        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setNextExecutionTimeStampAndPriority(earliestNextExecutionTimestamp, comTaskEnablementPriority);
        comTaskExecutionUpdater.update();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateDefaultWithASAPCopiesTheEarliestNextExecutionTimestamp");
        connectionTask.save();
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(earliestNextExecutionTimestamp, TaskPriorityConstants.DEFAULT_PRIORITY);

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(earliestNextExecutionTimestamp);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void testCreatePaused() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreatePaused");
        connectionTask.save();

        //Business method
        connectionTask.pause();

        // Asserts
        assertThat(connectionTask.isPaused()).isTrue();
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    public void testCreateOfDifferentConfig() {
        DeviceConfiguration mockedDeviceConfiguration = mock(DeviceConfiguration.class);
        when(mockedDeviceConfiguration.getDeviceConfiguration()).thenReturn(mockedDeviceConfiguration);
        when(mockedDeviceConfiguration.getCommunicationConfiguration()).thenReturn(mockedDeviceConfiguration);
        PartialScheduledConnectionTask partialScheduledConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialScheduledConnectionTask.getId()).thenReturn(PARTIAL_SCHEDULED_CONNECTION_TASK3_ID);
        when(partialScheduledConnectionTask.getName()).thenReturn("testCreateOfDifferentConfig");
        when(partialScheduledConnectionTask.getConfiguration()).thenReturn(mockedDeviceConfiguration);
        when(partialScheduledConnectionTask.getPluggableClass()).thenReturn(noParamsConnectionTypePluggableClass);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateOfDifferentConfig", partialScheduledConnectionTask);

        // Business method
        connectionTask.save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testPause() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testPause");
        connectionTask.save();

        // Business method
        connectionTask.pause();

        // Asserts
        assertThat(connectionTask.isPaused()).isTrue();
    }

    @Test(expected = DuplicateConnectionTaskException.class)
    @Transactional
    public void testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask() {
        ScheduledConnectionTaskImpl firstTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask");
        firstTask.save();
        ScheduledConnectionTaskImpl secondTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));

        // Business method
        secondTask.save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testCreateSecondTaskAgainstTheSameDevice() {
        ScheduledConnectionTaskImpl firstTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDevice-1");
        firstTask.save();

        ScheduledConnectionTaskImpl secondTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDevice", this.partialScheduledConnectionTask2);

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
    public void testCreateAgainstAnotherDeviceBasedOnTheSamePartialConnectionTask() {
        ScheduledConnectionTaskImpl firstTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateAgainstAnotherDeviceBasedOnSamePartialConnectionTask-1");
        firstTask.save();
        ScheduledConnectionTaskImpl secondTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.otherDevice, this.partialScheduledConnectionTask, outboundTcpipComPortPool));

        // Business method
        secondTask.save();

        // Both tasks should be successfully created with the same name
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
        assertThat(secondTask.getName()).isEqualTo(firstTask.getName());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS_KEY + "}")
    public void testCreateMinimizeConnectionsWithSimultaneous() {
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testCreateMinimizeConnectionsWithSimultaneous", new TemporalExpression(EVERY_DAY));
        connectionTask.setSimultaneousConnectionsAllowed(true);

        // Business method
        connectionTask.save();

        // Asserts
        // assertThat(e.getMessageId()).isEqualTo("simultaneousConnectionsNotAllowedForMinimizeConnectionStrategy");
    }

    @Test
    @Transactional
    public void testCreateWithAllIpProperties() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionMethod).isNotNull();
        ComPortPool comPortPool = connectionTask.getComPortPool();
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
    public void testCreateWithOnlyRequiredIpPropertiesAndNoDefaultsOnPluggableClass() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
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
    public void testCreateWithOnlyRequiredIpPropertiesAndSomeDefaultsOnPluggableClass() {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME));
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME));
        ipConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
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
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClass() {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        // Do not set any properties on the ScheduledConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // no properties are locally defined, all 3 are inherited from the connection type pluggable class
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        Object codeTablePropertyValue = typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME);
        assertThat(codeTablePropertyValue).isInstanceOf(Code.class);
        Code actualCodeTable = (Code) codeTablePropertyValue;
        assertThat(actualCodeTable.getId()).isEqualTo(codeTable.getId());
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClassAndPartialConnectionTask() {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialScheduledConnectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        // Do not set any properties on the ScheduledConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionMethod.getPluggableClass().getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);   // 2 properties are inherited from the partial connection task and 1 is inherited from the connection type pluggable class
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        Object codeTablePropertyValue = connectionTask.getTypedProperties().getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME);
        assertThat(codeTablePropertyValue).isInstanceOf(Code.class);
        Code actualCodeTable = (Code) codeTablePropertyValue;
        assertThat(actualCodeTable.getId()).isEqualTo(codeTable.getId());
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromPartialConnectionTask() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS_PROPERTY_VALUE);
        partialScheduledConnectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        partialScheduledConnectionTask.setProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        // Do not set any properties on the ScheduledConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ConnectionMethod connectionMethod = connectionTask.getConnectionMethod();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
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
    public void testUpdateIpConnectionTypeProperty() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
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
    public void testAddIpConnectionTypeProperty() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, codeTable);
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
    public void testRemoveIpConnectionTypeProperty() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
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
    public void testReturnToInheritedProperty() {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME));
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), UPDATED_PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.removeProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME));
        ipConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
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

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.COMPORT_TYPE_NOT_SUPPORTED_KEY + "}")
    public void testCreateWithIpWithModemComPortPool() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask =
                ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().
                        newAsapConnectionTask(
                                this.device,
                                this.partialScheduledConnectionTask,
                                outboundModemComPortPool));
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Expected BusinessException because the ComPortType of the ComPortPool is not supported by the ConnectionType
        //assertThat(e.getMessageId()).isEqualTo("comPortTypeXOfComPortPoolYIsNotSupportedByConnectionTypePluggableClassZ");
    }

    @Test
    @Transactional
    public void testGetPropertiesOnMultipleDates() {
        // Create task with properties on may first 2011
        Date mayFirst2011 = freezeClock(2011, Calendar.MAY, 1);
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, null);
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
    public void testGetPropertiesOnMultipleDatesAfterReload() {
        // Create task with properties on may first 2011

        Date mayFirst2011 = freezeClock(2011, Calendar.MAY, 1);
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null, null);
        connectionTask.save();

        freezeClock(2012, Calendar.MAY, 1);
        connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        connectionTask.save();

        // Business method
        ScheduledConnectionTaskImpl reloaded = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get());

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
    public void testCreateWithMissingRequiredProperty() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(ipConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        TypedProperties partialProperties = TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs()));
        ScheduledConnectionTaskImpl connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionTask, null, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY + "}")
    public void testCreateWithNonExistingProperty() {
        ScheduledConnectionTaskImpl connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        // Add values for non existing property
        connectionTask.setProperty("doesNotExist", "I don't care");

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testCreateWithCommunicationWindowWithoutViolations() {
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testCreateWithCommunicationWindowWithoutViolations");
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNull();
        assertThat(connectionTask.getCommunicationWindow()).isNotNull();
        assertThat(connectionTask.getProperties().isEmpty()).as("Was not expecting any properties on the Outbound Connection Task").isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW_KEY + "}")
    public void createWithoutOffsetNotWithinCommunicationWindow() {
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "createWithoutOffsetAndCommunicationWindow",
                        new TemporalExpression(EVERY_DAY));
        connectionTask.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        connectionTask.save();

        // Expecting a BusinessException because the offset is outside the communication window.
        //assertThat(e.getMessageId()).isEqualTo("OffsetXIsNotWithinComWindowY");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW_KEY + "}")
    public void createWithOffsetWithinDayButOutsideCommunicationWindow() {
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations("createWithOffsetWithinDayButOutsideCommunicationWindow",
                                new TemporalExpression(
                                        EVERY_DAY,
                                        new TimeDuration(12, TimeDuration.HOURS))
                        );
        connectionTask.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        connectionTask.save();

        // Expecting a BusinessException because the offset is outside the communication window.
        // assertThat(e.getMessageId()).isEqualTo("OffsetXIsNotWithinComWindowY");
    }

    @Test
    @Transactional
    public void createWithOffsetOutsideDayButWithinCommunicationWindow() {
        String name = "createWithOffsetOutsideDayButWithinCommunicationWindow";
        partialScheduledConnectionTask.setName(name);
        partialScheduledConnectionTask.save();
        // Set it to execute every week, at 01:30 (am) of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_HOUR * 25 + DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.SECONDS);
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations(name, new TemporalExpression(frequency, offset));

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

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW_KEY + "}")
    public void createWithOffsetOutsideDayAndOutsideCommunicationWindow() {
        TimeDuration frequency = new TimeDuration(1, TimeDuration.WEEKS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_HOUR * 24 + DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.SECONDS);
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "createWithOffsetOutsideDayAndOutsideCommunicationWindow",
                        new TemporalExpression(frequency, offset));
        connectionTask.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testCreateWithExistingComTaskExecutions() {
        ComTaskExecution comTaskExecution = createComTaskExecution();

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateWithExistingComTaskExecutions");
        connectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        Device reloadedDevice = getReloadedDevice(device);
        // Asserts
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            assertThat(taskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
        }
    }

    @Test
    @Transactional
    public void createWithComTaskUsingDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        Date febFirst = freezeClock(2013, Calendar.FEBRUARY, 1);
        ComTaskExecution comTaskExecution = createComTaskExecutionAndSetNextExecutionTimeStamp(febFirst);

        ScheduledConnectionTaskImpl defaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations("createWithComTaskUsingDefaultTestNextExecutionTimeStamp");
        defaultConnectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(defaultConnectionTask);

        // asserts
        assertThat(defaultConnectionTask.getNextExecutionTimestamp()).isEqualTo(febFirst);
    }

    @Test
    @Transactional
    public void updateToAsapDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        Date comTaskNextExecutionTimeStamp = freezeClock(2013, Calendar.FEBRUARY, 13);

        freezeClock(2013, Calendar.FEBRUARY, 13, 10, 53, 20, 0);    // anything, as long as it's different from comTaskNextExecutionTimeStamp

        ScheduledConnectionTaskImpl notDefaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations("updateToDefaultTestNextExecutionTimeStamp");
        notDefaultConnectionTask.save();
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();

        createComTaskExecutionAndSetNextExecutionTimeStamp(comTaskNextExecutionTimeStamp);

        // Business method
        deviceDataService.setDefaultConnectionTask(notDefaultConnectionTask);
        ScheduledConnectionTask reloaded = deviceDataService.findScheduledConnectionTask(notDefaultConnectionTask.getId()).get();

        // Asserts after update
        assertThat(reloaded.getNextExecutionTimestamp()).isEqualTo(comTaskNextExecutionTimeStamp);
    }

    @Test
    @Transactional
    public void updateToMinimizeDefaultTestNextExecutionTimeStamp() throws SQLException, BusinessException {
        Date comTaskNextExecutionTimeStamp = freezeClock(2013, Calendar.FEBRUARY, 13);

        freezeClock(2013, Calendar.FEBRUARY, 13, 10, 53, 20, 0);    // anything, as long as it's different from comTaskNextExecutionTimeStamp

        ScheduledConnectionTaskImpl notDefaultConnectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("updateToDefaultTestNextExecutionTimeStamp", new TemporalExpression(EVERY_HOUR));
        notDefaultConnectionTask.save();
        Date nextExecutionTimestamp = notDefaultConnectionTask.getNextExecutionTimestamp();
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();

        ComTaskExecution comTaskExecution = createComTaskExecutionAndSetNextExecutionTimeStamp(comTaskNextExecutionTimeStamp);

        // Business method
        deviceDataService.setDefaultConnectionTask(notDefaultConnectionTask);
        ScheduledConnectionTask reloaded = deviceDataService.findScheduledConnectionTask(notDefaultConnectionTask.getId()).get();

        // Asserts after update
        assertThat(reloaded.getNextExecutionTimestamp()).isEqualTo(nextExecutionTimestamp);
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(nextExecutionTimestamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED_KEY + "}")
    public void testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs() {
        ScheduledConnectionTaskImpl connectionTask = this.createWithoutNextExecutionSpecs(ConnectionStrategy.MINIMIZE_CONNECTIONS, "testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs");

        // Busines method
        connectionTask.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
    public void testCreateWithoutComPortPool() {
        ScheduledConnectionTaskImpl connectionTask =
                ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().
                        newAsapConnectionTask(
                                this.device,
                                this.partialScheduledConnectionTask,
                                null));

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testAllowSimultaneousConnections() {
        // First one - allow simultaneous connections
        ScheduledConnectionTaskImpl outboundTrue = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        outboundTrue.setSimultaneousConnectionsAllowed(true);
        outboundTrue.save();

        // second one - deny simultaneous connections
        ScheduledConnectionTaskImpl outboundFalse = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask2, outboundTcpipComPortPool));
        outboundFalse.setSimultaneousConnectionsAllowed(false);
        outboundFalse.save();

        // Asserts
        assertThat(outboundTrue.isSimultaneousConnectionsAllowed()).isTrue();
        assertThat(outboundFalse.isSimultaneousConnectionsAllowed()).isFalse();
    }

    @Test
    @Transactional
    public void testLoad() {
        ScheduledConnectionTaskImpl created = this.createAsapWithNoPropertiesWithoutViolations("testLoad");
        created.save();

        // Business method
        ScheduledConnectionTask loaded = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(created.getId()).get();

        // Asserts
        assertThat(created.getDevice().getId()).isEqualTo(loaded.getDevice().getId());
        assertThat(created.getComPortPool().getId()).isEqualTo(loaded.getComPortPool().getId());
        assertThat(created.getConnectionStrategy()).isEqualTo(loaded.getConnectionStrategy());
        assertThat(created.getCommunicationWindow()).isEqualTo(loaded.getCommunicationWindow());
        assertThat(created.getNextExecutionSpecs()).isEqualTo(loaded.getNextExecutionSpecs());
        assertThat(created.getInitiatorTask()).isEqualTo(loaded.getInitiatorTask());
        assertThat(created.isSimultaneousConnectionsAllowed()).isEqualTo(loaded.isSimultaneousConnectionsAllowed());
        assertThat(created.getNextExecutionTimestamp()).isEqualTo(loaded.getNextExecutionTimestamp());
        assertThat(created.getPlannedNextExecutionTimestamp()).isEqualTo(loaded.getPlannedNextExecutionTimestamp());
    }

    @Test
    @Transactional
    public void testUpdate() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testUpdate");
        connectionTask.save();

        connectionTask.setCommunicationWindow(FROM_TEN_PM_TO_TWO_AM);
        connectionTask.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        TimeDuration elevenPM = new TimeDuration(23, TimeDuration.HOURS);
        connectionTask.setNextExecutionSpecsFrom(new TemporalExpression(EVERY_DAY, elevenPM));

        // Business method
        connectionTask.save();

        // Asserts
        ScheduledConnectionTaskImpl updated = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get());
        assertThat(ConnectionStrategy.MINIMIZE_CONNECTIONS).isEqualTo(updated.getConnectionStrategy());
        assertThat(FROM_TEN_PM_TO_TWO_AM).isEqualTo(updated.getCommunicationWindow());
        assertThat(EVERY_DAY).isEqualTo(updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
        assertThat(elevenPM).isEqualTo(updated.getNextExecutionSpecs().getTemporalExpression().getOffset());
    }

    @Test
    @Transactional
    public void testSwitchToAsapStrategyShouldRemoveNextExecSpec() {
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testSwitchToAsapStrategyAndRemoveNextExecSpec",
                        new TemporalExpression(EVERY_HOUR));
        connectionTask.save();

        connectionTask.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);

        // Business method
        connectionTask.save();
        ScheduledConnectionTask updated = deviceDataService.findScheduledConnectionTask(connectionTask.getId()).get();

        // Asserts
        assertThat(ConnectionStrategy.AS_SOON_AS_POSSIBLE).isEqualTo(updated.getConnectionStrategy());
        assertThat(updated.getNextExecutionSpecs()).isNull();
    }

    @Test
    @Transactional
    public void testSwitchToMinimizeConnectionStrategy() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testSwitchToMinimizeConnectionStrategy");
        connectionTask.save();

        connectionTask.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        connectionTask.setNextExecutionSpecsFrom(new TemporalExpression(EVERY_HOUR));

        // Business method
        connectionTask.save();
        ScheduledConnectionTaskImpl updated = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get());

        // Asserts
        assertThat(ConnectionStrategy.MINIMIZE_CONNECTIONS).isEqualTo(updated.getConnectionStrategy());
        assertThat(updated.getNextExecutionSpecs()).isNotNull();
        assertThat(EVERY_HOUR).isEqualTo(updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
    }

    @Test
    @Transactional
    public void testScheduleNow() {
        Date mayLast2012 = freezeClock(2012, Calendar.MAY, 31);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testScheduleNow");
        connectionTask.save();

        // Business method
        connectionTask.scheduleNow();

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(mayLast2012);
    }

    @Test
    @Transactional
    public void testScheduleOnDate() {
        Date mayLast2012 = freezeClock(2012, Calendar.MAY, 31);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testScheduleOnDate");
        connectionTask.save();

        // Business method
        connectionTask.schedule(mayLast2012);

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(mayLast2012);
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionTimestampForUTCDevice() {
        Date expectedNextExecutionTimestamp = freezeClock(2012, Calendar.MAY, 31, 1, 0, 0, 0);     // Frequency of rescheduling is 1 hour
        freezeClock(2011, Calendar.MAY, 31);    // Anything, as long as it is not 2012, May 31st - the data that is set below just after the save

        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testUpdateNextExecutionTimestampForUTCDevice", new TemporalExpression(EVERY_HOUR));
        connectionTask.save();

        freezeClock(2012, Calendar.MAY, 31);
        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionTimestampForUSPacificDevice() {
        // US/Pacific timezone has offset to UTC of -8 and when DST is applied, it has offset to UTC of -7
        TimeZone usPacific = TimeZone.getTimeZone("US/Pacific");

        Date mayLast2012 = freezeClock(2012, Calendar.MAY, 31, usPacific);    // This is in UTC

        Calendar calendar = Calendar.getInstance(usPacific);
        calendar.setTime(mayLast2012);
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Date expectedNextExecutionTimestamp = calendar.getTime();
//        when(this.device.getDeviceTimeZone()).thenReturn(usPacific);

        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testUpdateNextExecutionTimestampForUSPacificDevice",
                        new TemporalExpression(EVERY_HOUR));
        connectionTask.save();

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionTimestampOnDSTFromWinterToSummer() {
        // Europe/Brussels timezone has offset to UTC of +1 and when DST is applied, it has offset to UTC of +2
        TimeZone brussels = TimeZone.getTimeZone("Europe/Brussels");

        // This is in UTC and corresponds to 02:00am in Brussels, exactly when DST applies
        Date oneMinuteBeforeDST = freezeClock(2011, Calendar.MARCH, 27, 1, 0, 0, 0);

        Calendar calendar = Calendar.getInstance(brussels);
        calendar.set(2011, Calendar.MARCH, 27, 2, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Date expectedNextExecutionTimestamp = calendar.getTime();
//        when(this.device.getDeviceTimeZone()).thenReturn(brussels);

        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testUpdateNextExecutionTimestampOnDSTFromWinterToSummer",
                        new TemporalExpression(EVERY_HOUR));
        connectionTask.save();

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionTimestampOnDSTFromSummerToWinter() {
        // Europe/Brussels timezone has offset to UTC of +1 and when DST is applied, it has offset to UTC of +2
        TimeZone brussels = TimeZone.getTimeZone("Europe/Brussels");

        // This is in UTC and corresponds to 03:00am in Brussels, exactly when DST is switched off
        Date oneMinuteBeforeDST = freezeClock(2011, Calendar.OCTOBER, 30, 1, 0, 0, 0);

        Calendar calendar = Calendar.getInstance(brussels);
        calendar.set(2011, Calendar.OCTOBER, 30, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR, 3);     // The calendar is now at 3 am
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Date expectedNextExecutionTimestamp = calendar.getTime();
        //TODO check to update these tests!
//        when(this.device.getDeviceTimeZone()).thenReturn(brussels);

        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testUpdateNextExecutionTimestampOnDSTFromSummerToWinter",
                        new TemporalExpression(EVERY_HOUR));
        connectionTask.save();

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testDeleteWithNoProperties() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testDeleteWithNoProperties");
        connectionTask.save();
        long id = connectionTask.getId();

        // Business method
        connectionTask.delete();

        // Asserts
        assertThat(inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testDeleteWithProperties() {
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testDeleteWithProperties");
        connectionTask.save();
        long id = connectionTask.getId();
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.delete();

        // Asserts
        assertThat(inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(id).isPresent()).isFalse();
        RelationAttributeType connectionMethodAttributeType = ipConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test(expected = ConnectionTaskIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenComServerIsExecutingTest() throws SQLException, BusinessException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("makeObsoleteWhenComServerIsExecutingTest");
        connectionTask.save();
        this.attemptLock(connectionTask);

        // Business method
        connectionTask.makeObsolete();
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithNoProperties() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testMakeObsoleteWithNoProperties");
        connectionTask.save();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertThat(connectionTask.isObsolete()).isTrue();
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testMakeObsoleteAlsoMakesRelationsObsolete() {
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testMakeObsoleteAlsoMakesRelationsObsolete");
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertThat(connectionTask.isObsolete()).isTrue();
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
        assertThat(connectionTask.getConnectionMethod()).isNotNull();
        RelationAttributeType connectionMethodAttributeType = ipConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).hasSize(1);
    }

    @Test
    @Transactional
    public void testIsObsoleteAfterReload() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testIsObsoleteAfterReload");
        connectionTask.save();
        long id = connectionTask.getId();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        ScheduledConnectionTask obsolete = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(id).get();
        assertThat(obsolete).as("The ConnectionTask should be marked for delete, but still present in DB").isNotNull();
        assertThat(obsolete.isObsolete()).isTrue();
        assertThat(obsolete.getObsoleteDate()).isNotNull();
    }

    @Test(expected = CannotUpdateObsoleteConnectionTaskException.class)
    @Transactional
    public void testUpdateAfterMakeObsolete() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testUpdateAfterMakeObsolete");
        connectionTask.save();
        connectionTask.makeObsolete();

        // Business method
        connectionTask.setCommunicationWindow(FROM_TEN_PM_TO_TWO_AM);
        connectionTask.save();

        // Asserts: see expected CannotUpdateObsoleteConnectionTaskException
    }

    @Test(expected = ConnectionTaskIsAlreadyObsoleteException.class)
    @Transactional
    public void testMakeObsoleteTwice() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testMakeObsoleteTwice");
        connectionTask.save();
        connectionTask.makeObsolete();

        // Business method
        connectionTask.makeObsolete();

        // Asserts: see expected ConnectionTaskIsAlreadyObsoleteException
    }

    @Test(expected = ConnectionTaskIsAlreadyObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenSomeOneElseMadeItObsoleteTest() throws SQLException {
        final ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("makeObsoleteWhenSomeOneElseMadeItObsoleteTest");
        connectionTask.save();
        try (PreparedStatement statement = this.getUpdateObsoleteDateSqlBuilder(connectionTask.getId()).getStatement(Environment.DEFAULT.get().getConnection())) {
            int updateCount = statement.executeUpdate();
            if (updateCount != 1) {
                throw new SQLException("updated zero rows");
            }
        }

        // Business method
        connectionTask.makeObsolete();
    }

    private SqlBuilder getUpdateObsoleteDateSqlBuilder(long id) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCONNECTIONTASK.name());
        sqlBuilder.append(" set obsolete_date = ? where id = ?");
        sqlBuilder.bindDate(clock.now());
        sqlBuilder.bindLong(id);
        return sqlBuilder;
    }

    /**
     * Making a ConnectionTask obsolete results in updating the ComTaskExecutions with an empty ConnectionTask.
     */
    @Test
    @Transactional
    public void testMakeObsoleteWithActiveComTasks() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testMakeObsoleteWithActiveComTasks");
        connectionTask.save();
        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, clock.now());
        assertThat(comTaskExecution.getConnectionTask()).isNotNull();
        // Business method
        connectionTask.makeObsolete();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
    }

    @Test(expected = CannotDeleteUsedDefaultConnectionTaskException.class)
    @Transactional
    public void testCannotDeleteDefaultTaskThatIsInUse() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCannotDeleteDefaultTaskThatIsInUse");
        connectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        createComTaskExecution();

        // Business method
        connectionTask.delete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testDeletedAndSetComTaskToNoConnectionTask() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testDeletedAndSetComTaskToNoConnectionTask");
        connectionTask.save();
        ComTaskExecution comTaskExecution = createComTaskExecution();

        // Business method
        connectionTask.delete();

        // Asserts
        assertThat(comTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    @Ignore
    // Todo: Enable when ComSession (and related classes) has been moved to this bundle
    public void testDeleteWithComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testDeleteWithComSessions");
        connectionTask.save();
        List<ComSession> comSessions = new ArrayList<>();
        ComSession comSession = mock(ComSession.class);
        comSessions.add(comSession);

        // Business method
        connectionTask.delete();

        // Asserts
        verify(comSession).delete();
    }

    @Test
    @Transactional
    public void testFindConnectionTaskByDeviceAfterDelete() {
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testFindOutboundByDeviceAfterDelete", new TemporalExpression(EVERY_HOUR));
        connectionTask.save();
        List<ConnectionTask> outboundByDeviceBeforeDelete = inMemoryPersistence.getDeviceDataService().findConnectionTasksByDevice(connectionTask.getDevice());

        // Business methods
        connectionTask.delete();
        List<ConnectionTask> outboundByDeviceAfterDelete = inMemoryPersistence.getDeviceDataService().findConnectionTasksByDevice(connectionTask.getDevice());

        // Asserts
        this.assertConnectionTask(outboundByDeviceBeforeDelete, connectionTask);
        assertThat(outboundByDeviceAfterDelete).isEmpty();
    }

    @Test
    @Transactional
    public void createMultipleOutboundsForSpecificDeviceWithoutViolations() {
        ScheduledConnectionTaskImpl task1 =
                this.createAsapWithNoPropertiesWithoutViolations("createMultipleOutboundsForSpecificDeviceWithoutViolations-1");
        task1.save();
        ScheduledConnectionTaskImpl task2 =
                this.createAsapWithNoPropertiesWithoutViolations(
                        "createMultipleOutboundsForSpecificDeviceWithoutViolations-2",
                        this.partialScheduledConnectionTask2
                );
        task2.save();
        List<ConnectionTask> outboundConnectionTasks = inMemoryPersistence.getDeviceDataService().findConnectionTasksByDevice(this.device);

        // asserts
        assertThat(task1).isNotNull();
        assertThat(task2).isNotNull();
        this.assertConnectionTask(outboundConnectionTasks, task1, task2);
    }

    @Test
    @Transactional
    public void updateWithDefaultWhenNoDefaultYetExistsTest() {
        ScheduledConnectionTaskImpl task1 =
                this.createAsapWithNoPropertiesWithoutViolations("updateWithDefaultWhenNoDefaultYetExistsTest-1");
        task1.save();
        ScheduledConnectionTaskImpl task2 =
                this.createAsapWithNoPropertiesWithoutViolations(
                        "updateWithDefaultWhenNoDefaultYetExistsTest-2",
                        this.partialScheduledConnectionTask2);
        task2.save();

        // Business method
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        List<ConnectionTask> outboundConnectionTasks = deviceDataService.findConnectionTasksByDevice(this.device);
        ConnectionTask defaultConnectionTaskForDevice = deviceDataService.findDefaultConnectionTaskForDevice(this.device);

        // prologue asserts
        this.assertConnectionTask(outboundConnectionTasks, task1, task2);
        assertThat(defaultConnectionTaskForDevice).isNull();

        // update to one task to the default task
        deviceDataService.setDefaultConnectionTask(task2);

        defaultConnectionTaskForDevice = deviceDataService.findDefaultConnectionTaskForDevice(this.device);

        // asserts
        assertThat(defaultConnectionTaskForDevice).isNotNull();
        assertThat(defaultConnectionTaskForDevice.getId()).isEqualTo(task2.getId());
    }

    @Test
    @Transactional
    public void testApplyComWindowWhenTaskDoesNotHaveAComWindow() throws SQLException, BusinessException {
        Date nextExecutionTimestamp = clock.now();
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithoutNextExecutionSpecs", null);
        connectionTask.save();

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
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampThatImmediatelyFallsWithinComWindow", FROM_ONE_AM_TO_TWO_AM);
        connectionTask.save();
        Date nextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 1, 30, 0, 0);   // UTC

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
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampBeforeComWindow", FROM_ONE_AM_TO_TWO_AM);
        connectionTask.save();
        Date nextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 0, 30, 0, 0);
        Date expectedModifiedNextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 1, 0, 0, 0);

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
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampAfterComWindow", FROM_ONE_AM_TO_TWO_AM);
        connectionTask.save();
        Date nextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 2, 30, 0, 0);
        Date expectedModifiedNextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 10, 1, 0, 0, 0);

        // Business method
        Date modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts: 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(expectedModifiedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testAttemptLock() {
        String name = "testAttemptLock";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();

        // Business method
        ScheduledConnectionTask lockedConnectionTask = this.attemptLock(connectionTask);

        // Asserts
        assertThat(lockedConnectionTask.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void testUnlock() {
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        String name = "testUnlock";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();
        ScheduledConnectionTaskImpl lockedConnectionTask = deviceDataService.attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Business method
        deviceDataService.unlockConnectionTask(lockedConnectionTask);

        // Asserts
        assertThat(connectionTask.getExecutingComServer()).isNull();
    }

    @Test
    @Transactional
    public void testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer() {
        String name = "testAttemptLockWhenAlreadyLockedByTheSameComServer";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();

        // Business method
        ScheduledConnectionTask lockedConnectionTask = this.attemptLock(connectionTask);

        // Asserts
        assertThat(lockedConnectionTask.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void testAttemptLockWhenAlreadyLockedByAnotherComServer() {
        String name = "testAttemptLockWhenAlreadyLockedByAnotherComServer";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();
        ScheduledConnectionTask lockedConnectionTask = this.attemptLock(connectionTask, this.getOnlineComServer());

        // Business method
        ScheduledConnectionTask shouldBeNull = this.attemptLock(connectionTask, this.getOtherOnlineComServer());

        // Asserts
        assertThat(shouldBeNull).isNull();
        assertThat(lockedConnectionTask.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void pauseIfNotPausedTest() throws SQLException, BusinessException {
        String name = "pauseIfNotPausedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();

        assertThat(connectionTask.isPaused()).isFalse();

        // Business method
        connectionTask.pause();
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedConnectionTask.isPaused()).isTrue();
    }

    @Test
    @Transactional
    public void pauseWhenAlreadyPausedTest() throws SQLException, BusinessException {
        String name = "pauseWhenAlreadyPausedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();

        // business method
        connectionTask.pause();
        connectionTask.pause();
        connectionTask.pause();
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedConnectionTask.isPaused()).isTrue();
    }

    @Test
    @Transactional
    public void resumeWhenPausedTest() throws SQLException, BusinessException {
        String name = "resumeWhenPausedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();
        connectionTask.pause();

        // business method
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get();
        reloadedConnectionTask.resume();
        reloadedConnectionTask = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedConnectionTask.isPaused()).isFalse();
    }

    @Test
    @Transactional
    public void resumeWhenAlreadyResumedTest() throws SQLException, BusinessException {
        String name = "resumeWhenAlreadyResumedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.save();

        // business method
        connectionTask.resume();
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedConnectionTask.isPaused()).isFalse();
    }

/* Todo: Enable once communication session objects have been ported to this bundle
    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessions() throws SQLException, BusinessException {
        ConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetLastComSessionWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache() throws SQLException, BusinessException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessionsWithActiveComSessionCache() throws SQLException, BusinessException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessions() throws SQLException, BusinessException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithComSessions");
        this.doTestGetLastComSessionWithComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessionsWithActiveLastComSessionCache() throws SQLException, BusinessException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessionsWithActiveComSessionCache() throws SQLException, BusinessException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithoutComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithSuccessfulLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, true);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithFailedLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithFailedLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, false);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithoutComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithoutComSessions");
        this.doTestGetLastSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithSuccessfulLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Success);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithSetupErrorLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSetupErrorLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.SetupError);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithBrokenLastComSession() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithBrokenLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Broken);
    }

    @Test
    @Transactional
    public void testGetLastTaskSummaryWithoutComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastTaskSummaryWithComSessions() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithComSessions(connectionTask, 4, 1, 1);
    }
*/

    @Test
    @Transactional
    public void testTriggerWithMinimizeStrategy() {
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testTriggerWithMinimizeStrategy", new TemporalExpression(EVERY_HOUR));
        connectionTask.save();
        Date comTaskDate = freezeClock(2013, Calendar.JUNE, 2);
        Date triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, comTaskDate);

        // Business method
        connectionTask.trigger(triggerDate);

        // Asserts
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(triggerDate);
    }

    @Test
    @Transactional
    public void testTriggerWithAsapStrategyAndOnlyPendingTasks() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testTriggerWithAsapStrategyAndOnlyPendingTasks");
        connectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        Date pastDate = freezeClock(2013, Calendar.JULY, 5);
        final Date triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);

        ComTaskExecution comTaskExecution1 = createComTaskExecutionAndSetNextExecutionTimeStamp(pastDate, comTaskEnablement1);
        ComTaskExecution comTaskExecution2 = createComTaskExecutionAndSetNextExecutionTimeStamp(pastDate, comTaskEnablement2);

        // Business method
        Date nextExecutionTimstamp = connectionTask.trigger(triggerDate);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        List<ComTaskExecution> comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).is(new Condition<List<ComTaskExecution>>() {
            @Override
            public boolean matches(List<ComTaskExecution> comTaskExecutions) {
                for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                    if(!comTaskExecution.getNextExecutionTimestamp().equals(triggerDate)){
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
    public void testTriggerWithAsapStrategyAndOnlyOnHoldAndWaitingTasks() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testTriggerWithAsapStrategyAndOnlyOnHoldAndWaitingTasks");
        connectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        final Date futureDate = freezeClock(2013, Calendar.JULY, 4);
        Date triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);
        ComTaskExecution comTaskExecution1 = createComTaskExecutionAndSetNextExecutionTimeStamp(futureDate, comTaskEnablement1);
        ((ServerComTaskExecution) comTaskExecution1).executionCompleted();
        comTaskExecution1.getDevice().getComTaskExecutionUpdater(comTaskExecution1).setNextExecutionTimeStampAndPriority(futureDate, 100).update();

        ComTaskExecution comTaskExecution2 = createComTaskExecution(comTaskEnablement2);

        // Business method
        Date nextExecutionTimstamp = connectionTask.trigger(triggerDate);

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
    public void testTriggerWithAsapStrategyAllComTaskStatusses() throws SQLException, BusinessException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testTriggerWithAsapStrategyAllComTaskStatusses");
        connectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        final Date futureDate = freezeClock(2013, Calendar.JULY, 4);
        final Date triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        ComTaskExecution comTaskExecution = createComTaskExecutionAndSetNextExecutionTimeStamp(futureDate, comTaskEnablement1);
        assertThat(comTaskExecution.getStatus()).isEqualTo(TaskStatus.NeverCompleted);
        connectionTask.trigger(triggerDate); // never completed
        Device reloadedDevice = getReloadedDevice(device);
        List<ComTaskExecution> comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return triggerDate.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        ((ServerComTaskExecution) comTaskExecution).executionCompleted();
        comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).setNextExecutionTimeStampAndPriority(futureDate, 100).update(); // waiting task
        assertThat(getReloadedComTaskExecution(device).getStatus()).isEqualTo(TaskStatus.Waiting);
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
        assertThat(getReloadedComTaskExecution(device).getStatus()).isEqualTo(TaskStatus.Busy);
        connectionTask.trigger(triggerDate);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureDate.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        ((ServerComTaskExecution) comTaskExecution).setLockedComPort(null);
        comTaskExecution.putOnHold(); // on hold task
        assertThat(getReloadedComTaskExecution(device).getStatus()).isEqualTo(TaskStatus.OnHold);
        connectionTask.trigger(triggerDate);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return comTaskExecution.getNextExecutionTimestamp() == null;
            }
        });
        comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).setNextExecutionTimeStampAndPriority(futureDate, 100).update();
        final Date futureTrigger = freezeClock(2013, Calendar.AUGUST, 5); // pending task
        assertThat(getReloadedComTaskExecution(device).getStatus()).isEqualTo(TaskStatus.Pending);
        connectionTask.trigger(futureTrigger);
        reloadedDevice = getReloadedDevice(device);
        comTaskExecutions = reloadedDevice.getComTaskExecutions();
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureTrigger.equals(comTaskExecution.getNextExecutionTimestamp());
            }
        });
        ((ServerComTaskExecution) comTaskExecution).executionFailed();  // make it retry
        assertThat(getReloadedComTaskExecution(device).getStatus()).isEqualTo(TaskStatus.Retrying);
        assertThat(comTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return futureTrigger.before(getReloadedComTaskExecution(device).getNextExecutionTimestamp());
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
        comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).createOrUpdateNextExecutionSpec(new TemporalExpression(TimeDuration.days(1))).update();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();
        ((ServerComTaskExecution) comTaskExecution).executionFailed();  // make it fail
        assertThat(getReloadedComTaskExecution(device).getStatus()).isEqualTo(TaskStatus.Failed);
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

    private OutboundComPort createOutboundComPort() {
        OnlineComServer onlineComServer = inMemoryPersistence.getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setName("ComServer");
        onlineComServer.setStoreTaskQueueSize(1);
        onlineComServer.setStoreTaskThreadPriority(1);
        onlineComServer.setChangesInterPollDelay(TimeDuration.minutes(5));
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setSchedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setNumberOfStoreTaskThreads(2);
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        OutboundComPort outboundComPort = outboundComPortBuilder.add();
        onlineComServer.save();
        return outboundComPort;
    }

    @Test
    @Transactional
    public void testSwitchFromInboundDefault() throws SQLException, BusinessException {
        InboundConnectionTaskImpl inboundConnectionTask = this.createSimpleInboundConnectionTask();
        inboundConnectionTask.save();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(inboundConnectionTask);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testSwitchFromOutboundDefault");
        connectionTask.save();

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        InboundConnectionTask reloadedInbound = inMemoryPersistence.getDeviceDataService().findInboundConnectionTask(inboundConnectionTask.getId()).get();
        ScheduledConnectionTaskImpl reloadedScheduled = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get());

        assertThat(reloadedInbound.isDefault()).isFalse();
        assertThat(reloadedScheduled.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testSetAsDefaultWithoutOtherDefaults() throws SQLException, BusinessException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testSetAsDefaultWithoutOtherDefaults");
        connectionTask.save();

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isDefault()).isTrue();
    }

    private void assertConnectionTask(List<ConnectionTask> outboundConnectionTasks, ScheduledConnectionTaskImpl... tasks) {
        assertThat(outboundConnectionTasks).isNotNull();
        assertThat(outboundConnectionTasks).hasSize(tasks.length);
        Set<Long> outboundConnectionTaskIds = new HashSet<>();
        for (ConnectionTask task : outboundConnectionTasks) {
            outboundConnectionTaskIds.add(task.getId());
        }
        Long[] expectedTaskIds = new Long[tasks.length];
        int i = 0;
        for (ScheduledConnectionTask task : tasks) {
            expectedTaskIds[i] = task.getId();
            i++;
        }
        assertThat(outboundConnectionTaskIds).contains(expectedTaskIds);
    }

    private ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask) {
        return this.attemptLock(connectionTask, this.getOnlineComServer());
    }

    private ScheduledConnectionTask attemptLock(ScheduledConnectionTask connectionTask, ComServer comServer) {
        return inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name) {
        return createAsapWithNoPropertiesWithoutViolations(name, this.partialScheduledConnectionTask);
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, PartialScheduledConnectionTask partialConnectionTask) {
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        partialConnectionTask.setName(name);
        partialConnectionTask.save();
        return ((ScheduledConnectionTaskImpl) deviceDataService.newAsapConnectionTask(this.device, partialConnectionTask, outboundTcpipComPortPool));
    }

    private ScheduledConnectionTaskImpl createMinimizeWithNoPropertiesWithoutViolations(String name, TemporalExpression temporalExpression) {
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        return ((ScheduledConnectionTaskImpl) deviceDataService.newMinimizeConnectionTask(
                this.device,
                this.partialScheduledConnectionTask,
                outboundTcpipComPortPool,
                temporalExpression));
    }

    private ScheduledConnectionTaskImpl createWithCommunicationWindowWithoutViolations(String name) {
        return this.createWithCommunicationWindowWithoutViolations(name, FROM_ONE_AM_TO_TWO_AM);
    }

    private ScheduledConnectionTaskImpl createWithCommunicationWindowWithoutViolations(String name, ComWindow comWindow) {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.setCommunicationWindow(comWindow);
        return connectionTask;
    }

    private ScheduledConnectionTaskImpl createWithoutNextExecutionSpecs(ConnectionStrategy connectionStrategy, String name) {
        ScheduledConnectionTaskImpl connectionTask;
        if (ConnectionStrategy.AS_SOON_AS_POSSIBLE.equals(connectionStrategy)) {
            connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newAsapConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool));
        }
        else {
            connectionTask = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getDeviceDataService().newMinimizeConnectionTask(this.device, this.partialScheduledConnectionTask, outboundTcpipComPortPool, null));
        }
        return connectionTask;
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask() {
        return this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask(PartialInboundConnectionTask partialConnectionTask) {
        return createSimpleInboundConnectionTask(partialConnectionTask, inboundTcpipComPortPool);
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask(final PartialInboundConnectionTask partialConnectionTask, final InboundComPortPool inboundComPortPool) {
        InboundConnectionTaskImpl inboundConnectionTask = ((InboundConnectionTaskImpl) getDeviceDataService().newInboundConnectionTask(this.device, partialConnectionTask, inboundComPortPool));
        inboundConnectionTask.save();
        return inboundConnectionTask;
    }

}
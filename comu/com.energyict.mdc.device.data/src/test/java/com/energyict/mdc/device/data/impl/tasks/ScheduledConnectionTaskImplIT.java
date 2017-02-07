
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.DuplicateConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.EarliestNextExecutionTimeStampAndPriority;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import org.joda.time.DateTimeConstants;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ScheduledConnectionTaskImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (14:12)
 */
public class ScheduledConnectionTaskImplIT extends ConnectionTaskImplIT {

    private static final TimeDuration EVERY_DAY = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
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
    public void refreshAllConnectionTypePluggableClasses() {
        refreshConnectionTypePluggableClasses();
    }

    @Test
    @Transactional
    public void testCreateWithNoPropertiesWithoutViolations() {
        String name = "testCreateWithNoPropertiesWithoutViolations";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getName()).isEqualTo(name);
        assertThat(connectionTask.getNextExecutionSpecs()).isNull();
        assertThat(connectionTask.getInitiatorTask()).isNull();
        assertThat(connectionTask.getProperties()).isEmpty();
        assertThat(connectionTask.isDefault()).isFalse();
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        assertThat(connectionTask.getCurrentRetryCount()).isEqualTo(0);
        assertThat(connectionTask.getRescheduleDelay()).isEqualTo(TimeDuration.minutes(5));
        assertThat(connectionTask.lastExecutionFailed()).isEqualTo(false);
        assertThat(connectionTask.getExecutingComServer()).isNull();
    }

    @Test
    @Transactional
    public void testCreatePaused() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreatePaused");

        //Business method
        connectionTask.deactivate();

        // Asserts
        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    public void testCreateOfDifferentConfig() {
        DeviceConfiguration mockedDeviceConfiguration = mock(DeviceConfiguration.class);
        PartialScheduledConnectionTask partialScheduledConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialScheduledConnectionTask.getId()).thenReturn(PARTIAL_SCHEDULED_CONNECTION_TASK3_ID);
        when(partialScheduledConnectionTask.getName()).thenReturn("testCreateOfDifferentConfig");
        when(partialScheduledConnectionTask.getConfiguration()).thenReturn(mockedDeviceConfiguration);
        when(partialScheduledConnectionTask.getPluggableClass()).thenReturn(outboundNoParamsConnectionTypePluggableClass);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateOfDifferentConfig", partialScheduledConnectionTask);

        // Business method
        connectionTask.update();

        // Asserts: see expected exception rule
    }

    @Test(expected = DuplicateConnectionTaskException.class)
    @Transactional
    public void testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask() {
        this.createAsapWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask");

        // Business method
        this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testCreateSecondTaskAgainstTheSameDevice() {
        ScheduledConnectionTaskImpl firstTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDevice-1");
        ScheduledConnectionTaskImpl secondTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateSecondTaskAgainstTheSameDevice", this.partialScheduledConnectionTask2);

        // Both tasks should be successfully created
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
    }

    @Test
    @Transactional
    public void testCreateAgainstAnotherDeviceBasedOnTheSamePartialConnectionTask() {
        ScheduledConnectionTaskImpl firstTask = this.createAsapWithNoPropertiesWithoutViolations("testCreateAgainstAnotherDeviceBasedOnSamePartialConnectionTask-1");

        ScheduledConnectionTaskImpl secondTask = (ScheduledConnectionTaskImpl) this.otherDevice.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();

        // Both tasks should be successfully created with the same name
        assertThat(firstTask).isNotNull();
        assertThat(secondTask).isNotNull();
        assertThat(secondTask.getName()).isEqualTo(firstTask.getName());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_MINIMIZE_STRATEGY_NOT_COMPATIBLE_WITH_SIMULTANEOUS_CONNECTIONS + "}")
    public void testCreateMinimizeConnectionsWithSimultaneous() {
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testCreateMinimizeConnectionsWithSimultaneous", new TemporalExpression(EVERY_DAY));
        connectionTask.setNumberOfSimultaneousConnections(2);

        // Business method
        device.save();

        // Asserts
        // assertThat(e.getMessageId()).isEqualTo("simultaneousConnectionsNotAllowedForMinimizeConnectionStrategy");
    }

    @Test
    @Transactional
    public void testCreateWithAllIpProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        ComPortPool comPortPool = connectionTask.getComPortPool();
        assertThat(comPortPool).isNotNull();
        assertThat(comPortPool.getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionTask.getPluggableClass().getConnectionType()).isEqualTo(outboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
    }

    private ScheduledConnectionTaskImpl createASimpleScheduledConnectionTask() {
        return (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                .add();
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndNoDefaultsOnPluggableClass() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionTask.getPluggableClass().getConnectionType()).isEqualTo(outboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(1);   // Only 1 property is locally defined and higher levels do not specify any property values
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(1);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isNull();
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndSomeDefaultsOnPluggableClass() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        // First update the properties of the ipConnectionType pluggable class
        outboundIpConnectionTypePluggableClass.removeProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get());
        outboundIpConnectionTypePluggableClass.setProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), PORT_PROPERTY_VALUE);
        outboundIpConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionTask.getPluggableClass().getConnectionType()).isEqualTo(outboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);   // 1 property is locally defined, 1 is inherited and the third is not specified at any level
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionProperties.PORT.propertyName())).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClass() {
        // First update the properties of the ipConnectionType pluggable class
        outboundIpConnectionTypePluggableClass.setProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get(), IP_ADDRESS_PROPERTY_VALUE);
        outboundIpConnectionTypePluggableClass.setProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), PORT_PROPERTY_VALUE);
        outboundIpConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        // Do not set any properties on the ScheduledConnectionTask

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionTask.getPluggableClass().getConnectionType()).isEqualTo(outboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);   // no properties are locally defined, all 2 are inherited from the connection type pluggable class
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionProperties.IP_ADDRESS.propertyName())).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionProperties.PORT.propertyName())).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClassAndPartialConnectionTask() {
        // First update the properties of the ipConnectionType pluggable class
        outboundIpConnectionTypePluggableClass.setProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get(), IP_ADDRESS_PROPERTY_VALUE);
        outboundIpConnectionTypePluggableClass.setProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), PORT_PROPERTY_VALUE);
        outboundIpConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialScheduledConnectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), UPDATED_PORT_PROPERTY_VALUE);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        // Do not set any properties on the ScheduledConnectionTask

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionTask.getPluggableClass().getConnectionType()).isEqualTo(outboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);   // 2 properties are inherited from the partial connection task and 1 is inherited from the connection type pluggable class
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionProperties.IP_ADDRESS.propertyName())).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionProperties.PORT.propertyName())).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromPartialConnectionTask() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), IP_ADDRESS_PROPERTY_VALUE);
        partialScheduledConnectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), PORT_PROPERTY_VALUE);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        // Do not set any properties on the ScheduledConnectionTask

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(outboundTcpipComPortPool.getId());
        assertThat(connectionTask.getPluggableClass().getConnectionType()).isEqualTo(outboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);   // no properties are locally defined, all 2 are inherited from the partial connection task
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionProperties.IP_ADDRESS.propertyName())).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionProperties.PORT.propertyName())).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateIpConnectionTypeProperty() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();

        connectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(2);  // Ip is default and has 2 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testAddIpConnectionTypeProperty() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);

        connectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), PORT_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(2);  // Ip is default and has 2 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testRemoveIpConnectionTypeProperty() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

        connectionTask.removeProperty(IpConnectionProperties.PORT.propertyName());

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(1);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(1);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isNull();
    }

    @Test
    @Transactional
    public void testReturnToInheritedProperty() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        // First update the properties of the ipConnectionType pluggable class
        outboundIpConnectionTypePluggableClass.removeProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get());
        outboundIpConnectionTypePluggableClass.setProperty(outboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), UPDATED_PORT_PROPERTY_VALUE);
        outboundIpConnectionTypePluggableClass.save();

        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (createASimpleScheduledConnectionTask());
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();

        connectionTask.removeProperty(IpConnectionProperties.PORT.propertyName());

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(2);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionProperties.PORT.propertyName())).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COMPORT_TYPE_NOT_SUPPORTED + "}")
    public void testCreateWithIpWithModemComPortPool() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();

        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundModemComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                .add();
        device.save();

        this.setIpConnectionProperties(scheduledConnectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

        // Business method
        scheduledConnectionTask.update();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testGetPropertiesOnMultipleDates() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        // Create task with properties on may first 2011
        Instant mayFirst2011 = freezeClock(2011, Calendar.MAY, 1);
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = createASimpleScheduledConnectionTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);
        connectionTask.saveAllProperties();

        freezeClock(2012, Calendar.MAY, 1);
        connectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        connectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), UPDATED_PORT_PROPERTY_VALUE);

        // Business method
        connectionTask.saveAllProperties();

        // Asserts
        List<ConnectionTaskProperty> allPropertiesOnMayFirst2011 = connectionTask.getProperties(mayFirst2011);
        assertThat(allPropertiesOnMayFirst2011).hasSize(1); // On May 1st, 2011 only the ip address was specified
        ConnectionTaskProperty property = allPropertiesOnMayFirst2011.get(0);
        assertThat(property.getName()).isEqualTo(IpConnectionProperties.IP_ADDRESS.propertyName());
        assertThat(property.getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testGetPropertiesOnMultipleDatesAfterReload() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        // Create task with properties on may first 2011
        Instant mayFirst2011 = freezeClock(2011, Calendar.MAY, 1);
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        ScheduledConnectionTaskImpl connectionTask = (createASimpleScheduledConnectionTask());
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);
        connectionTask.saveAllProperties();

        freezeClock(2012, Calendar.MAY, 1);
        connectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        connectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), UPDATED_PORT_PROPERTY_VALUE);
        connectionTask.saveAllProperties();

        // Business method
        ScheduledConnectionTaskImpl reloaded = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get());

        // Asserts
        List<ConnectionTaskProperty> allPropertiesOnMayFirst2011 = reloaded.getProperties(mayFirst2011);
        assertThat(allPropertiesOnMayFirst2011).hasSize(1); // On May 1st, 2011 only the ip address was specified
        ConnectionTaskProperty property = allPropertiesOnMayFirst2011.get(0);
        assertThat(property.getName()).isEqualTo(IpConnectionProperties.IP_ADDRESS.propertyName());
        assertThat(property.getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING + "}")
    public void testCreateWithMissingRequiredProperty() {
        partialScheduledConnectionTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialScheduledConnectionTask.save();
        TypedProperties.inheritingFrom(outboundIpConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs()));
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .add();

        this.setIpConnectionProperties(scheduledConnectionTask, null, PORT_PROPERTY_VALUE);

        // Business method
        scheduledConnectionTask.update();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC + "}")
    public void testCreateWithNonExistingProperty() {
        ScheduledConnectionTaskImpl connectionTask = (createASimpleScheduledConnectionTask());
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        // Add values for non existing property
        connectionTask.setProperty("doesNotExist", "I don't care");

        // Business method
        device.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testCreateWithCommunicationWindowWithoutViolations() {
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testCreateWithCommunicationWindowWithoutViolations");

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNull();
        assertThat(connectionTask.getCommunicationWindow()).isNotNull();
        assertThat(connectionTask.getProperties().isEmpty()).as("Was not expecting any properties on the Outbound Connection Task").isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW + "}")
    public void createWithoutOffsetNotWithinCommunicationWindow() {
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "createWithoutOffsetAndCommunicationWindow",
                        new TemporalExpression(EVERY_DAY));
        connectionTask.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        device.save();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_OFFSET_IS_NOT_WITHIN_WINDOW + "}")
    public void createWithOffsetWithinDayButOutsideCommunicationWindow() {
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations("createWithOffsetWithinDayButOutsideCommunicationWindow",
                                new TemporalExpression(
                                        EVERY_DAY,
                                        new TimeDuration(12, TimeDuration.TimeUnit.HOURS))
                        );
        connectionTask.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        device.save();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void createWithOffsetOutsideDayButWithinCommunicationWindow() {
        String name = "createWithOffsetOutsideDayButWithinCommunicationWindow";
        partialScheduledConnectionTask.setName(name);
        partialScheduledConnectionTask.save();
        // Set it to execute every week, at 01:30 (am) of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.WEEKS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_HOUR * 25 + DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.TimeUnit.SECONDS);
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations(name, new TemporalExpression(frequency, offset));

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
    public void createWithOffset() {
        String name = "createWithOffset";
        PartialScheduledConnectionTask partial = deviceConfiguration.newPartialScheduledConnectionTask(name, outboundNoParamsConnectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.MINIMIZE_CONNECTIONS).nextExecutionSpec().temporalExpression(TimeDuration.days(1)).set().build();
        partial.save();
        // Set it to execute every week, at 01:30 (am) of the second day of the week
        TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.MONTHS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.TimeUnit.SECONDS);
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(partial)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(new TemporalExpression(frequency, offset))
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE)
                .setNumberOfSimultaneousConnections(1)
                .add();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression()).isNotNull();
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getEvery()).isEqualTo(frequency);
        assertThat(connectionTask.getNextExecutionSpecs().getTemporalExpression().getOffset()).isEqualTo(offset);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_LONG_OFFSET_IS_NOT_WITHIN_WINDOW + "}")
    public void createWithOffsetOutsideDayAndOutsideCommunicationWindow() {
        TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.WEEKS);
        TimeDuration offset = new TimeDuration(DateTimeConstants.SECONDS_PER_HOUR * 24 + DateTimeConstants.SECONDS_PER_MINUTE * 30, TimeDuration.TimeUnit.SECONDS);
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "createWithOffsetOutsideDayAndOutsideCommunicationWindow",
                        new TemporalExpression(frequency, offset));
        connectionTask.setCommunicationWindow(FROM_ONE_AM_TO_TWO_AM);

        // Business method
        device.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void setDefaultConnectionWithObsoleteComTaskExecutionsTest() {
        ComTaskExecution comTaskExecution = createComTaskExecution();
        this.device.removeComTaskExecution(comTaskExecution);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("setDefaultConnectionWithObsoleteComTaskExecutionsTest");

        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        ComTaskExecution reloadedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.OUTBOUND_CONNECTION_TASK_NEXT_EXECUTION_SPECS_REQUIRED + "}")
    public void testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs() {
        // Business method
        this.createWithoutNextExecutionSpecs(ConnectionStrategy.MINIMIZE_CONNECTIONS, "testCreateWithMinimizeConnectionsAndNoNextExecutionSpecs");

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void testCreateWithMinimizeConnectionsWithNextExecutionSpecs() {
        // Business method
        device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(new TemporalExpression(new TimeDuration("5 minutes")))
                .add();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED + "}")
    @Transactional
    public void testCreateWithoutComPortPoolButIncompleteStatus() {
        // Business method
        device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(null)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                .add();
    }


    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED + "}")
    @Transactional
    public void testCreateWithoutComPortPool() {
        // Business method
        device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(null)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED + "}")
    public void testUpdateWithoutPoolTest() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("ShouldFailWithUpdate");
        ScheduledConnectionTask reloadedConnectionTask = (ScheduledConnectionTask) inMemoryPersistence.getConnectionTaskService().findConnectionTask(connectionTask.getId()).get();
        reloadedConnectionTask.setComPortPool(null);
        ((ScheduledConnectionTaskImpl) reloadedConnectionTask).update();
    }

    @Test
    @Transactional
    public void testNumberOfSimultaneousConnections() {
        // First one - allow simultaneous connections
        ScheduledConnectionTaskImpl outboundTrue = (createASimpleScheduledConnectionTask());
        outboundTrue.setNumberOfSimultaneousConnections(2);
        device.save();

        // second one - deny simultaneous connections
        ScheduledConnectionTaskImpl outboundFalse = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask2)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();

        outboundFalse.setNumberOfSimultaneousConnections(1);
        device.save();

        // Asserts
        assertThat(outboundTrue.getNumberOfSimultaneousConnections()).isEqualTo(2);
        assertThat(outboundFalse.getNumberOfSimultaneousConnections()).isEqualTo(1);
    }

    @Test
    @Transactional
    public void testLoad() {
        ScheduledConnectionTaskImpl created = this.createAsapWithNoPropertiesWithoutViolations("testLoad");

        // Business method
        ScheduledConnectionTask loaded = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(created.getId()).get();

        // Asserts
        assertThat(created.getDevice().getId()).isEqualTo(loaded.getDevice().getId());
        assertThat(created.getComPortPool().getId()).isEqualTo(loaded.getComPortPool().getId());
        assertThat(created.getConnectionStrategy()).isEqualTo(loaded.getConnectionStrategy());
        assertThat(created.getCommunicationWindow()).isEqualTo(loaded.getCommunicationWindow());
        assertThat(created.getNextExecutionSpecs()).isEqualTo(loaded.getNextExecutionSpecs());
        assertThat(created.getInitiatorTask()).isEqualTo(loaded.getInitiatorTask());
        assertThat(created.getNumberOfSimultaneousConnections()).isEqualTo(loaded.getNumberOfSimultaneousConnections());
        assertThat(created.getNextExecutionTimestamp()).isEqualTo(loaded.getNextExecutionTimestamp());
        assertThat(created.getPlannedNextExecutionTimestamp()).isEqualTo(loaded.getPlannedNextExecutionTimestamp());
    }

    @Test
    @Transactional
    public void testUpdate() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testUpdate");

        connectionTask.setCommunicationWindow(FROM_TEN_PM_TO_TWO_AM);
        connectionTask.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        TimeDuration elevenPM = new TimeDuration(23, TimeDuration.TimeUnit.HOURS);
        connectionTask.setNextExecutionSpecsFrom(new TemporalExpression(EVERY_DAY, elevenPM));

        // Business method
        connectionTask.update();

        // Asserts
        ScheduledConnectionTaskImpl updated = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get());
        assertThat(ConnectionStrategy.MINIMIZE_CONNECTIONS).isEqualTo(updated.getConnectionStrategy());
        assertThat(FROM_TEN_PM_TO_TWO_AM).isEqualTo(updated.getCommunicationWindow());
        assertThat(EVERY_DAY).isEqualTo(updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
        assertThat(elevenPM).isEqualTo(updated.getNextExecutionSpecs().getTemporalExpression().getOffset());
    }

    @Test
    @Transactional
    public void testSwitchToAsapStrategyShouldRemoveNextExecSpec() {
        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testSwitchToAsapStrategyAndRemoveNextExecSpec",
                        new TemporalExpression(EVERY_HOUR));

        connectionTask.setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE);

        // Business method
        connectionTask.update();
        ScheduledConnectionTask updated = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        // Asserts
        assertThat(updated.getConnectionStrategy()).isEqualTo(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        assertThat(updated.getNextExecutionSpecs()).isNull();
    }

    @Test
    @Transactional
    public void testSwitchToMinimizeConnectionStrategy() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testSwitchToMinimizeConnectionStrategy");

        connectionTask.setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        connectionTask.setNextExecutionSpecsFrom(new TemporalExpression(EVERY_HOUR));

        // Business method
        connectionTask.update();
        ScheduledConnectionTaskImpl updated = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get());

        // Asserts
        assertThat(updated.getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        assertThat(updated.getNextExecutionSpecs()).isNotNull();
        assertThat(EVERY_HOUR).isEqualTo(updated.getNextExecutionSpecs().getTemporalExpression().getEvery());
    }

    @Test
    @Transactional
    public void testScheduleNow() {
        Instant mayLast2012 = freezeClock(2012, Calendar.MAY, 31);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testScheduleNow");

        // Business method
        connectionTask.scheduleNow();

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(mayLast2012);
    }

    @Test
    @Transactional
    public void testScheduleOnDate() {
        Instant mayLast2012 = freezeClock(2012, Calendar.MAY, 31);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testScheduleOnDate");

        // Business method
        connectionTask.schedule(mayLast2012);

        // Asserts
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(mayLast2012);
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionTimestampForUTCDevice() {
        Instant expectedNextExecutionTimestamp = freezeClock(2012, Calendar.MAY, 31, 1, 0, 0, 0);     // Frequency of rescheduling is 1 hour
        freezeClock(2011, Calendar.MAY, 31);    // Anything, as long as it is not 2012, May 31st - the data that is set below just after the save

        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testUpdateNextExecutionTimestampForUTCDevice", new TemporalExpression(EVERY_HOUR));

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

        Instant mayLast2012 = freezeClock(2012, Calendar.MAY, 31, usPacific);    // This is in UTC

        Instant expectedNextExecutionTimestamp = mayLast2012.plus(java.time.Duration.ofHours(1));

        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testUpdateNextExecutionTimestampForUSPacificDevice",
                        new TemporalExpression(EVERY_HOUR));

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
        Instant oneMinuteBeforeDST = freezeClock(2011, Calendar.MARCH, 27, 1, 0, 0, 0);

        Calendar calendar = Calendar.getInstance(brussels);
        calendar.set(2011, Calendar.MARCH, 27, 2, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Instant expectedNextExecutionTimestamp = calendar.getTime().toInstant();
//        when(this.device.getDeviceTimeZone()).thenReturn(brussels);

        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testUpdateNextExecutionTimestampOnDSTFromWinterToSummer",
                        new TemporalExpression(EVERY_HOUR));

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
        Instant oneMinuteBeforeDST = freezeClock(2011, Calendar.OCTOBER, 30, 1, 0, 0, 0);

        Calendar calendar = Calendar.getInstance(brussels);
        calendar.set(2011, Calendar.OCTOBER, 30, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR, 3);     // The calendar is now at 3 am
        calendar.add(Calendar.HOUR, 1);     // Frequency of rescheduling is 1 hour

        Instant expectedNextExecutionTimestamp = calendar.getTime().toInstant();
        //TODO check to update these tests!
//        when(this.device.getDeviceTimeZone()).thenReturn(brussels);

        ScheduledConnectionTaskImpl connectionTask =
                this.createMinimizeWithNoPropertiesWithoutViolations(
                        "testUpdateNextExecutionTimestampOnDSTFromSummerToWinter",
                        new TemporalExpression(EVERY_HOUR));

        // Business method
        connectionTask.updateNextExecutionTimestamp();

        // Asserts
        assertThat(connectionTask.getPlannedNextExecutionTimestamp()).isEqualTo(expectedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testDeleteWithNoProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testDeleteWithNoProperties");
        long id = connectionTask.getId();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(id).get().isObsolete()).isTrue();
    }

    @Test
    @Transactional
    public void testDeleteWithProperties() {
        Instant now = this.freezeClock(2015, Calendar.MAY, 2);
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testDeleteWithProperties");
        long id = connectionTask.getId();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(id).get().isObsolete()).isTrue();
        CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet = inboundIpConnectionTypePluggableClass.getConnectionType()
                .getCustomPropertySet()
                .get();
        assertThat(inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now).isEmpty()).isTrue();
        // Todo: assert that old values were journalled properly but need support from CustomPropertySetService first
    }

    @Test(expected = ConnectionTaskIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenComServerIsExecutingTest() throws SQLException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("makeObsoleteWhenComServerIsExecutingTest");

        this.attemptLock(connectionTask);

        // Business method
        device.removeConnectionTask(connectionTask);
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithNoProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testMakeObsoleteWithNoProperties");

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isObsolete()).isTrue();
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testMakeObsoleteAlsoMakesRelationsObsolete() {
        Instant now = this.freezeClock(2015, Calendar.MAY, 2);
        ScheduledConnectionTaskImpl connectionTask = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testMakeObsoleteAlsoMakesRelationsObsolete");

        CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet = outboundIpConnectionTypePluggableClass.getConnectionType()
                .getCustomPropertySet()
                .get();
        assertThat(inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now).isEmpty()).isFalse();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isObsolete()).isTrue();
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
        assertThat(inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now).isEmpty()).isTrue();
        // Todo: assert that old values were journalled properly but need support from CustomPropertySetService first
    }

    @Test
    @Transactional
    public void testIsObsoleteAfterReload() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testIsObsoleteAfterReload");

        long id = connectionTask.getId();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        ScheduledConnectionTask obsolete = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(id).get();
        assertThat(obsolete).as("The ConnectionTask should be marked for delete, but still present in DB").isNotNull();
        assertThat(obsolete.isObsolete()).isTrue();
        assertThat(obsolete.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testUpdateDeviceWithObsoleteConnectionTask() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testUpdateAfterMakeObsolete");

        device.removeConnectionTask(connectionTask);

        device = getReloadedDevice(device);
        // Business method
        device.setName("AnotherName");
        device.save();

        // Make sure the device can be changed
    }

    @Test
    @Transactional
    public void testUpdateDeviceWithOtherDefaultConnectionTask() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testUpdateAfterMakeObsolete");

        device.removeConnectionTask(connectionTask);

        device = getReloadedDevice(device);
        ScheduledConnectionTaskImpl replacement = this.createAsapWithNoPropertiesWithoutViolations("testNewDefault");
        // Business method
        device.save();

        // Make sure the device can be changed
    }

    @Test
    @Transactional
    public void testMakeObsoleteTwice() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testMakeObsoleteTwice");

        device.removeConnectionTask(connectionTask);

        // Business method
        device.removeConnectionTask(connectionTask);
    }

    @Test(expected = OptimisticLockException.class)
    @Transactional
    public void makeObsoleteWhenSomeOneElseMadeItObsoleteTest() throws SQLException {
        final ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("makeObsoleteWhenSomeOneElseMadeItObsoleteTest");

        int updateCount = inMemoryPersistence.update(this.getUpdateObsoleteDateSqlBuilder(connectionTask.getId()));
        if (updateCount != 1) {
            throw new SQLException("updated zero rows");
        }

        // Business method
        device.removeConnectionTask(connectionTask);
    }

    private SqlBuilder getUpdateObsoleteDateSqlBuilder(long id) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" set obsolete_date = ?, ");
        sqlBuilder.append(" versioncount = 2");
        sqlBuilder.append(" where id = ?");
        sqlBuilder.bindDate(inMemoryPersistence.getClock().instant());
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

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, inMemoryPersistence.getClock().instant());
        assertThat(comTaskExecution.getConnectionTask()).isNotNull();
        // Business method
        device.removeConnectionTask(connectionTask);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    public void testDeletedAndSetComTaskToNoConnectionTask() {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testDeletedAndSetComTaskToNoConnectionTask");
        ComTaskExecution comTaskExecution = createComTaskExecution();

        // Business method
        this.device.removeComTaskExecution(comTaskExecution);

        // Asserts
        assertThat(comTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    public void testFindConnectionTaskByDeviceAfterDelete() {
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testFindOutboundByDeviceAfterDelete", new TemporalExpression(EVERY_HOUR));

        List<ConnectionTask> outboundByDeviceBeforeDelete = inMemoryPersistence.getConnectionTaskService().findConnectionTasksByDevice(connectionTask.getDevice());

        // Business methods
        this.device.removeConnectionTask(connectionTask);
        List<ConnectionTask> outboundByDeviceAfterDelete = inMemoryPersistence.getConnectionTaskService().findConnectionTasksByDevice(connectionTask.getDevice());

        // Asserts
        this.assertConnectionTask(outboundByDeviceBeforeDelete, connectionTask);
        assertThat(outboundByDeviceAfterDelete).isEmpty();
    }

    @Test
    @Transactional
    public void createMultipleOutboundsForSpecificDeviceWithoutViolations() {
        ScheduledConnectionTaskImpl task1 =
                this.createAsapWithNoPropertiesWithoutViolations("createMultipleOutboundsForSpecificDeviceWithoutViolations-1");

        ScheduledConnectionTaskImpl task2 =
                this.createAsapWithNoPropertiesWithoutViolations(
                        "createMultipleOutboundsForSpecificDeviceWithoutViolations-2",
                        this.partialScheduledConnectionTask2
                );

        List<ConnectionTask> outboundConnectionTasks = inMemoryPersistence.getConnectionTaskService().findConnectionTasksByDevice(this.device);

        // asserts
        assertThat(task1).isNotNull();
        assertThat(task2).isNotNull();
        this.assertConnectionTask(outboundConnectionTasks, task1, task2);
    }

    @Test
    @Transactional
    public void testApplyComWindowWhenTaskDoesNotHaveAComWindow() throws SQLException {
        Instant nextExecutionTimestamp = inMemoryPersistence.getClock().instant();
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithoutNextExecutionSpecs", null);

        // Business method
        Instant modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(nextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testApplyComWindowWithNextExecutionTimeStampThatImmediatelyFallsWithinComWindow() throws SQLException {
        this.toRestore = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampThatImmediatelyFallsWithinComWindow", FROM_ONE_AM_TO_TWO_AM);

        Instant nextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 1, 30, 0, 0);   // UTC

        // Business method
        Instant modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts; 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(nextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testApplyComWindowWithNextExecutionTimeStampBeforeComWindow() throws SQLException {
        this.toRestore = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampBeforeComWindow", FROM_ONE_AM_TO_TWO_AM);

        Instant nextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 0, 30, 0, 0);
        Instant expectedModifiedNextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 1, 0, 0, 0);

        // Business method
        Instant modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts; 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(expectedModifiedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testApplyComWindowWithNextExecutionTimeStampAfterComWindow() throws SQLException {
        this.toRestore = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ScheduledConnectionTaskImpl connectionTask = this.createWithCommunicationWindowWithoutViolations("testApplyComWindowWithNextExecutionTimeStampAfterComWindow", FROM_ONE_AM_TO_TWO_AM);

        Instant nextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 9, 2, 30, 0, 0);
        Instant expectedModifiedNextExecutionTimestamp = freezeClock(2013, Calendar.JANUARY, 10, 1, 0, 0, 0);

        // Business method
        Instant modifiedNextExecutionTimestamp = connectionTask.applyComWindowIfAny(nextExecutionTimestamp);

        // Asserts: 01:30 is already in the ComWindow so we are NOT expecting any modifications
        assertThat(modifiedNextExecutionTimestamp).isEqualTo(expectedModifiedNextExecutionTimestamp);
    }

    @Test
    @Transactional
    public void testAttemptLock() {
        String name = "testAttemptLock";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

        // Business method
        ScheduledConnectionTask lockedConnectionTask = this.attemptLock(connectionTask);

        // Asserts
        assertThat(lockedConnectionTask.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void testUnlock() {
        ServerConnectionTaskService connectionTaskService = inMemoryPersistence.getConnectionTaskService();
        String name = "testUnlock";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        ScheduledConnectionTaskImpl lockedConnectionTask = connectionTaskService.attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Business method
        connectionTaskService.unlockConnectionTask(lockedConnectionTask);

        // Asserts
        assertThat(connectionTask.getExecutingComServer()).isNull();
    }

    @Test
    @Transactional
    public void testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer() {
        String name = "testAttemptLockWhenAlreadyLockedByTheSameComServer";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

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

        ScheduledConnectionTask lockedConnectionTask = this.attemptLock(connectionTask, this.getOnlineComServer());

        // Business method
        ScheduledConnectionTask shouldBeNull = this.attemptLock(connectionTask, this.getOtherOnlineComServer());

        // Asserts
        assertThat(shouldBeNull).isNull();
        assertThat(lockedConnectionTask.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void pauseIfNotPausedTest() throws SQLException {
        String name = "pauseIfNotPausedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);

        // Business method
        connectionTask.deactivate();
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }

    @Test
    @Transactional
    public void pauseWhenAlreadyPausedTest() throws SQLException {
        String name = "pauseWhenAlreadyPausedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

        // business method
        connectionTask.deactivate();
        connectionTask.deactivate();
        connectionTask.deactivate();
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }

    @Test
    @Transactional
    public void resumeWhenPausedTest() throws SQLException {
        String name = "resumeWhenPausedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);
        connectionTask.deactivate();

        // business method
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();
        reloadedConnectionTask.activate();
        reloadedConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedConnectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Test
    @Transactional
    public void resumeWhenAlreadyResumedTest() throws SQLException {
        String name = "resumeWhenAlreadyResumedTest";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

        // business method
        connectionTask.activate();
        ConnectionTask reloadedConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(connectionTask.getStatus()).isEqualTo(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testConnectWithOtherProperties() throws SQLException, ConnectionException {
        String name = "testConnectWithOtherProperties";
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations(name);

        ConnectionTaskProperty connectionTaskProperty = mock(ConnectionTaskProperty.class);

        // business method
        connectionTask.connect(mock(ComPort.class), Arrays.asList(connectionTaskProperty));

        // Asserts: see expected exception rule
    }

/* Todo: Enable once communication session objects have been ported to this bundle
    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessions() throws SQLException {
        ConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetLastComSessionWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache() throws SQLException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithoutComSessionsWithActiveComSessionCache() throws SQLException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithoutComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithoutComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessions() throws SQLException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithComSessions");
        this.doTestGetLastComSessionWithComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessionsWithActiveLastComSessionCache() throws SQLException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveLastComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveLastComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastComSessionWithComSessionsWithActiveComSessionCache() throws SQLException {
        ConnectionTask connectionTask = (ConnectionTask) this.createAsapWithNoPropertiesWithoutViolations("testGetComSessionsWithComSessionsWithActiveComSessionCache");
        this.doTestGetLastComSessionWithComSessionsWithActiveComSessionCache(connectionTask);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithoutComSessions() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithoutComSessions");
        this.doTestGetSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithSuccessfulLastComSession() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, true);
    }

    @Test
    @Transactional
    public void testGetSuccessIndicatorWithFailedLastComSession() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetSuccessIndicatorWithFailedLastComSession");
        this.doTestGetSuccessIndicatorWithComSessions(connectionTask, false);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithoutComSessions() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithoutComSessions");
        this.doTestGetLastSuccessIndicatorWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithSuccessfulLastComSession() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSuccessfulLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Success);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithSetupErrorLastComSession() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithSetupErrorLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.SetupError);
    }

    @Test
    @Transactional
    public void testGetLastSuccessIndicatorWithBrokenLastComSession() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastSuccessIndicatorWithBrokenLastComSession");
        this.doTestGetLastSuccessIndicatorWithComSessions(connectionTask, ComSession.SuccessIndicator.Broken);
    }

    @Test
    @Transactional
    public void testGetLastTaskSummaryWithoutComSessions() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithoutComSessions(connectionTask);
    }

    @Test
    @Transactional
    public void testGetLastTaskSummaryWithComSessions() throws SQLException {
        ScheduledConnectionTask connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testGetLastTaskSummaryWithoutComSessions");
        this.doTestGetLastTaskSummaryWithComSessions(connectionTask, 4, 1, 1);
    }
*/

    @Test
    @Transactional
    public void testTriggerWithMinimizeStrategy() {
        ScheduledConnectionTaskImpl connectionTask = this.createMinimizeWithNoPropertiesWithoutViolations("testTriggerWithMinimizeStrategy", new TemporalExpression(EVERY_HOUR));

        Instant comTaskDate = freezeClock(2013, Calendar.JUNE, 2);
        Instant triggerDate = freezeClock(2013, Calendar.JUNE, 3);
        EarliestNextExecutionTimeStampAndPriority earliestNextExecutionTimestampAndPriority = new EarliestNextExecutionTimeStampAndPriority(triggerDate, TaskPriorityConstants.DEFAULT_PRIORITY);

        ComTaskExecution comTaskExecution = createComTaskExecutionWithConnectionTaskAndSetNextExecTimeStamp(connectionTask, comTaskDate);

        // Business method
        connectionTask.trigger(triggerDate);

        // Asserts
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(triggerDate.plusSeconds(3600));
    }

    @Test
    @Transactional
    public void testSwitchFromInboundDefault() throws SQLException {
        InboundConnectionTaskImpl inboundConnectionTask = this.createSimpleInboundConnectionTask();

        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(inboundConnectionTask);

        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testSwitchFromOutboundDefault");

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        // Asserts
        InboundConnectionTask reloadedInbound = inMemoryPersistence.getConnectionTaskService().findInboundConnectionTask(inboundConnectionTask.getId()).get();
        ScheduledConnectionTaskImpl reloadedScheduled = ((ScheduledConnectionTaskImpl) inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get());

        assertThat(reloadedInbound.isDefault()).isFalse();
        assertThat(reloadedScheduled.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testSetAsDefaultWithoutOtherDefaults() throws SQLException {
        ScheduledConnectionTaskImpl connectionTask = this.createAsapWithNoPropertiesWithoutViolations("testSetAsDefaultWithoutOtherDefaults");

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void defaultPartialShouldCreateDefaultTest() {
        this.partialScheduledConnectionTask.setDefault(true);
        this.partialScheduledConnectionTask.save();

        ScheduledConnectionTaskImpl myDefaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations("MyDefaultConnectionTask", this.partialScheduledConnectionTask);

        assertThat(myDefaultConnectionTask.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void defaultPartialShouldNotCreateDefaultWhenDeviceHasAlreadyADefaultConnectionTaskTest() {
        String myDefaultConnectionTaskName = "MyDefaultConnectionTask";
        ScheduledConnectionTaskImpl myDefaultConnectionTask = this.createAsapWithNoPropertiesWithoutViolations(myDefaultConnectionTaskName, this.partialScheduledConnectionTask2);
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(myDefaultConnectionTask);

        this.partialScheduledConnectionTask.setDefault(true);
        this.partialScheduledConnectionTask.save();

        String myNotDefaultConnectionTaskName = "ThisShouldNotBeDefault";
        ScheduledConnectionTaskImpl thisShouldNotBeDefault = this.createAsapWithNoPropertiesWithoutViolations(myNotDefaultConnectionTaskName, this.partialScheduledConnectionTask);
        Device device = inMemoryPersistence.getDeviceService().findDeviceById(myDefaultConnectionTask.getDevice().getId()).get();

        assertThat(device.getConnectionTasks().stream().filter(connectionTask -> connectionTask.getName().equals(myDefaultConnectionTaskName)).findFirst().get().isDefault()).isTrue();
        assertThat(device.getConnectionTasks().stream().filter(connectionTask -> connectionTask.getName().equals(myNotDefaultConnectionTaskName)).findFirst().get().isDefault()).isFalse();
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
        return inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, comServer);
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name) {
        return createAsapWithNoPropertiesWithoutViolations(name, this.partialScheduledConnectionTask);
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, PartialScheduledConnectionTask partialConnectionTask) {
        partialConnectionTask.setName(name);
        partialConnectionTask.save();
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
        return scheduledConnectionTask;
    }

    private ScheduledConnectionTaskImpl createMinimizeWithNoPropertiesWithoutViolations(String name, TemporalExpression temporalExpression) {
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(temporalExpression)
                .add();
        return scheduledConnectionTask;
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

        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = this.device.getScheduledConnectionTaskBuilder(this.partialScheduledConnectionTask)
                .setComPortPool(outboundTcpipComPortPool);

        scheduledConnectionTaskBuilder.setConnectionStrategy(connectionStrategy);
        return (ScheduledConnectionTaskImpl) scheduledConnectionTaskBuilder.add();
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask() {
        return this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask(PartialInboundConnectionTask partialConnectionTask) {
        return createSimpleInboundConnectionTask(partialConnectionTask, inboundTcpipComPortPool);
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask(final PartialInboundConnectionTask partialConnectionTask, final InboundComPortPool inboundComPortPool) {
        return (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(inboundComPortPool)
                .add();
    }

}
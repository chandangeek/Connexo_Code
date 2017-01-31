/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.exceptions.DuplicateConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InboundConnectionTaskImplIT extends ConnectionTaskImplIT {

    @Test
    @Transactional
    public void createWithoutViolations() {
        InboundConnectionTask inboundConnectionTask = this.createSimpleInboundConnectionTask();

        // asserts
        assertThat(inboundConnectionTask).isNotNull();
        assertThat(inboundConnectionTask.getDevice()).isEqualTo(this.device);
        assertThat(inboundConnectionTask.getPartialConnectionTask()).isEqualTo(this.partialInboundConnectionTask);
        assertThat(inboundConnectionTask.getComPortPool()).isEqualTo(inboundTcpipComPortPool);
        assertThat(inboundConnectionTask.getLastCommunicationStart()).isNull();
        assertThat(inboundConnectionTask.getLastSuccessfulCommunicationEnd()).isNull();
    }

    @Test
    @Transactional
    // Todo (JP-1125): Enable when ComTaskExecution has been moved to this bundle
    @Ignore
    public void testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault() {
/*
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        List<ComTaskExecution> comTasksThatRelyOnTheDefault = new ArrayList<>(1);
        comTasksThatRelyOnTheDefault.add(comTask);
        when(this.comTaskExecutionFactory.findForDefaultOutboundConnectionTask(this.device)).thenReturn(comTasksThatRelyOnTheDefault);
*/
        InboundConnectionTask inboundConnectionTask = createSimpleInboundConnectionTask();

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(inboundConnectionTask);

        // Asserts
        //verify(this.manager).defaultConnectionTaskChanged(this.device, inboundConnectionTask);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_COMPORT_POOL_REQUIRED + "}")
    public void createCreateWithoutPool() {
        // Business method
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask, null);

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE + "}")
    public void createTwoTasksWithTheSamePool() {
        this.createSimpleInboundConnectionTask();

        // Business method
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask2);

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void createTwoTasksWithDifferentPool() {
        this.createSimpleInboundConnectionTask();

        // Business method
        InboundConnectionTask inbound = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask2, inboundTcpipComPortPool2);

        // Asserts
        assertThat(inbound).isNotNull();
        assertThat(inbound.getDevice()).isEqualTo(this.device);
        assertThat(inbound.getPartialConnectionTask()).isEqualTo(this.partialInboundConnectionTask2);
        assertThat(inbound.getComPortPool()).isEqualTo(inboundTcpipComPortPool2);
    }

    @Test
    @Transactional
    public void createTaskWithInactivePool() {
        // Business method
        InboundConnectionTask inbound = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask, inactiveInboundTcpipComPortPool);

        // Asserts
        assertThat(inbound).isNotNull();
        assertThat(inbound.getDevice()).isEqualTo(this.device);
        assertThat(inbound.getPartialConnectionTask()).isEqualTo(this.partialInboundConnectionTask);
        assertThat(inbound.getComPortPool()).isEqualTo(inactiveInboundTcpipComPortPool);
    }


    @Test(expected = DuplicateConnectionTaskException.class)
    @Transactional
    public void createTwoTasksAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask() {
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);

        // Business method
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask, inboundTcpipComPortPool2);

        // Asserts: see expected exception rule
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    // Todo (JP-1122): enable this test when done
    @Ignore
    public void testCreateOfDifferentConfig() {
        PartialInboundConnectionTask partialInboundConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialInboundConnectionTask.getId()).thenReturn(PARTIAL_INBOUND_CONNECTION_TASK3_ID);
        when(partialInboundConnectionTask.getName()).thenReturn("testCreateOfDifferentConfig");
        when(partialInboundConnectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        when(partialInboundConnectionTask.getPluggableClass()).thenReturn(inboundNoParamsConnectionTypePluggableClass);

        // Business method
        this.createSimpleInboundConnectionTask(partialInboundConnectionTask);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void updateWithoutViolations() {
        InboundConnectionTaskImpl inboundConnectionTask = createSimpleInboundConnectionTask();

        // Business method
        inboundConnectionTask.setComPortPool(inboundTcpipComPortPool2);
        device.save();

        // Asserts
        assertThat(inboundConnectionTask).isNotNull();
        assertThat(inboundConnectionTask.getComPortPool()).isNotNull();
        assertThat(inboundConnectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL2_ID);
    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        InboundConnectionTask inboundConnectionTask = createSimpleInboundConnectionTask();

        // Asserts
        assertThat(inboundConnectionTask.getObsoleteDate()).isNull();
        assertThat(inboundConnectionTask.isObsolete()).isFalse();

        // Business method
        device.removeConnectionTask(inboundConnectionTask);

        // Asserts
        assertThat(inboundConnectionTask.getObsoleteDate()).isNotNull();
        assertThat(inboundConnectionTask.isObsolete()).isTrue();
    }

    @Test
    @Transactional
    public void testUpdateAfterObsolete() {
        InboundConnectionTaskImpl inboundConnectionTask = createSimpleInboundConnectionTask();
        inboundConnectionTask.makeObsolete();

        // Business method
        inboundConnectionTask.setComPortPool(inboundTcpipComPortPool2);
        device.save();
    }

    @Test
    @Transactional
    public void testUpdateDeviceWithObsoleteConnectionTask() {
        InboundConnectionTaskImpl connectionTask = createSimpleInboundConnectionTask();
        device.removeConnectionTask(connectionTask);

        device = getReloadedDevice(device);
        // Business method
        device.setName("AnotherName");
        device.save();
    }

    //@Test(expected = WhateverExceptionMakeObsoleteIsThrowing.class)
    @Transactional
    @Ignore
    // Todo: Wait for ComTaskExecution to be moved to this bundle and then create one that explicitly uses the InboundConnectionTaskImpl
    public void testCannotObsoleteDefaultTaskThatIsInUse() {
        InboundConnectionTaskImpl connectionTask = createSimpleInboundConnectionTask();
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        // Business method
        connectionTask.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    @Ignore
    // Todo: Wait for ComTaskExecution to be moved to this bundle and then create one that simple uses the default
    public void testDeletedAndSetComTaskToNoConnectionTask() {
        InboundConnectionTaskImpl connectionTask = createSimpleInboundConnectionTask();
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        ComTaskExecutionImpl obsoleteComTask = mock(ComTaskExecutionImpl.class);
        comTaskExecutions.add(obsoleteComTask);
        //when(this.comTaskExecutionFactory.findAllByConnectionTask(connectionTask)).thenReturn(comTaskExecutions);

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        verify(obsoleteComTask).connectionTaskRemoved();
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionTask(connectionTask.getId()).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testAttemptLock() {
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();

        // Business method
        InboundConnectionTask locked = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Asserts
        assertThat(locked).isNotNull();
        assertThat(locked.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void testUnlock() {
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Business method
        inMemoryPersistence.getConnectionTaskService().unlockConnectionTask(locked);

        // Asserts
        assertThat(locked.getExecutingComServer()).isNull();
    }

    @Test
    @Transactional
    public void testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer() {
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Business method
        InboundConnectionTask lockedForSecondTime = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Asserts
        assertThat(lockedForSecondTime).isNull();
        assertThat(locked.getExecutingComServer()).isNotNull();
    }

    @Test
    @Transactional
    public void testAttemptLockTwiceWillFail() {
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        /* Business method: here the test is a little different from testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer
         *                  because we are locking on the already locked ConnectionTask instead of the original connectionTask that is not locked in memory. */
        InboundConnectionTask lockedForSecondTime = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(locked, this.getOnlineComServer());

        // Asserts
        assertThat(lockedForSecondTime).isNull();
        assertThat(locked.getExecutingComServer()).isNotNull();
    }

    @Test
    @Transactional
    public void testAttemptLockWillFailWhenAlreadyLockedByAnotherComServer() {
        OnlineComServer otherComServer = this.createComServer("Other");
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, this.getOnlineComServer());

        // Business method
        InboundConnectionTask lockedForSecondTime = inMemoryPersistence.getConnectionTaskService().attemptLockConnectionTask(connectionTask, otherComServer);

        // Asserts
        assertThat(lockedForSecondTime).isNull();
        assertThat(locked.getExecutingComServer()).isNotNull();
        assertThat(locked.getExecutingComServer().getId()).isEqualTo(this.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void testCreateWithAllIpProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .add();


        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(inboundIpConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(2);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndSomeDefaultsOnPluggableClass() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        // First update the properties of the ipConnectionType pluggable class
        inboundIpConnectionTypePluggableClass.removeProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get());
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), PORT_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.save();

        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .add();

        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(inboundIpConnectionTypePluggableClass.getConnectionType());
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
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get(), IP_ADDRESS_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), PORT_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.save();

        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        connectionTask.setComPortPool(inboundTcpipComPortPool);
        // Do not add any properties to the ConnectionTask

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(inboundIpConnectionTypePluggableClass.getConnectionType());
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
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get(), IP_ADDRESS_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), PORT_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.save();

        TypedProperties partialConnectionTaskProperties = TypedProperties.inheritingFrom(inboundIpConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs()));
        partialConnectionTaskProperties.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskProperties.setProperty(IpConnectionProperties.PORT.propertyName(), UPDATED_PORT_PROPERTY_VALUE);
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialInboundConnectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), UPDATED_PORT_PROPERTY_VALUE);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        connectionTask.setComPortPool(inboundTcpipComPortPool);
        // Do not add any properties to the ConnectionTask

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getProperties()).hasSize(2);   // 2 properties are inherited from the partial connection task and 1 is inherited from the connection type pluggable class
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionProperties.IP_ADDRESS.propertyName())).isTrue();
        assertThat(connectionTask.getTypedProperties().getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(connectionTask.getTypedProperties().hasInheritedValueFor(IpConnectionProperties.PORT.propertyName())).isTrue();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesInheritedFromPartialConnectionTask() {
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), IP_ADDRESS_PROPERTY_VALUE);
        partialInboundConnectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), PORT_PROPERTY_VALUE);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        connectionTask.setComPortPool(inboundTcpipComPortPool);
        // Do not add any properties to the ConnectionTask

        // Business method
        device.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
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
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .add();
        device.save();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();

        // Business method
        connectionTask.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), UPDATED_IP_ADDRESS_PROPERTY_VALUE);
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
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .add();

        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, null);
        device.save();

        // Business method
        connectionTask.setProperty(IpConnectionProperties.PORT.propertyName(), PORT_PROPERTY_VALUE);
        device.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(2);  // Ip is default and has 2 properties
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(2);
        assertThat(typedProperties.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionProperties.PORT.propertyName())).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING + "}")
    @Transactional
    public void testRemoveIpConnectionTypeProperty() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                .add();

        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();
        connectionTask.activate();

        // Business method
        connectionTask.removeProperty(IpConnectionProperties.IP_ADDRESS.propertyName());
        device.save();
    }

    @Test
    @Transactional
    public void testReturnToInheritedProperty() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        // First update the properties of the ipConnectionType pluggable class
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.IP_ADDRESS.propertyName()).get(), IP_ADDRESS_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.setProperty(inboundIpConnectionTypePluggableClass.getPropertySpec(IpConnectionProperties.PORT.propertyName()).get(), UPDATED_PORT_PROPERTY_VALUE);
        inboundIpConnectionTypePluggableClass.save();

        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();

        // Business method
        connectionTask.removeProperty(IpConnectionProperties.PORT.propertyName());
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING + "}")
    public void testCreateWithMissingRequiredProperty() {
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .add();
        this.setIpConnectionProperties(connectionTask, null, PORT_PROPERTY_VALUE);

        // Business method
        connectionTask.update();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC + "}")
    public void testCreateWithNonExistingProperty() {
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundNoParamsConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        // Add values for non existing property
        connectionTask.setProperty("doesNotExist", "I don't care");

        // Business method
        device.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testDeleteWithNoProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        InboundConnectionTaskImpl connectionTask = this.createWithNoPropertiesWithoutViolations();
        long id = connectionTask.getId();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionTask(id).get().isObsolete()).isTrue();
    }

    @Test
    @Transactional
    public void testDeleteWithProperties() {
        Instant now = this.freezeClock(2015, Calendar.MAY, 2);
        InboundConnectionTaskImpl connectionTask = this.createInboundWithIpPropertiesWithoutViolations();
        long id = connectionTask.getId();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionTask(id).get().isObsolete()).isTrue();
        CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet = inboundIpConnectionTypePluggableClass.getConnectionType()
                .getCustomPropertySet()
                .get();
        assertThat(inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now).isEmpty()).isTrue();
        // Todo: assert that old values were journalled properly but need support from CustomPropertySetService first
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithNoProperties() {
        InboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isObsolete()).isTrue();
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testMakeObsoleteAlsoRemovesCustomProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        Instant now = this.freezeClock(2015, Calendar.MAY, 2);
        InboundConnectionTaskImpl connectionTask = this.createInboundWithIpPropertiesWithoutViolations();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isObsolete()).isTrue();
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
        CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet = inboundIpConnectionTypePluggableClass.getConnectionType()
                .getCustomPropertySet()
                .get();
        assertThat(inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now).isEmpty()).isTrue();
        // Todo: assert that old values were journalled properly but need support from CustomPropertySetService first
    }

    @Test
    @Transactional
    public void testSwitchFromOutboundDefault() throws SQLException {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        ScheduledConnectionTaskImpl outboundDefault = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testSwitchFromOutboundDefault");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(outboundDefault);

        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations();

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isDefault()).isTrue();
        // Reload the outbound default
        ScheduledConnectionTask oldDefault = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(outboundDefault.getId()).get();
        assertThat(oldDefault.isDefault()).isFalse();
    }

    @Test
    @Transactional
    public void testSetAsDefaultWithoutOtherDefault() throws SQLException {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations();

        // Business method
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testUnsetAsDefaultWithOtherConnectionTasks() throws SQLException {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations(false);
        ScheduledConnectionTask outboundDefault = this.createOutboundWithIpPropertiesWithoutViolations("testUnsetAsDefaultWithOtherConnectionTasks");
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(outboundDefault);

        // Business method
        inMemoryPersistence.getConnectionTaskService().clearDefaultConnectionTask(connectionTask.getDevice());

        // Need to reload the outbound default as the changes are done in the background
        ScheduledConnectionTask reloadedOutboundDefault = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(outboundDefault.getId()).get();

        // Asserts
        assertThat(connectionTask.isDefault()).isFalse();
        assertThat(reloadedOutboundDefault.isDefault()).isFalse();
    }

    @Test
    @Transactional
    public void testUnsetAsDefaultWithoutOtherDefaults() throws SQLException {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations(false);

        // Business method
        inMemoryPersistence.getConnectionTaskService().clearDefaultConnectionTask(connectionTask.getDevice());

        // Asserts
        assertThat(connectionTask.isDefault()).isFalse();
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask() {
        return this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask(PartialInboundConnectionTask partialConnectionTask) {
        return createSimpleInboundConnectionTask(partialConnectionTask, inboundTcpipComPortPool);
    }

    private InboundConnectionTaskImpl createSimpleInboundConnectionTask(final PartialInboundConnectionTask partialConnectionTask, final InboundComPortPool inboundComPortPool) {
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(inboundComPortPool)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                .add();
        return connectionTask;
    }

    private InboundConnectionTaskImpl createWithNoPropertiesWithoutViolations() {
        return this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
    }

    protected InboundConnectionTaskImpl createInboundWithIpPropertiesWithoutViolations() {
        return this.createInboundWithIpPropertiesWithoutViolations(false);
    }

    protected InboundConnectionTaskImpl createInboundWithIpPropertiesWithoutViolations(boolean defaultState) {
        partialInboundConnectionTask.setConnectionTypePluggableClass(inboundIpConnectionTypePluggableClass);
        partialInboundConnectionTask.save();
        InboundConnectionTaskImpl inboundConnectionTask = (InboundConnectionTaskImpl) this.device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setComPortPool(inboundTcpipComPortPool)
                .add();

        this.setIpConnectionProperties(inboundConnectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();
        if (defaultState) {
            inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(inboundConnectionTask);
        }
        return inboundConnectionTask;
    }

}
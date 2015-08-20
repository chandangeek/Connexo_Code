package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.google.common.collect.Range;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConnectionInitiationTaskImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-11 (16:37)
 */
public class ConnectionInitiationTaskImplIT extends ConnectionTaskImplIT {

    @Before
    public void getFirstProtocolDialectConfigurationPropertiesFromDeviceConfiguration() {
        this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
    }

    @Test
    @Transactional
    public void testCreateWithDifferentConnectionTypes() {
        // Create a first initiator task
        ConnectionInitiationTask firstInitiationTask = this.createWithoutPropertiesAndNoViolations();

        // Create a second initiator task with another connection type
        ConnectionInitiationTaskImpl secondInitiationTask = (ConnectionInitiationTaskImpl) device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask2)
                .setComPortPool(outboundTcpipComPortPool)
                .add();
        device.save();


        this.setIpConnectionProperties(secondInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

        // Business method
        secondInitiationTask.save();

        // Asserts
        assertThat(firstInitiationTask).isNotNull();
        assertThat(secondInitiationTask).isNotNull();
    }

    @Test
    @Transactional
    public void testCreateWithAllPropertiesAndNoViolations() {
        // Business method
        ConnectionInitiationTask connectionInitiationTask = this.createWithAllPropertiesAndNoViolations("testCreateWithAllPropertiesAndNoViolations");

        // Asserts
        assertThat(connectionInitiationTask).isNotNull();
        assertThat(connectionInitiationTask.getProperties()).hasSize(2);
        assertThat(connectionInitiationTask.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isNotNull();
        assertThat(connectionInitiationTask.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionInitiationTask.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNotNull();
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredPropertiesAndNoViolations() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask)
                .setComPortPool(outboundTcpipComPortPool)
                .add();
        device.save();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, null);

        // Business method
        connectionInitiationTask.save();

        // Asserts
        assertThat(connectionInitiationTask).isNotNull();
        assertThat(connectionInitiationTask.getProperties()).hasSize(1);
        assertThat(connectionInitiationTask.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isNotNull();
        assertThat(connectionInitiationTask.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionInitiationTask.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(connectionInitiationTask.getCurrentRetryCount()).isEqualTo(0);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING + "}")
    public void testCreateWithMissingRequiredProperty() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .add();
        device.save();
        this.setIpConnectionProperties(connectionInitiationTask, null, PORT_PROPERTY_VALUE);

        // Business method
        connectionInitiationTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    private ConnectionInitiationTaskImpl createSimpleConnectionInitiationTask() {
        return this.createSimpleConnectionInitiationTask(this.partialConnectionInitiationTask);
    }

    private ConnectionInitiationTaskImpl createSimpleConnectionInitiationTask(PartialConnectionInitiationTask partialConnectionInitiationTask) {
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask)
                .setComPortPool(outboundTcpipComPortPool)
                .add();
        device.save();
        return connectionInitiationTask;
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC + "}")
    public void testCreateWithNonExistingProperty() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundNoParamsConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        // Add values for non existing property
        connectionInitiationTask.setProperty("doesNotExist", "don't care");

        // Business method
        connectionInitiationTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COMPORT_TYPE_NOT_SUPPORTED + "}")
    public void testCreateWithIpWithModemComPortPool() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(modemNoParamsConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();

        // Business method
        connectionInitiationTask.save();

        // Expected BusinessException because the ComPortType of the ComPortPool is not supported by the ConnectionType
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    public void testCreateOfDifferentConfig() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("testCreateOfDifferentConfig");
        deviceConfigurationBuilder.isDirectlyAddressable(true);
        DeviceConfiguration deviceConfiguration2 = deviceConfigurationBuilder.add();
        deviceConfiguration2.activate();

        PartialConnectionInitiationTask partialConnectionInitiationTask = mock(PartialConnectionInitiationTask.class);
        when(partialConnectionInitiationTask.getId()).thenReturn(PARTIAL_CONNECTION_INITIATION_TASK2_ID);
        when(partialConnectionInitiationTask.getName()).thenReturn("testCreateOfDifferentConfig");
        when(partialConnectionInitiationTask.getConfiguration()).thenReturn(deviceConfiguration2);
        when(partialConnectionInitiationTask.getPluggableClass()).thenReturn(outboundNoParamsConnectionTypePluggableClass);

        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask(partialConnectionInitiationTask);

        // Business method
        connectionInitiationTask.save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testAddMissingProperties() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, null);
        connectionInitiationTask.save();

        // Business method
        connectionInitiationTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        connectionInitiationTask.save();

        // Asserts
        ConnectionInitiationTask updated = inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(connectionInitiationTask.getId()).get();
        assertThat(updated.getProperties()).hasSize(2);
        assertThat(updated.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isNotNull();
        assertThat(updated.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(updated.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNotNull();
        assertThat(updated.getProperty(IpConnectionType.PORT_PROPERTY_NAME).getValue()).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithoutProperties() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        connectionInitiationTask.save();

        // Business method
        connectionInitiationTask.makeObsolete();

        // Asserts
        assertTrue(connectionInitiationTask.isObsolete());
        assertNotNull(connectionInitiationTask.getObsoleteDate());
    }

    @Test
    @Transactional
    public void testIsObsoleteAfterReload() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        // Business methods
        connectionInitiationTask.makeObsolete();
        ConnectionInitiationTask reloaded = inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).get();

        // Asserts
        assertThat(reloaded).isNotNull();
        assertTrue(reloaded.isObsolete());
        assertNotNull(reloaded.getObsoleteDate());
    }

    @Test
    @Transactional
    public void testMakeObsoleteAlsoMakesRelationsObsolete() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionTask.save();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertNotNull(connectionTask.getObsoleteDate());
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), false)).isEmpty();
        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), true)).hasSize(1);
    }

    @Test
    @Transactional
    public void testReallyDeleteWithoutParams() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        // Business method
        connectionInitiationTask.delete();

        // Asserts
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testReallyDeleteWithParams() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        // Business method
        connectionInitiationTask.delete();

        // Asserts
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionInitiationTask.getRelations(connectionMethodAttributeType, Range.all(), false)).isEmpty();
        assertThat(connectionInitiationTask.getRelations(connectionMethodAttributeType, Range.all(), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test
    @Transactional
    public void testDeletedAndSetComTaskToNoConnectionTask() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        ScheduledComTaskExecution comTaskExecution = createComTaskExecution();
        ScheduledComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionInitiationTask);
        comTaskExecutionUpdater.update();

        // Business method
        connectionInitiationTask.delete();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testReallyDeleteWithObsoleteComTasks() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionTask.save();
        long id = connectionTask.getId();

        ScheduledComTaskExecution comTaskExecution = createComTaskExecution();
        ScheduledComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        ComTaskExecution update = comTaskExecutionUpdater.update();
        device.removeComTaskExecution(update);
        device.save();

        // Business method
        connectionTask.delete();

        List<ComTaskExecution> allComTaskExecutionsIncludingObsoleteForDevice = inMemoryPersistence.getCommunicationTaskService().findAllComTaskExecutionsIncludingObsoleteForDevice(device);
        // Asserts
        assertThat(allComTaskExecutionsIncludingObsoleteForDevice).are(new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return !comTaskExecution.getConnectionTask().isPresent();
            }
        });
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), false)).isEmpty();
        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionTask.save();

        // Asserts
        assertNull("ObsoleteDate should be null", connectionTask.getObsoleteDate());
        assertFalse("Should not be obsolete", connectionTask.isObsolete());

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertNotNull("ObsoleteDate should be set", connectionTask.getObsoleteDate());
        assertTrue("Should be obsolete", connectionTask.isObsolete());
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), false)).isEmpty();
        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), true)).hasSize(1);
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithActiveComTasks() {
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        connectionTask.save();

        ScheduledComTaskExecution comTaskExecution = createComTaskExecution();
        ScheduledComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();

        // Business method
        connectionTask.makeObsolete();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);

        // Asserts
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test(expected = CannotUpdateObsoleteConnectionTaskException.class)
    @Transactional
    public void testUpdateAfterMakeObsolete() {
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        connectionTask.save();

        connectionTask.makeObsolete();

        // Business method
        connectionTask.setComPortPool(outboundTcpipComPortPool2);
        connectionTask.save();

        // Asserts: see expected exception rule
    }

    @Test(expected = ConnectionTaskIsAlreadyObsoleteException.class)
    @Transactional
    public void testMakeObsoleteTwice() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        connectionInitiationTask.save();

        connectionInitiationTask.makeObsolete();

        // Business method
        connectionInitiationTask.makeObsolete();

        // Asserts: see expected exception rule
    }

    private ConnectionInitiationTaskImpl createWithAllPropertiesAndNoViolations(String name) {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.setName(name);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionInitiationTask.save();
        return connectionInitiationTask;
    }

    private ConnectionInitiationTaskImpl createWithoutPropertiesAndNoViolations() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        connectionInitiationTask.save();
        return connectionInitiationTask;
    }

}
package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.ConnectionTaskIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;

import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.*;

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

    @Test
    @Transactional
    public void testCreateWithDifferentConnectionTypes() {
        // Create a first initiator task
        ConnectionInitiationTask firstInitiationTask = this.createWithoutPropertiesAndNoViolations();

        // Create a second initiator task with another connection type
        ConnectionInitiationTaskImpl secondInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask2, outboundTcpipComPortPool));
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
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();

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
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService()
                .newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY + "}")
    public void testCreateWithMissingRequiredProperty() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService()
                .newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionInitiationTask, null, PORT_PROPERTY_VALUE);

        // Business method
        connectionInitiationTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY + "}")
    public void testCreateWithNonExistingProperty() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService()
                .newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        // Add values for non existing property
        connectionInitiationTask.setProperty("doesNotExist", "don't care");

        // Business method
        connectionInitiationTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.COMPORT_TYPE_NOT_SUPPORTED_KEY + "}")
    public void testCreateWithIpWithModemComPortPool() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(modemConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService()
                .newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));

        // Business method
        connectionInitiationTask.save();
        connectionInitiationTask.resume();
        // Expected BusinessException because the ComPortType of the ComPortPool is not supported by the ConnectionType
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    // Todo (JP-1122): enable this test when done
    @Ignore
    public void testCreateOfDifferentConfig() {
        DeviceConfiguration mockCommunicationConfig = mock(DeviceConfiguration.class);
        when(mockCommunicationConfig.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(mockCommunicationConfig);
        when(mockCommunicationConfig.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PartialConnectionInitiationTask partialConnectionInitiationTask = mock(PartialConnectionInitiationTask.class);
        when(partialConnectionInitiationTask.getId()).thenReturn(PARTIAL_CONNECTION_INITIATION_TASK2_ID);
        when(partialConnectionInitiationTask.getName()).thenReturn("testCreateOfDifferentConfig");
        when(partialConnectionInitiationTask.getConfiguration()).thenReturn(deviceConfiguration);
        when(partialConnectionInitiationTask.getPluggableClass()).thenReturn(outboundNoParamsConnectionTypePluggableClass);

        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));

        // Business method
        connectionInitiationTask.save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testAddMissingProperties() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, null);
        connectionInitiationTask.save();

        // Business method
        connectionInitiationTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        connectionInitiationTask.save();

        // Asserts
        ConnectionInitiationTask updated = inMemoryPersistence.getDeviceDataService().findConnectionInitiationTask(connectionInitiationTask.getId()).get();
        assertThat(updated.getProperties()).hasSize(2);
        assertThat(updated.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isNotNull();
        assertThat(updated.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(updated.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNotNull();
        assertThat(updated.getProperty(IpConnectionType.PORT_PROPERTY_NAME).getValue()).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithoutProperties() {
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
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
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        // Business methods
        connectionInitiationTask.makeObsolete();
        ConnectionInitiationTask reloaded = inMemoryPersistence.getDeviceDataService().findConnectionInitiationTask(id).get();

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
        ConnectionInitiationTaskImpl connectionTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionTask.save();
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertNotNull(connectionTask.getObsoleteDate());
        assertNotNull(connectionTask.getConnectionMethod());
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).hasSize(1);
    }

    @Test
    @Transactional
    public void testReallyDeleteWithoutParams() {
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        // Business method
        connectionInitiationTask.delete();

        // Asserts
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionInitiationTask(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testReallyDeleteWithParams() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionInitiationTask.save();
        RelationParticipant connectionMethod = (RelationParticipant) connectionInitiationTask.getConnectionMethod();
        long id = connectionInitiationTask.getId();

        // Business method
        connectionInitiationTask.delete();

        // Asserts
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionInitiationTask(id).isPresent()).isFalse();
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(connectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test
    @Transactional
    public void testDeletedAndSetComTaskToNoConnectionTask() {
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
        connectionInitiationTask.save();
        long id = connectionInitiationTask.getId();

        ComTaskExecution comTaskExecution = createComTaskExecution();
        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setConnectionTask(connectionInitiationTask);
        comTaskExecutionUpdater.update();

        // Business method
        connectionInitiationTask.delete();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionInitiationTask(id).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testReallyDeleteWithObsoleteComTasks() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionTask.save();
        long id = connectionTask.getId();
        RelationParticipant connectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        ComTaskExecution comTaskExecution = createComTaskExecution();
        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setConnectionTask(connectionTask);
        ComTaskExecution update = comTaskExecutionUpdater.update();
        device.removeComTaskExecution(update);
        device.save();

        // Business method
        connectionTask.delete();

        List<ComTaskExecution> allComTaskExecutionsIncludingObsoleteForDevice = inMemoryPersistence.getDeviceDataService().findAllComTaskExecutionsIncludingObsoleteForDevice(device);
        // Asserts
        assertThat(allComTaskExecutionsIncludingObsoleteForDevice).are(new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution comTaskExecution) {
                return comTaskExecution.getConnectionTask() == null;
            }
        });
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionInitiationTask(id).isPresent()).isFalse();
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(connectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionTask.save();
        RelationParticipant connectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Asserts
        assertNull("ObsoleteDate should be null", connectionTask.getObsoleteDate());
        assertFalse("Should not be obsolete", connectionTask.isObsolete());

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertNotNull("ObsoleteDate should be set", connectionTask.getObsoleteDate());
        assertTrue("Should be obsolete", connectionTask.isObsolete());
        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(connectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(connectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).hasSize(1);
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithActiveComTasks() {
        ConnectionInitiationTaskImpl connectionTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
        connectionTask.save();

        ComTaskExecution comTaskExecution = createComTaskExecution();
        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setConnectionTask(connectionTask);
        ComTaskExecution update = comTaskExecutionUpdater.update();

        // Business method
        connectionTask.makeObsolete();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);

        // Asserts
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
    }

    @Test(expected = CannotUpdateObsoleteConnectionTaskException.class)
    @Transactional
    public void testUpdateAfterMakeObsolete() {
        ConnectionInitiationTaskImpl connectionTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
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
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool);
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
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService()
                .newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        connectionInitiationTask.save();
        return connectionInitiationTask;
    }

    private ConnectionInitiationTaskImpl createWithoutPropertiesAndNoViolations() {
        ConnectionInitiationTaskImpl connectionInitiationTask = ((ConnectionInitiationTaskImpl) inMemoryPersistence.getDeviceDataService().newConnectionInitiationTask(this.device, this.partialConnectionInitiationTask, outboundTcpipComPortPool));
        connectionInitiationTask.save();
        return connectionInitiationTask;
    }

}
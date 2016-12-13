package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import java.time.Instant;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
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


        this.setIpConnectionProperties(secondInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

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
        assertThat(connectionInitiationTask.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isNotNull();
        assertThat(connectionInitiationTask.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName()).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionInitiationTask.getProperty(IpConnectionProperties.PORT.propertyName())).isNotNull();
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredPropertiesAndNoViolations() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = (ConnectionInitiationTaskImpl) device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask)
                .setComPortPool(outboundTcpipComPortPool)
                .add();

        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, null);

        // Business method
        device.save();

        // Asserts
        assertThat(connectionInitiationTask).isNotNull();
        assertThat(connectionInitiationTask.getProperties()).hasSize(1);
        assertThat(connectionInitiationTask.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isNotNull();
        assertThat(connectionInitiationTask.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName()).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(connectionInitiationTask.getProperty(IpConnectionProperties.PORT.propertyName())).isNull();
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
        this.setIpConnectionProperties(connectionInitiationTask, null, PORT_PROPERTY_VALUE);

        // Business method
        connectionInitiationTask.update();

        // Asserts: see ExpectedConstraintViolation rule
    }

    private ConnectionInitiationTaskImpl createSimpleConnectionInitiationTask() {
        return this.createSimpleConnectionInitiationTask(this.partialConnectionInitiationTask);
    }

    private ConnectionInitiationTaskImpl createSimpleConnectionInitiationTask(PartialConnectionInitiationTask partialConnectionInitiationTask) {
        return (ConnectionInitiationTaskImpl) device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask)
                .setComPortPool(outboundTcpipComPortPool)
                .add();
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
        device.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COMPORT_TYPE_NOT_SUPPORTED + "}")
    public void testCreateWithIpWithModemComPortPool() {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(modemNoParamsConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();

        // Business method
        createSimpleConnectionInitiationTask();

        // See expected constraint violation rule
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

        // Business method
        createSimpleConnectionInitiationTask(partialConnectionInitiationTask);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testAddMissingProperties() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, null);
        device.save();

        // Business method
        connectionInitiationTask.setProperty(IpConnectionProperties.PORT.propertyName(), PORT_PROPERTY_VALUE);
        device.save();

        // Asserts
        ConnectionInitiationTask updated = inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(connectionInitiationTask.getId()).get();
        assertThat(updated.getProperties()).hasSize(2);
        assertThat(updated.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName())).isNotNull();
        assertThat(updated.getProperty(IpConnectionProperties.IP_ADDRESS.propertyName()).getValue()).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(updated.getProperty(IpConnectionProperties.PORT.propertyName())).isNotNull();
        assertThat(updated.getProperty(IpConnectionProperties.PORT.propertyName()).getValue()).isEqualTo(PORT_PROPERTY_VALUE);
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithoutProperties() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        device.save();

        // Business method
        device.removeConnectionTask(connectionInitiationTask);

        // Asserts
        assertThat(connectionInitiationTask.isObsolete()).isTrue();
        assertThat(connectionInitiationTask.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testIsObsoleteAfterReload() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();

        long id = connectionInitiationTask.getId();

        // Business methods
        device.removeConnectionTask(connectionInitiationTask);

        ConnectionInitiationTask reloaded = inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).get();

        // Asserts
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.isObsolete()).isTrue();
        assertThat(reloaded.getObsoleteDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testMakeObsoleteAlsoMakesRelationsObsolete() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        Instant now = inMemoryPersistence.getClock().instant();
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();

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

//    @Test
//    @Transactional
//    public void testReallyDeleteWithoutParams() {
//        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
//
//        long id = connectionInitiationTask.getId();
//
//        // Business method
//        connectionInitiationTask.delete();
//
//        // Asserts
//        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
//    }

//    @Test
//    @Transactional
//    public void testReallyDeleteWithParams() {
//        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
//        partialConnectionInitiationTask.save();
//        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
//        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
//        connectionInitiationTask.save();
//        long id = connectionInitiationTask.getId();
//
//        // Business method
//        connectionInitiationTask.delete();
//
//        // Asserts
//        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
//        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
//        assertThat(connectionInitiationTask.getRelations(connectionMethodAttributeType, Range.all(), false)).isEmpty();
//        assertThat(connectionInitiationTask.getRelations(connectionMethodAttributeType, Range.all(), true)).isNotEmpty();    // The relations should have been made obsolete
//    }

//    @Test
//    @Transactional
//    public void testDeletedAndSetComTaskToNoConnectionTask() {
//        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
//        long id = connectionInitiationTask.getId();
//
//        ComTaskExecution comTaskExecution = createComTaskExecution();
//        ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
//        comTaskExecutionUpdater.connectionTask(connectionInitiationTask);
//        comTaskExecutionUpdater.update();
//
//        // Business method
//        connectionInitiationTask.delete;
//
//        // Asserts
//        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
//        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
//        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
//    }

//    @Test
//    @Transactional
//    public void testReallyDeleteWithObsoleteComTasks() {
//        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
//        partialConnectionInitiationTask.save();
//        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
//        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
//        connectionTask.save();
//        long id = connectionTask.getId();
//
//        ComTaskExecution comTaskExecution = createComTaskExecution();
//        ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
//        comTaskExecutionUpdater.connectionTask(connectionTask);
//        ComTaskExecution update = comTaskExecutionUpdater.update();
//        device.removeComTaskExecution(update);
//        device.save();
//
//        // Business method
//        connectionTask.delete();
//
//        List<ComTaskExecution> allComTaskExecutionsIncludingObsoleteForDevice = inMemoryPersistence.getCommunicationTaskService().findAllComTaskExecutionsIncludingObsoleteForDevice(device);
//        // Asserts
//        assertThat(allComTaskExecutionsIncludingObsoleteForDevice).are(new Condition<ComTaskExecution>() {
//            @Override
//            public boolean matches(ComTaskExecution comTaskExecution) {
//                return !comTaskExecution.getConnectionTask().isPresent();
//            }
//        });
//        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionInitiationTask(id).isPresent()).isFalse();
//        RelationAttributeType connectionMethodAttributeType = outboundIpConnectionTypePluggableClass.getDefaultAttributeType();
//        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), false)).isEmpty();
//        assertThat(connectionTask.getRelations(connectionMethodAttributeType, Range.all(), true)).isNotEmpty();    // The relations should have been made obsolete
//    }

    @Test
    @Transactional
    public void testMakeObsolete() {
        this.grantAllViewAndEditPrivilegesToPrincipal();
        Instant now = this.freezeClock(2015, Calendar.MAY, 2);
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.save();

        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);
        device.save();
        CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>> customPropertySet = outboundIpConnectionTypePluggableClass.getConnectionType()
                .getCustomPropertySet()
                .get();
        CustomPropertySetValues values = inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now);
        assertThat(values.size()).isEqualTo(2); // Remember that we set both ip address and the port

        // Asserts
        assertThat(connectionTask.getObsoleteDate()).isNull();
        assertThat(connectionTask.isObsolete()).isFalse();

        // Business method
        device.removeConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.getObsoleteDate()).isNotNull();
        assertThat(connectionTask.isObsolete()).isTrue();

        assertThat(inMemoryPersistence.getCustomPropertySetService().getUniqueValuesFor(customPropertySet, connectionTask, now).isEmpty()).isTrue();
        // Todo: assert that old values were journalled properly but need support from CustomPropertySetService first
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithActiveComTasks() {
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();

        ComTaskExecution comTaskExecution = createComTaskExecution();
        ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();

        // Business method
        device.removeConnectionTask(connectionTask);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(comTaskExecution);

        // Asserts
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    public void testUpdateDeviceWithObsoleteConnectionTask() {
        ConnectionInitiationTaskImpl connectionTask = createSimpleConnectionInitiationTask();

        device.removeConnectionTask(connectionTask);

        device = getReloadedDevice(device);
        // Business method
        device.setName("AnotherName");
        device.save();

        // Make sure the device can be updated
    }

    @Test
    @Transactional
    public void testMakeObsoleteTwice() {
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();

        device.removeConnectionTask(connectionInitiationTask);

        // Business method
        device.removeConnectionTask(connectionInitiationTask);
    }

    private ConnectionInitiationTaskImpl createWithAllPropertiesAndNoViolations(String name) {
        partialConnectionInitiationTask.setConnectionTypePluggableClass(outboundIpConnectionTypePluggableClass);
        partialConnectionInitiationTask.setName(name);
        partialConnectionInitiationTask.save();
        ConnectionInitiationTaskImpl connectionInitiationTask = createSimpleConnectionInitiationTask();
        this.setIpConnectionProperties(connectionInitiationTask, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE);

        return connectionInitiationTask;
    }

    private ConnectionInitiationTaskImpl createWithoutPropertiesAndNoViolations() {
        return createSimpleConnectionInitiationTask();
    }

}
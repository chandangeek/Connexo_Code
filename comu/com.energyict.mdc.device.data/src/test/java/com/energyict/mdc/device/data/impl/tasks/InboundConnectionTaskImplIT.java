package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.ValueRequiredException;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.exceptions.CannotUpdateObsoleteConnectionTaskException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.PartialConnectionTaskNotPartOfDeviceConfigurationException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.protocol.api.codetables.Code;
import org.junit.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link InboundConnectionTaskImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/09/12
 * Time: 10:49
 */
public class InboundConnectionTaskImplIT extends ConnectionTaskImplIT {

    @Test
    @Transactional
    public void createWithoutViolations() throws BusinessException, SQLException {
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
    public void testCreateDefaultWithAlreadyExistingComTasksThatUseTheDefault() throws BusinessException, SQLException {
/*
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        List<ComTaskExecution> comTasksThatRelyOnTheDefault = new ArrayList<>(1);
        comTasksThatRelyOnTheDefault.add(comTask);
        when(this.comTaskExecutionFactory.findForDefaultOutboundConnectionTask(this.device)).thenReturn(comTasksThatRelyOnTheDefault);
*/
        InboundConnectionTask inboundConnectionTask = createSimpleInboundConnectionTask();

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(inboundConnectionTask);

        // Asserts
        //verify(this.manager).defaultConnectionTaskChanged(this.device, inboundConnectionTask);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_UNIQUE_INBOUND_COMPORT_POOL_PER_DEVICE_KEY + "}")
    public void createTwoTasksWithTheSamePool() throws BusinessException, SQLException {
        this.createSimpleInboundConnectionTask();

        // Business method
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask2);

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void createTwoTasksWithDifferentPool() throws BusinessException, SQLException {
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.DUPLICATE_CONNECTION_TASK_KEY + "}")
    public void createTwoTasksAgainstTheSameDeviceBasedOnTheSamePartialConnectionTask() throws BusinessException, SQLException {
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);

        // Business method
        this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask, inboundTcpipComPortPool2);

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test(expected = PartialConnectionTaskNotPartOfDeviceConfigurationException.class)
    @Transactional
    public void testCreateOfDifferentConfig() throws BusinessException, SQLException {
        DeviceCommunicationConfiguration mockCommunicationConfig = mock(DeviceCommunicationConfiguration.class);
        when(mockCommunicationConfig.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        PartialInboundConnectionTask partialInboundConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialInboundConnectionTask.getConfiguration()).thenReturn(mockCommunicationConfig);

        // Business method
        this.createSimpleInboundConnectionTask(partialInboundConnectionTask);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void updateWithoutViolations() {
        InboundConnectionTask inboundConnectionTask = createSimpleInboundConnectionTask();

        // Business method
        inboundConnectionTask.setComPortPool(inboundTcpipComPortPool2);
        inboundConnectionTask.save();

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
        assertNull("ObsoleteDate should be null", inboundConnectionTask.getObsoleteDate());
        assertFalse("Should not be obsolete", inboundConnectionTask.isObsolete());

        // Business method
        inboundConnectionTask.makeObsolete();

        // Asserts
        assertNotNull("ObsoleteDate should be set", inboundConnectionTask.getObsoleteDate());
        assertTrue("Should be obsolete", inboundConnectionTask.isObsolete());
    }

    @Test(expected = CannotUpdateObsoleteConnectionTaskException.class)
    @Transactional
    public void testUpdateAfterObsolete() throws BusinessException, SQLException {
        InboundConnectionTask inboundConnectionTask = createSimpleInboundConnectionTask();
        inboundConnectionTask.makeObsolete();

        // Business method
        inboundConnectionTask.setComPortPool(inboundTcpipComPortPool2);
        inboundConnectionTask.save();

        // Asserts: see expected exception rule
    }

    @Test(expected = BusinessException.class)
    @Transactional
    @Ignore
    // Todo: Wait for ComTaskExecution to be moved to this bundle and then create one that explicitly uses the InboundConnectionTask
    public void testCannotDeleteDefaultTaskThatIsInUse() throws BusinessException, SQLException {
        InboundConnectionTask connectionTask = createSimpleInboundConnectionTask();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Business method
        connectionTask.delete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    @Ignore
    // Todo: Wait for ComTaskExecution to be moved to this bundle and then create one that simple uses the default
    public void testDeletedAndSetComTaskToNoConnectionTask() throws BusinessException, SQLException {
        InboundConnectionTask connectionTask = createSimpleInboundConnectionTask();
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        ComTaskExecution obsoleteComTask = mock(ComTaskExecution.class);
        comTaskExecutions.add(obsoleteComTask);
        //when(this.comTaskExecutionFactory.findAllByConnectionTask(connectionTask)).thenReturn(comTaskExecutions);

        // Business method
        connectionTask.delete();

        // Asserts
        verify(obsoleteComTask).connectionTaskRemoved();
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionTask(connectionTask.getId()).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testAttemptLock () throws BusinessException, SQLException {
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);

        // Business method
        InboundConnectionTask locked = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        // Asserts
        assertThat(locked).isNotNull();
        assertThat(locked.getExecutingComServer()).isEqualTo(comServer);
    }

    @Test
    @Transactional
    public void testUnlock () throws BusinessException, SQLException {
        final OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        // Business method
        inMemoryPersistence.getDeviceDataService().unlockConnectionTask(locked);

        // Asserts
        assertThat(locked.getExecutingComServer()).isNull();
    }

    @Test
    @Transactional
    public void testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer () throws BusinessException, SQLException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        // Business method
        InboundConnectionTask lockedForSecondTime = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        // Asserts
        assertThat(lockedForSecondTime).isNull();
        assertThat(locked.getExecutingComServer()).isNotNull();
    }

    @Test
    @Transactional
    public void testAttemptLockTwiceWillFail() throws BusinessException, SQLException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        /* Business method: here the test is a little different from testAttemptLockWillFailWhenAlreadyLockedByTheSameComServer
         *                  because we are locking on the already locked ConnectionTask instead of the original connectionTask that is not locked in memory. */
        InboundConnectionTask lockedForSecondTime = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(locked, comServer);

        // Asserts
        assertThat(lockedForSecondTime).isNull();
        assertThat(locked.getExecutingComServer()).isNotNull();
    }

    @Test
    @Transactional
    public void testAttemptLockWillFailWhenAlreadyLockedByAnotherComServer () throws BusinessException, SQLException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        OnlineComServer otherComServer = mock(OnlineComServer.class);
        when(otherComServer.getId()).thenReturn(COMSERVER2_ID);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask();
        InboundConnectionTask locked = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        // Business method
        InboundConnectionTask lockedForSecondTime = inMemoryPersistence.getDeviceDataService().attemptLockConnectionTask(connectionTask, comServer);

        // Asserts
        assertThat(lockedForSecondTime).isNull();
        assertThat(locked.getExecutingComServer()).isNotNull();
        assertThat(locked.getExecutingComServer().getId()).isEqualTo(COMSERVER_ID);
    }

    @Test
    @Transactional
    public void testCreateWithAllIpProperties() throws BusinessException, SQLException {
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(3);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isEqualTo(codeTable);
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndNoDefaultsOnPluggableClass () throws BusinessException, SQLException {
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
        assertThat(connectionTask.getProperties()).hasSize(1);   // Only 1 property is locally defined and higher levels do not specify any property values
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(1);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isNull();
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isNull();
    }

    @Test
    @Transactional
    public void testCreateWithOnlyRequiredIpPropertiesAndSomeDefaultsOnPluggableClass () throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), null);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), null);
        ipConnectionTypePluggableClass.save();

        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        List<PropertySpec> allIpPropertySpecs = this.getOutboundIpPropertySpecs();
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(allIpPropertySpecs)));
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, null, null);

        // Business method
        connectionTask.save();;

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
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
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClass () throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs())));
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        connectionTask.setComPortPool(inboundTcpipComPortPool);
        // Do not add any properties to the ConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
        assertThat(connectionTask.getComPortPool()).isNotNull();
        assertThat(connectionTask.getComPortPool().getId()).isEqualTo(INBOUND_COMPORT_POOL1_ID);
        assertThat(connectionTask.getConnectionType()).isEqualTo(ipConnectionTypePluggableClass.getConnectionType());
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
    public void testCreateWithAllPropertiesInheritedFromConnectionTypePluggableClassAndPartialConnectionTask () throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        TypedProperties partialConnectionTaskProperties = TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs()));
        partialConnectionTaskProperties.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskProperties.setProperty(IpConnectionType.PORT_PROPERTY_NAME, UPDATED_PORT_PROPERTY_VALUE);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(partialConnectionTaskProperties);
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        connectionTask.setComPortPool(inboundTcpipComPortPool);
        // Do not add any properties to the ConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
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
    public void testCreateWithAllPropertiesInheritedFromPartialConnectionTask () throws BusinessException, SQLException {
        TypedProperties partialConnectionTaskTypedProperties = TypedProperties.empty();
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS_PROPERTY_VALUE);
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
        partialConnectionTaskTypedProperties.setProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME, codeTable);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(partialConnectionTaskTypedProperties);
        PartialConnectionTaskProperty partialIpAddressProperty = mock(PartialConnectionTaskProperty.class);
        when(partialIpAddressProperty.getName()).thenReturn(IpConnectionType.IP_ADDRESS_PROPERTY_NAME);
        when(partialIpAddressProperty.getValue()).thenReturn(IP_ADDRESS_PROPERTY_VALUE);
        when(partialIpAddressProperty.getPartialConnectionTask()).thenReturn(this.partialInboundConnectionTask);
        PartialConnectionTaskProperty partialPortProperty = mock(PartialConnectionTaskProperty.class);
        when(partialPortProperty.getName()).thenReturn(IpConnectionType.PORT_PROPERTY_NAME);
        when(partialPortProperty.getValue()).thenReturn(PORT_PROPERTY_VALUE);
        when(partialPortProperty.getPartialConnectionTask()).thenReturn(this.partialInboundConnectionTask);
        PartialConnectionTaskProperty partialCodeTableProperty = mock(PartialConnectionTaskProperty.class);
        when(partialCodeTableProperty.getName()).thenReturn(IpConnectionType.CODE_TABLE_PROPERTY_NAME);
        when(partialCodeTableProperty.getValue()).thenReturn(codeTable);
        when(partialCodeTableProperty.getPartialConnectionTask()).thenReturn(this.partialInboundConnectionTask);
        List<PartialConnectionTaskProperty> partialConnectionTaskProperties = Arrays.asList(partialIpAddressProperty, partialPortProperty, partialCodeTableProperty);
        when(this.partialInboundConnectionTask.getProperties()).thenReturn(partialConnectionTaskProperties);
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        connectionTask.setComPortPool(inboundTcpipComPortPool);
        // Do not add any properties to the ConnectionTask

        // Business method
        connectionTask.save();

        // Asserts
        assertThat(connectionTask).isNotNull();
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
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();

        // Business method
        connectionTask.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, UPDATED_IP_ADDRESS_PROPERTY_VALUE);
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
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, null, codeTable);
        connectionTask.save();

        // Business method
        connectionTask.setProperty(IpConnectionType.PORT_PROPERTY_NAME, PORT_PROPERTY_VALUE);
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
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();

        // Business method
        connectionTask.removeProperty(IpConnectionType.PORT_PROPERTY_NAME);
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
    public void testReturnToInheritedProperty () throws BusinessException, SQLException {
        // First update the properties of the ipConnectionType pluggable class
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.IP_ADDRESS_PROPERTY_NAME), IP_ADDRESS_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.PORT_PROPERTY_NAME), UPDATED_PORT_PROPERTY_VALUE);
        ipConnectionTypePluggableClass.setProperty(ipConnectionTypePluggableClass.getPropertySpec(IpConnectionType.CODE_TABLE_PROPERTY_NAME), codeTable);
        ipConnectionTypePluggableClass.save();

        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        TypedProperties partialInboundProperties = TypedProperties.inheritingFrom(ipConnectionTypePluggableClass.getProperties(this.getOutboundIpPropertySpecs()));
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(partialInboundProperties);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        connectionTask.save();

        // Business method
        connectionTask.removeProperty(IpConnectionType.PORT_PROPERTY_NAME);
        connectionTask.save();

        // Asserts
        assertThat(connectionTask.getProperties()).hasSize(3);
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        assertThat(typedProperties.size()).isEqualTo(3);
        assertThat(typedProperties.getProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME)).isEqualTo(IP_ADDRESS_PROPERTY_VALUE);
        assertThat(typedProperties.hasInheritedValueFor(IpConnectionType.PORT_PROPERTY_NAME)).isTrue();
        assertThat(typedProperties.getProperty(IpConnectionType.PORT_PROPERTY_NAME)).isEqualTo(UPDATED_PORT_PROPERTY_VALUE);
        assertThat(typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME)).isInstanceOf(Code.class);
        Code actualCodeTable = (Code) typedProperties.getProperty(IpConnectionType.CODE_TABLE_PROPERTY_NAME);
        assertThat(actualCodeTable.getId()).isEqualTo(codeTable.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_PROPERTY_MISSING_KEY + "}")
    public void testCreateWithMissingRequiredProperty() throws BusinessException, SQLException {
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        when(this.partialInboundConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, null, PORT_PROPERTY_VALUE, codeTable);

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_PROPERTY_NOT_IN_SPEC_KEY + "}")
    public void testCreateWithNonExistingProperty() throws BusinessException, SQLException {
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        InboundConnectionTask connectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(connectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        // Add values for non existing property
        connectionTask.setProperty("doesNotExist", "I don't care");

        // Business method
        connectionTask.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testDeleteWithNoProperties() throws BusinessException, SQLException {
        InboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations();
        long id = connectionTask.getId();

        // Business method
        connectionTask.delete();

        // Asserts
        assertFalse(inMemoryPersistence.getDeviceDataService().findConnectionTask(id).isPresent());
    }

    @Test
    @Transactional
    public void testDeleteWithProperties() throws BusinessException, SQLException {
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.createInboundWithIpPropertiesWithoutViolations();
        long id = connectionTask.getId();
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.delete();

        // Asserts
        assertFalse(inMemoryPersistence.getDeviceDataService().findConnectionTask(id).isPresent());
        RelationAttributeType connectionMethodAttributeType = ipConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).isNotEmpty();    // The relations should have been made obsolete
    }

    @Test
    @Transactional
    public void testMakeObsoleteWithNoProperties() throws BusinessException, SQLException {
        InboundConnectionTask connectionTask = this.createWithNoPropertiesWithoutViolations();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertNotNull(connectionTask.getObsoleteDate());
    }

    @Test
    @Transactional
    public void testMakeObsoleteAlsoMakesRelationsObsolete() throws BusinessException, SQLException {
        InboundConnectionTaskImpl connectionTask = (InboundConnectionTaskImpl) this.createInboundWithIpPropertiesWithoutViolations();
        RelationParticipant ipConnectionMethod = (RelationParticipant) connectionTask.getConnectionMethod();

        // Business method
        connectionTask.makeObsolete();

        // Asserts
        assertTrue(connectionTask.isObsolete());
        assertNotNull(connectionTask.getObsoleteDate());
        assertNotNull(connectionTask.getConnectionMethod());
        RelationAttributeType connectionMethodAttributeType = ipConnectionTypePluggableClass.getDefaultAttributeType();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), false)).isEmpty();
        assertThat(ipConnectionMethod.getRelations(connectionMethodAttributeType, new Interval(null, null), true)).hasSize(1);
    }

    @Test
    @Transactional
    public void testSwitchFromOutboundDefault () throws SQLException, BusinessException {
        ScheduledConnectionTaskImpl outboundDefault = (ScheduledConnectionTaskImpl) this.createOutboundWithIpPropertiesWithoutViolations("testSwitchFromOutboundDefault");
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(outboundDefault);

        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations();

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        verify(outboundDefault).clearDefault();
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testSetAsDefaultWithoutOtherDefault () throws SQLException, BusinessException {
        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations();

        // Business method
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        // Asserts
        assertThat(connectionTask.isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testUnsetAsDefaultWithOtherConnectionTasks () throws SQLException, BusinessException {
        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations(false);
        ScheduledConnectionTask outboundDefault = this.createOutboundWithIpPropertiesWithoutViolations("testUnsetAsDefaultWithOtherConnectionTasks");
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(outboundDefault);

        // Business method
        inMemoryPersistence.getDeviceDataService().clearDefaultConnectionTask(connectionTask.getDevice());

        // Need to reload the outbound default as the changes are done in the background
        ScheduledConnectionTask reloadedOutboundDefault = inMemoryPersistence.getDeviceDataService().findScheduledConnectionTask(connectionTask.getId()).get();

        // Asserts
        assertThat(connectionTask.isDefault()).isFalse();
        assertThat(reloadedOutboundDefault.isDefault()).isFalse();
    }

    @Test
    @Transactional
    public void testUnsetAsDefaultWithoutOtherDefaults () throws SQLException, BusinessException {
        InboundConnectionTask connectionTask = this.createInboundWithIpPropertiesWithoutViolations(false);

        // Business method
        inMemoryPersistence.getDeviceDataService().clearDefaultConnectionTask(connectionTask.getDevice());

        // Asserts
        assertThat(connectionTask.isDefault()).isFalse();
    }

    private InboundConnectionTask createSimpleInboundConnectionTask() {
        return this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
    }

    private InboundConnectionTask createSimpleInboundConnectionTask(PartialInboundConnectionTask partialConnectionTask) {
        return createSimpleInboundConnectionTask(partialConnectionTask, inboundTcpipComPortPool);
    }

    private InboundConnectionTask createSimpleInboundConnectionTask(final PartialInboundConnectionTask partialConnectionTask, final InboundComPortPool inboundComPortPool) {
        InboundConnectionTask inboundConnectionTask = getDeviceDataService().newInboundConnectionTask(device, partialConnectionTask, inboundComPortPool);
        inboundConnectionTask.save();
        return inboundConnectionTask;
    }

    private InboundConnectionTask createWithNoPropertiesWithoutViolations() {
        return this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
    }

    protected InboundConnectionTask createInboundWithIpPropertiesWithoutViolations() {
        return this.createInboundWithIpPropertiesWithoutViolations(false);
    }

    protected InboundConnectionTask createInboundWithIpPropertiesWithoutViolations(boolean defaultState) {
        when(this.partialInboundConnectionTask.getPluggableClass()).thenReturn(ipConnectionTypePluggableClass);
        InboundConnectionTask inboundConnectionTask = this.createSimpleInboundConnectionTask(this.partialInboundConnectionTask);
        this.addIpConnectionProperties(inboundConnectionTask, inboundTcpipComPortPool, IP_ADDRESS_PROPERTY_VALUE, PORT_PROPERTY_VALUE, codeTable);
        inboundConnectionTask.save();
        if (defaultState) {
            inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(inboundConnectionTask);
        }
        return inboundConnectionTask;
    }

}
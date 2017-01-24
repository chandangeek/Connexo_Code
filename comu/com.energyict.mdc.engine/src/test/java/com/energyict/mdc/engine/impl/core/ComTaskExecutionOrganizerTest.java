package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ComTaskExecutionOrganizer} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/04/13
 * Time: 8:38
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionOrganizerTest {

    private static final int AUTHENTICATION_DEVICE_ACCESS_LEVEL = 97;
    private static final int ENCRYPTION_DEVICE_ACCESS_LEVEL = 101;
    private static final long SECURITY_PROPERTY_SET_ID = 111L;

    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel;
    @Mock
    private EncryptionDeviceAccessLevel encryptionDeviceAccessLevel;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private ComTaskEnablement comTaskEnablement;

    private long nextSecurityPropertyId = 1;
    private long nextDeviceId = 1;

    @Before
    public void setUp() {
        when(this.authenticationDeviceAccessLevel.getId()).thenReturn(AUTHENTICATION_DEVICE_ACCESS_LEVEL);
        when(this.encryptionDeviceAccessLevel.getId()).thenReturn(ENCRYPTION_DEVICE_ACCESS_LEVEL);
        when(this.comTaskEnablement.getSecurityPropertySet()).thenReturn(this.securityPropertySet);
        when(this.securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(this.securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(this.securityPropertySet.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.topologyService.getPhysicalGateway(any(Device.class))).thenReturn(Optional.empty());
    }

    private ComTask createMockedTopologyComTask() {
        ComTask comTask = mock(ComTask.class);
        ProtocolTask basicCheck = mock(TopologyTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(basicCheck));
        return comTask;
    }

    private ComTask createMockedLoadProfilesComTask() {
        ComTask comTask = mock(ComTask.class);
        ProtocolTask basicCheck = mock(LoadProfilesTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(basicCheck));
        return comTask;
    }

    private ComTask createMockedLogBooksComTask() {
        ComTask comTask = mock(ComTask.class);
        ProtocolTask basicCheck = mock(LogBooksTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(basicCheck));
        return comTask;
    }

    private ComTask createMockedRegistersComTask() {
        ComTask comTask = mock(ComTask.class);
        ProtocolTask basicCheck = mock(RegistersTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(basicCheck));
        return comTask;
    }


    private ComTaskExecution createMockedComTaskExecution(Device device, ComTask comTask) {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        List<ProtocolTask> protocolTasks = comTask.getProtocolTasks();
        when(comTaskExecution.getProtocolTasks()).thenReturn(protocolTasks);
        when(comTaskExecution.getDevice()).thenReturn(device);
        return comTaskExecution;
    }

    private Device getMockedDevice(boolean slave) {
        final Device device = this.mockDevice();
        final DeviceType deviceType = mock(DeviceType.class);
        final DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.isLogicalSlave()).thenReturn(slave);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        return device;
    }

    @Test
    public void testSingleDeviceSingleComTask() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(comTaskExecution));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecution = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        assertThat(orderedExecution).hasSize(1);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps singlePair = orderedExecution.get(0);
        assertThat(singlePair.getComTaskExecution()).isEqualTo(comTaskExecution);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = singlePair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps).isNotNull();
        assertThat(comTaskExecutionConnectionSteps.isLogOnRequired()).isTrue();
        assertThat(comTaskExecutionConnectionSteps.isLogOffRequired()).isTrue();
        assertThat(comTaskExecutionConnectionSteps.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps.isDaisyChainedLogOffRequired()).isFalse();
    }

    @Test
    public void testSingleDeviceTwoComTasks() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(comTaskExecution1, comTaskExecution2));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecution = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        assertThat(orderedExecution).hasSize(2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPair = orderedExecution.get(0);
        assertThat(firstPair.getComTaskExecution()).isEqualTo(comTaskExecution1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = firstPair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
        assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPair = orderedExecution.get(1);
        assertThat(secondPair.getComTaskExecution()).isEqualTo(comTaskExecution2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = secondPair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isTrue();
    }

    @Test
    public void testSingleDeviceMultipleComTasks() {
        Device device = getMockedDevice(false);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution4 = createMockedComTaskExecution(device, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(comTaskExecution1, comTaskExecution2, comTaskExecution3, comTaskExecution4));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecution = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        assertThat(orderedExecution).hasSize(4);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPair = orderedExecution.get(0);
        assertThat(firstPair.getComTaskExecution()).isEqualTo(comTaskExecution1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = firstPair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
        assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPair = orderedExecution.get(1);
        assertThat(secondPair.getComTaskExecution()).isEqualTo(comTaskExecution2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = secondPair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdPair = orderedExecution.get(2);
        assertThat(thirdPair.getComTaskExecution()).isEqualTo(comTaskExecution3);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3 = thirdPair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps3.isLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOffRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps3.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourthPair = orderedExecution.get(3);
        assertThat(fourthPair.getComTaskExecution()).isEqualTo(comTaskExecution4);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4 = fourthPair.getComTaskExecutionConnectionSteps();
        assertThat(comTaskExecutionConnectionSteps4.isLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOnRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOffRequired()).isFalse();
        assertThat(comTaskExecutionConnectionSteps4.isLogOffRequired()).isTrue();
    }

    @Test
    public void testTwoDevicesEachOneComTaskExecution() {
        Device device1 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        DeviceConfiguration device1Configuration = device1.getDeviceConfiguration();
        when(comTaskEnablement1.getDeviceConfiguration()).thenReturn(device1Configuration);
        SecurityPropertySet securityPropertySet1 = this.mockSecurityPropertySet();
        when(securityPropertySet1.getDeviceConfiguration()).thenReturn(device1Configuration);
        when(securityPropertySet1.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet1.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet1);
        when(device1Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement1));
        Device device2 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        DeviceConfiguration device2Configuration = device2.getDeviceConfiguration();
        when(comTaskEnablement2.getDeviceConfiguration()).thenReturn(device2Configuration);
        SecurityPropertySet securityPropertySet2 = this.mockSecurityPropertySet();
        when(securityPropertySet2.getDeviceConfiguration()).thenReturn(device2Configuration);
        when(securityPropertySet2.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet2.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet2);
        when(device2Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement2));
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(comTaskExecution1, comTaskExecution2));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
            if (deviceOrganizedComTaskExecution.getDevice().getId() == device1.getId()) {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice1).hasSize(1);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps singlePairDevice1 = orderedExecutionDevice1.get(0);
                assertThat(singlePairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = singlePairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();
            }
            else {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice2).hasSize(1);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps singlePairDevice2 = orderedExecutionDevice2.get(0);
                assertThat(singlePairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = singlePairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isTrue();
            }
        }
    }

    @Test
    public void testTwoDevicesEachTwoComTaskExecutions() {
        Device device1 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        DeviceConfiguration device1Configuration = device1.getDeviceConfiguration();
        when(comTaskEnablement1.getDeviceConfiguration()).thenReturn(device1Configuration);
        SecurityPropertySet securityPropertySet1 = this.mockSecurityPropertySet();
        when(securityPropertySet1.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet1.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet1);
        Device device2 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        DeviceConfiguration device2Configuration = device2.getDeviceConfiguration();
        when(comTaskEnablement2.getDeviceConfiguration()).thenReturn(device2Configuration);
        SecurityPropertySet securityPropertySet2 = this.mockSecurityPropertySet();
        when(securityPropertySet2.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet2.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet2);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution4 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        when(device1Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement1));
        when(device2Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement2));

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(comTaskExecution1, comTaskExecution2, comTaskExecution3, comTaskExecution4));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
            if (deviceOrganizedComTaskExecution.getDevice().getId() == device1.getId()) {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice1).hasSize(2);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice1 = orderedExecutionDevice1.get(0);
                assertThat(firstPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = firstPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice1 = orderedExecutionDevice1.get(1);
                assertThat(lastPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = lastPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isFalse();
            }
            else {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice2).hasSize(2);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice2 = orderedExecutionDevice2.get(0);
                assertThat(firstPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution3);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3 = firstPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps3.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice2 = orderedExecutionDevice2.get(1);
                assertThat(lastPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution4);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4 = lastPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps4.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4.isLogOffRequired()).isTrue();
            }
        }
    }

    @Test
    public void testTwoDevicesEachMultipleComTaskExecutions() {
        Device device1 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        DeviceConfiguration device1Configuration = device1.getDeviceConfiguration();
        when(comTaskEnablement1.getDeviceConfiguration()).thenReturn(device1Configuration);
        SecurityPropertySet securityPropertySet1 = this.mockSecurityPropertySet();
        when(comTaskEnablement1.getDeviceConfiguration()).thenReturn(device1Configuration);
        when(comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet1);
        when(securityPropertySet1.getDeviceConfiguration()).thenReturn(device1Configuration);
        when(securityPropertySet1.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet1.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(device1Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement1));
        Device device2 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        DeviceConfiguration device2Configuration = device2.getDeviceConfiguration();
        when(comTaskEnablement2.getDeviceConfiguration()).thenReturn(device2Configuration);
        SecurityPropertySet securityPropertySet2 = this.mockSecurityPropertySet();
        when(comTaskEnablement2.getDeviceConfiguration()).thenReturn(device2Configuration);
        when(comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet2);
        when(securityPropertySet2.getDeviceConfiguration()).thenReturn(device2Configuration);
        when(securityPropertySet2.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet2.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(device2Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement2));

        ComTaskExecution comTaskExecution1_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution4_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());

        ComTaskExecution comTaskExecution1_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution4_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1_d1, comTaskExecution2_d1, comTaskExecution3_d1, comTaskExecution4_d1,
                        comTaskExecution1_d2, comTaskExecution2_d2, comTaskExecution3_d2, comTaskExecution4_d2));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
            if (deviceOrganizedComTaskExecution.getDevice().getId() == device1.getId()) {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice1).hasSize(4);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice1 = orderedExecutionDevice1.get(0);
                assertThat(firstPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d1 = firstPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1_d1.isLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d1.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice1 = orderedExecutionDevice1.get(1);
                assertThat(secondPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution2_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d1 = secondPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2_d1.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d1.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdPairDevice1 = orderedExecutionDevice1.get(2);
                assertThat(thirdPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution3_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d1 = thirdPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps3_d1.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d1.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice1 = orderedExecutionDevice1.get(3);
                assertThat(lastPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution4_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4_d1 = lastPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps4_d1.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4_d1.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps4_d1.isLogOffRequired()).isFalse();
            }
            else {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice2).hasSize(4);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice2 = orderedExecutionDevice2.get(0);
                assertThat(firstPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution1_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d2 = firstPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d2.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice2 = orderedExecutionDevice2.get(1);
                assertThat(secondPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution2_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d2 = secondPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d2.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdPairDevice2 = orderedExecutionDevice2.get(2);
                assertThat(thirdPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution3_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d2 = thirdPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps3_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d2.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice2 = orderedExecutionDevice2.get(3);
                assertThat(lastPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution4_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4_d2 = lastPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps4_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4_d2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4_d2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps4_d2.isLogOffRequired()).isTrue();
            }
        }
    }

    @Test
    public void threeDevicesEachThreeComTaskExecutionsTest() {
        Device device1 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        DeviceConfiguration device1Configuration = device1.getDeviceConfiguration();
        when(comTaskEnablement1.getDeviceConfiguration()).thenReturn(device1Configuration);
        SecurityPropertySet securityPropertySet1 = this.mockSecurityPropertySet();
        when(securityPropertySet1.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet1.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet1);
        Device device2 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        DeviceConfiguration device2Configuration = device2.getDeviceConfiguration();
        when(comTaskEnablement2.getDeviceConfiguration()).thenReturn(device2Configuration);
        SecurityPropertySet securityPropertySet2 = this.mockSecurityPropertySet();
        when(securityPropertySet2.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet2.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet2);
        Device device3 = getMockedDevice(false);
        ComTaskEnablement comTaskEnablement3 = mock(ComTaskEnablement.class);
        DeviceConfiguration device3Configuration = device3.getDeviceConfiguration();
        when(comTaskEnablement3.getDeviceConfiguration()).thenReturn(device3Configuration);
        SecurityPropertySet securityPropertySet3 = this.mockSecurityPropertySet();
        when(securityPropertySet3.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(securityPropertySet3.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(comTaskEnablement3.getSecurityPropertySet()).thenReturn(securityPropertySet3);

        ComTaskExecution comTaskExecution1_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());

        ComTaskExecution comTaskExecution1_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());

        ComTaskExecution comTaskExecution1_d3 = createMockedComTaskExecution(device3, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d3 = createMockedComTaskExecution(device3, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d3 = createMockedComTaskExecution(device3, createMockedTopologyComTask());
        when(device1Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement1));
        when(device2Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement2));
        when(device3Configuration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(comTaskEnablement3));

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1_d1, comTaskExecution2_d1, comTaskExecution3_d1,
                        comTaskExecution1_d2, comTaskExecution2_d2, comTaskExecution3_d2,
                        comTaskExecution1_d3, comTaskExecution2_d3, comTaskExecution3_d3));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(3);
        for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
            if (deviceOrganizedComTaskExecution.getDevice().getId() == device1.getId()) {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice1).hasSize(3);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice1 = orderedExecutionDevice1.get(0);
                assertThat(firstPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d1 = firstPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1_d1.isLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d1.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice1 = orderedExecutionDevice1.get(1);
                assertThat(secondPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution2_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d1 = secondPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2_d1.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d1.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice1 = orderedExecutionDevice1.get(2);
                assertThat(lastPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution3_d1);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d1 = lastPairDevice1.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps3_d1.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps3_d1.isLogOffRequired()).isFalse();
            }
            else if (deviceOrganizedComTaskExecution.getDevice().getId() == device2.getId()) {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice2).hasSize(3);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice2 = orderedExecutionDevice2.get(0);
                assertThat(firstPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution1_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d2 = firstPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d2.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice2 = orderedExecutionDevice2.get(1);
                assertThat(secondPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution2_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d2 = secondPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d2.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice2 = orderedExecutionDevice2.get(2);
                assertThat(lastPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution3_d2);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d2 = lastPairDevice2.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps3_d2.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps3_d2.isLogOffRequired()).isFalse();
            }
            else {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice3 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice3).hasSize(3);

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice3 = orderedExecutionDevice3.get(0);
                assertThat(firstPairDevice3.getComTaskExecution()).isEqualTo(comTaskExecution1_d3);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d3 = firstPairDevice3.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps1_d3.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d3.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(comTaskExecutionConnectionSteps1_d3.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps1_d3.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice3 = orderedExecutionDevice3.get(1);
                assertThat(secondPairDevice3.getComTaskExecution()).isEqualTo(comTaskExecution2_d3);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d3 = secondPairDevice3.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps2_d3.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d3.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d3.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps2_d3.isLogOffRequired()).isFalse();

                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice3 = orderedExecutionDevice3.get(2);
                assertThat(lastPairDevice3.getComTaskExecution()).isEqualTo(comTaskExecution3_d3);

                final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d3 = lastPairDevice3.getComTaskExecutionConnectionSteps();
                assertThat(comTaskExecutionConnectionSteps3_d3.isLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d3.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d3.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(comTaskExecutionConnectionSteps3_d3.isLogOffRequired()).isTrue();
            }
        }
    }

    @Test
    public void comTaskExecutionWithBasicCheckInFrontOfTheLineTest() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());
        ComTaskExecution comTaskExecution4 = createMockedComTaskExecution(device, createMockedTaskWithBasicCheckInMiddleOfProtocolTasks());
        ComTaskExecution comTaskExecution5 = createMockedComTaskExecution(device, createMockedLogBooksComTask());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3, comTaskExecution4, comTaskExecution5));

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithSteps = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();

        // asserts
        assertThat(comTasksWithSteps.get(0).getComTaskExecution()).isEqualTo(comTaskExecution4);
        assertThat(comTasksWithSteps.get(1).getComTaskExecution()).isEqualTo(comTaskExecution1);
        assertThat(comTasksWithSteps.get(2).getComTaskExecution()).isEqualTo(comTaskExecution2);
        assertThat(comTasksWithSteps.get(3).getComTaskExecution()).isEqualTo(comTaskExecution3);
        assertThat(comTasksWithSteps.get(4).getComTaskExecution()).isEqualTo(comTaskExecution5);
    }

    @Test
    public void comTaskExecutionsWithSlaveCapabilitiesButNoMasterTest() {
        Device device = getMockedDevice(false);

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3));

        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution_1.getDevice().getId()).isEqualTo(device.getId());
    }

    @Test
    public void comTaskExecutionsWithSlaveCapabilitiesAndMasterTest() {
        Device device = getMockedDevice(true);
        Device master = getMockedDevice(false);
        ComTaskEnablement masterComTaskEnablement = mock(ComTaskEnablement.class);
        DeviceConfiguration masterDeviceConfiguration = master.getDeviceConfiguration();
        when(masterComTaskEnablement.getDeviceConfiguration()).thenReturn(masterDeviceConfiguration);
        when(masterComTaskEnablement.getSecurityPropertySet()).thenReturn(this.securityPropertySet);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(masterDeviceConfiguration);
        when(this.topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(master));

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.empty());
        when(masterDeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(masterComTaskEnablement));
        when(masterDeviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3));

        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution_1.getDevice().getId()).isEqualTo(master.getId());
    }

    /**
     * The master does have a slave, but the slave can itself setup a connection.
     */
    @Test
    public void comTaskExecutionsWithFakeSlaveCapabilitiesAndMasterTest() {
        Device device = getMockedDevice(false);
        DeviceConfiguration slaveDeviceConfiguration = device.getDeviceConfiguration();
        ComTaskEnablement slaveComTaskEnablement = mock(ComTaskEnablement.class);
        when(slaveComTaskEnablement.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        SecurityPropertySet slaveSecurityPropertySet = this.mockSecurityPropertySet();
        when(slaveSecurityPropertySet.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(slaveSecurityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(slaveSecurityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(slaveComTaskEnablement.getSecurityPropertySet()).thenReturn(slaveSecurityPropertySet);
        when(slaveDeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(slaveComTaskEnablement));
        Device master = getMockedDevice(false);
        DeviceConfiguration masterDeviceConfiguration = master.getDeviceConfiguration();
        ComTaskEnablement masterComTaskEnablement = mock(ComTaskEnablement.class);
        when(masterComTaskEnablement.getDeviceConfiguration()).thenReturn(masterDeviceConfiguration);
        SecurityPropertySet masterSecurityPropertySet = this.mockSecurityPropertySet();
        when(masterSecurityPropertySet.getDeviceConfiguration()).thenReturn(masterDeviceConfiguration);
        when(masterSecurityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(this.authenticationDeviceAccessLevel);
        when(masterSecurityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(this.encryptionDeviceAccessLevel);
        when(masterComTaskEnablement.getSecurityPropertySet()).thenReturn(masterSecurityPropertySet);
        when(masterDeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(masterComTaskEnablement));
        when(this.topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(master));

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3));

        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution_1.getDevice().getId()).isEqualTo(device.getId());
    }

    @Test
    public void comTaskExecutionsWithSlaveCapabilitiesAndMultipleHierarchyTest() {
        Device device = getMockedDevice(true);
        Device master1 = getMockedDevice(true);
        Device masterOfMaster1 = getMockedDevice(false);
        when(this.topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(master1));
        when(this.topologyService.getPhysicalGateway(master1)).thenReturn(Optional.of(masterOfMaster1));
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));
        DeviceConfiguration master1DeviceConfiguration = master1.getDeviceConfiguration();
        when(master1DeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.empty());
        DeviceConfiguration masterOfMaster1DeviceConfiguration = masterOfMaster1.getDeviceConfiguration();
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(masterOfMaster1DeviceConfiguration);
        when(masterOfMaster1DeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));
        when(masterOfMaster1DeviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(this.securityPropertySet));

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3));

        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution_1.getDevice().getId()).isEqualTo(masterOfMaster1.getId());
    }

    @Test
    public void comTaskExecutionWithoutSlaveCapabilitiesButWithMasterDeviceTest() {
        Device device = getMockedDevice(false);
        Device master1 = getMockedDevice(false);
        Device masterOfMaster1 = getMockedDevice(false);
        when(this.topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(master1));
        when(this.topologyService.getPhysicalGateway(master1)).thenReturn(Optional.of(masterOfMaster1));
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));
        DeviceConfiguration master1DeviceConfiguration = master1.getDeviceConfiguration();
        when(master1DeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.empty());
        DeviceConfiguration masterOfMaster1DeviceConfiguration = masterOfMaster1.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(masterOfMaster1DeviceConfiguration);
        when(this.securityPropertySet.getDeviceConfiguration()).thenReturn(masterOfMaster1DeviceConfiguration);
        when(masterOfMaster1DeviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3));

        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution_1.getDevice().getId()).isEqualTo(device.getId());
    }

    @Test(expected = DeviceConfigurationException.class)
    public void getEmptyDeviceProtocolSecurityPropertySetTest() {
        Device device = getMockedDevice(false);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());
        when(deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.empty());

        ComTaskExecutionOrganizer organizer = new ComTaskExecutionOrganizer(topologyService);

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = organizer.defineComTaskExecutionOrders(Arrays.asList(comTaskExecution));

        // asserts: see expected exception rule
    }

    @Test
    public void getNonEmptyDeviceProtocolSecurityPropertySetTest() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());

        final int authenticationAccessLevelId = 12;
        final int encryptionAccessLevelId = 65;

        final String securityPropName1 = "securityPropName1";
        final String securityPropName2 = "securityPropName2";
        final String securityPropName3 = "securityPropName3";
        final String securityPropValue1 = "securityPropValue1";
        final String securityPropValue2 = "securityPropValue2";
        final String securityPropValue3 = "securityPropValue3";
        List<Pair<String, String>> securityPropertyNameValues = createSecurityPropertyNameValueList(securityPropName1, securityPropValue1, securityPropName2, securityPropValue2, securityPropName3, securityPropValue3);
        final SecurityPropertySet securityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId, encryptionAccessLevelId, securityPropertyNameValues);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(device.getDeviceConfiguration().getComTaskEnablementFor(comTaskExecution.getComTask())).thenReturn(Optional.of(comTaskEnablement));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(comTaskExecution));

        // asserts
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity = deviceOrganizedComTaskExecutions.get(0).getComTasksWithStepsAndSecurity();
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps = comTasksWithStepsAndSecurity.get(0);
        final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet();
        assertThat(deviceProtocolSecurityPropertySet).isNotNull();
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(securityPropName1)).isEqualTo(securityPropValue1);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(securityPropName2)).isEqualTo(securityPropValue2);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(securityPropName3)).isEqualTo(securityPropValue3);
    }

    @Test
    public void differentSecurityPropertySetsForComTaskEnablementsTest() {
        Device device = getMockedDevice(false);
        ComTaskExecution firstComTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution secondComTaskExecution = createMockedComTaskExecution(device, createMockedRegistersComTask());

        final int authenticationAccessLevelId_1 = 12;
        final int encryptionAccessLevelId_1 = 65;

        final int authenticationAccessLevelId_2 = 17;
        final int encryptionAccessLevelId_2 = 23;

        final String securityPropName1 = "securityPropName1";
        final String securityPropName2 = "securityPropName2";
        final String securityPropName3 = "securityPropName3";
        final String securityPropValue1 = "securityPropValue1";

        final String securityPropValue2 = "securityPropValue2";
        final String securityPropValue3 = "securityPropValue3";

        // create first mock of securitySet
        List<Pair<String, String>> firstSecurityPropertyNameValues = createSecurityPropertyNameValueList(securityPropName1, securityPropValue1, securityPropName2, securityPropValue2);
        final SecurityPropertySet firstSecurityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId_1, encryptionAccessLevelId_1, firstSecurityPropertyNameValues);
        ComTaskEnablement firstComTaskEnablement = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(firstComTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(firstComTaskExecution.getComTask())).thenReturn(Optional.of(firstComTaskEnablement));

        // create second mock of securitySet
        List<Pair<String, String>> secondSecurityPropertyNameValues = createSecurityPropertyNameValueList(securityPropName3, securityPropValue3);
        final SecurityPropertySet secondSecurityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId_2, encryptionAccessLevelId_2, secondSecurityPropertyNameValues);
        ComTaskEnablement secondComTaskEnablement = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet);
        when(secondComTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablementFor(secondComTaskExecution.getComTask())).thenReturn(Optional.of(secondComTaskEnablement));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(firstComTaskExecution, secondComTaskExecution));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()).hasSize(2);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps first = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(0);
        assertThat(first.getComTaskExecution()).isEqualTo(firstComTaskExecution);
        assertThat(first.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_1);
        assertThat(first.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_1);
        assertThat(first.getDeviceProtocolSecurityPropertySet().getSecurityProperties().getProperty(securityPropName1)).isEqualTo(securityPropValue1);
        assertThat(first.getDeviceProtocolSecurityPropertySet().getSecurityProperties().getProperty(securityPropName2)).isEqualTo(securityPropValue2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps second = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(1);
        assertThat(second.getComTaskExecution()).isEqualTo(secondComTaskExecution);
        assertThat(second.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_2);
        assertThat(second.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_2);
        assertThat(second.getDeviceProtocolSecurityPropertySet().getSecurityProperties().getProperty(securityPropName3)).isEqualTo(securityPropValue3);
    }

    @Test
    public void twoDevicesEachTwoSecuritySetsEachSetTwoComTasksTest() {
        /* we will use the same id's for both devices */
        final int authenticationId_1 = 12;
        final int encryptionId_1 = 65;
        final int authenticationId_2 = 17;
        final int encryptionId_2 = 23;

        /* we will reuse the securityPropertyList */
        List<Pair<String, String>> securityPropertyNameValueList = createSecurityPropertyNameValueList();

        Device firstDevice = getMockedDevice(false);
        ComTaskExecution firstCTD_1 = createMockedComTaskExecution(firstDevice, createMockedTopologyComTask());
        ComTaskExecution secondCTD_1 = createMockedComTaskExecution(firstDevice, createMockedRegistersComTask());
        ComTaskExecution thirdCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLoadProfilesComTask());
        ComTaskExecution fourthCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLogBooksComTask());

        // create mock of first securitySet of first device
        final SecurityPropertySet firstSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D1);

        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(firstCTD_1.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D1));
        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(secondCTD_1.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D1));

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D1);

        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(thirdCTD_1.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D1));
        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(fourthCTD_1.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D1));

        Device secondDevice = getMockedDevice(false);
        final ComTaskExecution firstCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLoadProfilesComTask());
        final ComTaskExecution secondCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLogBooksComTask());
        final ComTaskExecution thirdCTD_2 = createMockedComTaskExecution(secondDevice, createMockedTopologyComTask());
        final ComTaskExecution fourthCTD_2 = createMockedComTaskExecution(secondDevice, createMockedRegistersComTask());

        // create mock of first securitySet of second device
        final SecurityPropertySet firstSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D2);

        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(firstCTD_2.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D2));
        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(secondCTD_2.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D2));

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D2);

        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(thirdCTD_2.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D2));
        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(fourthCTD_2.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D2));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(firstCTD_1, firstCTD_2, secondCTD_1, secondCTD_2, thirdCTD_1, thirdCTD_2, fourthCTD_1, fourthCTD_2));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
            if (deviceOrganizedComTaskExecution.getDevice().getId() == firstDevice.getId()) {
                assertThat(deviceOrganizedComTaskExecution.getComTaskExecutions()).containsOnly(firstCTD_1, secondCTD_1, thirdCTD_1, fourthCTD_1);
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity1 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(comTasksWithStepsAndSecurity1).isNotEmpty();
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(0);
                assertThat(firstCTWSAS_d1.getComTaskExecution()).isEqualTo(firstCTD_1);
                assertThat(firstCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
                assertThat(firstCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(1);
                assertThat(secondCTWSAS_d1.getComTaskExecution()).isEqualTo(secondCTD_1);
                assertThat(secondCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
                assertThat(secondCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(2);
                assertThat(thirdCTWSAS_d1.getComTaskExecution()).isEqualTo(thirdCTD_1);
                assertThat(thirdCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
                assertThat(thirdCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourthCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(3);
                assertThat(fourthCTWSAS_d1.getComTaskExecution()).isEqualTo(fourthCTD_1);
                assertThat(fourthCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
                assertThat(fourthCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
            }
            else {
                assertThat(deviceOrganizedComTaskExecution.getComTaskExecutions()).containsOnly(firstCTD_2, secondCTD_2, thirdCTD_2, fourthCTD_2);
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity2 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(comTasksWithStepsAndSecurity2).isNotEmpty();
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(0);
                assertThat(firstCTWSAS_d2.getComTaskExecution()).isEqualTo(firstCTD_2);
                assertThat(firstCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
                assertThat(firstCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(1);
                assertThat(secondCTWSAS_d2.getComTaskExecution()).isEqualTo(secondCTD_2);
                assertThat(secondCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
                assertThat(secondCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(2);
                assertThat(thirdCTWSAS_d2.getComTaskExecution()).isEqualTo(thirdCTD_2);
                assertThat(thirdCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
                assertThat(thirdCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
                final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourthCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(3);
                assertThat(fourthCTWSAS_d2.getComTaskExecution()).isEqualTo(fourthCTD_2);
                assertThat(fourthCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
                assertThat(fourthCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
            }
        }
    }

    @Test
    public void twoDevicesMultipleSecuritySetsMultipleComTasksConnectionStepsTest() {
        /* we will use the same id's for both devices */
        final int authenticationId_1 = 12;
        final int encryptionId_1 = 65;
        final int authenticationId_2 = 17;
        final int encryptionId_2 = 23;

        /* we will reuse the securityPropertyList */
        List<Pair<String, String>> securityPropertyNameValueList = createSecurityPropertyNameValueList();

        Device firstDevice = getMockedDevice(false);
        ComTaskExecution firstCTD_1 = createMockedComTaskExecution(firstDevice, createMockedTaskWithBasicCheckInMiddleOfProtocolTasks());
        ComTaskExecution secondCTD_1 = createMockedComTaskExecution(firstDevice, createMockedTopologyComTask());
        ComTaskExecution thirdCTD_1 = createMockedComTaskExecution(firstDevice, createMockedRegistersComTask());
        ComTaskExecution fourthCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLoadProfilesComTask());
        ComTaskExecution fifthCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLogBooksComTask());

        // create mock of first securitySet of first device
        final SecurityPropertySet firstSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D1);

        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(firstCTD_1.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D1));
        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(secondCTD_1.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D1));
        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(fifthCTD_1.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D1));

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D1);

        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(thirdCTD_1.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D1));
        when(firstDevice.getDeviceConfiguration().getComTaskEnablementFor(fourthCTD_1.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D1));

        Device secondDevice = getMockedDevice(false);
        final ComTaskExecution firstCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLoadProfilesComTask());
        final ComTaskExecution secondCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLogBooksComTask());
        final ComTaskExecution thirdCTD_2 = createMockedComTaskExecution(secondDevice, createMockedTopologyComTask());
        final ComTaskExecution fourthCTD_2 = createMockedComTaskExecution(secondDevice, createMockedRegistersComTask());

        // create mock of first securitySet of second device
        final SecurityPropertySet firstSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D2);

        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(firstCTD_2.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D2));
        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(secondCTD_2.getComTask())).thenReturn(Optional.of(firstComTaskEnablement_D2));

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D2);

        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(thirdCTD_2.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D2));
        when(secondDevice.getDeviceConfiguration().getComTaskEnablementFor(fourthCTD_2.getComTask())).thenReturn(Optional.of(secondComTaskEnablement_D2));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(firstCTD_1, firstCTD_2, secondCTD_1, secondCTD_2, thirdCTD_1, thirdCTD_2, fourthCTD_1, fourthCTD_2, fifthCTD_1));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        for (DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution : deviceOrganizedComTaskExecutions) {
            if (deviceOrganizedComTaskExecution.getDevice().getId() == firstDevice.getId()) {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice1).hasSize(5);

                final ComTaskExecutionConnectionSteps connectionSteps_D1_1 = orderedExecutionDevice1.get(0).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D1_1).isNotNull();
                assertThat(connectionSteps_D1_1.isLogOnRequired()).isTrue();
                assertThat(connectionSteps_D1_1.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(connectionSteps_D1_1.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D1_2 = orderedExecutionDevice1.get(1).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D1_2).isNotNull();
                assertThat(connectionSteps_D1_2.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_2.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(connectionSteps_D1_2.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D1_3 = orderedExecutionDevice1.get(2).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D1_3).isNotNull();
                assertThat(connectionSteps_D1_3.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_3.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_3.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(connectionSteps_D1_3.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D1_4 = orderedExecutionDevice1.get(3).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D1_4).isNotNull();
                assertThat(connectionSteps_D1_4.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_4.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(connectionSteps_D1_4.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(connectionSteps_D1_4.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D1_5 = orderedExecutionDevice1.get(4).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D1_5).isNotNull();
                assertThat(connectionSteps_D1_5.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_5.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(connectionSteps_D1_5.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(connectionSteps_D1_5.isLogOffRequired()).isFalse();
            }
            else {
                final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
                assertThat(orderedExecutionDevice2).hasSize(4);

                final ComTaskExecutionConnectionSteps connectionSteps_D2_1 = orderedExecutionDevice2.get(0).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D2_1).isNotNull();
                assertThat(connectionSteps_D2_1.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D2_1.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(connectionSteps_D2_1.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(connectionSteps_D2_1.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D2_2 = orderedExecutionDevice2.get(1).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D2_2).isNotNull();
                assertThat(connectionSteps_D2_2.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D2_2.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(connectionSteps_D2_2.isDaisyChainedLogOffRequired()).isTrue();
                assertThat(connectionSteps_D2_2.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D2_3 = orderedExecutionDevice2.get(2).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D2_3).isNotNull();
                assertThat(connectionSteps_D2_3.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D2_3.isDaisyChainedLogOnRequired()).isTrue();
                assertThat(connectionSteps_D2_3.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(connectionSteps_D2_3.isLogOffRequired()).isFalse();

                final ComTaskExecutionConnectionSteps connectionSteps_D2_4 = orderedExecutionDevice2.get(3).getComTaskExecutionConnectionSteps();
                assertThat(connectionSteps_D2_4).isNotNull();
                assertThat(connectionSteps_D2_4.isLogOnRequired()).isFalse();
                assertThat(connectionSteps_D2_4.isDaisyChainedLogOnRequired()).isFalse();
                assertThat(connectionSteps_D2_4.isDaisyChainedLogOffRequired()).isFalse();
                assertThat(connectionSteps_D2_4.isLogOffRequired()).isTrue();
            }
        }
    }

    @Test
    public void basicCheckSwitchDifferentSecuritySetsTest() {
        Device device = getMockedDevice(false);
        ComTaskExecution firstComTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution secondComTaskExecution = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution thirdComTaskExecution = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());
        ComTaskExecution fourthComTaskExecution = createMockedComTaskExecution(device, createMockedTaskWithBasicCheckInMiddleOfProtocolTasks());

        final int authenticationAccessLevelId_1 = 12;
        final int encryptionAccessLevelId_1 = 65;

        final int authenticationAccessLevelId_2 = 17;
        final int encryptionAccessLevelId_2 = 23;

        /* We will reuse the nameValue pairs */
        List<Pair<String, String>> securityPropertyNameValueList = createSecurityPropertyNameValueList();
        // create first mock of securitySet
        final SecurityPropertySet firstSecurityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId_1, encryptionAccessLevelId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet);
        when(device.getDeviceConfiguration().getComTaskEnablementFor(firstComTaskExecution.getComTask())).thenReturn(Optional.of(firstComTaskEnablement));

        // create second mock of securitySet
        final SecurityPropertySet secondSecurityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId_2, encryptionAccessLevelId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet);
        when(device.getDeviceConfiguration().getComTaskEnablementFor(secondComTaskExecution.getComTask())).thenReturn(Optional.of(secondComTaskEnablement));

        // create third mock of securitySet -> reusing the firstSecurityPropertySet as it should be the same
        ComTaskEnablement thirdComTaskEnablement = mock(ComTaskEnablement.class);
        when(thirdComTaskEnablement.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet);
        when(device.getDeviceConfiguration().getComTaskEnablementFor(thirdComTaskExecution.getComTask())).thenReturn(Optional.of(thirdComTaskEnablement));

        // create fourth mock of securitySet -> reusing the secondSecurityPropertySet as it should be the same
        ComTaskEnablement fourthComTaskEnablement = mock(ComTaskEnablement.class);
        when(fourthComTaskEnablement.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet);
        when(device.getDeviceConfiguration().getComTaskEnablementFor(fourthComTaskExecution.getComTask())).thenReturn(Optional.of(fourthComTaskEnablement));

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                new ComTaskExecutionOrganizer(topologyService).defineComTaskExecutionOrders(Arrays.asList(firstComTaskExecution, secondComTaskExecution, thirdComTaskExecution, fourthComTaskExecution));

        // asserts
        assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        assertThat(deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()).hasSize(4);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps first = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(0);
        // the basicCheck comTask should be the first
        assertThat(first.getComTaskExecution()).isEqualTo(fourthComTaskExecution);
        assertThat(first.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_2);
        assertThat(first.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps second = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(1);
        assertThat(second.getComTaskExecution()).isEqualTo(secondComTaskExecution);
        assertThat(second.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_2);
        assertThat(second.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps third = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(2);
        assertThat(third.getComTaskExecution()).isEqualTo(firstComTaskExecution);
        assertThat(third.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_1);
        assertThat(third.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_1);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourth = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(3);
        assertThat(fourth.getComTaskExecution()).isEqualTo(thirdComTaskExecution);
        assertThat(fourth.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_1);
        assertThat(fourth.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_1);
    }

    private List<Pair<String, String>> createSecurityPropertyNameValueList(String... arguments) {
        if (arguments.length % 2 != 0) {
            throw new ApplicationException("The provided arguments should be a multiple of two, otherwise we can't create pairs!");
        }
        List<Pair<String, String>> pairList = new ArrayList<>();
        for (int i = 0; i < arguments.length; i += 2) {
            String name = arguments[i];
            String value = arguments[i + 1];
            pairList.add(Pair.of(name, value));
        }
        return pairList;
    }

    private SecurityPropertySet createMockedSecurityPropertySet(final Device device, final int authenticationAccessLevelId, final int encryptionAccessLevelId,
                                                                List<Pair<String, String>> securityPropertiesNameValues) {
        SecurityPropertySet securityPropertySet = this.mockSecurityPropertySet();
        List<SecurityProperty> securityProperties = new ArrayList<>();
        for (Pair<String, String> securityPropertiesNameValue : securityPropertiesNameValues) {
            securityProperties.add(createMockedSecurityProperty(securityPropertiesNameValue.getFirst(), securityPropertiesNameValue.getLast()));
        }
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(authenticationAccessLevelId);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(encryptionAccessLevelId);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityProperties(securityPropertySet)).thenReturn(securityProperties);
        return securityPropertySet;
    }

    private Device mockDevice() {
        Device device = mock(Device.class);
        long id = this.nextDeviceId++;
        when(device.getId()).thenReturn(id);
        when(device.toString()).thenReturn("Device with ID : " + id);
        return device;
    }

    private SecurityPropertySet mockSecurityPropertySet() {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(this.nextSecurityPropertyId++);
        return securityPropertySet;
    }

    private SecurityProperty createMockedSecurityProperty(String name, Object value) {
        SecurityProperty securityProperty = mock(SecurityProperty.class);
        when(securityProperty.getName()).thenReturn(name);
        when(securityProperty.getValue()).thenReturn(value);
        return securityProperty;
    }

    private ComTask createMockedTaskWithBasicCheckInMiddleOfProtocolTasks() {
        ComTask comTask = mock(ComTask.class);
        ProtocolTask loadProfileTask = mock(LoadProfilesTask.class);
        ProtocolTask logBookTask = mock(LogBooksTask.class);
        ProtocolTask basicCheckTask = mock(BasicCheckTask.class);
        ProtocolTask topologyTask = mock(TopologyTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(loadProfileTask, logBookTask, basicCheckTask, topologyTask));
        return comTask;
    }

}

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.communication.tasks.ServerComTaskEnablementFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionConnectionSteps;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionOrganizer;
import com.energyict.mdc.engine.impl.core.DeviceOrganizedComTaskExecution;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.TopologyTask;
import com.energyict.mdw.core.CommunicationDevice;
import com.energyict.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.mdc.engine.impl.core.ComTaskExecutionOrganizer} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/04/13
 * Time: 8:38
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionOrganizerTest {

    @Mock
    private ServerManager manager;
    @Mock
    private ServerComTaskEnablementFactory comTaskEnablementFactory;

    @Before
    public void setUp() {
        ManagerFactory.setCurrent(manager);
        when(manager.getComTaskEnablementFactory()).thenReturn(comTaskEnablementFactory);
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
        when(comTaskExecution.getDevice()).thenReturn(device);
        return comTaskExecution;
    }

    private CommunicationDevice getMockedDevice(boolean slave) {
        final CommunicationDevice device = mock(CommunicationDevice.class);
        final DeviceType deviceType = mock(DeviceType.class);
        final DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        final DeviceCommunicationConfiguration deviceCommunicationConfiguration = mock(DeviceCommunicationConfiguration.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.isLogicalSlave()).thenReturn(slave);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
        return device;
    }

    @Test
    public void testSingleDeviceSingleComTask() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(comTaskExecution);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecution = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecution).hasSize(1);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps singlePair = orderedExecution.get(0);
        Assertions.assertThat(singlePair.getComTaskExecution()).isEqualTo(comTaskExecution);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = singlePair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps).isNotNull();
        Assertions.assertThat(comTaskExecutionConnectionSteps.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps.isLogOffRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps.isDaisyChainedLogOffRequired()).isFalse();
    }

    @Test
    public void testSingleDeviceTwoComTasks() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(comTaskExecution1, comTaskExecution2);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecution = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecution).hasSize(2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPair = orderedExecution.get(0);
        Assertions.assertThat(firstPair.getComTaskExecution()).isEqualTo(comTaskExecution1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = firstPair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPair = orderedExecution.get(1);
        Assertions.assertThat(secondPair.getComTaskExecution()).isEqualTo(comTaskExecution2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = secondPair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isTrue();
    }

    @Test
    public void testSingleDeviceMultipleComTasks() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution4 = createMockedComTaskExecution(device, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3, comTaskExecution4);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecution = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecution).hasSize(4);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPair = orderedExecution.get(0);
        Assertions.assertThat(firstPair.getComTaskExecution()).isEqualTo(comTaskExecution1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = firstPair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPair = orderedExecution.get(1);
        Assertions.assertThat(secondPair.getComTaskExecution()).isEqualTo(comTaskExecution2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = secondPair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdPair = orderedExecution.get(2);
        Assertions.assertThat(thirdPair.getComTaskExecution()).isEqualTo(comTaskExecution3);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3 = thirdPair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourthPair = orderedExecution.get(3);
        Assertions.assertThat(fourthPair.getComTaskExecution()).isEqualTo(comTaskExecution4);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4 = fourthPair.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isLogOffRequired()).isTrue();
    }

    @Test
    public void testTwoDevicesEachOneComTaskExecution() {
        Device device1 = getMockedDevice(false);
        Device device2 = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device1);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution_1.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice1).hasSize(1);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps singlePairDevice1 = orderedExecutionDevice1.get(0);
        Assertions.assertThat(singlePairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = singlePairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_2 = deviceOrganizedComTaskExecutions.get(1);
        Assertions.assertThat(deviceOrganizedComTaskExecution_2.getDevice()).isEqualTo(device2);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution_2.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice2).hasSize(1);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps singlePairDevice2 = orderedExecutionDevice2.get(0);
        Assertions.assertThat(singlePairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = singlePairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isTrue();
    }

    @Test
    public void testTwoDevicesEachTwoComTaskExecutions() {
        Device device1 = getMockedDevice(false);
        Device device2 = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution4 = createMockedComTaskExecution(device2, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3, comTaskExecution4);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device1);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution_1.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice1).hasSize(2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice1 = orderedExecutionDevice1.get(0);
        Assertions.assertThat(firstPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1 = firstPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice1 = orderedExecutionDevice1.get(1);
        Assertions.assertThat(lastPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2 = lastPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_2 = deviceOrganizedComTaskExecutions.get(1);
        Assertions.assertThat(deviceOrganizedComTaskExecution_2.getDevice()).isEqualTo(device2);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution_2.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice2).hasSize(2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice2 = orderedExecutionDevice2.get(0);
        Assertions.assertThat(firstPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution3);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3 = firstPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice2 = orderedExecutionDevice2.get(1);
        Assertions.assertThat(lastPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution4);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4 = lastPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4.isLogOffRequired()).isTrue();
    }

    @Test
    public void testTwoDevicesEachMultipleComTaskExecutions() {
        Device device1 = getMockedDevice(false);
        Device device2 = getMockedDevice(false);

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
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1_d1, comTaskExecution2_d1, comTaskExecution3_d1, comTaskExecution4_d1,
                        comTaskExecution1_d2, comTaskExecution2_d2, comTaskExecution3_d2, comTaskExecution4_d2);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device1);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution_1.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice1).hasSize(4);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice1 = orderedExecutionDevice1.get(0);
        Assertions.assertThat(firstPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d1 = firstPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice1 = orderedExecutionDevice1.get(1);
        Assertions.assertThat(secondPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution2_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d1 = secondPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdPairDevice1 = orderedExecutionDevice1.get(2);
        Assertions.assertThat(thirdPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution3_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d1 = thirdPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice1 = orderedExecutionDevice1.get(3);
        Assertions.assertThat(lastPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution4_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4_d1 = lastPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d1.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d1.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d1.isLogOffRequired()).isFalse();


        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_2 = deviceOrganizedComTaskExecutions.get(1);
        Assertions.assertThat(deviceOrganizedComTaskExecution_2.getDevice()).isEqualTo(device2);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution_2.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice2).hasSize(4);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice2 = orderedExecutionDevice2.get(0);
        Assertions.assertThat(firstPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution1_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d2 = firstPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice2 = orderedExecutionDevice2.get(1);
        Assertions.assertThat(secondPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution2_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d2 = secondPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdPairDevice2 = orderedExecutionDevice2.get(2);
        Assertions.assertThat(thirdPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution3_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d2 = thirdPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice2 = orderedExecutionDevice2.get(3);
        Assertions.assertThat(lastPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution4_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps4_d2 = lastPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps4_d2.isLogOffRequired()).isTrue();
    }

    @Test
    public void threeDevicesEachThreeComTaskExecutionsTest() {
        Device device1 = getMockedDevice(false);
        Device device2 = getMockedDevice(false);
        Device device3 = getMockedDevice(false);

        ComTaskExecution comTaskExecution1_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d1 = createMockedComTaskExecution(device1, createMockedTopologyComTask());

        ComTaskExecution comTaskExecution1_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d2 = createMockedComTaskExecution(device2, createMockedTopologyComTask());

        ComTaskExecution comTaskExecution1_d3 = createMockedComTaskExecution(device3, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2_d3 = createMockedComTaskExecution(device3, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution3_d3 = createMockedComTaskExecution(device3, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1_d1, comTaskExecution2_d1, comTaskExecution3_d1,
                        comTaskExecution1_d2, comTaskExecution2_d2, comTaskExecution3_d2,
                        comTaskExecution1_d3, comTaskExecution2_d3, comTaskExecution3_d3);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(3);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device1);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution_1.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice1).hasSize(3);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice1 = orderedExecutionDevice1.get(0);
        Assertions.assertThat(firstPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution1_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d1 = firstPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice1 = orderedExecutionDevice1.get(1);
        Assertions.assertThat(secondPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution2_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d1 = secondPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice1 = orderedExecutionDevice1.get(2);
        Assertions.assertThat(lastPairDevice1.getComTaskExecution()).isEqualTo(comTaskExecution3_d1);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d1 = lastPairDevice1.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d1.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_2 = deviceOrganizedComTaskExecutions.get(1);
        Assertions.assertThat(deviceOrganizedComTaskExecution_2.getDevice()).isEqualTo(device2);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution_2.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice2).hasSize(3);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice2 = orderedExecutionDevice2.get(0);
        Assertions.assertThat(firstPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution1_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d2 = firstPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice2 = orderedExecutionDevice2.get(1);
        Assertions.assertThat(secondPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution2_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d2 = secondPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice2 = orderedExecutionDevice2.get(2);
        Assertions.assertThat(lastPairDevice2.getComTaskExecution()).isEqualTo(comTaskExecution3_d2);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d2 = lastPairDevice2.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d2.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_3 = deviceOrganizedComTaskExecutions.get(2);
        Assertions.assertThat(deviceOrganizedComTaskExecution_3.getDevice()).isEqualTo(device3);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice3 = deviceOrganizedComTaskExecution_3.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice3).hasSize(3);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstPairDevice3 = orderedExecutionDevice3.get(0);
        Assertions.assertThat(firstPairDevice3.getComTaskExecution()).isEqualTo(comTaskExecution1_d3);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps1_d3 = firstPairDevice3.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d3.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d3.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d3.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps1_d3.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondPairDevice3 = orderedExecutionDevice3.get(1);
        Assertions.assertThat(secondPairDevice3.getComTaskExecution()).isEqualTo(comTaskExecution2_d3);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps2_d3 = secondPairDevice3.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d3.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d3.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d3.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps2_d3.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps lastPairDevice3 = orderedExecutionDevice3.get(2);
        Assertions.assertThat(lastPairDevice3.getComTaskExecution()).isEqualTo(comTaskExecution3_d3);

        final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps3_d3 = lastPairDevice3.getComTaskExecutionConnectionSteps();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d3.isLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d3.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d3.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(comTaskExecutionConnectionSteps3_d3.isLogOffRequired()).isTrue();
    }

    @Test
    public void comTaskExecutionWithBasicCheckInFrontOfTheLineTest() {
        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());
        ComTaskExecution comTaskExecution4 = createMockedComTaskExecution(device, createMockedTaskWithBasicCheckInMiddleOfProtocolTasks());
        ComTaskExecution comTaskExecution5 = createMockedComTaskExecution(device, createMockedLogBooksComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3, comTaskExecution4, comTaskExecution5);

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithSteps = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();

        // asserts
        Assertions.assertThat(comTasksWithSteps.get(0).getComTaskExecution()).isEqualTo(comTaskExecution4);
        Assertions.assertThat(comTasksWithSteps.get(1).getComTaskExecution()).isEqualTo(comTaskExecution1);
        Assertions.assertThat(comTasksWithSteps.get(2).getComTaskExecution()).isEqualTo(comTaskExecution2);
        Assertions.assertThat(comTasksWithSteps.get(3).getComTaskExecution()).isEqualTo(comTaskExecution3);
        Assertions.assertThat(comTasksWithSteps.get(4).getComTaskExecution()).isEqualTo(comTaskExecution5);
    }

    @Test
    public void comTaskExecutionsWithSlaveCapabilitiesButNoMasterTest() {
        Device device = getMockedDevice(false);

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3);

        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device);
    }

    @Test
    public void comTaskExecutionsWithSlaveCapabilitiesAndMasterTest() {
        Device device = getMockedDevice(true);
        Device master = getMockedDevice(false);
        when(device.getPhysicalGateway()).thenReturn(master);

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3);

        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(master);
    }

    /**
     * The master does have a slave, but he slave can itself setup a connection
     */
    @Test
    public void comTaskExecutionsWithFakeSlaveCapabilitiesAndMasterTest() {
        Device device = getMockedDevice(false);
        Device master = getMockedDevice(false);
        when(device.getPhysicalGateway()).thenReturn(master);

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3);

        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device);
    }

    @Test
    public void comTaskExecutionsWithSlaveCapabilitiesAndMultipleHierarchyTest() {
        Device device = getMockedDevice(true);
        Device master1 = getMockedDevice(true);
        Device masterOfMaster1 = getMockedDevice(false);
        when(device.getPhysicalGateway()).thenReturn(master1);
        when(master1.getPhysicalGateway()).thenReturn(masterOfMaster1);

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3);

        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(masterOfMaster1);
    }

    @Test
    public void comTaskExecutionWithoutSlaveCapabilitiesButWithMasterDeviceTest() {
        Device device = getMockedDevice(false);
        Device master1 = getMockedDevice(false);
        Device masterOfMaster1 = getMockedDevice(false);
        when(device.getPhysicalGateway()).thenReturn(master1);
        when(master1.getPhysicalGateway()).thenReturn(masterOfMaster1);

        ComTaskExecution comTaskExecution1 = createMockedComTaskExecution(device, createMockedTopologyComTask());
        ComTaskExecution comTaskExecution2 = createMockedComTaskExecution(device, createMockedRegistersComTask());
        ComTaskExecution comTaskExecution3 = createMockedComTaskExecution(device, createMockedLoadProfilesComTask());

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(
                        comTaskExecution1, comTaskExecution2, comTaskExecution3);

        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(device);
    }

    @Test
    public void getEmptyDeviceProtocolSecurityPropertySetTest() {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(any(DeviceCommunicationConfiguration.class), any(ComTask.class))).thenReturn(comTaskEnablement);

        Device device = getMockedDevice(false);
        ComTaskExecution comTaskExecution = createMockedComTaskExecution(device, createMockedTopologyComTask());

        // business exception
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(comTaskExecution);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity = deviceOrganizedComTaskExecutions.get(0).getComTasksWithStepsAndSecurity();
        Assertions.assertThat(comTasksWithStepsAndSecurity).isNotNull();
        Assertions.assertThat(comTasksWithStepsAndSecurity).hasSize(1);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps = comTasksWithStepsAndSecurity.get(0);
        Assertions.assertThat(comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet()).isNotNull();
        Assertions.assertThat(comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet()).isEqualTo(ComTaskExecutionOrganizer.EMPTY_DEVICE_PROTOCOL_SECURITY_PROPERTY_SET);
    }

    @Test
    public void getNonEmptyDeviceProtocolSecurityPropertySetTest() {
        CommunicationDevice device = getMockedDevice(false);
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
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), comTaskExecution.getComTask())).thenReturn(comTaskEnablement);

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(comTaskExecution);

        // asserts
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity = deviceOrganizedComTaskExecutions.get(0).getComTasksWithStepsAndSecurity();
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps comTaskWithSecurityAndConnectionSteps = comTasksWithStepsAndSecurity.get(0);
        final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = comTaskWithSecurityAndConnectionSteps.getDeviceProtocolSecurityPropertySet();
        Assertions.assertThat(deviceProtocolSecurityPropertySet).isNotNull();
        Assertions.assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId);
        Assertions.assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId);
        Assertions.assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(securityPropName1)).isEqualTo(securityPropValue1);
        Assertions.assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(securityPropName2)).isEqualTo(securityPropValue2);
        Assertions.assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(securityPropName3)).isEqualTo(securityPropValue3);
    }

    @Test
    public void differentSecurityPropertySetsForComTaskEnablementsTest() {
        CommunicationDevice device = getMockedDevice(false);
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
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), firstComTaskExecution.getComTask())).thenReturn(firstComTaskEnablement);

        // create second mock of securitySet
        List<Pair<String, String>> secondSecurityPropertyNameValues = createSecurityPropertyNameValueList(securityPropName3, securityPropValue3);
        final SecurityPropertySet secondSecurityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId_2, encryptionAccessLevelId_2, secondSecurityPropertyNameValues);
        ComTaskEnablement secondComTaskEnablement = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), secondComTaskExecution.getComTask())).thenReturn(secondComTaskEnablement);

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = ComTaskExecutionOrganizer.defineComTaskExecutionOrders(firstComTaskExecution, secondComTaskExecution);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()).hasSize(2);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps first = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(0);
        Assertions.assertThat(first.getComTaskExecution()).isEqualTo(firstComTaskExecution);
        Assertions.assertThat(first.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_1);
        Assertions.assertThat(first.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_1);
        Assertions.assertThat(first.getDeviceProtocolSecurityPropertySet().getSecurityProperties().getProperty(securityPropName1)).isEqualTo(securityPropValue1);
        Assertions.assertThat(first.getDeviceProtocolSecurityPropertySet().getSecurityProperties().getProperty(securityPropName2)).isEqualTo(securityPropValue2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps second = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(1);
        Assertions.assertThat(second.getComTaskExecution()).isEqualTo(secondComTaskExecution);
        Assertions.assertThat(second.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_2);
        Assertions.assertThat(second.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_2);
        Assertions.assertThat(second.getDeviceProtocolSecurityPropertySet().getSecurityProperties().getProperty(securityPropName3)).isEqualTo(securityPropValue3);
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

        CommunicationDevice firstDevice = getMockedDevice(false);
        ComTaskExecution firstCTD_1 = createMockedComTaskExecution(firstDevice, createMockedTopologyComTask());
        ComTaskExecution secondCTD_1 = createMockedComTaskExecution(firstDevice, createMockedRegistersComTask());
        ComTaskExecution thirdCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLoadProfilesComTask());
        ComTaskExecution fourthCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLogBooksComTask());

        // create mock of first securitySet of first device
        final SecurityPropertySet firstSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D1);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                firstCTD_1.getComTask())).thenReturn(firstComTaskEnablement_D1);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                secondCTD_1.getComTask())).thenReturn(firstComTaskEnablement_D1);

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D1);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                thirdCTD_1.getComTask())).thenReturn(secondComTaskEnablement_D1);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                fourthCTD_1.getComTask())).thenReturn(secondComTaskEnablement_D1);

        CommunicationDevice secondDevice = getMockedDevice(false);
        final ComTaskExecution firstCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLoadProfilesComTask());
        final ComTaskExecution secondCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLogBooksComTask());
        final ComTaskExecution thirdCTD_2 = createMockedComTaskExecution(secondDevice, createMockedTopologyComTask());
        final ComTaskExecution fourthCTD_2 = createMockedComTaskExecution(secondDevice, createMockedRegistersComTask());

        // create mock of first securitySet of second device
        final SecurityPropertySet firstSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D2);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                firstCTD_2.getComTask())).thenReturn(firstComTaskEnablement_D2);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                secondCTD_2.getComTask())).thenReturn(firstComTaskEnablement_D2);

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D2);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                thirdCTD_2.getComTask())).thenReturn(secondComTaskEnablement_D2);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                fourthCTD_2.getComTask())).thenReturn(secondComTaskEnablement_D2);

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(firstCTD_1, firstCTD_2, secondCTD_1, secondCTD_2, thirdCTD_1, thirdCTD_2, fourthCTD_1, fourthCTD_2);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        final DeviceOrganizedComTaskExecution firstDeviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(firstDeviceOrganizedComTaskExecution.getDevice()).isEqualTo(firstDevice);
        Assertions.assertThat(firstDeviceOrganizedComTaskExecution.getComTaskExecutions()).containsOnly(firstCTD_1, secondCTD_1, thirdCTD_1, fourthCTD_1);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity1 = firstDeviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(comTasksWithStepsAndSecurity1).isNotEmpty();
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(0);
        Assertions.assertThat(firstCTWSAS_d1.getComTaskExecution()).isEqualTo(firstCTD_1);
        Assertions.assertThat(firstCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
        Assertions.assertThat(firstCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(1);
        Assertions.assertThat(secondCTWSAS_d1.getComTaskExecution()).isEqualTo(secondCTD_1);
        Assertions.assertThat(secondCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
        Assertions.assertThat(secondCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(2);
        Assertions.assertThat(thirdCTWSAS_d1.getComTaskExecution()).isEqualTo(thirdCTD_1);
        Assertions.assertThat(thirdCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
        Assertions.assertThat(thirdCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourthCTWSAS_d1 = comTasksWithStepsAndSecurity1.get(3);
        Assertions.assertThat(fourthCTWSAS_d1.getComTaskExecution()).isEqualTo(fourthCTD_1);
        Assertions.assertThat(fourthCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
        Assertions.assertThat(fourthCTWSAS_d1.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);

        final DeviceOrganizedComTaskExecution secondDeviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(1);
        Assertions.assertThat(secondDeviceOrganizedComTaskExecution.getDevice()).isEqualTo(secondDevice);
        Assertions.assertThat(secondDeviceOrganizedComTaskExecution.getComTaskExecutions()).containsOnly(firstCTD_2, secondCTD_2, thirdCTD_2, fourthCTD_2);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> comTasksWithStepsAndSecurity2 = secondDeviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(comTasksWithStepsAndSecurity2).isNotEmpty();
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps firstCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(0);
        Assertions.assertThat(firstCTWSAS_d2.getComTaskExecution()).isEqualTo(firstCTD_2);
        Assertions.assertThat(firstCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
        Assertions.assertThat(firstCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps secondCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(1);
        Assertions.assertThat(secondCTWSAS_d2.getComTaskExecution()).isEqualTo(secondCTD_2);
        Assertions.assertThat(secondCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_1);
        Assertions.assertThat(secondCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_1);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps thirdCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(2);
        Assertions.assertThat(thirdCTWSAS_d2.getComTaskExecution()).isEqualTo(thirdCTD_2);
        Assertions.assertThat(thirdCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
        Assertions.assertThat(thirdCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourthCTWSAS_d2 = comTasksWithStepsAndSecurity2.get(3);
        Assertions.assertThat(fourthCTWSAS_d2.getComTaskExecution()).isEqualTo(fourthCTD_2);
        Assertions.assertThat(fourthCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationId_2);
        Assertions.assertThat(fourthCTWSAS_d2.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionId_2);
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

        CommunicationDevice firstDevice = getMockedDevice(false);
        ComTaskExecution firstCTD_1 = createMockedComTaskExecution(firstDevice, createMockedTaskWithBasicCheckInMiddleOfProtocolTasks());
        ComTaskExecution secondCTD_1 = createMockedComTaskExecution(firstDevice, createMockedTopologyComTask());
        ComTaskExecution thirdCTD_1 = createMockedComTaskExecution(firstDevice, createMockedRegistersComTask());
        ComTaskExecution fourthCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLoadProfilesComTask());
        ComTaskExecution fifthCTD_1 = createMockedComTaskExecution(firstDevice, createMockedLogBooksComTask());

        // create mock of first securitySet of first device
        final SecurityPropertySet firstSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D1);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                firstCTD_1.getComTask())).thenReturn(firstComTaskEnablement_D1);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                secondCTD_1.getComTask())).thenReturn(firstComTaskEnablement_D1);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                fifthCTD_1.getComTask())).thenReturn(firstComTaskEnablement_D1);

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D1 = createMockedSecurityPropertySet(firstDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D1 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D1.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D1);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                thirdCTD_1.getComTask())).thenReturn(secondComTaskEnablement_D1);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                firstDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                fourthCTD_1.getComTask())).thenReturn(secondComTaskEnablement_D1);

        CommunicationDevice secondDevice = getMockedDevice(false);
        final ComTaskExecution firstCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLoadProfilesComTask());
        final ComTaskExecution secondCTD_2 = createMockedComTaskExecution(secondDevice, createMockedLogBooksComTask());
        final ComTaskExecution thirdCTD_2 = createMockedComTaskExecution(secondDevice, createMockedTopologyComTask());
        final ComTaskExecution fourthCTD_2 = createMockedComTaskExecution(secondDevice, createMockedRegistersComTask());

        // create mock of first securitySet of second device
        final SecurityPropertySet firstSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_1, encryptionId_1, securityPropertyNameValueList);
        ComTaskEnablement firstComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(firstComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet_D2);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                firstCTD_2.getComTask())).thenReturn(firstComTaskEnablement_D2);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                secondCTD_2.getComTask())).thenReturn(firstComTaskEnablement_D2);

        // create mock of second securitySet of first device
        final SecurityPropertySet secondSecurityPropertySet_D2 = createMockedSecurityPropertySet(secondDevice, authenticationId_2, encryptionId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement_D2 = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement_D2.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet_D2);

        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                thirdCTD_2.getComTask())).thenReturn(secondComTaskEnablement_D2);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                secondDevice.getDeviceConfiguration().getCommunicationConfiguration(),
                fourthCTD_2.getComTask())).thenReturn(secondComTaskEnablement_D2);

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(firstCTD_1, firstCTD_2, secondCTD_1, secondCTD_2, thirdCTD_1, thirdCTD_2, fourthCTD_1, fourthCTD_2, fifthCTD_1);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).isNotNull();
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(2);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_1 = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution_1.getDevice()).isEqualTo(firstDevice);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice1 = deviceOrganizedComTaskExecution_1.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice1).hasSize(5);

        final ComTaskExecutionConnectionSteps connectionSteps_D1_1 = orderedExecutionDevice1.get(0).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D1_1).isNotNull();
        Assertions.assertThat(connectionSteps_D1_1.isLogOnRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D1_1.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_1.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D1_2 = orderedExecutionDevice1.get(1).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D1_2).isNotNull();
        Assertions.assertThat(connectionSteps_D1_2.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_2.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_2.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D1_3 = orderedExecutionDevice1.get(2).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D1_3).isNotNull();
        Assertions.assertThat(connectionSteps_D1_3.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_3.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_3.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D1_3.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D1_4 = orderedExecutionDevice1.get(3).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D1_4).isNotNull();
        Assertions.assertThat(connectionSteps_D1_4.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_4.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D1_4.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_4.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D1_5 = orderedExecutionDevice1.get(4).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D1_5).isNotNull();
        Assertions.assertThat(connectionSteps_D1_5.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_5.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D1_5.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D1_5.isLogOffRequired()).isFalse();

        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution_2 = deviceOrganizedComTaskExecutions.get(1);
        Assertions.assertThat(deviceOrganizedComTaskExecution_2.getDevice()).isEqualTo(secondDevice);
        final List<DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps> orderedExecutionDevice2 = deviceOrganizedComTaskExecution_2.getComTasksWithStepsAndSecurity();
        Assertions.assertThat(orderedExecutionDevice2).hasSize(4);

        final ComTaskExecutionConnectionSteps connectionSteps_D2_1 = orderedExecutionDevice2.get(0).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D2_1).isNotNull();
        Assertions.assertThat(connectionSteps_D2_1.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_1.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D2_1.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_1.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D2_2 = orderedExecutionDevice2.get(1).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D2_2).isNotNull();
        Assertions.assertThat(connectionSteps_D2_2.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_2.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_2.isDaisyChainedLogOffRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D2_2.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D2_3 = orderedExecutionDevice2.get(2).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D2_3).isNotNull();
        Assertions.assertThat(connectionSteps_D2_3.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_3.isDaisyChainedLogOnRequired()).isTrue();
        Assertions.assertThat(connectionSteps_D2_3.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_3.isLogOffRequired()).isFalse();

        final ComTaskExecutionConnectionSteps connectionSteps_D2_4 = orderedExecutionDevice2.get(3).getComTaskExecutionConnectionSteps();
        Assertions.assertThat(connectionSteps_D2_4).isNotNull();
        Assertions.assertThat(connectionSteps_D2_4.isLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_4.isDaisyChainedLogOnRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_4.isDaisyChainedLogOffRequired()).isFalse();
        Assertions.assertThat(connectionSteps_D2_4.isLogOffRequired()).isTrue();
    }

    @Test
    public void basicCheckSwitchDifferentSecuritySetsTest() {
        CommunicationDevice device = getMockedDevice(false);
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
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), firstComTaskExecution.getComTask())).thenReturn(firstComTaskEnablement);

        // create second mock of securitySet
        final SecurityPropertySet secondSecurityPropertySet = createMockedSecurityPropertySet(device, authenticationAccessLevelId_2, encryptionAccessLevelId_2, securityPropertyNameValueList);
        ComTaskEnablement secondComTaskEnablement = mock(ComTaskEnablement.class);
        when(secondComTaskEnablement.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), secondComTaskExecution.getComTask())).thenReturn(secondComTaskEnablement);

        // create third mock of securitySet -> reusing the firstSecurityPropertySet as it should be the same
        ComTaskEnablement thirdComTaskEnablement = mock(ComTaskEnablement.class);
        when(thirdComTaskEnablement.getSecurityPropertySet()).thenReturn(firstSecurityPropertySet);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), thirdComTaskExecution.getComTask())).thenReturn(thirdComTaskEnablement);

        // create fourth mock of securitySet -> reusing the secondSecurityPropertySet as it should be the same
        ComTaskEnablement fourthComTaskEnablement = mock(ComTaskEnablement.class);
        when(fourthComTaskEnablement.getSecurityPropertySet()).thenReturn(secondSecurityPropertySet);
        when(this.comTaskEnablementFactory.findByDeviceCommunicationConfigurationAndComTask(
                device.getDeviceConfiguration().getCommunicationConfiguration(), fourthComTaskExecution.getComTask())).thenReturn(fourthComTaskEnablement);

        // business method
        final List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions =
                ComTaskExecutionOrganizer.defineComTaskExecutionOrders(firstComTaskExecution, secondComTaskExecution, thirdComTaskExecution, fourthComTaskExecution);

        // asserts
        Assertions.assertThat(deviceOrganizedComTaskExecutions).hasSize(1);
        final DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = deviceOrganizedComTaskExecutions.get(0);
        Assertions.assertThat(deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity()).hasSize(4);
        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps first = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(0);
        // the basicCheck comTask should be the first
        Assertions.assertThat(first.getComTaskExecution()).isEqualTo(fourthComTaskExecution);
        Assertions.assertThat(first.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_2);
        Assertions.assertThat(first.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps second = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(1);
        Assertions.assertThat(second.getComTaskExecution()).isEqualTo(secondComTaskExecution);
        Assertions.assertThat(second.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_2);
        Assertions.assertThat(second.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_2);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps third = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(2);
        Assertions.assertThat(third.getComTaskExecution()).isEqualTo(firstComTaskExecution);
        Assertions.assertThat(third.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_1);
        Assertions.assertThat(third.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_1);

        final DeviceOrganizedComTaskExecution.ComTaskWithSecurityAndConnectionSteps fourth = deviceOrganizedComTaskExecution.getComTasksWithStepsAndSecurity().get(3);
        Assertions.assertThat(fourth.getComTaskExecution()).isEqualTo(thirdComTaskExecution);
        Assertions.assertThat(fourth.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(authenticationAccessLevelId_1);
        Assertions.assertThat(fourth.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(encryptionAccessLevelId_1);
    }

    private List<Pair<String, String>> createSecurityPropertyNameValueList(String... arguments) {
        if (arguments.length % 2 != 0) {
            throw new ApplicationException("The provided arguments should be a multiple of two, otherwise we can't create pairs!");
        }
        List<Pair<String, String>> pairList = new ArrayList<>();
        for (int i = 0; i < arguments.length; i += 2) {
            String name = arguments[i];
            String value = arguments[i + 1];
            pairList.add(new Pair<>(name, value));
        }
        return pairList;
    }

    private SecurityPropertySet createMockedSecurityPropertySet(final CommunicationDevice device, final int authenticationAccessLevelId, final int encryptionAccessLevelId,
                                                                List<Pair<String, String>> securityPropertiesNameValues) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
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
        when(device.getProtocolSecurityProperties(securityPropertySet)).thenReturn(securityProperties);
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

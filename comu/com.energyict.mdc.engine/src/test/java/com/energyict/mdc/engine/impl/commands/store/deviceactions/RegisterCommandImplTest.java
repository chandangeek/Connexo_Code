package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.offline.OfflineDeviceImpl;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link RegisterCommandImpl} component.
 *
 * @author gna
 * @since 14/06/12 - 14:20
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandImplTest extends AbstractComCommandExecuteTest {
    private static final String MR_ID = RegisterCommandImplTest.class.getSimpleName();
    private static final long DEVICE_ID = 1;
    private static final String DEVICE_SERIALNUMBER = "MyDeviceSerialNumber";
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private Device device;

    private Clock clock = Clock.systemDefaultZone();
    private IssueService issueService = new IssueServiceImpl();
    private DeviceService deviceService = mock(DeviceService.class);

    @Before
    public void initializeMocks() {
        when(this.comTaskExecution.getDevice()).thenReturn(this.device);
        when(this.device.getmRID()).thenReturn(MR_ID);
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        RegisterCommand registerCommand = new RegisterCommandImpl(null, mock(RegistersTask.class), null);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void registersTaskNullTest() {
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        RegisterCommand registerCommand = new RegisterCommandImpl(groupedDeviceCommand, null, null);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void deviceNullTest() {
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        RegisterCommand registerCommand = new RegisterCommandImpl(groupedDeviceCommand, mock(RegistersTask.class), null);
        // should have gotten an exception
    }

    @Test
    public void commandTypeTest() {
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        RegisterCommand registerCommand = new RegisterCommandImpl(groupedDeviceCommand, mock(RegistersTask.class), mock(ComTaskExecution.class));

        // asserts
        assertEquals(ComCommandTypes.REGISTERS_COMMAND, registerCommand.getCommandType());
    }

    @Test
    public void addAdditionalRegisterGroupsTest() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getmRID()).thenReturn(MR_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getDevice()).thenReturn(device);

        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        OfflineRegister offlineRegister_A = mock(OfflineRegister.class);
        OfflineRegister offlineRegister_B = mock(OfflineRegister.class);
        OfflineRegister offlineRegister_C = mock(OfflineRegister.class);
        DeviceIdentifier deviceIdentifierById = new DeviceIdentifierById(DEVICE_ID);
        when(offlineRegister_A.getDeviceIdentifier()).thenReturn(deviceIdentifierById);
        when(offlineRegister_B.getDeviceIdentifier()).thenReturn(deviceIdentifierById);
        when(offlineRegister_C.getDeviceIdentifier()).thenReturn(deviceIdentifierById);

        RegistersTask registersTask_A = mock(RegistersTask.class);
        RegisterGroup registerGroupA = mock(RegisterGroup.class);
        when(registerGroupA.getId()).thenReturn(1L);
        List<RegisterGroup> registerGroups_A = Arrays.asList(registerGroupA); // just a dummy arrayList, content isn't important (as long as it is not an empty list)
        when(registersTask_A.getRegisterGroups()).thenReturn(registerGroups_A);

        RegistersTask registersTask_B = mock(RegistersTask.class);
        RegisterGroup registerGroupB = mock(RegisterGroup.class);
        when(registerGroupB.getId()).thenReturn(2L);
        List<RegisterGroup> registerGroups_B = Arrays.asList(registerGroupB);
        when(registersTask_B.getRegisterGroups()).thenReturn(registerGroups_B);

        when(offlineDevice.getAllOfflineRegisters()).thenReturn(Arrays.asList(offlineRegister_A, offlineRegister_B, offlineRegister_C));
        when(((OfflineDeviceImpl) offlineDevice).getRegistersForRegisterGroupAndMRID(Arrays.asList(1L), MR_ID)).thenReturn(Arrays.asList(offlineRegister_A, offlineRegister_C));
        when(((OfflineDeviceImpl) offlineDevice).getRegistersForRegisterGroupAndMRID(Arrays.asList(2L), MR_ID)).thenReturn(Arrays.asList(offlineRegister_A, offlineRegister_B));

        GroupedDeviceCommand groupedDeviceCommand = spy(getGroupedDeviceCommand());
        when(groupedDeviceCommand.getOfflineDevice()).thenReturn(offlineDevice);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        doReturn(readRegistersCommand).when(groupedDeviceCommand).getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class));
        RegisterCommand registerCommand = spy(new RegisterCommandImpl(groupedDeviceCommand, registersTask_A, comTaskExecution));

        // asserts
        assertEquals(ComCommandTypes.REGISTERS_COMMAND, registerCommand.getCommandType());
        verify(readRegistersCommand).addRegisters(Arrays.asList(offlineRegister_A, offlineRegister_C));

        registerCommand.addAdditionalRegisterGroups(registersTask_B, offlineDevice, comTaskExecution);

        // asserts
        verify(readRegistersCommand).addRegisters(Arrays.asList(offlineRegister_A, offlineRegister_B));
    }

    @Test
    public void addAdditionalRegisterGroupsAllGroupsOverruleTest() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getmRID()).thenReturn(MR_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getDevice()).thenReturn(device);

        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        OfflineRegister offlineRegister_A = mockOfflineRegister(DEVICE_ID, DEVICE_SERIALNUMBER, 1L, ObisCode.fromString("1.0.1.8.0.255"));
        OfflineRegister offlineRegister_B = mockOfflineRegister(DEVICE_ID, DEVICE_SERIALNUMBER, 2L, ObisCode.fromString("1.0.2.8.0.255"));
        OfflineRegister offlineRegister_C = mockOfflineRegister(DEVICE_ID, DEVICE_SERIALNUMBER, 3L, ObisCode.fromString("1.0.3.8.0.255"));

        RegistersTask registersTask_A = mock(RegistersTask.class);
        RegisterGroup registerGroupA = mock(RegisterGroup.class);
        when(registerGroupA.getId()).thenReturn(1L);
        List<RegisterGroup> registerGroups_A = Arrays.asList(registerGroupA); // just a dummy arrayList, content isn't important
        when(registersTask_A.getRegisterGroups()).thenReturn(registerGroups_A);

        RegistersTask registersTask_B = mock(RegistersTask.class);
        List<RegisterGroup> registerGroups_B = new ArrayList<>(0); // No groups defined - thus 'all registers'
        when(registersTask_B.getRegisterGroups()).thenReturn(registerGroups_B);

        when(offlineDevice.getAllOfflineRegisters()).thenReturn(Arrays.asList(offlineRegister_A, offlineRegister_B, offlineRegister_C));
        when(((OfflineDeviceImpl) offlineDevice).getRegistersForRegisterGroupAndMRID(Arrays.asList(1L), MR_ID)).thenReturn(Arrays.asList(offlineRegister_A, offlineRegister_C));
        when(((OfflineDeviceImpl) offlineDevice).getRegistersForRegisterGroupAndMRID(new ArrayList<>(), MR_ID)).thenReturn(Arrays.asList(offlineRegister_B));

        GroupedDeviceCommand groupedDeviceCommand = spy(getGroupedDeviceCommand());
        when(groupedDeviceCommand.getOfflineDevice()).thenReturn(offlineDevice);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        doReturn(readRegistersCommand).when(groupedDeviceCommand).getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class));
        RegisterCommand registerCommand = spy(new RegisterCommandImpl(groupedDeviceCommand, registersTask_A, comTaskExecution));

        // asserts
        assertEquals(ComCommandTypes.REGISTERS_COMMAND, registerCommand.getCommandType());
        verify(readRegistersCommand).addRegisters(Arrays.asList(offlineRegister_A, offlineRegister_C));

        registerCommand.addAdditionalRegisterGroups(registersTask_B, offlineDevice, comTaskExecution);

        // asserts
        verify(readRegistersCommand).addRegisters(Arrays.asList(offlineRegister_A, offlineRegister_B, offlineRegister_C));
        // expecting all registers are added
    }

    private OfflineRegister mockOfflineRegister(long deviceId, String serialNumber, Long registerId, ObisCode obisCode) {
        OfflineRegister offlineRegister = mock(OfflineRegister.class);
        when(offlineRegister.getDeviceIdentifier()).thenReturn(new DeviceIdentifierById(deviceId));
        when(offlineRegister.getSerialNumber()).thenReturn(serialNumber);
        when(offlineRegister.getRegisterId()).thenReturn(registerId);
        when(offlineRegister.getObisCode()).thenReturn(obisCode);
        when(offlineRegister.getDeviceMRID()).thenReturn(MR_ID);
        return offlineRegister;
    }

    @Test
    public void addListOfCollectedDataItemsTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = spy(getGroupedDeviceCommand());
        when(groupedDeviceCommand.getOfflineDevice()).thenReturn(device);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        doReturn(readRegistersCommand).when(groupedDeviceCommand).getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class));
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        RegisterCommand registerCommand = spy(new RegisterCommandImpl(groupedDeviceCommand, mock(RegistersTask.class), comTaskExecution));

        CollectedData noCollectedRegisterCollectedData = mock(CollectedData.class);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class));
        List<CollectedData> collectedDataItems = new ArrayList<>(2);
        collectedDataItems.add(noCollectedRegisterCollectedData);
        collectedDataItems.add(collectedRegister);

        // Business methods
        registerCommand.addListOfCollectedDataItems(collectedDataItems);
        List<CollectedData> collectedDataList = registerCommand.getCollectedData();

        // asserts
        assertNotNull(collectedDataList);
        assertEquals(2, collectedDataList.size());
        Assert.assertEquals(noCollectedRegisterCollectedData, collectedDataList.get(0));
        assertTrue(collectedDataList.get(1) instanceof CollectedRegisterList);
        Assert.assertEquals(1, ((CollectedRegisterList) collectedDataList.get(1)).getCollectedRegisters().size());
        Assert.assertEquals(collectedRegister, ((CollectedRegisterList) collectedDataList.get(1)).getCollectedRegisters().get(0));
    }

}
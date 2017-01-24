package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the {@link ReadRegistersCommandImpl} component.
 *
 * @author gna
 * @since 18/06/12 - 13:56
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadRegistersCommandImplTest extends AbstractComCommandExecuteTest {

    private static final String MRID = "MRID";
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private Device device;
    @Mock
    private IdentificationService identificationService;

    @Before
    public void initializeMocks() {
        when(this.comTaskExecution.getDevice()).thenReturn(this.device);
        ComTask comTask = mock(ComTask.class);
        when(this.comTaskExecution.getComTask()).thenReturn(comTask);
        when(this.device.getmRID()).thenReturn(ReadRegistersCommandImplTest.class.getSimpleName());
    }

    @Test
    public void commandTypeTest() {
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        CompositeComCommand commandOwner = mock(CompositeComCommand.class);
        ReadRegistersCommand readRegistersCommand = new ReadRegistersCommandImpl(groupedDeviceCommand, commandOwner);

        // asserts
        assertThat(readRegistersCommand.getCommandType()).isEqualTo(ComCommandTypes.READ_REGISTERS_COMMAND);
    }

    @Test
    public void readRegistersTest() {
        ExecutionContext executionContext = newTestExecutionContext();
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        RegisterCommand registerCommand = groupedDeviceCommand.getRegisterCommand(mock(RegistersTask.class), groupedDeviceCommand, comTaskExecution);
        ReadRegistersCommand readRegistersCommand = groupedDeviceCommand.getReadRegistersCommand(registerCommand, comTaskExecution);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(MRID);
        ObisCode obisCode = ObisCode.fromString("1.1.1.1.1.1");
        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.getDeviceRegisterObisCode()).thenReturn(obisCode);
        when(collectedRegister.getRegisterIdentifier()).thenReturn(registerIdentifier);
        when(deviceProtocol.readRegisters(Matchers.<List<OfflineRegister>>any())).thenReturn(Arrays.asList(collectedRegister));

        OfflineRegister offlineRegister = mock(OfflineRegister.class);
        when(offlineRegister.getReadingTypeMRID()).thenReturn(MRID);
        when(offlineRegister.getObisCode()).thenReturn(obisCode);
        when(offlineRegister.getReadingTypeMRID()).thenReturn(readingType.getMRID());
        readRegistersCommand.addRegisters(Arrays.asList(offlineRegister));
        readRegistersCommand.execute(deviceProtocol, executionContext);
        String journalMessage = readRegistersCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // asserts
        assertThat(readRegistersCommand.getCommandOwner().getCollectedData()).hasSize(1);
        assertThat(readRegistersCommand.getIssues()).isEmpty();
        assertThat(readRegistersCommand.getProblems()).isEmpty();
        assertThat(readRegistersCommand.getWarnings()).isEmpty();
        assertEquals(ComCommandDescriptionTitle.ReadRegistersCommandImpl.getDescription() + " {registers: (1.1.1.1.1.1)}", journalMessage);
    }

    @Test
    public void uniqueRegistersTest() {

        final ObisCode regObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        final ObisCode regObisCode2 = ObisCode.fromString("1.0.1.8.2.255");
        final ObisCode regObisCode3 = ObisCode.fromString("1.0.1.8.3.255");
        final ObisCode regObisCode4 = ObisCode.fromString("1.0.1.8.4.255");

        OfflineDevice device = mock(OfflineDevice.class);
        Register reg1 = createMockedRegisters(regObisCode1);
        Register reg2 = createMockedRegisters(regObisCode2);
        Register reg3 = createMockedRegisters(regObisCode3);
        Register reg4 = createMockedRegisters(regObisCode4);

        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        RegisterCommand registerCommand = mock(RegisterCommand.class);
        ReadRegistersCommandImpl readRegistersCommand = (ReadRegistersCommandImpl) groupedDeviceCommand.getReadRegistersCommand(registerCommand, comTaskExecution);
        OfflineRegister register1 = new OfflineRegisterImpl(reg1, this.identificationService);
        OfflineRegister register2 = new OfflineRegisterImpl(reg2, identificationService);
        OfflineRegister register3 = new OfflineRegisterImpl(reg3, identificationService);
        OfflineRegister register4 = new OfflineRegisterImpl(reg4, identificationService);
        OfflineRegister register5 = new OfflineRegisterImpl(reg1, identificationService);
        OfflineRegister register6 = new OfflineRegisterImpl(reg3, identificationService);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        List<CollectedRegister> collectedRegisters = new ArrayList<>(4);
        collectedRegisters.add(createCollectedRegister(regObisCode1));
        collectedRegisters.add(createCollectedRegister(regObisCode2));
        collectedRegisters.add(createCollectedRegister(regObisCode3));
        collectedRegisters.add(createCollectedRegister(regObisCode4));
        when(deviceProtocol.readRegisters(anyList())).thenReturn(collectedRegisters);

        readRegistersCommand.execute(deviceProtocol, newTestExecutionContext());
        readRegistersCommand.addRegisters(Arrays.asList(register1, register2, register3, register4, register2, register4, register5, register6));
        String infoJournalMessage = readRegistersCommand.toJournalMessageDescription(LogLevel.INFO);
        String debugJournalMessage = readRegistersCommand.toJournalMessageDescription(LogLevel.DEBUG);

        assertThat(readRegistersCommand.getOfflineRegisters()).hasSize(4);
        assertEquals(ComCommandDescriptionTitle.ReadRegistersCommandImpl.getDescription() + " {nrOfRegistersToRead: 4}", infoJournalMessage);
        assertEquals(ComCommandDescriptionTitle.ReadRegistersCommandImpl.getDescription() + " {registers: (1.0.1.8.1.255 - 1.2 Wh), (1.0.1.8.2.255 - 1.2 Wh), (1.0.1.8.3.255 - 1.2 Wh), (1.0.1.8.4.255 - 1.2 Wh)}", debugJournalMessage);
    }

    private Register createMockedRegisters(final ObisCode obisCode) {
        final String serialNumber = "MeterSerialNumber";
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(((NumericalRegisterSpec) registerSpec).getOverflowValue()).thenReturn(Optional.empty());
        RegisterGroup registerGroup = mock(RegisterGroup.class);
        when(registerGroup.getId()).thenReturn(1L);
        Device mockedDevice = mock(Device.class);
        when(mockedDevice.getSerialNumber()).thenReturn(serialNumber);
        when(mockedDevice.getmRID()).thenReturn(serialNumber);
        Register register = mock(Register.class);
        when(register.getDeviceObisCode()).thenReturn(obisCode);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getDevice()).thenReturn(mockedDevice);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        return register;
    }

    @Test
    public void textRegisterTest() {
        String collectedText = "ThisIsMyCollectedText";
        OfflineDevice device = mock(OfflineDevice.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("MRID");
        ExecutionContext executionContext = this.newTestExecutionContext();
        RegistersTask registersTask = mock(RegistersTask.class);
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();

        RegisterCommand registerCommand = groupedDeviceCommand.getRegisterCommand(registersTask, groupedDeviceCommand, comTaskExecution);
        ReadRegistersCommand readRegistersCommand = groupedDeviceCommand.getReadRegistersCommand(registerCommand, comTaskExecution);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        when(collectedRegister.getText()).thenReturn(collectedText);
        when(deviceProtocol.readRegisters(Matchers.<List<OfflineRegister>>any())).thenReturn(Arrays.asList(collectedRegister));

        final ObisCode regObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        Register reg1 = createMockTextRegister(regObisCode1, readingType);
        OfflineRegister register1 = new OfflineRegisterImpl(reg1, identificationService);

        readRegistersCommand.addRegisters(Arrays.asList(register1));
        groupedDeviceCommand.execute(executionContext);

        // asserts
        List<CollectedData> collectedData = readRegistersCommand.getCommandOwner().getCollectedData();
        assertThat(collectedData).hasSize(1);
        assertThat(((DeviceRegisterList) collectedData.get(0)).getCollectedRegisters().get(0).getText()).isEqualTo(collectedText);
    }

    @Test
    public void textRegisterFromQuantityTest() {
        Quantity quantity = new Quantity("123654", Unit.get("kWh"));
        OfflineDevice device = mock(OfflineDevice.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("MRID");
        ExecutionContext executionContext = this.newTestExecutionContext();
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        RegistersTask registersTask = mock(RegistersTask.class);
        RegisterCommand registerCommand = groupedDeviceCommand.getRegisterCommand(registersTask, groupedDeviceCommand, comTaskExecution);
        ReadRegistersCommand readRegistersCommand = groupedDeviceCommand.getReadRegistersCommand(registerCommand, comTaskExecution);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        when(collectedRegister.getCollectedQuantity()).thenReturn(quantity);
        when(deviceProtocol.readRegisters(Matchers.<List<OfflineRegister>>any())).thenReturn(Arrays.asList(collectedRegister));

        final ObisCode regObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        Register reg1 = createMockTextRegister(regObisCode1, readingType);
        OfflineRegister register1 = new OfflineRegisterImpl(reg1, identificationService);

        readRegistersCommand.addRegisters(Arrays.asList(register1));
        groupedDeviceCommand.execute(executionContext);

        // asserts
        List<CollectedData> collectedData = readRegistersCommand.getCommandOwner().getCollectedData();
        assertThat(collectedData).hasSize(1);
        assertThat(((DeviceRegisterList) collectedData.get(0)).getCollectedRegisters().get(0).getText()).isEqualTo(quantity.toString());
    }

    private Register createMockTextRegister(final ObisCode obisCode, final ReadingType readingType) {
        final String serialNumber = "MeterSerialNumber";
        RegisterSpec registerSpec = mock(TextualRegisterSpec.class);
        when(registerSpec.isTextual()).thenReturn(true);
        when(registerSpec.getDeviceObisCode()).thenReturn(obisCode);
        RegisterGroup registerGroup = mock(RegisterGroup.class);
        when(registerGroup.getId()).thenReturn(1L);
        Device mockedDevice = mock(Device.class);
        when(mockedDevice.getSerialNumber()).thenReturn(serialNumber);
        Register register = mock(Register.class);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(register.getDevice()).thenReturn(mockedDevice);
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        return register;
    }

    private CollectedRegister createCollectedRegister(final ObisCode obisCode) {
        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.getObisCode()).thenReturn(obisCode);
        when(registerIdentifier.getDeviceRegisterObisCode()).thenReturn(obisCode);
        CollectedRegister deviceRegister = new DefaultDeviceRegister(registerIdentifier);
        deviceRegister.setCollectedData(new Quantity(1.2, Unit.get(BaseUnit.WATTHOUR)));
        return deviceRegister;
    }
}
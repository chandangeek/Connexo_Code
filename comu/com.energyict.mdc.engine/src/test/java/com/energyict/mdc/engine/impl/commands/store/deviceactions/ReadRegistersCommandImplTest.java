package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.commands.offline.OfflineRegisterImpl;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.DeviceRegisterList;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.metering.ReadingType;

import java.util.Arrays;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private IdentificationService identificationService;

    @Test
    public void commandTypeTest() {
        CommandRoot commandRoot = mock(CommandRoot.class);
        CompositeComCommand commandOwner = mock(CompositeComCommand.class);
        ReadRegistersCommand readRegistersCommand = new ReadRegistersCommandImpl(commandOwner, commandRoot);

        // asserts
        assertThat(readRegistersCommand.getCommandType()).isEqualTo(ComCommandTypes.READ_REGISTERS_COMMAND);
    }

    @Test
    public void readRegistersTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(device, executionContext, commandRootServiceProvider);
        ReadRegistersCommand readRegistersCommand = commandRoot.getReadRegistersCommand(commandRoot, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        when(deviceProtocol.readRegisters(Matchers.<List<OfflineRegister>>any())).thenReturn(Arrays.asList(collectedRegister));

        readRegistersCommand.execute(deviceProtocol, executionContext);

        // asserts
        assertThat(readRegistersCommand.getCommandOwner().getCollectedData()).hasSize(1);
        assertThat(readRegistersCommand.getIssues()).isEmpty();
        assertThat(readRegistersCommand.getProblems()).isEmpty();
        assertThat(readRegistersCommand.getWarnings()).isEmpty();
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

        CommandRoot commandRoot = new CommandRootImpl(device, AbstractComCommandExecuteTest.newTestExecutionContext(), commandRootServiceProvider);
        ReadRegistersCommandImpl readRegistersCommand = (ReadRegistersCommandImpl) commandRoot.getReadRegistersCommand(commandRoot, comTaskExecution);
        OfflineRegister register1 = new OfflineRegisterImpl(reg1, this.identificationService);
        OfflineRegister register2 = new OfflineRegisterImpl(reg2, identificationService);
        OfflineRegister register3 = new OfflineRegisterImpl(reg3, identificationService);
        OfflineRegister register4 = new OfflineRegisterImpl(reg4, identificationService);
        OfflineRegister register5 = new OfflineRegisterImpl(reg1, identificationService);
        OfflineRegister register6 = new OfflineRegisterImpl(reg3, identificationService);

        readRegistersCommand.addRegisters(Arrays.asList(register1, register2, register3, register4, register2, register4, register5, register6));

        assertThat(readRegistersCommand.getOfflineRegisters()).hasSize(4);
    }

    private Register createMockedRegisters(final ObisCode obisCode) {
        final String serialNumber = "MeterSerialNumber";
        RegisterSpec registerSpec = mock(RegisterSpec.class, withSettings().extraInterfaces(NumericalRegisterSpec.class));
        when(registerSpec.getDeviceObisCode()).thenReturn(obisCode);
        RegisterGroup registerGroup = mock(RegisterGroup.class);
        when(registerGroup.getId()).thenReturn(1L);
        Device mockedDevice = mock(Device.class);
        when(mockedDevice.getSerialNumber()).thenReturn(serialNumber);
        when(mockedDevice.getmRID()).thenReturn(serialNumber);
        Register register = mock(Register.class);
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
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(device, executionContext, commandRootServiceProvider);
        RegistersTask registersTask = mock(RegistersTask.class);
        RegisterCommand registerCommand = commandRoot.getRegisterCommand(registersTask, commandRoot, comTaskExecution);
        ReadRegistersCommand readRegistersCommand = commandRoot.getReadRegistersCommand(registerCommand, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        when(collectedRegister.getReadingType()).thenReturn(readingType);
        when(collectedRegister.getText()).thenReturn(collectedText);
        when(deviceProtocol.readRegisters(Matchers.<List<OfflineRegister>>any())).thenReturn(Arrays.asList(collectedRegister));

        final ObisCode regObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        Register reg1 = createMockTextRegister(regObisCode1, readingType);
        OfflineRegister register1 = new OfflineRegisterImpl(reg1, identificationService);

        readRegistersCommand.addRegisters(Arrays.asList(register1));
        commandRoot.execute(deviceProtocol, executionContext);

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
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(device, executionContext, commandRootServiceProvider);
        RegistersTask registersTask = mock(RegistersTask.class);
        RegisterCommand registerCommand = commandRoot.getRegisterCommand(registersTask, commandRoot, comTaskExecution);
        ReadRegistersCommand readRegistersCommand = commandRoot.getReadRegistersCommand(registerCommand, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedRegister collectedRegister = mock(CollectedRegister.class);
        when(collectedRegister.getResultType()).thenReturn(ResultType.Supported);
        when(collectedRegister.getReadingType()).thenReturn(readingType);
        when(collectedRegister.getCollectedQuantity()).thenReturn(quantity);
        when(deviceProtocol.readRegisters(Matchers.<List<OfflineRegister>>any())).thenReturn(Arrays.asList(collectedRegister));

        final ObisCode regObisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        Register reg1 = createMockTextRegister(regObisCode1, readingType);
        OfflineRegister register1 = new OfflineRegisterImpl(reg1, identificationService);

        readRegistersCommand.addRegisters(Arrays.asList(register1));
        commandRoot.execute(deviceProtocol, executionContext);

        // asserts
        List<CollectedData> collectedData = readRegistersCommand.getCommandOwner().getCollectedData();
        assertThat(collectedData).hasSize(1);
        assertThat(((DeviceRegisterList) collectedData.get(0)).getCollectedRegisters().get(0).getText()).isEqualTo(quantity.toString());
    }

    private Register createMockTextRegister(final ObisCode obisCode, final ReadingType readingType){
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
}
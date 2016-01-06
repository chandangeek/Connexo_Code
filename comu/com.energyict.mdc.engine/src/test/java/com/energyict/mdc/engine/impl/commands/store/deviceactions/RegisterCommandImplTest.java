package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.RegistersTask;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link RegisterCommandImpl} component.
 *
 * @author gna
 * @since 14/06/12 - 14:20
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterCommandImplTest {

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private Device device;

    private Clock clock = Clock.systemDefaultZone();
    private IssueService issueService = new IssueServiceImpl();

    @Before
    public void initializeMocks() {
        when(this.comTaskExecution.getDevice()).thenReturn(this.device);
        when(this.device.getmRID()).thenReturn(RegisterCommandImplTest.class.getSimpleName());
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new RegisterCommandImpl(mock(RegistersTask.class), device, null, null);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void registersTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new RegisterCommandImpl(null, device, mock(CommandRoot.class), null);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void deviceNullTest() {
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.findOrCreateReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        new RegisterCommandImpl(mock(RegistersTask.class), null, commandRoot, null);
        // should have gotten an exception
    }

    @Test
    public void commandTypeTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.findOrCreateReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRootServiceProvider.issueService()).thenReturn(this.issueService);
        when(commandRootServiceProvider.clock()).thenReturn(this.clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, commandRoot, this.comTaskExecution);

        // asserts
        Assert.assertEquals(ComCommandTypes.REGISTERS_COMMAND, registerCommand.getCommandType());
        assertNotNull(registerCommand.getRegistersTask());
    }

    @Test
    public void addListOfCollectedDataItemsTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        when(commandRootServiceProvider.clock()).thenReturn(clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        when(commandRoot.findOrCreateReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, commandRoot, this.comTaskExecution);

        CollectedData noCollectedRegisterCollectedData = mock(CollectedData.class);
        DefaultDeviceRegister collectedRegister = new DefaultDeviceRegister(mock(RegisterIdentifier.class), mock(ReadingType.class));
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
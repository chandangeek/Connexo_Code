package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadRegistersCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegisterCommand;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.util.time.impl.DefaultClock;

import org.junit.*;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

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
public class RegisterCommandImplTest {

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void setupServiceProvider () {
        this.serviceProvider.setClock(new DefaultClock());
        this.serviceProvider.setIssueService(new IssueServiceImpl());
        this.serviceProvider.setDeviceDataService(mock(DeviceDataService.class));
        ServiceProvider.instance.set(this.serviceProvider);
    }

    @After
    public void resetServiceProvider () {
        ServiceProvider.instance.set(null);
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, null, null);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void registersTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        RegisterCommand registerCommand = new RegisterCommandImpl(null, device, mock(CommandRoot.class), null);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void deviceNullTest() {
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), null, commandRoot, null);
        // should have gotten an exception
    }

    @Test
    public void commandTypeTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRootServiceProvider.issueService()).thenReturn(serviceProvider.issueService());
        when(commandRootServiceProvider.clock()).thenReturn(serviceProvider.clock());
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, commandRoot, null);

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
        when(commandRootServiceProvider.issueService()).thenReturn(serviceProvider.issueService());
        when(commandRootServiceProvider.clock()).thenReturn(serviceProvider.clock());
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        when(commandRoot.getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, commandRoot, null);

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
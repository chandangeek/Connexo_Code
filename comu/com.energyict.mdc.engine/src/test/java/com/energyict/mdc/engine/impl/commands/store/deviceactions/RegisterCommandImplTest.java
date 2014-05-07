package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.CompositeComCommand;
import com.energyict.mdc.commands.ReadRegistersCommand;
import com.energyict.mdc.commands.RegisterCommand;
import com.energyict.mdc.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.tasks.RegistersTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.test.MockEnvironmentTranslations;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.RegisterCommandImpl} component
 *
 * @author gna
 * @since 14/06/12 - 14:20
 */
public class RegisterCommandImplTest {

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, null, null, deviceDataService);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void registersTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        RegisterCommand registerCommand = new RegisterCommandImpl(null, device, mock(CommandRoot.class), null, deviceDataService);
        // should have gotten an exception
    }

    @Test(expected = CodingException.class)
    public void deviceNullTest() {
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), null, commandRoot, null, deviceDataService);
        // should have gotten an exception
    }

    @Test
    public void commandTypeTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, commandRoot, null, deviceDataService);

        // asserts
        Assert.assertEquals(ComCommandTypes.REGISTERS_COMMAND, registerCommand.getCommandType());
        assertNotNull(registerCommand.getRegistersTask());
    }

    @Test
    public void addListOfCollectedDataItemsTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        CommandRoot commandRoot = mock(CommandRoot.class);
        ReadRegistersCommand readRegistersCommand = mock(ReadRegistersCommand.class);
        when(commandRoot.getReadRegistersCommand(Matchers.<CompositeComCommand>any(), any(ComTaskExecution.class))).thenReturn(readRegistersCommand);
        RegisterCommand registerCommand = new RegisterCommandImpl(mock(RegistersTask.class), device, commandRoot, null, deviceDataService);

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
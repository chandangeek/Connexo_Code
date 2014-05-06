package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.core.CommandFactory;
import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import org.junit.*;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link AddPropertiesCommand} component
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 14:43
 */
public class AddPropertiesCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
        TypedProperties typedProperties = mock(TypedProperties.class);
        AddPropertiesCommand addPropertiesCommand = new AddPropertiesCommand(commandRoot, typedProperties, typedProperties, null);

        assertEquals(ComCommandTypes.ADD_PROPERTIES_COMMAND, addPropertiesCommand.getCommandType());
    }

    @Test
    public void verifyAddPropertiesTest(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        TypedProperties typedProperties = mock(TypedProperties.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, typedProperties, null);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the addProperties is called on the deviceProtocol
        verify(deviceProtocol).copyProperties(typedProperties);
    }

    @Test
    public void verifyAddDeviceProtocolDialectPropertiesTest(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        TypedProperties typedProperties = mock(TypedProperties.class);
        TypedProperties otherTypedProperties = mock(TypedProperties.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, otherTypedProperties, null);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the addDeviceProtocolDialectProperties is called on the deviceProtocol
        verify(deviceProtocol).addDeviceProtocolDialectProperties(otherTypedProperties);
    }

    @Test
    public void verifyOrderOfPropertySetCalls(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        TypedProperties typedProperties = mock(TypedProperties.class);
        TypedProperties otherTypedProperties = mock(TypedProperties.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, otherTypedProperties, null);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the addProperties is called before the addDeviceProtocolDialectProperties
        InOrder order = Mockito.inOrder(deviceProtocol);
        order.verify(deviceProtocol).copyProperties(typedProperties);
        order.verify(deviceProtocol).addDeviceProtocolDialectProperties(otherTypedProperties);
        order.verify(deviceProtocol).setSecurityPropertySet(Matchers.<DeviceProtocolSecurityPropertySet>any());
    }

    @Test
    public void verifyAddSecurityPropertySetTest(){
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        TypedProperties typedProperties = mock(TypedProperties.class);
        TypedProperties otherTypedProperties = mock(TypedProperties.class);
        DeviceProtocolSecurityPropertySet securityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, otherTypedProperties, securityPropertySet);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the setSecurityPropertySet is called on the deviceProtocol
        verify(deviceProtocol).setSecurityPropertySet(securityPropertySet);
    }}
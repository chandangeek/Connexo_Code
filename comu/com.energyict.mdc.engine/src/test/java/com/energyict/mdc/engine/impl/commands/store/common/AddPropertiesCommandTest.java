package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link AddPropertiesCommand} component
 * <p>
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 14:43
 */
@RunWith(MockitoJUnitRunner.class)
public class AddPropertiesCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, this.newTestExecutionContext(), this.commandRootServiceProvider);
        TypedProperties typedProperties = mock(TypedProperties.class);
        AddPropertiesCommand addPropertiesCommand = new AddPropertiesCommand(commandRoot, typedProperties, typedProperties, null);

        assertThat(addPropertiesCommand.getCommandType()).isEqualTo(ComCommandTypes.ADD_PROPERTIES_COMMAND);
    }

    @Test
    public void verifyAddPropertiesTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
        TypedProperties typedProperties = mock(TypedProperties.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, typedProperties, null);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the addProperties is called on the deviceProtocol
        verify(deviceProtocol).copyProperties(typedProperties);
    }

    @Test
    public void verifyAddDeviceProtocolDialectPropertiesTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
        TypedProperties typedProperties = mock(TypedProperties.class);
        TypedProperties otherTypedProperties = mock(TypedProperties.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, otherTypedProperties, null);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the addDeviceProtocolDialectProperties is called on the deviceProtocol
        verify(deviceProtocol).addDeviceProtocolDialectProperties(otherTypedProperties);
    }

    @Test
    public void verifyOrderOfPropertySetCalls() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
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
    public void verifyAddSecurityPropertySetTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
        TypedProperties typedProperties = mock(TypedProperties.class);
        TypedProperties otherTypedProperties = mock(TypedProperties.class);
        DeviceProtocolSecurityPropertySet securityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        CommandFactory.createAddProperties(commandRoot, null, typedProperties, otherTypedProperties, securityPropertySet);

        // adapter call
        commandRoot.execute(deviceProtocol, executionContext);

        // verify that the setSecurityPropertySet is called on the deviceProtocol
        verify(deviceProtocol).setSecurityPropertySet(securityPropertySet);
    }
}
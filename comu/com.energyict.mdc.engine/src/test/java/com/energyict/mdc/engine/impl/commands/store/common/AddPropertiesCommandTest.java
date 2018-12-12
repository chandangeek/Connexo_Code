/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.device.data.TypedPropertiesValueAdapter;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AddPropertiesCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        TypedProperties typedProperties = TypedProperties.empty();
        AddPropertiesCommand addPropertiesCommand = new AddPropertiesCommand(groupedDeviceCommand, typedProperties, typedProperties, null);

        assertEquals(ComCommandTypes.ADD_PROPERTIES_COMMAND, addPropertiesCommand.getCommandType());
    }

    @Test
    public void verifyAddPropertiesTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        TypedProperties typedProperties = TypedProperties.empty();
        TypedProperties uplAdaptedProperties = (TypedProperties) TypedPropertiesValueAdapter.adaptToUPLValues(offlineDevice, typedProperties);
        CommandFactory.createAddProperties(groupedDeviceCommand, comTaskExecution, typedProperties, typedProperties, null);

        // adapter call
        groupedDeviceCommand.execute(executionContext);

        // verify that the addProperties is called on the deviceProtocol
        verify(deviceProtocol).copyProperties(uplAdaptedProperties);
    }

    @Test
    public void verifyAddDeviceProtocolDialectPropertiesTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        TypedProperties typedProperties = TypedProperties.empty();
        TypedProperties uplAdaptedProperties = (TypedProperties) TypedPropertiesValueAdapter.adaptToUPLValues(offlineDevice, typedProperties);
        TypedProperties otherTypedProperties = TypedProperties.empty();
        TypedProperties uplAdaptedOtherProperties = (TypedProperties) TypedPropertiesValueAdapter.adaptToUPLValues(offlineDevice, otherTypedProperties);
        CommandFactory.createAddProperties(groupedDeviceCommand, comTaskExecution, typedProperties, otherTypedProperties, null);

        // adapter call
        groupedDeviceCommand.execute(executionContext);

        // verify that the addDeviceProtocolDialectProperties is called on the deviceProtocol
        verify(deviceProtocol).addDeviceProtocolDialectProperties(uplAdaptedOtherProperties);
    }

    @Test
    public void verifyOrderOfPropertySetCalls() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        TypedProperties typedProperties = TypedProperties.empty();
        TypedProperties uplAdaptedProperties = (TypedProperties) TypedPropertiesValueAdapter.adaptToUPLValues(offlineDevice, typedProperties);
        TypedProperties otherTypedProperties = TypedProperties.empty();
        TypedProperties uplAdaptedOtherTypedProperties = (TypedProperties) TypedPropertiesValueAdapter.adaptToUPLValues(offlineDevice, otherTypedProperties);
        CommandFactory.createAddProperties(groupedDeviceCommand, comTaskExecution, typedProperties, otherTypedProperties, null);

        // adapter call
        groupedDeviceCommand.execute(executionContext);

        // verify that the addProperties is called before the addDeviceProtocolDialectProperties
        InOrder order = Mockito.inOrder(deviceProtocol);
        order.verify(deviceProtocol).copyProperties(uplAdaptedProperties);
        order.verify(deviceProtocol).addDeviceProtocolDialectProperties(uplAdaptedOtherTypedProperties);
        order.verify(deviceProtocol).setSecurityPropertySet(Matchers.<DeviceProtocolSecurityPropertySet>any());
    }

    @Test
    public void verifyAddSecurityPropertySetTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        TypedProperties typedProperties = TypedProperties.empty();
        TypedProperties otherTypedProperties = TypedProperties.empty();
        DeviceProtocolSecurityPropertySet securityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        CommandFactory.createAddProperties(groupedDeviceCommand, comTaskExecution, typedProperties, otherTypedProperties, securityPropertySet);

        // adapter call
        groupedDeviceCommand.execute(executionContext);

        // verify that the setSecurityPropertySet is called on the deviceProtocol
        verify(deviceProtocol).setSecurityPropertySet(securityPropertySet);
    }
}
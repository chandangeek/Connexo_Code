/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

public class AddPropertiesCommand extends SimpleComCommand {

    private final TypedProperties deviceProperties;
    private final TypedProperties protocolDialectProperties;
    private final DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet;

    public AddPropertiesCommand(GroupedDeviceCommand groupedDeviceCommand, TypedProperties deviceProperties, TypedProperties protocolDialectProperties, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        super(groupedDeviceCommand);
        this.deviceProperties = deviceProperties;
        this.protocolDialectProperties = protocolDialectProperties;
        this.deviceProtocolSecurityPropertySet = deviceProtocolSecurityPropertySet;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        /*
       Do not change the order in which these three actions are called:

       1/ deviceProtocol.copyProperties(...)
       2/ deviceProtocol.addDeviceProtocolDialectProperties(...)
       3/ deviceProtocol.setSecurityPropertySet(...)

       The adapters depend on the order of method calling to forward the property set to
       the old protocols. The properties are hierarchical which means that the last set
         of properties may overwrite the previously set value.
       Thank you for your cooperation.
        */


        deviceProtocol.copyProperties(deviceProperties);
        deviceProtocol.addDeviceProtocolDialectProperties(protocolDialectProperties);
        deviceProtocol.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.ADD_PROPERTIES_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Load the device protocol properties";
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

}
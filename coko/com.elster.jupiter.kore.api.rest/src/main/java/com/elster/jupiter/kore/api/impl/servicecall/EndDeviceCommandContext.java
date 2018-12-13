/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;


import com.elster.jupiter.metering.EndDevice;

public class EndDeviceCommandContext {
    private EndDevice endDevice;
    private EndDeviceCommandInfo endDeviceCommandInfo;
    private CommandHelper commandHelper;

    public EndDeviceCommandContext(EndDevice endDevice, EndDeviceCommandInfo endDeviceCommandInfo, CommandHelper commandHelper) {
        this.endDevice = endDevice;
        this.endDeviceCommandInfo = endDeviceCommandInfo;
        this.commandHelper = commandHelper;
    }

    public EndDevice getEndDevice() {
        return endDevice;
    }

    public EndDeviceCommandInfo getEndDeviceCommandInfo() {
        return endDeviceCommandInfo;
    }

    public CommandHelper getCommandHelper() {
        return commandHelper;
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;

public interface CommandFactory {

    EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType) throws UnsupportedCommandException;

    EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate) throws UnsupportedCommandException;

    EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity quantity) throws UnsupportedCommandException;

    EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice) throws UnsupportedCommandException;

}
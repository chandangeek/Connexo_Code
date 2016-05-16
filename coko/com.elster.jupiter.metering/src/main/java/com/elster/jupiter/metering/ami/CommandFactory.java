package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;

public interface CommandFactory{

    EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, Instant activationDate, Quantity limit);
    EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate);
    EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate);
    EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate);
    EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity limit);
    EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice);

}
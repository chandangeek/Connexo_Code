package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;

public interface CommandFactory{

    EndDeviceCommand createCommand(EndDeviceControlType type);
    EndDeviceCommand createArmCommand(boolean armForOpen);
    EndDeviceCommand createConnectCommand();
    EndDeviceCommand createDisconnectCommand();
    EndDeviceCommand createEnableLoadLimitCommand(Quantity quantity);
    EndDeviceCommand createDisableLoadLimitCommand();

}
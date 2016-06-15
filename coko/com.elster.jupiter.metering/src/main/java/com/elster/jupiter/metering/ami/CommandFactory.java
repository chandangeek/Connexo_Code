package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;

public interface CommandFactory {

    EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType);

    EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen);

    EndDeviceCommand createConnectCommand(EndDevice endDevice);

    EndDeviceCommand createDisconnectCommand(EndDevice endDevice);

    EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice);

    EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice);

}
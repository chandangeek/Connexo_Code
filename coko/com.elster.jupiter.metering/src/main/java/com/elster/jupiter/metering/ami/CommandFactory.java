package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.units.Quantity;

public interface CommandFactory{

    EndDeviceCommand createCommand(EndDeviceControlType type, EndDevice endDevice);
    EndDeviceCommand createConnectCommand(EndDevice endDevice, boolean amrForOpen);
    EndDeviceCommand createDisconnectCommand(EndDevice endDevice, boolean amrForClosure);
    EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity quantity);
    EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice);

}
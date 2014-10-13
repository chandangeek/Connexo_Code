package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.security.Privileges;

public class DeviceMessageUserActionAdapter extends MapBasedXmlAdapter<DeviceMessageUserAction> {

    public DeviceMessageUserActionAdapter() {
        register("", null);
        register(Privileges.EXECUTE_DEVICE_MESSAGE_1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        register(Privileges.EXECUTE_DEVICE_MESSAGE_2, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
        register(Privileges.EXECUTE_DEVICE_MESSAGE_3, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
        register(Privileges.EXECUTE_DEVICE_MESSAGE_4, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4);
    }
}
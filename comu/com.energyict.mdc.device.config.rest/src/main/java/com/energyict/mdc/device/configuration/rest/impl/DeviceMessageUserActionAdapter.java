package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.DeviceMessageUserAction;

public class DeviceMessageUserActionAdapter extends MapBasedXmlAdapter<DeviceMessageUserAction> {

    public DeviceMessageUserActionAdapter() {
        register("", null);
        register("execute.device.message.level1", DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        register("execute.device.message.level2", DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
        register("execute.device.message.level3", DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
        register("execute.device.message.level4", DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4);
    }
}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.DeviceMessageUserAction;

public class DeviceMessageUserActionAdapter extends MapBasedXmlAdapter<DeviceMessageUserAction> {

    public DeviceMessageUserActionAdapter() {
        register("", null);
        this.register(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        this.register(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
        this.register(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
        this.register(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4);
    }

    private void register(DeviceMessageUserAction action) {
        this.register(action.getPrivilege(), action);
    }

}
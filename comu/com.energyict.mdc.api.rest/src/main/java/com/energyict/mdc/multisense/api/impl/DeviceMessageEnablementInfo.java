package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceMessageUserAction;

import java.util.Set;

/**
 * Created by bvn on 11/2/15.
 */
public class DeviceMessageEnablementInfo extends LinkInfo {
    public Long messageId;
    public Set<DeviceMessageUserAction> userActions;
    public LinkInfo deviceConfiguration;
}

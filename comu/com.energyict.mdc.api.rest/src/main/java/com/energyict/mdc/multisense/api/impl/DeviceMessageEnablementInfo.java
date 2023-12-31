/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;

import java.util.Set;

/**
 * Created by bvn on 11/2/15.
 */
public class DeviceMessageEnablementInfo extends LinkInfo<Long> {
    public Long messageId;
    public Set<DeviceMessageUserAction> userActions;
    public DeviceConfigurationInfo deviceConfiguration;
}

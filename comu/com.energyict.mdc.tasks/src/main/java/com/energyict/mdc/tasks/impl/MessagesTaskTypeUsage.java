/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;

public interface MessagesTaskTypeUsage extends HasId {

    public long getId();

    public DeviceMessageCategory getDeviceMessageCategory();

}
/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.time.Instant;
import java.util.List;

public interface MultiSenseEndDeviceCommand {

    List<DeviceMessage> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate);
}

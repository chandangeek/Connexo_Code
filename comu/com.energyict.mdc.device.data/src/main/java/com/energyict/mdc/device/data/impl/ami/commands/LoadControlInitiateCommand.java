/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.commands;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.EndDeviceCommandImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 1/07/2016 - 15:41
 */
public class LoadControlInitiateCommand extends EndDeviceCommandImpl {

    public LoadControlInitiateCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        super(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    @Override
    public List<DeviceMessage<Device>> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {
        List<DeviceMessageId> deviceMessageIds = Collections.singletonList(
                hasCommandArgumentValueFor(DeviceMessageConstants.overThresholdDurationAttributeName)
                        ? DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION
                        : DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD
        );
        return doCreateCorrespondingMultiSenseDeviceMessages(serviceCall, Instant.now(), deviceMessageIds);
    }
}
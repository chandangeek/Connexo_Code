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
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 1/07/2016 - 15:48
 */
public class CloseRemoteSwitchCommand extends EndDeviceCommandImpl {

    public CloseRemoteSwitchCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        super(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    @Override
    public List<DeviceMessage<Device>> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        boolean useReleaseDate = false;

        if (hasCommandArgumentValueFor(DeviceMessageConstants.contactorActivationDateAttributeName) && deviceHasSupportFor(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
            deviceMessageIds.add(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        } else {
            deviceMessageIds.add(DeviceMessageId.CONTACTOR_CLOSE);
            useReleaseDate = true;
        }
        return doCreateCorrespondingMultiSenseDeviceMessages(serviceCall, useReleaseDate ? releaseDate : Instant.now(), deviceMessageIds);
    }
}

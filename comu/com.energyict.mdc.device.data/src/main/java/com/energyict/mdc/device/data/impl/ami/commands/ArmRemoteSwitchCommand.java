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
import java.util.Arrays;
import java.util.List;

/**
 * @author sva
 * @since 1/07/2016 - 15:50
 */
public class ArmRemoteSwitchCommand extends EndDeviceCommandImpl {

    public ArmRemoteSwitchCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType, List<DeviceMessageId> possibleDeviceMessageIds, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        super(endDevice, endDeviceControlType, possibleDeviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    @Override
    public List<DeviceMessage<Device>> createCorrespondingMultiSenseDeviceMessages(ServiceCall serviceCall, Instant releaseDate) {
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        boolean useReleaseDate = false;

        if (hasCommandArgumentValueFor(DeviceMessageConstants.contactorActivationDateAttributeName) && deviceHasSupportFor(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
            deviceMessageIds.addAll(Arrays.asList(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE));
        } else {
            deviceMessageIds.addAll(Arrays.asList(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_ARM));
            useReleaseDate = true;
        }
        return doCreateCorrespondingMultiSenseDeviceMessages(serviceCall, useReleaseDate ? releaseDate : Instant.now(), deviceMessageIds);
    }
}

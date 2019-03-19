/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.impl.TimeOfUseCampaignBuilderImpl;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface TimeOfUseCampaignBuilder {

    TimeOfUseCampaignBuilder addName(String name);
    TimeOfUseCampaignBuilder addType(DeviceType deviceType);
    TimeOfUseCampaignBuilder addCalendar(Calendar calendar);
    TimeOfUseCampaignBuilder addActivationTimeBoundaries(Instant activationStart, Instant activationEnd);
    TimeOfUseCampaignBuilder addActivationDate(Instant activationDate);
    TimeOfUseCampaignBuilder addActivationOption(String activationOption);
    TimeOfUseCampaignBuilder addDeviceGroup(String deviceGroup);
    TimeOfUseCampaignBuilder addUpdateType(String updateType);
    TimeOfUseCampaignBuilder addValidationTimeout(long validationTimeout);

    TimeOfUseCampaignBuilder addWithUniqueCalendarName(boolean withCalendarNameValidation);

    TimeOfUseCampaign create();
}

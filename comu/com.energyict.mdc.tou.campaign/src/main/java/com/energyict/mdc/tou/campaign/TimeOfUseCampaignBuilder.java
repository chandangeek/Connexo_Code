/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface TimeOfUseCampaignBuilder {

    TimeOfUseCampaignBuilder withUploadTimeBoundaries(Instant activationStart, Instant activationEnd);

    TimeOfUseCampaignBuilder withActivationDate(Instant activationDate);

    TimeOfUseCampaignBuilder withActivationOption(String activationOption);

    TimeOfUseCampaignBuilder withDeviceGroup(String deviceGroup);

    TimeOfUseCampaignBuilder withUpdateType(String updateType);

    TimeOfUseCampaignBuilder withValidationTimeout(long validationTimeout);

    TimeOfUseCampaignBuilder withUniqueCalendarName(boolean withCalendarNameValidation);

    TimeOfUseCampaign create();
}

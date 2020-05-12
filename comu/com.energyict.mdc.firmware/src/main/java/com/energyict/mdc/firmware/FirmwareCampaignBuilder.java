/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface FirmwareCampaignBuilder {

    FirmwareCampaignBuilder withUploadTimeBoundaries(Instant activationStart, Instant activationEnd);

    FirmwareCampaignBuilder withDeviceGroup(String deviceGroup);

    FirmwareCampaignBuilder withDeviceType(DeviceType deviceType);

    FirmwareCampaignBuilder withValidationTimeout(TimeDuration validationTimeout);

    FirmwareCampaignBuilder withFirmwareType(FirmwareType firmwareType);

    FirmwareCampaignBuilder withManagementOption(ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions);

    FirmwareCampaignBuilder withFirmwareUploadComTaskId(Long id);

    FirmwareCampaignBuilder withValidationComTaskId(Long id);

    FirmwareCampaignBuilder withFirmwareUploadConnectionStrategy(String name);

    FirmwareCampaignBuilder withValidationConnectionStrategy(String name);

    FirmwareCampaign create();

    FirmwareCampaignBuilder withUniqueFirmwareVersion(boolean withUniqueFirmwareVersion);

    FirmwareCampaignBuilder addProperty(PropertySpec propertySpec, Object propertyValue);
}

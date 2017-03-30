/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

public class FirmwareMessageInfoFactory {
    private final MdcPropertyUtils mdcPropertyUtils;
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareMessageInfoFactory(MdcPropertyUtils mdcPropertyUtils, FirmwareService firmwareService) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.firmwareService = firmwareService;
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, Device device, String uploadOption, String firmwareType) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
            if (BaseFirmwareVersion.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
                return firmwareService.getAllUpgradableFirmwareVersionsFor(device, firmwareType != null ? FirmwareTypeFieldAdapter.INSTANCE.unmarshal(firmwareType) : null);
            }
            return null;
        };
        return from(deviceMessageSpec, uploadOption, provider);
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, DeviceType deviceType, String uploadOption, String firmwareType) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
            if (BaseFirmwareVersion.class.equals(propertySpec.getValueFactory().getValueType())){
                FirmwareVersionFilter filter = firmwareService.filterForFirmwareVersion(deviceType);
                if (firmwareType != null) {
                    filter.addFirmwareTypes(Collections.singletonList(FirmwareTypeFieldAdapter.INSTANCE.unmarshal(firmwareType)));
                }
                filter.addFirmwareStatuses(Arrays.asList(FirmwareStatus.FINAL, FirmwareStatus.TEST));
                return firmwareService.findAllFirmwareVersions(filter).find();
            }
            return null;
        };
        return from(deviceMessageSpec, uploadOption, provider);
    }

    private FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, String uploadOption, PropertyDefaultValuesProvider provider) {
        FirmwareMessageInfo info = new FirmwareMessageInfo();
        info.uploadOption = uploadOption;
        info.localizedValue = deviceMessageSpec.getName();
        info.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceMessageSpec.getPropertySpecs(), TypedProperties.empty(), provider);
        return info;
    }
}

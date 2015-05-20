package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;
import java.util.Arrays;

public class FirmwareMessageInfoFactory {
    private final MdcPropertyUtils mdcPropertyUtils;
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareMessageInfoFactory(MdcPropertyUtils mdcPropertyUtils, FirmwareService firmwareService) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.firmwareService = firmwareService;
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, Device device, String uploadOption, String firmwareType) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) ->
                firmwareService.getAllUpgradableFirmwareVersionsFor(device, firmwareType!= null ? FirmwareType.get(firmwareType): null);
        return from(deviceMessageSpec, uploadOption, provider);
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, DeviceType deviceType, String uploadOption, String firmwareType) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
            if (FirmwareVersion.class.equals(propertySpec.getValueFactory().getValueType())){
                FirmwareVersionFilter filter = new FirmwareVersionFilter(deviceType);
                if (firmwareType != null) {
                    filter.setFirmwareTypes(Arrays.asList(FirmwareType.get(firmwareType)));
                }
                filter.setFirmwareStatuses(Arrays.asList(FirmwareStatus.FINAL, FirmwareStatus.TEST));
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

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;

public class FirmwareMessageInfoFactory {
    private final Thesaurus thesaurus;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public FirmwareMessageInfoFactory(Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils) {
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, Device device, String uploadOption){
        FirmwareMessageInfo info = new FirmwareMessageInfo();
        info.uploadOption = uploadOption;
        info.localizedValue = deviceMessageSpec.getName();
        info.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceMessageSpec.getPropertySpecs(), TypedProperties.empty(), device);
        return info;
    }
}

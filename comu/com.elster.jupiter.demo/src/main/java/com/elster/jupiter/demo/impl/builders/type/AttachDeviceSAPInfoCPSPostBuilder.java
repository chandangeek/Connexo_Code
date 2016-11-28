package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import java.util.function.Consumer;

public class AttachDeviceSAPInfoCPSPostBuilder implements Consumer<DeviceType> {
    public static final String CPS_ID = "com.energyict.mdc.device.config.cps.DeviceSAPInfoCustomPropertySet";

    private final CustomPropertySetService customPropertySetService;

    @Inject
    public AttachDeviceSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(DeviceType deviceType) {
        if (deviceType.getCustomPropertySets()
                .stream()
                .filter(rcps -> rcps.getCustomPropertySet() != null)
                .noneMatch(rcps -> CPS_ID.equals(rcps.getCustomPropertySet().getId()))) {
            this.customPropertySetService.findActiveCustomPropertySet(CPS_ID).ifPresent(deviceType::addCustomPropertySet);
        }
    }
}

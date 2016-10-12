package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.demo.impl.builders.type.AttachChannelSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachEMeterInfoCPSPostBuilder;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Consumer;

public class SetCustomAttributeValuesToDevicePostBuilder implements Consumer<Device> {

    private CustomPropertySetService customPropertySetService;
    private Clock clock;

    @Inject
    SetCustomAttributeValuesToDevicePostBuilder(CustomPropertySetService customPropertySetService, Clock clock) {
        this.clock = clock;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(Device device) {
        setChannelSAPInfoCPS(device);
        setDeviceSAPInfoCPS(device);
        setEMeterInfoCPS(device);
    }

    private void setChannelSAPInfoCPS(Device device) {
        for (Channel channel : device.getChannels()) {
            Optional<CustomPropertySet> customPropertySet = device.getDeviceType()
                    .getLoadProfileTypeCustomPropertySet(channel.getLoadProfile()
                            .getLoadProfileSpec()
                            .getLoadProfileType())
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .filter(cps -> cps.getId().equals(AttachChannelSAPInfoCPSPostBuilder.CPS_ID));
            if (customPropertySet.isPresent()) {
                CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(clock.instant());
                values.setProperty("logicalRegisterNumber", BigDecimal.ZERO);
                values.setProperty("profileNumber", BigDecimal.ZERO);
                values.setProperty("inUse", false);
                values.setProperty("billingFactor", BigDecimal.ZERO);
                customPropertySetService.setValuesVersionFor(customPropertySet.get(), channel.getChannelSpec(), values, values
                        .getEffectiveRange(), device.getId());
            }
        }
    }

    private void setDeviceSAPInfoCPS(Device device) {
        Optional<CustomPropertySet> customPropertySet = device.getDeviceType().getCustomPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> cps.getId().equals(AttachDeviceSAPInfoCPSPostBuilder.CPS_ID))
                .findFirst();

        if (customPropertySet.isPresent()) {
            CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(clock.instant());
            values.setProperty("usageType", "usageType");
            values.setProperty("inUse", false);
            customPropertySetService.setValuesVersionFor(customPropertySet.get(), device, values, values.getEffectiveRange());
        }
    }


    private void setEMeterInfoCPS(Device device) {
        Optional<CustomPropertySet> customPropertySet = device.getDeviceType().getCustomPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> cps.getId().equals(AttachEMeterInfoCPSPostBuilder.CPS_ID))
                .findFirst();

        if (customPropertySet.isPresent()) {
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            values.setProperty("manufacturer", "manufacturer");
            values.setProperty("modelNumber", "modelNumber");
            values.setProperty("configScheme", 1L);
            values.setProperty("serviceCompany", "SERV1");
            values.setProperty("technician", "technician");
            values.setProperty("replaceBy", clock.instant());
            values.setProperty("maxCurrentRating", Quantity.create(BigDecimal.ZERO, 100, "A"));
            values.setProperty("maxVoltage", Quantity.create(BigDecimal.ZERO, 400, "V"));

            customPropertySetService.setValuesFor(customPropertySet.get(), device, values);
        }
    }

}

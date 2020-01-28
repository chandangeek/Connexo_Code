/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceChannelSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceRegisterSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachEMeterInfoCPSPostBuilder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class SetCustomAttributeValuesToDevicePostBuilder implements Consumer<Device> {

    private CustomPropertySetService customPropertySetService;
    private Clock clock;
    private Random random = new Random();
    private long[] ratings = {50, 75, 100};
    private long[] voltages = {250, 300, 350, 400};

    @Inject
    SetCustomAttributeValuesToDevicePostBuilder(CustomPropertySetService customPropertySetService, Clock clock) {
        this.clock = clock;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(Device device) {
        random = new Random(device.getId());
        setEMeterInfoCPS(device);
        setDeviceSAPInfoCPS(device);
        setDeviceRegisterSAPInfoCPS(device);
        setDeviceChannelSAPInfoCPS(device);
    }

    private void setEMeterInfoCPS(Device device) {
        Optional<CustomPropertySet> customPropertySet = device.getDeviceType().getCustomPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> cps.getId().equals(AttachEMeterInfoCPSPostBuilder.CPS_ID))
                .findFirst();
        if (customPropertySet.isPresent()) {
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            values.setProperty("configScheme", getPossibleValues(customPropertySet.get(), "configScheme")
                    .get(random.nextInt(getPossibleValues(customPropertySet.get(), "configScheme").size())));
            values.setProperty("serviceCompany", getPossibleValues(customPropertySet.get(), "serviceCompany")
                    .get(random.nextInt(getPossibleValues(customPropertySet.get(), "serviceCompany").size())));
            values.setProperty("maxCurrentRating", Quantity.create(BigDecimal.valueOf(ratings[random.nextInt(ratings.length)]), 0, "A"));
            values.setProperty("maxVoltage", Quantity.create(BigDecimal.valueOf(voltages[random.nextInt(voltages.length)]), 0, "V"));

            this.customPropertySetService.setValuesFor(customPropertySet.get(), device, values);
        }
    }

    private void setDeviceChannelSAPInfoCPS(Device device) {
        for (Channel channel : device.getChannels()) {
            Optional<CustomPropertySet> customPropertySet = device.getDeviceType()
                    .getLoadProfileTypeCustomPropertySet(channel.getLoadProfile().getLoadProfileSpec().getLoadProfileType())
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .filter(cps -> cps.getId().equals(AttachDeviceChannelSAPInfoCPSPostBuilder.CPS_ID));
            if (customPropertySet.isPresent()) {
                CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(this.clock.instant());
                values.setProperty("device", device.getId());
                values.setProperty("channelSpec", channel.getChannelSpec());
                this.customPropertySetService.setValuesVersionFor(customPropertySet.get(), channel.getChannelSpec(), values,
                        values.getEffectiveRange(), device.getId());
            }
        }
    }

    private void setDeviceRegisterSAPInfoCPS(Device device) {
        for (Register register : device.getRegisters()) {
            Optional<CustomPropertySet> customPropertySet = device.getDeviceType()
                    .getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType())
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .filter(cps -> cps.getId().equals(AttachDeviceRegisterSAPInfoCPSPostBuilder.CPS_ID));
            if (customPropertySet.isPresent()) {
                CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(this.clock.instant());
                values.setProperty("device", device.getId());
                values.setProperty("registerSpec", register.getRegisterSpec());
                this.customPropertySetService.setValuesVersionFor(customPropertySet.get(), register.getRegisterSpec(), values,
                        values.getEffectiveRange(), device.getId());
            }
        }
    }

    private void setDeviceSAPInfoCPS(Device device) {
        Optional<CustomPropertySet> customPropertySet = device.getDeviceType().getCustomPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> cps.getId().equals(AttachDeviceSAPInfoCPSPostBuilder.CPS_ID))
                .findFirst();
        if (customPropertySet.isPresent()) {
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            values.setProperty("deviceIdentifier", device.getmRID());
            device.getLocation().ifPresent(
                    location ->
                            values.setProperty("deviceLocation", location.toString())
            );
            values.setProperty("pointOfDelivery", null);
            this.customPropertySetService.setValuesFor(customPropertySet.get(), device, values);
        }
    }

    private List getPossibleValues(CustomPropertySet customPropertySet, String propertyName) {
        List<PropertySpec> proprtySpecs = customPropertySet.getPropertySpecs();
        return proprtySpecs.stream().filter(ps -> ps.getName().equals(propertyName))
                .findFirst().map(ps -> ps.getPossibleValues().getAllValues()).orElse(Collections.emptyList());
    }
}

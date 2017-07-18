/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.demo.impl.builders.type.AttachChannelSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachDeviceSAPInfoCPSPostBuilder;
import com.elster.jupiter.demo.impl.builders.type.AttachEMeterInfoCPSPostBuilder;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

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
        setChannelSAPInfoCPS(device);
        setDeviceSAPInfoCPS(device);
        setEMeterInfoCPS(device);
    }

    private void setChannelSAPInfoCPS(Device device) {
        for (Channel channel : device.getChannels()) {
            Optional<CustomPropertySet> customPropertySet = device.getDeviceType()
                    .getLoadProfileTypeCustomPropertySet(channel.getLoadProfile().getLoadProfileSpec().getLoadProfileType())
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .filter(cps -> cps.getId().equals(AttachChannelSAPInfoCPSPostBuilder.CPS_ID));
            if (customPropertySet.isPresent()) {
                CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(clock.instant());
                values.setProperty("logicalRegisterNumber", new BigDecimal(channel.getId()));
                values.setProperty("profileNumber", new BigDecimal(getProfileNumber(channel)));
                values.setProperty("inUse", random.nextBoolean());
                values.setProperty("billingFactor", BigDecimal.ONE);
                this.customPropertySetService.setValuesVersionFor(customPropertySet.get(), channel.getChannelSpec(), values,
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
            CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(this.clock.instant());
            values.setProperty("usageType", "-");
            values.setProperty("inUse", random.nextBoolean());
            this.customPropertySetService.setValuesVersionFor(customPropertySet.get(), device, values, values.getEffectiveRange());
        }
    }

    private void setEMeterInfoCPS(Device device) {
        Optional<CustomPropertySet> customPropertySet = device.getDeviceType().getCustomPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> cps.getId().equals(AttachEMeterInfoCPSPostBuilder.CPS_ID))
                .findFirst();
        if (customPropertySet.isPresent()) {
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            values.setProperty("manufacturer", device.getDeviceType().getName().split(" ")[0]);
            values.setProperty("modelNumber", device.getDeviceType().getName().split(" ")[1]);
            values.setProperty("configScheme", getPossibleValues(customPropertySet.get(), "configScheme")
                    .get(random.nextInt(getPossibleValues(customPropertySet.get(), "configScheme").size())));
            values.setProperty("serviceCompany", getPossibleValues(customPropertySet.get(), "serviceCompany")
                    .get(random.nextInt(getPossibleValues(customPropertySet.get(), "serviceCompany").size())));
//            values.setProperty("technician", "technician");
//            values.setProperty("replaceBy", clock.instant());
            values.setProperty("maxCurrentRating", Quantity.create(BigDecimal.valueOf(ratings[random.nextInt(ratings.length)]), 0, "A"));
            values.setProperty("maxVoltage", Quantity.create(BigDecimal.valueOf(voltages[random.nextInt(voltages.length)]), 0, "V"));

            this.customPropertySetService.setValuesFor(customPropertySet.get(), device, values);
        }
    }

    private long getProfileNumber(Channel channel) {
        if (channel.getInterval().equals(TimeDuration.days(1))) {
            return 2;
        } else if (channel.getInterval().equals(TimeDuration.months(1))) {
            return 3;
        } else {
            return 1;
        }
    }

    private List getPossibleValues(CustomPropertySet customPropertySet, String propertyName) {
        List<PropertySpec> proprtySpecs = customPropertySet.getPropertySpecs();
        return proprtySpecs.stream().filter(ps -> ps.getName().equals(propertyName))
                .findFirst().map(ps -> ps.getPossibleValues().getAllValues()).orElse(Collections.emptyList());
    }
}

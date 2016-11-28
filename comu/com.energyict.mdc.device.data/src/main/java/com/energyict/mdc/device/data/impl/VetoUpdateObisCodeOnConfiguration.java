package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Vetos the fact that you can not change the obiscode of a config channel/register when you already have a device were you
 * have overridden another channel/register with the obiscode
 */
public class VetoUpdateObisCodeOnConfiguration extends LocalizedException {

    public VetoUpdateObisCodeOnConfiguration(Thesaurus thesaurus, List<Device> deviceWithOverruledObisCodeForOtherReadingType) {
        super(thesaurus, MessageSeeds.VETO_CANNOT_CHANGE_OBISCODE_CONFIG_ALREADY_OVERRIDDEN_DEVICE, getProperDeviceNames(deviceWithOverruledObisCodeForOtherReadingType));
    }

    private static String getProperDeviceNames(List<Device> deviceWithOverruledObisCodeForOtherReadingType) {
        String s = deviceWithOverruledObisCodeForOtherReadingType.stream().map(Device::getName).collect(Collectors.joining(","));
        return String.format("[%s]", s);
    }
}

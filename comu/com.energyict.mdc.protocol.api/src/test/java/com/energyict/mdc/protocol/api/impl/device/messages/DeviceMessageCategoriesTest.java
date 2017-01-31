/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceMessageCategories} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:12)
 */
public class DeviceMessageCategoriesTest {

    @Test
    public void testAllCategoriesHaveAUniqueNameResourceKey () {
        // Business method
        Set<String> nameResourceKeys = Arrays.stream(DeviceMessageCategories.values()).
                map(DeviceMessageCategories::getNameResourceKey).
                collect(Collectors.toSet());

        // Asserts
        assertThat(nameResourceKeys).hasSize(DeviceMessageCategories.values().length);
    }

    @Test
    public void testAllCategoriesMessageSpecsHaveAUniqueNameResourceKey () {
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ActivityCalendarDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(AdvancedTestMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(AlarmConfigurationMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ChannelConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ClockDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ConfigurationChangeDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ContactorDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(DeviceActionMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(DisplayDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(DLMSConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(EIWebConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(FirmwareDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(GeneralDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(LoadBalanceDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(LoadProfileMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(LogBookDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(MailConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(MBusConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(MBusSetupDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ModbusConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ModemConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(NetworkConnectivityMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(OpusConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(PeakShaverConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(PLCConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(PowerConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(PPPConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(PrepaidConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(PricingInformationMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(SecurityMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(SMSConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(TotalizersConfigurationDeviceMessage.values());
        this.deviceMessageSpecEnumValuesHaveAUniqueResourceKey(ZigBeeConfigurationDeviceMessage.values());
    }

    private void deviceMessageSpecEnumValuesHaveAUniqueResourceKey(DeviceMessageSpecEnum... values) {
        // Business method
        Set<String> nameResourceKeys = Arrays.stream(values).
                map(DeviceMessageSpecEnum::getKey).
                collect(Collectors.toSet());

        // Asserts
        assertThat(nameResourceKeys).hasSize(values.length);
    }

}
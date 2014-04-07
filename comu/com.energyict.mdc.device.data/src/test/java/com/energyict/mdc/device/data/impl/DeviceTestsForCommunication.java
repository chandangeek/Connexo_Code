package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Copyrights EnergyICT
 * Date: 04/04/14
 * Time: 12:06
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTestsForCommunication extends PersistenceIntegrationTest {

    private DeviceConfiguration createDeviceConfigurationWithConnectionType() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithRegisterMappings");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration;

//        RegisterSpec.RegisterSpecBuilder registerSpecBuilder1 = configurationWithConnectionType.newRegisterSpec(registerMapping1);
//        registerSpecBuilder1.setNumberOfDigits(9);
//        RegisterSpec.RegisterSpecBuilder registerSpecBuilder2 = configurationWithConnectionType.newRegisterSpec(registerMapping2);
//        registerSpecBuilder2.setNumberOfDigits(9);
//        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        deviceType.save();
        return deviceConfiguration;
    }

}

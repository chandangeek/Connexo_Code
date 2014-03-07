package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceType;
import org.junit.Before;

/**
 * Copyrights EnergyICT
 * Date: 7/03/14
 * Time: 15:09
 */
public abstract class DeviceTypeProvidingPersistenceTest extends PersistenceTest {

    static final String DEVICE_TYPE_NAME = PersistenceTest.class.getName() + "Type";
    protected DeviceType deviceType;

    @Before
    private void initializeDeviceType() {
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
    }
}

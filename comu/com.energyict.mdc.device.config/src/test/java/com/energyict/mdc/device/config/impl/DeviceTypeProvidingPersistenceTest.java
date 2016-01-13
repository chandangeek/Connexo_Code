package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.RegisterType;
import org.junit.Before;

import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 7/03/14
 * Time: 15:09
 */
public abstract class DeviceTypeProvidingPersistenceTest extends PersistenceTest {

    static final String DEVICE_TYPE_NAME = PersistenceTest.class.getName() + "Type";
    protected DeviceType deviceType;

    @Before
    public void initializeDeviceType() {
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
    }

    RegisterType createOrSetRegisterType(ReadingType readingType, ObisCode channelTypeObisCode){
        Optional<RegisterType> xRegisterType =
                inMemoryPersistence.getMasterDataService()
                        .findRegisterTypeByReadingType(readingType);
        if (xRegisterType.isPresent()) {
            return xRegisterType.get();
        }
        else {
            RegisterType registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, channelTypeObisCode);
            registerType.save();
            return registerType;
        }
    }

}
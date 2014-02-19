package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link ChannelSpecImpl} component
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 15:48
 */
public class ChannelSpecImplTest extends CommonDeviceConfigSpecsTest {

    private static final String DEVICE_CONFIGURATION_NAME = ChannelSpecImplTest.class.getName() + "Config";
    private static final String LOAD_PROFILE_TYPE_NAME = ChannelSpecImplTest.class.getSimpleName() + "LoadProfileType";

    private final ObisCode loadProfileTypeObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final ObisCode overruledLoadProfileSpecObisCode = ObisCode.fromString("1.0.99.2.0.255");

    private TimeDuration interval = TimeDuration.days(1);

    private DeviceConfiguration deviceConfiguration;
    private LoadProfileType loadProfileType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration() {
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            loadProfileType = this.inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME, loadProfileTypeObisCode, interval);
            loadProfileType.save();

            // Business method
            deviceType.setDescription("For loadProfileSpec Test purposes only");
            deviceType.addLoadProfileType(loadProfileType);
            DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
            deviceConfiguration = deviceConfigurationBuilder.add();
            deviceType.save();
            ctx.commit();
        }
    }


    private LoadProfileSpec createDefaultTestingLoadProfileSpecWithOverruledObisCode() {
        LoadProfileSpec loadProfileSpec;
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfiguration.createLoadProfileSpec(this.loadProfileType);
            loadProfileSpecBuilder.setOverruledObisCode(overruledLoadProfileSpecObisCode);
            loadProfileSpec = loadProfileSpecBuilder.add();
            tctx.commit();
        }
        return loadProfileSpec;
    }

//    @Test
//    public void createChannelSpecTest() {
////        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
////            this.deviceConfiguration.createChannelSpec()
////            tctx.commit();
////        }
//
//    }
}

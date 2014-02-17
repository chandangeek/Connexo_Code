package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeIsNotConfiguredOnDeviceTypeException;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the {@link LoadProfileSpecImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 09:55
 */
public class LoadProfileSpecImplTest extends CommonDeviceConfigSpecsTest {

    private static final String DEVICE_CONFIGURATION_NAME = LoadProfileSpecImplTest.class.getName() + "Config";
    private static final String LOAD_PROFILE_TYPE_NAME = LoadProfileSpecImplTest.class.getSimpleName() + "LoadProfileType";

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

    @Test
    public void createLoadProfileSpecTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        assertThat(loadProfileSpec.getLoadProfileType()).isEqualTo(this.loadProfileType);
        assertThat(loadProfileSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(loadProfileSpec.getDeviceObisCode()).isEqualTo(this.overruledLoadProfileSpecObisCode);
        assertThat(loadProfileSpec.getObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getInterval()).isEqualTo(this.interval);
    }

    @Test
    public void updateLoadProfileSpecTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LoadProfileSpec.LoadProfileSpecUpdater loadProfileSpecUpdater = this.deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec);
            loadProfileSpec.setOverruledObisCode(null);
            loadProfileSpecUpdater.update();
            tctx.commit();
        }

        assertThat(loadProfileSpec.getLoadProfileType()).isEqualTo(this.loadProfileType);
        assertThat(loadProfileSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(loadProfileSpec.getDeviceObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getInterval()).isEqualTo(this.interval);
    }

    @Test(expected = LoadProfileTypeIsNotConfiguredOnDeviceTypeException.class)
    public void createWithIncorrectLoadProfileTypeTest() {
        try (TransactionContext tctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            LoadProfileType loadProfileType = this.inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME+"I", loadProfileTypeObisCode, interval);
            loadProfileType.save();

            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
            loadProfileSpecBuilder.add();

        }
    }
}

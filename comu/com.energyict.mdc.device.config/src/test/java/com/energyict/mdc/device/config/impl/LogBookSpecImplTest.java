package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsNotConfiguredOnDeviceTypeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link LogBookSpecImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/02/14
 * Time: 11:22
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = LogBookSpecImplTest.class.getName() + "Config";
    private static final String LOGBOOK_TYPE_NAME = LogBookSpecImplTest.class.getName() + "LogBookType";

    private final ObisCode logBookTypeObisCode = ObisCode.fromString("0.0.99.98.0.255");
    private final ObisCode overruledLogBookSpecObisCode = ObisCode.fromString("1.0.99.97.0.255");

    private DeviceConfiguration deviceConfiguration;
    private LogBookType logBookType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration() {
        logBookType = inMemoryPersistence.getMasterDataService().newLogBookType(LOGBOOK_TYPE_NAME, logBookTypeObisCode);
        logBookType.save();

        // Business method
        deviceType.setDescription("For logBookSpec Test purposes only");
        deviceType.addLogBookType(logBookType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }


    private DeviceConfiguration getReloadedDeviceConfiguration(){
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceConfiguration(this.deviceConfiguration.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + this.deviceConfiguration.getId()));
    }

    private LogBookSpec createDefaultTestingLogBookSpecWithOverruledObisCode() {
        LogBookSpec logBookSpec;
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = getReloadedDeviceConfiguration().createLogBookSpec(this.logBookType);
        logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
        logBookSpec = logBookSpecBuilder.add();
        return logBookSpec;
    }

    @Test
    @Transactional
    public void createLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        assertThat(logBookSpec.getLogBookType()).isEqualTo(this.logBookType);
        assertThat(logBookSpec.getDeviceObisCode()).isEqualTo(overruledLogBookSpecObisCode);
        assertThat(logBookSpec.getDeviceConfiguration().getId()).isEqualTo(this.getReloadedDeviceConfiguration().getId());
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test
    @Transactional
    public void updateLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        LogBookSpec.LogBookSpecUpdater logBookSpecUpdater = this.getReloadedDeviceConfiguration().getLogBookSpecUpdaterFor(logBookSpec);
        logBookSpecUpdater.setOverruledObisCode(null);
        logBookSpecUpdater.update();

        assertThat(logBookSpec.getLogBookType()).isEqualTo(this.logBookType);
        assertThat(logBookSpec.getDeviceObisCode()).isEqualTo(this.logBookTypeObisCode);
        assertThat(logBookSpec.getDeviceConfiguration().getId()).isEqualTo(this.getReloadedDeviceConfiguration().getId());
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test(expected = LogbookTypeIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void createWithIncorrectLogBookType() {
        LogBookSpec logBookSpec;
        LogBookType logBookType;

        logBookType = inMemoryPersistence.getMasterDataService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
        logBookType.save();

        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = getReloadedDeviceConfiguration().createLogBookSpec(logBookType);
        logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
        logBookSpec = logBookSpecBuilder.add();
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void addWithActiveDeviceConfigurationTest() {
        this.getReloadedDeviceConfiguration().activate();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = getReloadedDeviceConfiguration().createLogBookSpec(this.logBookType);
        logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
        LogBookSpec logBookSpec = logBookSpecBuilder.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffTypeButSameObisCodeTest() {
        LogBookType otherLogBookType = inMemoryPersistence.getMasterDataService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", logBookTypeObisCode);
        otherLogBookType.save();
        this.deviceType.addLogBookType(otherLogBookType);
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = getReloadedDeviceConfiguration().createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = getReloadedDeviceConfiguration().createLogBookSpec(otherLogBookType);
        LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButOverruledAsSameObisCodeTest() {
        LogBookType otherLogBookType = inMemoryPersistence.getMasterDataService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
        otherLogBookType.save();
        this.deviceType.addLogBookType(otherLogBookType);
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = getReloadedDeviceConfiguration().createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = getReloadedDeviceConfiguration().createLogBookSpec(otherLogBookType);
        logBookSpecBuilder2.setOverruledObisCode(logBookTypeObisCode);
        LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButSameAfterUpdateTest() {
        LogBookSpec logBookSpec2;
        LogBookType otherLogBookType = inMemoryPersistence.getMasterDataService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
        otherLogBookType.save();
        this.deviceType.addLogBookType(otherLogBookType);
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = getReloadedDeviceConfiguration().createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = getReloadedDeviceConfiguration().createLogBookSpec(otherLogBookType);
        logBookSpec2 = logBookSpecBuilder2.add();
        LogBookSpec.LogBookSpecUpdater logBookSpecUpdater = this.getReloadedDeviceConfiguration().getLogBookSpecUpdaterFor(logBookSpec2);
        logBookSpecUpdater.setOverruledObisCode(logBookTypeObisCode);
        logBookSpecUpdater.update();
    }

    @Test
    @Transactional
    public void successfulDeleteTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        getReloadedDeviceConfiguration().deleteLogBookSpec(logBookSpec);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotDeleteWhenConfigIsActiveTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        this.getReloadedDeviceConfiguration().activate();
        this.getReloadedDeviceConfiguration().deleteLogBookSpec(logBookSpec);
    }

    @Test
    @Transactional
    public void cloneWithOverruledObisCodeTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LogBookSpec clonedLogBookSpec = ((ServerLogBookSpec) logBookSpec).cloneForDeviceConfig(clone);

        assertThat(clonedLogBookSpec.getDeviceConfiguration().getId()).isEqualTo(clone.getId());
        assertThat(clonedLogBookSpec.getObisCode()).isEqualTo(logBookTypeObisCode);
        assertThat(clonedLogBookSpec.getDeviceObisCode()).isEqualTo(overruledLogBookSpecObisCode);
    }

    @Test
    @Transactional
    public void cloneWithoutOverruledObisCodeTest() {
        LogBookSpec logBookSpec = getReloadedDeviceConfiguration().createLogBookSpec(this.logBookType).add();
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LogBookSpec clonedLogBookSpec = ((ServerLogBookSpec) logBookSpec).cloneForDeviceConfig(clone);

        assertThat(clonedLogBookSpec.getDeviceConfiguration().getId()).isEqualTo(clone.getId());
        assertThat(clonedLogBookSpec.getObisCode()).isEqualTo(logBookTypeObisCode);
        assertThat(clonedLogBookSpec.getDeviceObisCode()).isEqualTo(logBookTypeObisCode);
    }
}
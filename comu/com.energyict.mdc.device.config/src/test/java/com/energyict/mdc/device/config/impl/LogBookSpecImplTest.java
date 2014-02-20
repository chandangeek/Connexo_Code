package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.common.Transactional;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
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
public class LogBookSpecImplTest extends PersistenceTest {

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
        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME, logBookTypeObisCode);
        logBookType.save();

        // Business method
        deviceType.setDescription("For logBookSpec Test purposes only");
        deviceType.addLogBookType(logBookType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }

    private LogBookSpec createDefaultTestingLogBookSpecWithOverruledObisCode() {
        LogBookSpec logBookSpec;
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(this.logBookType);
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
        assertThat(logBookSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test
    @Transactional
    public void updateLogBookSpecTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        LogBookSpec.LogBookSpecUpdater logBookSpecUpdater = this.deviceConfiguration.getLogBookSpecUpdaterFor(logBookSpec);
        logBookSpecUpdater.setOverruledObisCode(null);
        logBookSpecUpdater.update();

        assertThat(logBookSpec.getLogBookType()).isEqualTo(this.logBookType);
        assertThat(logBookSpec.getDeviceObisCode()).isEqualTo(this.logBookTypeObisCode);
        assertThat(logBookSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(logBookSpec.getObisCode()).isEqualTo(this.logBookTypeObisCode);
    }

    @Test(expected = LogbookTypeIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void createWithIncorrectLogBookType() {
        LogBookSpec logBookSpec;
        LogBookType logBookType;

        logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
        logBookType.save();

        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(logBookType);
        logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
        logBookSpec = logBookSpecBuilder.add();
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void addWithActiveDeviceConfigurationTest() {
        this.deviceConfiguration.activate();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder = deviceConfiguration.createLogBookSpec(this.logBookType);
        logBookSpecBuilder.setOverruledObisCode(overruledLogBookSpecObisCode);
        LogBookSpec logBookSpec = logBookSpecBuilder.add();
    }

    @Test(expected = DuplicateLogBookTypeException.class)
    @Transactional
    public void addTwoSpecsWithSameLogBookTypeTest() {
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffTypeButSameObisCodeTest() {
        LogBookType otherLogBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", logBookTypeObisCode);
        otherLogBookType.save();
        this.deviceType.addLogBookType(otherLogBookType);
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(otherLogBookType);
        LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButOverruledAsSameObisCodeTest() {
        LogBookType otherLogBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
        otherLogBookType.save();
        this.deviceType.addLogBookType(otherLogBookType);
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(otherLogBookType);
        logBookSpecBuilder2.setOverruledObisCode(logBookTypeObisCode);
        LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButSameAfterUpdateTest() {
        LogBookSpec logBookSpec2;
        LogBookType otherLogBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType(LOGBOOK_TYPE_NAME + "Incorrect", ObisCode.fromString("1.0.1.0.1.0"));
        otherLogBookType.save();
        this.deviceType.addLogBookType(otherLogBookType);
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(this.logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(otherLogBookType);
        logBookSpec2 = logBookSpecBuilder2.add();
        LogBookSpec.LogBookSpecUpdater logBookSpecUpdater = this.deviceConfiguration.getLogBookSpecUpdaterFor(logBookSpec2);
        logBookSpecUpdater.setOverruledObisCode(logBookTypeObisCode);
        logBookSpecUpdater.update();
    }

    @Test
    @Transactional
    public void successfulDeleteTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        deviceConfiguration.deleteLogBookSpec(logBookSpec);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotDeleteWhenConfigIsActiveTest() {
        LogBookSpec logBookSpec = createDefaultTestingLogBookSpecWithOverruledObisCode();

        this.deviceConfiguration.activate();
        this.deviceConfiguration.deleteLogBookSpec(logBookSpec);
    }
}
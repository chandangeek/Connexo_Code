package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.common.Transactional;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeIsNotConfiguredOnDeviceTypeException;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME, loadProfileTypeObisCode, interval);
        loadProfileType.save();

        // Business method
        deviceType.setDescription("For loadProfileSpec Test purposes only");
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }

    private LoadProfileSpec createDefaultTestingLoadProfileSpecWithOverruledObisCode() {
        LoadProfileSpec loadProfileSpec;
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfiguration.createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledLoadProfileSpecObisCode);
        loadProfileSpec = loadProfileSpecBuilder.add();
        return loadProfileSpec;
    }

    @Test
    @Transactional
    public void createLoadProfileSpecTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        assertThat(loadProfileSpec.getLoadProfileType()).isEqualTo(this.loadProfileType);
        assertThat(loadProfileSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(loadProfileSpec.getDeviceObisCode()).isEqualTo(this.overruledLoadProfileSpecObisCode);
        assertThat(loadProfileSpec.getObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getInterval()).isEqualTo(this.interval);
    }

    @Test
    @Transactional
    public void updateLoadProfileSpecTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        LoadProfileSpec.LoadProfileSpecUpdater loadProfileSpecUpdater = this.deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec);
        loadProfileSpec.setOverruledObisCode(null);
        loadProfileSpecUpdater.update();

        assertThat(loadProfileSpec.getLoadProfileType()).isEqualTo(this.loadProfileType);
        assertThat(loadProfileSpec.getDeviceConfiguration()).isEqualTo(this.deviceConfiguration);
        assertThat(loadProfileSpec.getDeviceObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getInterval()).isEqualTo(this.interval);
    }

    @Test(expected = LoadProfileTypeIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void createWithIncorrectLoadProfileTypeTest() {
        LoadProfileType loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "Incorrect", loadProfileTypeObisCode, interval);
        loadProfileType.save();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpecBuilder.add();
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void addWithActiveDeviceConfigurationTest() {
        this.deviceConfiguration.activate();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec.add();
    }

    @Test(expected = DuplicateLoadProfileTypeException.class)
    @Transactional
    public void addTwoSpecsWithSameLoadProfileTypeTest() {
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec2 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffTypeButSameObisCodeTest() {
        LoadProfileType loadProfileType2 = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "other", loadProfileTypeObisCode, interval);
        loadProfileType2.save();
        this.deviceType.addLoadProfileType(loadProfileType2);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec2 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType2);
        loadProfileSpec2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButOverruledAsSameTest() {
        LoadProfileType loadProfileType2 = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "other", ObisCode.fromString("1.0.99.98.0.255"), interval);
        loadProfileType2.save();
        this.deviceType.addLoadProfileType(loadProfileType2);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec2 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType2);
        loadProfileSpec2.setOverruledObisCode(loadProfileTypeObisCode);
        loadProfileSpec2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButSameAfterUpdateTest() {
        LoadProfileType loadProfileType2 = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "other", ObisCode.fromString("1.0.99.98.0.255"), interval);
        loadProfileType2.save();
        this.deviceType.addLoadProfileType(loadProfileType2);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder1 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpecBuilder1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder2 = this.deviceConfiguration.createLoadProfileSpec(loadProfileType2);
        LoadProfileSpec loadProfileSpec = loadProfileSpecBuilder2.add();
        LoadProfileSpec.LoadProfileSpecUpdater loadProfileSpecUpdater = this.deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec);
        loadProfileSpecUpdater.setOverruledObisCode(loadProfileTypeObisCode);
        loadProfileSpecUpdater.update();
    }

    @Test
    @Transactional
    public void successfulDeleteTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        this.deviceConfiguration.deleteLoadProfileSpec(loadProfileSpec);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveDeviceConfigurationTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        this.deviceConfiguration.activate();
        this.deviceConfiguration.deleteLoadProfileSpec(loadProfileSpec);
    }

    @Test
    @Transactional
    public void buildingCompletionListenerTest() {
        LoadProfileSpec.BuildingCompletionListener buildingCompletionListener = mock(LoadProfileSpec.BuildingCompletionListener.class);
        LoadProfileSpec loadProfileSpec;
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfiguration.createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledLoadProfileSpecObisCode);
        loadProfileSpecBuilder.notifyOnAdd(buildingCompletionListener);
        loadProfileSpec = loadProfileSpecBuilder.add();

        verify(buildingCompletionListener).loadProfileSpecBuildingProcessCompleted(loadProfileSpec);
    }
}

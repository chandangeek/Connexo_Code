package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.masterdata.RegisterType;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link LoadProfileSpecImpl} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 09:55
 */
public class LoadProfileSpecImplTest extends DeviceTypeProvidingPersistenceTest {

    private static final String DEVICE_CONFIGURATION_NAME = LoadProfileSpecImplTest.class.getName() + "Config";
    private static final String LOAD_PROFILE_TYPE_NAME = LoadProfileSpecImplTest.class.getSimpleName() + "LoadProfileType";

    private final ObisCode loadProfileTypeObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private final ObisCode overruledLoadProfileSpecObisCode = ObisCode.fromString("1.0.99.2.0.255");

    private TimeDuration interval = TimeDuration.days(1);

    private DeviceConfiguration deviceConfiguration;
    private LoadProfileType loadProfileType;
    private RegisterType registerType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.setupReadingTypeInExistingTransaction();
        this.initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration();
    }

    private void initializeDeviceTypeWithLogBookTypeAndDeviceConfiguration() {
        loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME, loadProfileTypeObisCode, interval, Arrays.asList(registerType));
        loadProfileType.save();

        // Business method
        deviceType.setDescription("For loadProfileSpec Test purposes only");
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration(DEVICE_CONFIGURATION_NAME);
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
    }

    private DeviceConfiguration getReloadedDeviceConfiguration(){
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceConfiguration(this.deviceConfiguration.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + this.deviceConfiguration.getId()));
    }

    private LoadProfileSpec createDefaultTestingLoadProfileSpecWithOverruledObisCode() {
        LoadProfileSpec loadProfileSpec;
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = getReloadedDeviceConfiguration().createLoadProfileSpec(this.loadProfileType);
        loadProfileSpecBuilder.setOverruledObisCode(overruledLoadProfileSpecObisCode);
        loadProfileSpec = loadProfileSpecBuilder.add();
        return loadProfileSpec;
    }

    @Test
    @Transactional
    public void createLoadProfileSpecTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        assertThat(loadProfileSpec.getLoadProfileType().getId()).isEqualTo(this.loadProfileType.getId());
        assertThat(loadProfileSpec.getDeviceConfiguration().getId()).isEqualTo(getReloadedDeviceConfiguration().getId());
        assertThat(loadProfileSpec.getDeviceObisCode()).isEqualTo(this.overruledLoadProfileSpecObisCode);
        assertThat(loadProfileSpec.getObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getInterval()).isEqualTo(this.interval);
    }

    @Test
    @Transactional
    public void updateLoadProfileSpecTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        LoadProfileSpec.LoadProfileSpecUpdater loadProfileSpecUpdater = this.getReloadedDeviceConfiguration().getLoadProfileSpecUpdaterFor(loadProfileSpec);
        loadProfileSpec.setOverruledObisCode(null);
        loadProfileSpecUpdater.update();

        assertThat(loadProfileSpec.getLoadProfileType().getId()).isEqualTo(this.loadProfileType.getId());
        assertThat(loadProfileSpec.getDeviceConfiguration().getId()).isEqualTo(getReloadedDeviceConfiguration().getId());
        assertThat(loadProfileSpec.getDeviceObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getObisCode()).isEqualTo(this.loadProfileTypeObisCode);
        assertThat(loadProfileSpec.getInterval()).isEqualTo(this.interval);
    }

    @Test(expected = LoadProfileTypeIsNotConfiguredOnDeviceTypeException.class)
    @Transactional
    public void createWithIncorrectLoadProfileTypeTest() {
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "Incorrect", loadProfileTypeObisCode, interval, Arrays.asList(registerType));
        loadProfileType.save();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType);
        loadProfileSpecBuilder.add();
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void addWithActiveDeviceConfigurationTest() {
        this.getReloadedDeviceConfiguration().activate();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType);
        loadProfileSpec.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffTypeButSameObisCodeTest() {
        LoadProfileType loadProfileType2 = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "other", loadProfileTypeObisCode, interval, Arrays.asList(registerType));
        loadProfileType2.save();
        this.deviceType.addLoadProfileType(loadProfileType2);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType);
        loadProfileSpec1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec2 = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType2);
        loadProfileSpec2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButOverruledAsSameTest() {
        LoadProfileType loadProfileType2 = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "other", ObisCode.fromString("1.0.99.98.0.255"), interval, Arrays.asList(registerType));
        loadProfileType2.save();
        this.deviceType.addLoadProfileType(loadProfileType2);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType);
        loadProfileSpec1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec2 = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType2);
        loadProfileSpec2.setOverruledObisCode(loadProfileTypeObisCode);
        loadProfileSpec2.add();
    }

    @Test(expected = DuplicateObisCodeException.class)
    @Transactional
    public void addTwoSpecsWithDiffObisCodeButSameAfterUpdateTest() {
        LoadProfileType loadProfileType2 = inMemoryPersistence.getMasterDataService().newLoadProfileType(LOAD_PROFILE_TYPE_NAME + "other", ObisCode.fromString("1.0.99.98.0.255"), interval, Arrays.asList(registerType));
        loadProfileType2.save();
        this.deviceType.addLoadProfileType(loadProfileType2);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder1 = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType);
        loadProfileSpecBuilder1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder2 = this.getReloadedDeviceConfiguration().createLoadProfileSpec(loadProfileType2);
        LoadProfileSpec loadProfileSpec = loadProfileSpecBuilder2.add();
        LoadProfileSpec.LoadProfileSpecUpdater loadProfileSpecUpdater = this.getReloadedDeviceConfiguration().getLoadProfileSpecUpdaterFor(loadProfileSpec);
        loadProfileSpecUpdater.setOverruledObisCode(loadProfileTypeObisCode);
        loadProfileSpecUpdater.update();
    }

    @Test
    @Transactional
    public void successfulDeleteTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        this.getReloadedDeviceConfiguration().deleteLoadProfileSpec(loadProfileSpec);
    }

    @Test(expected = CannotDeleteFromActiveDeviceConfigurationException.class)
    @Transactional
    public void deleteFromActiveDeviceConfigurationTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();

        this.getReloadedDeviceConfiguration().activate();
        this.getReloadedDeviceConfiguration().deleteLoadProfileSpec(loadProfileSpec);
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        ReadingType readingType = PersistenceTest.inMemoryPersistence.getMeteringService().getReadingType(code).get();
        registerType = PersistenceTest.inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType).get();
    }

    @Test
    @Transactional
    public void cloneWithOverruledObisCodeTest() {
        LoadProfileSpec loadProfileSpec = createDefaultTestingLoadProfileSpecWithOverruledObisCode();
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LoadProfileSpec clonedLoadProfileSpec = ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone);

        assertThat(clonedLoadProfileSpec.getDeviceConfiguration().getId()).isEqualTo(clone.getId());
        assertThat(clonedLoadProfileSpec.getDeviceObisCode()).isEqualTo(overruledLoadProfileSpecObisCode);
        assertThat(clonedLoadProfileSpec.getObisCode()).isEqualTo(loadProfileTypeObisCode);
    }

    @Test
    @Transactional
    public void cloneWithoutOverruledObisCodeTest() {
        LoadProfileSpec loadProfileSpec = getReloadedDeviceConfiguration().createLoadProfileSpec(this.loadProfileType).add();
        DeviceConfiguration clone = deviceType.newConfiguration("MyClone").add();

        LoadProfileSpec clonedLoadProfileSpec = ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone);

        assertThat(clonedLoadProfileSpec.getDeviceConfiguration().getId()).isEqualTo(clone.getId());
        assertThat(clonedLoadProfileSpec.getDeviceObisCode()).isEqualTo(loadProfileTypeObisCode);
        assertThat(clonedLoadProfileSpec.getObisCode()).isEqualTo(loadProfileTypeObisCode);
    }

}

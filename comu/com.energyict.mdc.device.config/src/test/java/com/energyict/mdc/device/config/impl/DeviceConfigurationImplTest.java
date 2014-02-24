package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import org.junit.Test;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;

/**
 * Tests the {@link DeviceConfigurationImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/02/14
 * Time: 10:21
 */
public class DeviceConfigurationImplTest extends PersistenceTest {

    @Test
    @Transactional
    public void testDeviceConfigurationCreation() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("DeviceConfiguration");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();

        assertThat(deviceConfiguration).isNotNull();
        assertThat(deviceConfiguration.getRegisterSpecs().size()).isZero();
        assertThat(deviceConfiguration.getChannelSpecs().size()).isZero();
        assertThat(deviceConfiguration.getLoadProfileSpecs().size()).isZero();
        assertThat(deviceConfiguration.getLogBookSpecs().size()).isZero();
    }

    @Test
    @Transactional
    public void reloadAfterCreationTest() {
        String deviceConfigurationName = "DeviceConfiguration";
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration(deviceConfigurationName);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();


        DeviceConfiguration reloaded = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfigurationByNameAndDeviceType(deviceConfigurationName, deviceType);

        assertThat(reloaded.getId()).isNotEqualTo(0);
        assertThat(reloaded.getDeviceType()).isEqualTo(this.deviceType);
    }

    @Test
    @Transactional
    public void updateNameTest() {
        String originalName = "DeviceConfiguration-Original";
        String updatedName = "DeviceConfiguration-Updated";
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration(originalName);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();

        deviceConfiguration.setName(updatedName);
        deviceConfiguration.save();

        DeviceConfiguration reloaded1 = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfigurationByNameAndDeviceType(originalName, deviceType);
        DeviceConfiguration reloaded2 = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfigurationByNameAndDeviceType(updatedName, deviceType);

        assertThat(reloaded1).isNull();
        assertThat(reloaded2).isNotNull();
    }

    @Test(expected = NameIsRequiredException.class)
    @Transactional
    public void createWithoutANameTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("");
        try {
            DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        } catch (NameIsRequiredException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.DEVICE_CONFIGURATION_NAME_IS_REQUIRED)) {
                fail("Should have gotten the exception indicating that the name is a required attribute of a device configuration.");
            } else {
                throw e;
            }
        }
    }

    @Test(expected = DuplicateNameException.class)
    @Transactional
    public void duplicateNameTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DeviceConfiguration");
        deviceConfigurationBuilder1.add();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder2 = this.deviceType.newConfiguration("DeviceConfiguration");
        try {
            deviceConfigurationBuilder2.add();
        } catch (DuplicateNameException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.DEVICE_CONFIGURATION_ALREADY_EXISTS)) {
                fail("Should have gotten the exception indicating that the name of the configuration already exists.");
            } else {
                throw e;
            }
        }
    }

    @Test
    @Transactional
    public void duplicateNameButForOtherDeviceTypeTest() {
        String deviceConfigurationName = "DeviceConfiguration";
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration(deviceConfigurationName);
        DeviceConfiguration deviceConfiguration1 = deviceConfigurationBuilder1.add();

        DeviceType deviceType2 = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME + "2", deviceProtocolPluggableClass);
        deviceType2.save();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder2 = deviceType2.newConfiguration(deviceConfigurationName);
        DeviceConfiguration deviceConfiguration2 = deviceConfigurationBuilder2.add();

        assertThat(deviceConfiguration1.getName().equals(deviceConfiguration2.getName()));
    }


    private LoadProfileType createDefaultLoadProfileType() {
        LoadProfileType loadProfileType = inMemoryPersistence.getDeviceConfigurationService().newLoadProfileType("LPTName", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1));
        loadProfileType.save();
        this.deviceType.addLoadProfileType(loadProfileType);
        return loadProfileType;
    }

    @Test(expected = DuplicateLoadProfileTypeException.class)
    @Transactional
    public void addTwoSpecsWithSameLoadProfileTypeTest() {
        LoadProfileType loadProfileType = createDefaultLoadProfileType();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec2 = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        loadProfileSpec2.add();
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddLoadProfileSpecToActiveDeviceConfigTest() {
        LoadProfileType loadProfileType = createDefaultLoadProfileType();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        deviceConfiguration.activate();

        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpec1 = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        try {
            loadProfileSpec1.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if(!e.getMessageSeed().equals(MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION)){
                fail("Should have gotten the exception indicating that the load profile spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }


    private LogBookType createDefaultLogBookType() {
        LogBookType logBookType = inMemoryPersistence.getDeviceConfigurationService().newLogBookType("LBTName", ObisCode.fromString("0.0.99.98.0.255"));
        logBookType.save();
        this.deviceType.addLogBookType(logBookType);
        return logBookType;
    }

    @Test(expected = DuplicateLogBookTypeException.class)
    @Transactional
    public void addTwoSpecsWithSameLogBookTypeTest() {
        LogBookType logBookType = createDefaultLogBookType();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();

        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(logBookType);
        LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(logBookType);
        LogBookSpec logBookSpec2 = logBookSpecBuilder2.add();
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddLogBookSpecToActiveDeviceConfigTest() {
        LogBookType logBookType = createDefaultLogBookType();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        deviceConfiguration.activate();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder1 = deviceConfiguration.createLogBookSpec(logBookType);
        try {
            LogBookSpec logBookSpec1 = logBookSpecBuilder1.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if(!e.getMessageSeed().equals(MessageSeeds.LOGBOOK_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION)){
                fail("Should have gotten the exception indicating that the log book spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddChannelSpecToActiveDeviceConfigTest() {
        RegisterMapping registerMapping = createDefaultRegisterMapping();
        Phenomenon phenomenon = createDefaultPhenomenon();
        LoadProfileType loadProfileType = createDefaultLoadProfileType();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        deviceConfiguration.activate();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = deviceConfiguration.createChannelSpec(registerMapping, phenomenon, loadProfileSpecBuilder.add());
        try {
            channelSpecBuilder.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if(!e.getMessageSeed().equals(MessageSeeds.CHANNEL_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION)){
                fail("Should have gotten the exception indicating that the channel spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private Phenomenon createDefaultPhenomenon() {
        Phenomenon phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon("DefPhenom", Unit.get("kWh"));
        phenomenon.save();
        return phenomenon;
    }

    private RegisterMapping createDefaultRegisterMapping() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        ProductSpec productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(readingType);
        productSpec.save();
        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping("RMName", ObisCode.fromString("1.0.1.8.0.255"), productSpec);
        registerMapping.save();
        this.deviceType.addRegisterMapping(registerMapping);
        return registerMapping;
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddRegisterSpecToActiveDeviceConfigTest() {
        RegisterMapping registerMapping = createDefaultRegisterMapping();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        deviceConfiguration.activate();

        RegisterSpec.RegisterSpecBuilder registerSpecBuilder = deviceConfiguration.createRegisterSpec(registerMapping);
        try {
            registerSpecBuilder.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if(!e.getMessageSeed().equals(MessageSeeds.REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG)){
                fail("Should have gotten the exception indicating that the register spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }
}

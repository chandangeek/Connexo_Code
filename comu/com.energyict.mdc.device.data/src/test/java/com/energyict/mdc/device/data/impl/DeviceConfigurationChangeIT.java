package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigToSameConfig;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 15.09.15
 * Time: 11:58
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationChangeIT extends PersistenceIntegrationTest {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    private String readingTypeMRID1 = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private String readingTypeMRID2 = "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0";


    @Test
    @Transactional
    public void simpleConfigChangeNoConflictsNoDataSourceTest() {
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();
        DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
    }

    @Test(expected = CannotChangeDeviceConfigToSameConfig.class)
    @Transactional
    public void changeConfigToSameConfigTest() {
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, firstDeviceConfiguration);
    }

    @Test
    @Transactional
    public void configChangeCreatesNewMeterActivationTest() {
        Instant initialClock = freezeClock(2012, 2, 9, 1, 11, 0, 0);
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();
        DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        device.activate(initialClock);

        Instant instant = freezeClock(2015, 9, 18, 11, 30, 0, 0);

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);
        assertThat(modifiedDevice.getCurrentMeterActivation().get().getStart()).isEqualTo(instant);
        assertThat(modifiedDevice.getMeterActivationsMostRecentFirst()).hasSize(2);
    }

    @Test
    @Transactional
    public void changeConfigWithSingleSameRegisterSpecTest() {
        RegisterType registerType = getRegisterTypeForReadingType(readingTypeMRID1);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters().get(0).getRegisterSpec().getId()).isEqualTo(secondDeviceConfiguration.getRegisterSpecs().get(0).getId());
        assertThat(modifiedDevice.getRegisters().get(0).getReadingType().getMRID()).isEqualTo(readingTypeMRID1);
    }

    @Test
    @Transactional
    public void changeConfigWithSingleOtherRegisterSpecTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType2);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        // registerType1 will NOT exist anymore on the device

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters().get(0).getRegisterSpec().getId()).isEqualTo(secondDeviceConfiguration.getRegisterSpecs().get(0).getId());
        assertThat(modifiedDevice.getRegisters().get(0).getReadingType().getMRID()).isEqualTo(readingTypeMRID2);
    }

    @Test
    @Transactional
    public void changeConfigWithSingleRegisterSpecToTwoRegisterSpecsTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType1, registerType2);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(2);
        assertThat(modifiedDevice.getRegisters()).haveExactly(1, new Condition<Register>() {
            @Override
            public boolean matches(Register register) {
                return register.getRegisterSpecId() == secondDeviceConfiguration.getRegisterSpecs().get(0).getId() &&
                        register.getReadingType().getMRID().equals(secondDeviceConfiguration.getRegisterSpecs().get(0).getReadingType().getMRID());
            }
        });
        assertThat(modifiedDevice.getRegisters()).haveExactly(1, new Condition<Register>() {
            @Override
            public boolean matches(Register register) {
                return register.getRegisterSpecId() == secondDeviceConfiguration.getRegisterSpecs().get(1).getId() &&
                        register.getReadingType().getMRID().equals(secondDeviceConfiguration.getRegisterSpecs().get(1).getReadingType().getMRID());
            }
        });
    }

    @Test
    @Transactional
    public void changeConfigWithTwoRegisterSpecsToOneRegisterSpecTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1, registerType2);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType1);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters()).haveExactly(1, new Condition<Register>() {
            @Override
            public boolean matches(Register register) {
                return register.getRegisterSpecId() == secondDeviceConfiguration.getRegisterSpecs().get(0).getId() &&
                        register.getReadingType().getMRID().equals(secondDeviceConfiguration.getRegisterSpecs().get(0).getReadingType().getMRID());
            }
        });
    }

    private LoadProfileType createLoadProfileType(String loadProfileName, ObisCode obisCode, RegisterType... registerTypes) {
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(loadProfileName, obisCode, TimeDuration.minutes(15), Arrays.asList(registerTypes));
        loadProfileType.save();
        return loadProfileType;
    }

    @Test
    @Transactional
    public void changeConfigWithSingleOtherLoadProfileSpecTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        LoadProfileType loadProfileType1 = createLoadProfileType("MyLoadProfile", ObisCode.fromString("1.0.99.1.0.255"), registerType1, registerType2);
        LoadProfileType loadProfileType2 = createLoadProfileType("MySecondLoadProfile", ObisCode.fromString("1.0.99.1.0.255"), registerType1);

        enhanceDeviceTypeWithLoadProfileTypes(this.deviceType, loadProfileType1, loadProfileType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithLoadProfileTypes(firstConfigBuilder, loadProfileType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithLoadProfileTypes(secondDeviceConfigBuilder, loadProfileType2);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLoadProfiles()).hasSize(1);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getLoadProfileSpec().getId()).isEqualTo(secondDeviceConfiguration.getLoadProfileSpecs().get(0).getId());
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).hasSize(1);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).haveExactly(1, new Condition<Channel>() {
            @Override
            public boolean matches(Channel channel) {
                return channel.getChannelSpec().getId() == secondDeviceConfiguration.getChannelSpecs().get(0).getId();
            }
        });
    }

    @Test
    @Transactional
    public void changeConfigWithSingleSameLoadProfileSpecTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        LoadProfileType loadProfileType = createLoadProfileType("MyLoadProfile", ObisCode.fromString("1.0.99.1.0.255"), registerType1, registerType2);

        enhanceDeviceTypeWithLoadProfileTypes(this.deviceType, loadProfileType);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithLoadProfileTypes(firstConfigBuilder, loadProfileType);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithLoadProfileTypes(secondDeviceConfigBuilder, loadProfileType);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLoadProfiles()).hasSize(1);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getLoadProfileSpec().getId()).isEqualTo(secondDeviceConfiguration.getLoadProfileSpecs().get(0).getId());
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).hasSize(2);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).haveExactly(1, new Condition<Channel>() {
            @Override
            public boolean matches(Channel channel) {
                return channel.getChannelSpec().getId() == secondDeviceConfiguration.getChannelSpecs().get(0).getId();
            }
        });
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).haveExactly(1, new Condition<Channel>(){
            @Override
            public boolean matches(Channel channel) {
                return channel.getChannelSpec().getId() == secondDeviceConfiguration.getChannelSpecs().get(1).getId();
            }
        });
    }

    private RegisterType getRegisterTypeForReadingType(String readingTypeMRID) {
        return inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(inMemoryPersistence.getMeteringService().getReadingType(readingTypeMRID).get()).get();
    }

    private void enhanceDeviceTypeWithRegisterTypes(DeviceType deviceType, RegisterType... registerType) {
        Stream.of(registerType).forEach(deviceType::addRegisterType);
        deviceType.save();
    }

    private void enhanceDeviceTypeWithLoadProfileTypes(DeviceType deviceType, LoadProfileType... loadProfileTypes) {
        Stream.of(loadProfileTypes).forEach(deviceType::addLoadProfileType);
        deviceType.save();
    }

    private void enhanceConfigBuilderWithRegisterTypes(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder, RegisterType... registerType) {
        Stream.of(registerType).forEach(getNewNumericalRegisterSpec(deviceConfigBuilder));
    }

    private void enhanceConfigBuilderWithLoadProfileTypes(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder, LoadProfileType... loadProfileTypes) {
        Stream.of(loadProfileTypes).forEach(getLoadProfileSpec(deviceConfigBuilder));
    }

    private Consumer<LoadProfileType> getLoadProfileSpec(DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder){
        return loadProfileType -> {
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
            loadProfileType.getChannelTypes().stream().forEach(channelType -> deviceConfigurationBuilder.newChannelSpec(channelType, loadProfileSpecBuilder));
        };
    }

    private Consumer<RegisterType> getNewNumericalRegisterSpec(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder) {
        return registerType -> {
            NumericalRegisterSpec.Builder builder = deviceConfigBuilder.newNumericalRegisterSpec(registerType);
            builder.setNumberOfDigits(9);
            builder.setNumberOfFractionDigits(3);
        };
    }
}
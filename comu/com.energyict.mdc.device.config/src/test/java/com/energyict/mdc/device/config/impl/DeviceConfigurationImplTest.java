/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceMessageEnablement;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.config.TextualRegisterSpec;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.util.conditions.Where.where;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DeviceConfigurationImplTest extends DeviceTypeProvidingPersistenceTest {
    private final BigDecimal overflowValue = BigDecimal.valueOf(10000);

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Test
    @Transactional
    public void testAddSecondLoadProfileSpec() throws SQLException {
        final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
        final ObisCode OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
        final ObisCode OBIS_CODE2 = ObisCode.fromString("1.0.99.2.0.255");

        MasterDataService masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        DeviceConfigurationService deviceConfigurationService = PersistenceTest.inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testAddSecondLoadProfileSpec1";
        String loadProfileTypeName2 = "testAddSecondLoadProfileSpec2";

        LoadProfileType loadProfileType;
        ReadingType readingType = setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType 1
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, INTERVAL_15_MINUTES, Arrays.asList(registerType));
        loadProfileType.setDescription("For testing purposes only");
        ChannelType channelTypeForRegisterType = loadProfileType.findChannelType(registerType).get();
        loadProfileType.save();

        // Setup LoadProfileType 2
        LoadProfileType loadProfileType2 = masterDataService.newLoadProfileType(loadProfileTypeName2, OBIS_CODE2, INTERVAL_15_MINUTES, Arrays.asList(registerType));
        loadProfileType2.setDescription("For testing purposes only");
        loadProfileType2.save();

        // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addRegisterType(registerType);
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addLoadProfileType(loadProfileType2);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.newChannelSpec(channelTypeForRegisterType, loadProfileSpecBuilder).overflow(BigDecimal.valueOf(999999)).nbrOfFractionDigits(3);
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder2 = configurationBuilder.newLoadProfileSpec(loadProfileType2);

        // Business method
        configurationBuilder.add();

        // Asserts
    }

    private ReadingType setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        return PersistenceTest.inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

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

        // Business method
        deviceConfigurationBuilder.add();

        // Asserts
        List<DeviceConfiguration> reloaded = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfigurationsByDeviceType(deviceType);
        assertThat(reloaded).isNotEmpty();
    }

    @Test
    @Transactional
    public void updateNameTest() {
        String originalName = "DeviceConfiguration-Original";
        String updatedName = "DeviceConfiguration-Updated";
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration(originalName);

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();

        // Business method
        deviceConfiguration.setName(updatedName);
        deviceConfiguration.save();

        // Asserts
        List<DeviceConfiguration> deviceConfigurations = inMemoryPersistence.getDeviceConfigurationService().findDeviceConfigurationsByDeviceType(deviceType);

        assertThat(deviceConfigurations).hasSize(1);
        DeviceConfiguration reloaded = deviceConfigurations.get(0);
        assertThat(reloaded.getName()).isEqualTo(updatedName);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void createWithoutNameTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("");
        deviceConfigurationBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name")
    public void createWithWhiteSpaceNameTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration(" ");
        deviceConfigurationBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
    public void duplicateNameTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DeviceConfiguration");
        deviceConfigurationBuilder1.add();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder2 = this.deviceType.newConfiguration("DeviceConfiguration");
        deviceConfigurationBuilder2.add();
    }

    @Test
    @Transactional
    public void duplicateNameButForOtherDeviceTypeTest() {
        String deviceConfigurationName = "DeviceConfiguration";
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration(deviceConfigurationName);
        DeviceConfiguration deviceConfiguration1 = deviceConfigurationBuilder1.add();

        DeviceType deviceType2 = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME + "2", deviceProtocolPluggableClass);

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder2 = deviceType2.newConfiguration(deviceConfigurationName);
        DeviceConfiguration deviceConfiguration2 = deviceConfigurationBuilder2.add();

        assertThat(deviceConfiguration1.getName().equals(deviceConfiguration2.getName()));
    }


    private LoadProfileType createDefaultLoadProfileType(RegisterType registerType) {
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService()
                .newLoadProfileType("LPTName", ObisCode.fromString("1.0.99.1.0.255"), TimeDuration.days(1), Arrays.asList(registerType));
        loadProfileType.save();
        this.deviceType.addLoadProfileType(loadProfileType);
        return loadProfileType;
    }

    private LoadProfileType createDefaultLoadProfileType() {
        return this.createDefaultLoadProfileType(createDefaultRegisterType());
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
            if (!e.getMessageSeed().equals(MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION)) {
                fail("Should have gotten the exception indicating that the load profile spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }


    private LogBookType createDefaultLogBookType() {
        LogBookType logBookType = inMemoryPersistence.getMasterDataService().newLogBookType("LBTName", ObisCode.fromString("0.0.99.98.0.255"));
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
        logBookSpecBuilder1.add();
        LogBookSpec.LogBookSpecBuilder logBookSpecBuilder2 = deviceConfiguration.createLogBookSpec(logBookType);
        logBookSpecBuilder2.add();
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
            logBookSpecBuilder1.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.LOGBOOK_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION)) {
                fail("Should have gotten the exception indicating that the log book spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddChannelSpecToActiveDeviceConfigTest() {
        RegisterType registerType = createDefaultRegisterType();
        LoadProfileType loadProfileType = createDefaultLoadProfileType(registerType);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        deviceConfiguration.activate();
        ChannelType channelTypeForRegisterType = loadProfileType.findChannelType(registerType).get();
        ChannelSpec.ChannelSpecBuilder channelSpecBuilder = deviceConfiguration.createChannelSpec(channelTypeForRegisterType, loadProfileSpecBuilder.add());
        try {
            channelSpecBuilder.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.CHANNEL_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION)) {
                fail("Should have gotten the exception indicating that the channel spec could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private RegisterType createDefaultRegisterType() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).accumulate(Accumulation.DELTADELTA).code();
        Unit unit = Unit.get("kWh");
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        ObisCode obisCode = ObisCode.fromString("1.0.1.8.0.255");
        Optional<RegisterType> xregisterType =
                inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType);
        RegisterType registerType;
        if (xregisterType.isPresent()) {
            registerType = xregisterType.get();
        } else {
            registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, obisCode);
            registerType.save();
        }

        this.deviceType.addRegisterType(registerType);
        return registerType;
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddNumericalRegisterSpecToActiveDeviceConfigTest() {
        RegisterType registerType = createDefaultRegisterType();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        deviceConfiguration.activate();

        NumericalRegisterSpec.Builder registerSpecBuilder = deviceConfiguration.createNumericalRegisterSpec(registerType)
                .overflowValue(overflowValue)
                .numberOfFractionDigits(0);
        try {
            registerSpecBuilder.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG)) {
                fail("Should have gotten the exception indicating that the register configuration could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test(expected = CannotAddToActiveDeviceConfigurationException.class)
    @Transactional
    public void cannotAddTextualRegisterSpecToActiveDeviceConfigTest() {
        RegisterType registerType = createDefaultRegisterType();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder1 = this.deviceType.newConfiguration("DevConfName");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder1.add();
        deviceConfiguration.activate();

        TextualRegisterSpec.Builder registerSpecBuilder = deviceConfiguration.createTextualRegisterSpec(registerType);
        try {
            registerSpecBuilder.add();
        } catch (CannotAddToActiveDeviceConfigurationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG)) {
                fail("Should have gotten the exception indicating that the register configuration could not be added to an active device configuration, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    @Transactional
    public void testSetDeviceConfigDirectlyAddressable() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION));
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("direct address").add();
        deviceConfiguration.addCommunicationFunction(DeviceCommunicationFunction.PROTOCOL_SESSION);
        deviceConfiguration.save();

        DeviceConfiguration refreshedDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        assertThat(refreshedDeviceConfiguration.isDirectlyAddressable()).isTrue();
        assertThat(refreshedDeviceConfiguration.canActAsGateway()).isFalse();
    }

    @Test
    @Transactional
    public void testSetDeviceConfigGateway() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("gateway").add();
        deviceConfiguration.addCommunicationFunction(DeviceCommunicationFunction.GATEWAY);
        deviceConfiguration.setGatewayType(GatewayType.HOME_AREA_NETWORK);
        deviceConfiguration.save();

        DeviceConfiguration refreshedDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        assertThat(refreshedDeviceConfiguration.isDirectlyAddressable()).isFalse();
        assertThat(refreshedDeviceConfiguration.canActAsGateway()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIG_DIRECT_ADDRESS_NOT_ALLOWED + "}", property = "isDirectlyAddressable")
    public void testSetDeviceConfigDirectlyAddressableWhenProtocolDoesNotAllowIt() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("direct address").add();
        deviceConfiguration.addCommunicationFunction(DeviceCommunicationFunction.PROTOCOL_SESSION);
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIG_GATEWAY_NOT_ALLOWED + "}", property = "canActAsGateway")
    public void testSetDeviceConfigGatewayWhenProtocolDoesNotAllowIt() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.<DeviceProtocolCapabilities>emptyList());
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("gateway").add();
        deviceConfiguration.addCommunicationFunction(DeviceCommunicationFunction.GATEWAY);
        deviceConfiguration.setGatewayType(GatewayType.HOME_AREA_NETWORK);
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void testCanUpdateNameWhenDeviceConfigIfInUse() throws Exception {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").add();
        deviceConfiguration.activate();

        deviceConfiguration.setName("updated");
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void testCanUpdateDescriptionWhenDeviceConfigIfInUse() throws Exception {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").add();
        deviceConfiguration.activate();

        deviceConfiguration.setDescription("updated");
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIG_ACTIVE_FIELD_IMMUTABLE + "}", property = "canActAsGateway", strict = false)
    public void testCanNotUpdateGatewayWhenDeviceConfigIfInUse() throws Exception {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").canActAsGateway(false).add();
        deviceConfiguration.activate();

        deviceConfiguration.setCanActAsGateway(true);
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIG_ACTIVE_FIELD_IMMUTABLE + "}", property = "gatewayType", strict = false)
    public void testCanNotUpdateGatewayTypeWhenDeviceConfigIfInUse() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").gatewayType(GatewayType.HOME_AREA_NETWORK).add();
        deviceConfiguration.activate();

        deviceConfiguration.setGatewayType(GatewayType.LOCAL_AREA_NETWORK);
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_GATEWAY_TYPE + "}", property = "gatewayType", strict = false)
    public void testCanNotSaveWithoutGatewayTypeWhenActsAsGateway() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType.newConfiguration("first").canActAsGateway(true).add();
    }

    @Test
    @Transactional
    public void testSaveDeviceConfigurationWithGateWayType() throws Exception {
        when(deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        deviceType.newConfiguration("first").gatewayType(GatewayType.HOME_AREA_NETWORK).add();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_CONFIG_ACTIVE_FIELD_IMMUTABLE + "}", property = "isDirectlyAddressable", strict = false)
    public void testCanNotUpdateDirectAddressWhenDeviceConfigIfInUse() throws Exception {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("first").description("this is it!").isDirectlyAddressable(false).add();
        deviceConfiguration.activate();

        deviceConfiguration.setDirectlyAddressable(true);
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void initialCreationOfConfigCreatesDefaultMessageEnablementsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Initial").add();
        List<DeviceMessageEnablement> deviceMessageEnablements = deviceConfiguration.getDeviceMessageEnablements();
        assertThat(deviceMessageEnablements).hasSize(deviceMessageIds.size());
        deviceMessageEnablements.stream().forEach(deviceMessageEnablement -> assertThat(deviceMessageEnablement.getUserActions()).
                containsOnly(
                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1,
                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2,
                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3));
    }

    @Test
    @Transactional
    public void removeDeviceMessageEnablementTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Remove").add();
        deviceConfiguration.removeDeviceMessageEnablement(DeviceMessageId.CONTACTOR_CLOSE);
        DeviceConfiguration reloadDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        assertThat(reloadDeviceConfiguration.getDeviceMessageEnablements()).hasSize(deviceMessageIds.size() - 1);
    }

    @Test
    @Transactional
    public void removeNonExistingDeviceMessageEnablementShouldNotFailTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("NonExisting").add();
        assertThat(deviceConfiguration.removeDeviceMessageEnablement(DeviceMessageId.DLMS_CONFIGURATION_SET_DEVICE_ID)).isFalse();
    }

    @Test
    @Transactional
    public void removeAUserActionFromAnExistingEnablementTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("RemoveExistingUserAction").add();
        Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = findDeviceMessageEnablementFor(deviceConfiguration, DeviceMessageId.CONTACTOR_CLOSE);

        assertThat(deviceMessageEnablementOptional.get().removeDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1)).isTrue();
    }

    @Test
    @Transactional
    public void removeUserActionFromExistingEnablementWhichDoesntExistTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("RemoveNonExistingUserAction").add();
        Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = findDeviceMessageEnablementFor(deviceConfiguration, DeviceMessageId.CONTACTOR_CLOSE);

        assertThat(deviceMessageEnablementOptional.get().removeDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4)).isFalse();
    }

    private Optional<DeviceMessageEnablement> findDeviceMessageEnablementFor(DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId) {
        return deviceConfiguration.getDeviceMessageEnablements().stream()
                .filter(dme -> DeviceMessageId.find(dme.getDeviceMessageDbValue()).isPresent())
                .filter(dme -> dme.getDeviceMessageId().equals(deviceMessageId)).findAny();
    }

    @Test
    @Transactional
    public void addUserActionWhichDoesntExistYetTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("addUserActionWhichDoesntExistYetTest").add();
        Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = findDeviceMessageEnablementFor(deviceConfiguration, DeviceMessageId.CONTACTOR_CLOSE);

        assertThat(deviceMessageEnablementOptional.get().addDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4)).isTrue();
    }

    @Test
    @Transactional
    public void addUserActionWhichExistsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("addUserActionWhichDoesntExistYetTest").add();
        Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = findDeviceMessageEnablementFor(deviceConfiguration, DeviceMessageId.CONTACTOR_CLOSE);

        assertThat(deviceMessageEnablementOptional.get().addDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1)).isFalse();
    }

    @Test
    @Transactional
    public void removeDeviceMessageEnablementRemovesUserActionsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("RemoveUserActions").add();

        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceConfiguration reloadedDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        Optional<DeviceMessageEnablement> dme = reloadedDeviceConfiguration.getDeviceMessageEnablements()
                .stream()
                .filter(deviceMessageEnablement -> DeviceMessageId.find(deviceMessageEnablement.getDeviceMessageDbValue()).isPresent())
                .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(contactorClose))
                .findAny();

        List<DeviceMessageEnablementImpl.DeviceMessageUserActionRecord> deviceMessageUserActionRecords = inMemoryPersistence.getDataModel()
                .mapper(DeviceMessageEnablementImpl.DeviceMessageUserActionRecord.class)
                .find("deviceMessageEnablement", dme.get());
        assertThat(deviceMessageUserActionRecords).hasSize(3);

        deviceConfiguration.removeDeviceMessageEnablement(contactorClose);

        deviceMessageUserActionRecords = inMemoryPersistence.getDataModel().mapper(DeviceMessageEnablementImpl.DeviceMessageUserActionRecord.class).find("deviceMessageEnablement", dme.get());
        assertThat(deviceMessageUserActionRecords).hasSize(0);
    }

    @Test
    @Transactional
    public void setSupportAllDeviceProtocolMessagesTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("setSupportAllDeviceProtocolMessagesTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};

        DeviceConfiguration reloadDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        reloadDeviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        reloadDeviceConfiguration.save();

        DeviceConfiguration configWithSupportAllMessages = reloadDeviceConfiguration(reloadDeviceConfiguration);

        assertThat(configWithSupportAllMessages.supportsAllProtocolMessages()).isTrue();
        assertThat(configWithSupportAllMessages.getAllProtocolMessagesUserActions()).containsOnly(deviceMessageUserActions);
    }

    @Test
    @Transactional
    public void settingSupportAllDeviceProtocolMessagesShouldRemoveMessageEnablementsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("settingSupportAllDeviceProtocolMessagesShouldRemoveMessageEnablementsTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};

        DeviceConfiguration reloadDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        reloadDeviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        reloadDeviceConfiguration.save();

        DeviceConfiguration configWithSupportAllMessages = reloadDeviceConfiguration(reloadDeviceConfiguration);

        assertThat(configWithSupportAllMessages.getDeviceMessageEnablements()).hasSize(0);
    }

    @Test
    @Transactional
    public void setSupportAllDeviceProtocolMessagesWithOtherUserActionsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("setSupportAllDeviceProtocolMessagesWithOtherUserActionsTest").add();
        DeviceMessageUserAction[] firstDeviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};
        DeviceMessageUserAction[] secondDeviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3};

        deviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, firstDeviceMessageUserActions);
        deviceConfiguration.save();

        DeviceConfiguration reloadDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        reloadDeviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, secondDeviceMessageUserActions);
        reloadDeviceConfiguration.save();

        DeviceConfiguration configWithLessUserActions = reloadDeviceConfiguration(reloadDeviceConfiguration);
        assertThat(configWithLessUserActions.getAllProtocolMessagesUserActions()).containsOnly(secondDeviceMessageUserActions);
    }

    @Test
    @Transactional
    public void removeSupportAllDeviceProtocolMessagesRemovesUserActionsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("removeSupportAllDeviceProtocolMessagesRemovesUserActionsTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};

        deviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        deviceConfiguration.save();

        DeviceConfiguration reloadDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        reloadDeviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(false);
        reloadDeviceConfiguration.save();

        DeviceConfiguration configWithNoSupportedMessages = reloadDeviceConfiguration(reloadDeviceConfiguration);

        assertThat(configWithNoSupportedMessages.getAllProtocolMessagesUserActions()).hasSize(0);
    }

    @Test
    @Transactional
    public void addingSpecificDeviceMessageEnablementsRemovesTheAllFlagTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("addingSpecificDeviceMessageEnablementsRemovesTheAllFlagTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};

        deviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        deviceConfiguration.save();

        DeviceConfiguration reloadDeviceConfiguration = reloadDeviceConfiguration(deviceConfiguration);
        DeviceMessageEnablement deviceMessageEnablement = reloadDeviceConfiguration.createDeviceMessageEnablement(DeviceMessageId.CONTACTOR_CLOSE)
                .addUserActions(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1)
                .build();

        DeviceConfiguration configWithEnablements = reloadDeviceConfiguration(reloadDeviceConfiguration);
        assertThat(configWithEnablements.supportsAllProtocolMessages()).isFalse();
        assertThat(configWithEnablements.getAllProtocolMessagesUserActions()).hasSize(0);
        assertThat(configWithEnablements.getDeviceMessageEnablements()).hasSize(1);
        assertThat(configWithEnablements.getDeviceMessageEnablements().get(0).getDeviceMessageId()).isEqualTo(deviceMessageEnablement.getDeviceMessageId());
    }

    @Test
    @Transactional
    public void deleteDeviceConfigurationDeletesMessageEnablementsAndUserActionsTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("deleteDeviceConfigurationDeletesMessageEnablementsAndUserActionsTest").add();

        deviceType.removeConfiguration(deviceConfiguration);

        List<DeviceMessageEnablementImpl> deviceMessageEnablements = inMemoryPersistence.getDataModel().mapper(DeviceMessageEnablementImpl.class).find();
        List<DeviceMessageEnablementImpl.DeviceMessageUserActionRecord> deviceMessageUserActionRecords = inMemoryPersistence.getDataModel()
                .mapper(DeviceMessageEnablementImpl.DeviceMessageUserActionRecord.class)
                .find();

        assertThat(deviceMessageEnablements).hasSize(0);
        assertThat(deviceMessageUserActionRecords).hasSize(0);
    }

    @Test
    @Transactional
    public void currentUserHasCorrectLevelTest() {
        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString(), Matchers.eq(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1.getPrivilege()))).thenReturn(true);
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("currentUserHasCorrectLevelTest").add();
        assertThat(deviceConfiguration.isAuthorized(DeviceMessageId.CONTACTOR_CLOSE)).isTrue();
    }

    @Test
    @Transactional
    public void currentUserDoesntHaveCorrectLevelTest() {
        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString(), anyString())).thenReturn(false);

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("currentUserDoesntHaveCorrectLevelTest").add();

        assertThat(deviceConfiguration.isAuthorized(DeviceMessageId.CONTACTOR_CLOSE)).isFalse();
    }

    @Test
    @Transactional
    public void currentUserHasAllPrivilegeButNotConfiguredOnConfigTest() {
        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString(), anyString())).thenReturn(true);

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("currentUserHasAllPrivilegeButNotConfiguredOnConfigTest").add();

        assertThat(deviceConfiguration.isAuthorized(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID)).isFalse();
    }

    @Test
    @Transactional
    public void deviceConfigHasAllProtocolMessagesAndUserHasCorrectLevelTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("deviceConfigHasAllProtocolMessagesAndUserHasCorrectLevelTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};
        deviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        deviceConfiguration.save();

        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString(), Matchers.eq(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1.getPrivilege()))).thenReturn(true);

        assertThat(deviceConfiguration.isAuthorized(DeviceMessageId.CONTACTOR_CLOSE)).isTrue();
    }


    @Test
    @Transactional
    public void deviceConfigHasAllProtocolMessagesAndUserDoesntHaveCorrectLevelTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("deviceConfigHasAllProtocolMessagesAndUserHasCorrectLevelTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};
        deviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        deviceConfiguration.save();

        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString(), anyString())).thenReturn(false);

        assertThat(deviceConfiguration.isAuthorized(DeviceMessageId.CONTACTOR_CLOSE)).isFalse();
    }

    @Test
    @Transactional
    public void deviceConfigHasAllProtocolMessagesAndUserHasLevelsButProtocolDoesntSupportTheMessageTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("deviceConfigHasAllProtocolMessagesAndUserHasCorrectLevelTest").add();
        DeviceMessageUserAction[] deviceMessageUserActions = {DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1, DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2};
        deviceConfiguration.setSupportsAllProtocolMessagesWithUserActions(true, deviceMessageUserActions);
        deviceConfiguration.save();

        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString(), Matchers.eq(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1.getPrivilege()))).thenReturn(true);

        assertThat(deviceConfiguration.isAuthorized(DeviceMessageId.PLC_CONFIGURATION_SET_PAN_ID)).isFalse();
    }

    private DeviceConfiguration reloadDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceConfiguration(deviceConfiguration.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + deviceConfiguration.getId()));
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE + "}")
    public void createDataloggerConfigWithoutResourcesTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("createDataLoggerConfigWithoutResourcesTest").dataloggerEnabled(true).add();
        deviceConfiguration.activate();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MULTI_ELEMENT_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE + "}")
    public void createMultiElementConfigWithoutResourcesTest() {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("createMultiElementMeterConfigWithoutResourcesTest").multiElementEnabled(true).add();
        deviceConfiguration.activate();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE + "}")
    public void createDataloggerSlaveConfigWithoutResourcesTest() {
        DeviceType dataloggerSlaveDeviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder("DataloggerSlave", inMemoryPersistence.getDeviceLifeCycleConfigurationService()
                        .findDefaultDeviceLifeCycle().get()).create();
        DeviceConfiguration deviceConfiguration = dataloggerSlaveDeviceType.newConfiguration("createDataloggerSlaveConfigWithoutResourcesTest").add();
        deviceConfiguration.activate();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MULTI_ELEMENT_SUBMETER_AT_LEAST_ONE_DATASOURCE + "}")
    public void createSubmeterConfigWithoutResourcesTest() {
        DeviceType dataloggerSlaveDeviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newMultiElementSlaveDeviceTypeBuilder("Submeter", inMemoryPersistence.getDeviceLifeCycleConfigurationService()
                        .findDefaultDeviceLifeCycle().get()).create();
        DeviceConfiguration deviceConfiguration = dataloggerSlaveDeviceType.newConfiguration("createSubmeterConfigWithoutResourcesTest").add();
        deviceConfiguration.activate();
    }

    @Test
    @Transactional
    public void newConfigurationIsNotSetAsDefault(){
        DeviceConfiguration deviceConfiguration = this.deviceType.newConfiguration("configuration").add();
        assertFalse(deviceConfiguration.isDefault());
    }

    @Test
    @Transactional
    public void onlyOneConfigurationIsDefault(){
        DeviceConfiguration deviceConfiguration1 = this.deviceType.newConfiguration("configuration1").add();
        DeviceConfiguration deviceConfiguration2 = this.deviceType.newConfiguration("configuration2").add();

        deviceConfiguration1.setDefaultStatus(true);
        deviceConfiguration2.setDefaultStatus(true);

        List<DeviceConfigurationImpl> configurations = inMemoryPersistence.getDataModel()
                .query(DeviceConfigurationImpl.class)
                .select(where(DeviceConfigurationImpl.Fields.IS_DEFAULT.fieldName()).isEqualTo(true));

        assertThat(configurations.size()).isEqualTo(1);
        assertEquals("configuration2", configurations.get(0).getName());
    }

}
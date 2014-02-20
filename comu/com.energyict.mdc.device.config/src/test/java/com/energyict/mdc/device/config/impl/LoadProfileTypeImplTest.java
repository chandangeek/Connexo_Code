package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.common.Transactional;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateIntervalWhenLoadProfileTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.UnsupportedIntervalException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LoadProfileTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-13 (14:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileTypeImplTest extends PersistenceTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);
    private static final ObisCode OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");

    private ReadingType readingType;
    private Phenomenon phenomenon;

    @Test
    @Transactional
    public void testCreateWithoutSave() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testCreateWithoutViolations";

        // Business method
        TimeDuration interval = INTERVAL_15_MINUTES;
        LoadProfileType loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(interval);
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testCreateWithoutViolations";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        // Business method
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(interval);
    }

    @Test(expected = DuplicateNameException.class)
    @Transactional
    public void testDuplicateName() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testDuplicateName";
        TimeDuration interval = INTERVAL_15_MINUTES;

        // Setup first LoadProfileType
        LoadProfileType loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        try {
            // Business method
            LoadProfileType loadProfileType2 = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
            loadProfileType2.setDescription("For testing purposes only");
            loadProfileType2.save();
        } catch (DuplicateNameException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_ALREADY_EXISTS);
            throw e;
        }
    }

    @Test(expected = NameIsRequiredException.class)
    @Transactional
    public void testCreateWithoutName() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType(null, OBIS_CODE, interval);
        } catch (NameIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_NAME_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = NameIsRequiredException.class)
    @Transactional
    public void testCreateWithEmptyName() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("", OBIS_CODE, interval);
        } catch (NameIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_NAME_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = ObisCodeIsRequiredException.class)
    @Transactional
    public void testCreateWithoutObisCode() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithoutObisCode", null, interval);
        } catch (ObisCodeIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = IntervalIsRequiredException.class)
    @Transactional
    public void testCreateWithoutInterval() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithoutInterval", OBIS_CODE, null);
        } catch (IntervalIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = IntervalIsRequiredException.class)
    @Transactional
    public void testCreateWithEmptyInterval() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(0, TimeDuration.MINUTES);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithEmptyInterval", OBIS_CODE, interval);
        } catch (IntervalIsRequiredException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithIntervalInWeeks() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(1, TimeDuration.WEEKS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithIntervalInWeeks", OBIS_CODE, interval);
        } catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithNegativeIntervalSeconds() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(-1, TimeDuration.SECONDS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithNegativeIntervalSeconds", OBIS_CODE, interval);
        } catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_MUST_BE_STRICTLY_POSITIVE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithMultipleDays() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.DAYS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithMultipleDays", OBIS_CODE, interval);
        } catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_DAYS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithMultipleMonths() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.MONTHS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithMultipleMonths", OBIS_CODE, interval);
        } catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_MONTHS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithMultipleYears() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.YEARS);

        try {
            // Business method
            deviceConfigurationService.newLoadProfileType("testCreateWithMultipleYears", OBIS_CODE, interval);
        } catch (UnsupportedIntervalException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_YEARS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test
    @Transactional
    public void testUpdateInterval() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testUpdateInterval";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        TimeDuration updatedInterval = new TimeDuration(1, TimeDuration.HOURS);
        loadProfileType.setInterval(updatedInterval);

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(updatedInterval);
    }

    @Test(expected = CannotUpdateIntervalWhenLoadProfileTypeIsInUseException.class)
    @Transactional
    public void testUpdateIntervalWhileInUse() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testUpdateIntervalWhileInUse";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        // Setup LoadProfileType
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Setup DeviceType with a DeviceConfiguration and a LoadProfileSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(loadProfileType);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.add();
        deviceType.save();

        // Business method
        TimeDuration updatedInterval = new TimeDuration(1, TimeDuration.HOURS);
        loadProfileType.setInterval(updatedInterval);

        // Asserts: expecting CannotUpdateIntervalWhenLoadProfileTypeIsInUseException
    }

    @Test
    @Transactional
    public void testCreateWithRegisterMapping() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testCreateWithRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        long registerMappingId;
        this.setupReadingTypeInExistingTransaction();

        // Setup ProductSpec
        ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
        productSpec.save();

        // Setup RegisterMapping
        RegisterMapping registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
        registerMapping.save();

        // Setup LoadProfileType with RegisterMapping
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.addRegisterMapping(registerMapping);

        // Business method
        loadProfileType.save();
        registerMappingId = registerMapping.getId();

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getRegisterMappings()).hasSize(1);
        RegisterMapping registerMapping2 = loadProfileType.getRegisterMappings().get(0);
        assertThat(registerMapping2.getId()).isEqualTo(registerMappingId);
    }

    @Test
    @Transactional
    public void testAddRegisterMapping() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testAddRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();

        // Setup ProductSpec
        ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
        productSpec.save();

        // Setup RegisterMapping
        registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
        registerMapping.save();

        // Setup LoadProfileType without RegisterMapping
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        loadProfileType.addRegisterMapping(registerMapping);

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getRegisterMappings()).containsOnly(registerMapping);
    }

    @Test
    @Transactional
    public void testRemoveRegisterMapping() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testRemoveRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();

        // Setup ProductSpec
        ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
        productSpec.save();

        // Setup RegisterMapping
        registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
        registerMapping.save();

        // Setup LoadProfileType with RegisterMapping
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();

        // Business method
        loadProfileType.removeRegisterMapping(registerMapping);

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getRegisterMappings()).isEmpty();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testRemoveRegisterMappingWhileInUse() {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testRemoveRegisterMappingWhileInUse";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        this.setupPhenomenaInExistingTransaction();
        this.setupReadingTypeInExistingTransaction();

        // Setup ProductSpec
        ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
        productSpec.save();

        // Setup RegisterMapping
        registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
        registerMapping.save();

        // Setup LoadProfileType with RegisterMapping
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();

        // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.addRegisterMapping(registerMapping);
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
        configurationBuilder.add();
        deviceType.save();

        try {
            // Business method
            loadProfileType.removeRegisterMapping(registerMapping);
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC);
            throw e;
        }
    }

    @Test
    @Transactional
    public void testSimpleDelete() throws SQLException {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testSimpleDelete";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        loadProfileType.delete();

        // Asserts
        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
    }

    @Test
    @Transactional
    public void testSimpleDeleteWithRegisterMappings() throws SQLException {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testSimpleDeleteWithRegisterMapping";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterMapping registerMapping;
        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();

        // Setup ProductSpec
        ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
        productSpec.save();

        // Setup RegisterMapping
        registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
        registerMapping.save();

        // Setup LoadProfileType with RegisterMapping
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();

        // Business method
        loadProfileType.delete();

        // Asserts
        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
        this.assertRegisterMappingsDoNotExist(loadProfileType);
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testDeleteWhenInUseByDeviceType() throws SQLException {
        DeviceConfigurationServiceImpl deviceConfigurationService = inMemoryPersistence.getDeviceConfigurationService();
        String loadProfileTypeName = "testDeleteWhenInUseByDeviceType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        this.setupPhenomenaInExistingTransaction();
        this.setupReadingTypeInExistingTransaction();

        // Setup ProductSpec
        ProductSpec productSpec = deviceConfigurationService.newProductSpec(this.readingType);
        productSpec.save();

        // Setup RegisterMapping
        RegisterMapping registerMapping = deviceConfigurationService.newRegisterMapping("testCreateWithRegisterMapping", OBIS_CODE, productSpec);
        registerMapping.save();

        // Setup LoadProfileType
        loadProfileType = deviceConfigurationService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.addRegisterMapping(registerMapping);
        loadProfileType.save();

        // Setup DeviceType with a DeviceConfiguration and LoadProfileSpec and ChannelSpec that uses the LoadProfileType
        DeviceType deviceType = deviceConfigurationService.newDeviceType("testUpdateIntervalWhileInUse", this.deviceProtocolPluggableClass);
        deviceType.addRegisterMapping(registerMapping);
        deviceType.addLoadProfileType(loadProfileType);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder configurationBuilder = deviceType.newConfiguration("Configuration");
        LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = configurationBuilder.newLoadProfileSpec(loadProfileType);
        configurationBuilder.newChannelSpec(registerMapping, this.phenomenon, loadProfileSpecBuilder);
        configurationBuilder.add();


        // Business method
        try {
            loadProfileType.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES);
            throw e;
        }

        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
    }

    private void assertLoadProfileTypeDoesNotExist(LoadProfileType loadProfileType) throws SQLException {
        List<LoadProfileType> loadProfileTypes = inMemoryPersistence.getDeviceConfigurationService().getDataModel().mapper(LoadProfileType.class).find("id", loadProfileType.getId());
        assertThat(loadProfileTypes).as("Was not expecting to find any LoadProfileTypes after deletinon.").isEmpty();
    }

    private void assertRegisterMappingsDoNotExist(LoadProfileType loadProfileType) throws SQLException {
        List<LoadProfileTypeRegisterMappingUsage> usages = inMemoryPersistence.getDeviceConfigurationService().getDataModel().mapper(LoadProfileTypeRegisterMappingUsage.class).find("LOADPROFILETYPEID", loadProfileType.getId());
        assertThat(usages).as("Was not expecting to find any register mapping usages for LoadProfileType {0} after deletion", loadProfileType.getName()).isEmpty();
    }

    private void setupPhenomenaInExistingTransaction() {
        this.phenomenon = inMemoryPersistence.getDeviceConfigurationService().newPhenomenon(DeviceTypeImplTest.class.getSimpleName(), Unit.get("kWh"));
        this.phenomenon.save();
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}
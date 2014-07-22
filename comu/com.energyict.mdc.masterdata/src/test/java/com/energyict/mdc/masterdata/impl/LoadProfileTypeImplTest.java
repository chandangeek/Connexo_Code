package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.IntervalIsRequiredException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.UnsupportedIntervalException;
import com.google.common.base.Optional;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

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
    private Unit unit;
    private Phenomenon phenomenon;

    @Test
    @Transactional
    public void testCreateWithoutSave() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testCreateWithoutViolations";

        // Business method
        TimeDuration interval = INTERVAL_15_MINUTES;
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
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
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testCreateWithoutViolations";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        // Business method
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getName()).isEqualTo(loadProfileTypeName);
        assertThat(loadProfileType.getDescription()).isNotEmpty();
        assertThat(loadProfileType.getObisCode()).isEqualTo(OBIS_CODE);
        assertThat(loadProfileType.getInterval()).isEqualTo(interval);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
    public void testDuplicateName() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testDuplicateName";
        TimeDuration interval = INTERVAL_15_MINUTES;

        // Setup first LoadProfileType
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        LoadProfileType loadProfileType2 = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType2.setDescription("For testing purposes only");
        loadProfileType2.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", strict = false)
    public void testCreateWithoutName() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(null, OBIS_CODE, interval);

        // Business method
        loadProfileType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testCreateWithEmptyName() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("", OBIS_CODE, interval);

        // Business method
        loadProfileType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    public void testCreateWithoutObisCode() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("testCreateWithoutObisCode", null, interval);

        // Business method
        loadProfileType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test(expected = IntervalIsRequiredException.class)
    @Transactional
    public void testCreateWithoutInterval() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithoutInterval", OBIS_CODE, null);
        }
        catch (IntervalIsRequiredException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = IntervalIsRequiredException.class)
    @Transactional
    public void testCreateWithEmptyInterval() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = new TimeDuration(0, TimeDuration.MINUTES);

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithEmptyInterval", OBIS_CODE, interval);
        }
        catch (IntervalIsRequiredException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithIntervalInWeeks() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = new TimeDuration(1, TimeDuration.WEEKS);

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithIntervalInWeeks", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithNegativeIntervalSeconds() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = new TimeDuration(-1, TimeDuration.SECONDS);

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithNegativeIntervalSeconds", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_MUST_BE_STRICTLY_POSITIVE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithMultipleDays() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.DAYS);

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithMultipleDays", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_DAYS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithMultipleMonths() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.MONTHS);

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithMultipleMonths", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_MONTHS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test(expected = UnsupportedIntervalException.class)
    @Transactional
    public void testCreateWithMultipleYears() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = new TimeDuration(2, TimeDuration.YEARS);

        try {
            // Business method
            masterDataService.newLoadProfileType("testCreateWithMultipleYears", OBIS_CODE, interval);
        }
        catch (UnsupportedIntervalException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.INTERVAL_IN_YEARS_MUST_BE_ONE);
            throw e;
        }
    }

    @Test
    @Transactional
    public void testUpdateInterval() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testUpdateInterval";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
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

    @Test
    @Transactional
    public void testCreateWithRegisterType() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testCreateWithRegisterType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        this.setupPhenomenaInExistingTransaction();
        this.setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.createChannelTypeForRegisterType(registerType);

        // Business method
        loadProfileType.save();

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getChannelTypes()).hasSize(1);
        Optional<ChannelType> channelTypeByTemplateRegisterAndInterval = masterDataService.findChannelTypeByTemplateRegisterAndInterval(registerType, interval);

        MeasurementType measurementType2 = loadProfileType.getChannelTypes().get(0);
        assertThat(measurementType2.getId()).isEqualTo(channelTypeByTemplateRegisterAndInterval.get().getId());
    }

    @Test
    @Transactional
    public void testAddRegisterType() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testAddRegisterType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        this.setupPhenomenaInExistingTransaction();
        this.setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType without RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        loadProfileType.createChannelTypeForRegisterType(registerType);

        // Asserts
        assertThat(loadProfileType).isNotNull();
        Optional<ChannelType> channelTypeByTemplateRegisterAndInterval = masterDataService.findChannelTypeByTemplateRegisterAndInterval(registerType, interval);
        assertThat(loadProfileType.getChannelTypes()).hasSize(1);
        assertThat(loadProfileType.getChannelTypes().get(0).getId()).isEqualTo(channelTypeByTemplateRegisterAndInterval.get().getId());
    }

    @Test
    @Transactional
    public void testRemoveRegisterType() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testRemoveRegisterType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();
        this.setupPhenomenaInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.createChannelTypeForRegisterType(registerType);
        loadProfileType.save();

        // Business method
        loadProfileType.removeChannelType(loadProfileType.getChannelTypes().get(0));

        // Asserts
        assertThat(loadProfileType).isNotNull();
        assertThat(loadProfileType.getChannelTypes()).isEmpty();
    }

    @Test
    @Transactional
    public void testSimpleDelete() throws SQLException {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testSimpleDelete";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        loadProfileType.delete();

        // Asserts
        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
    }

    @Test
    @Transactional
    public void testSimpleDeleteWithChannelTypes() throws SQLException {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testSimpleDeleteWithChannelTypes";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterType registerType;
        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();
        this.setupPhenomenaInExistingTransaction();

        // Setup RegisterType
        registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.createChannelTypeForRegisterType(registerType);
        loadProfileType.save();

        // Business method
        loadProfileType.delete();

        // Asserts
        this.assertLoadProfileTypeDoesNotExist(loadProfileType);
        this.assertRegisterTypessDoNotExist(loadProfileType);
    }

    @Test
    @Transactional
    public void updateChannelTypesWhenIntervalChangesTest() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "updateChannelTypesWhenIntervalChangesTest";
        TimeDuration interval = INTERVAL_15_MINUTES;

        RegisterType registerType;
        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();
        this.setupPhenomenaInExistingTransaction();

        // Setup RegisterType
        registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval);
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.createChannelTypeForRegisterType(registerType);
        loadProfileType.save();

        LoadProfileType reloadedLoadProfile = masterDataService.findLoadProfileType(loadProfileType.getId()).get();
        TimeDuration newInterval = TimeDuration.days(1);
        reloadedLoadProfile.setInterval(newInterval);
        reloadedLoadProfile.save();

        LoadProfileType finalLoadProfile = masterDataService.findLoadProfileType(reloadedLoadProfile.getId()).get();

        assertThat(finalLoadProfile.getChannelTypes()).hasSize(1);
        assertThat(finalLoadProfile.getInterval()).isEqualTo(newInterval);
        assertThat(finalLoadProfile.getChannelTypes().get(0).getInterval()).isEqualTo(newInterval);
        assertThat(inMemoryPersistence.getMasterDataService().findAllChannelTypes().find()).hasSize(2); // the old one should still exist
    }

    private void assertLoadProfileTypeDoesNotExist(LoadProfileType loadProfileType) {
        List<LoadProfileType> loadProfileTypes = PersistenceTest.inMemoryPersistence.getMasterDataService().getDataModel().mapper(LoadProfileType.class).find("id", loadProfileType.getId());
        assertThat(loadProfileTypes).as("Was not expecting to find any LoadProfileTypes after deletinon.").isEmpty();
    }

    private void assertRegisterTypessDoNotExist(LoadProfileType loadProfileType) {
        List<LoadProfileTypeChannelTypeUsage> usages = PersistenceTest.inMemoryPersistence.getMasterDataService()
                .getDataModel()
                .mapper(LoadProfileTypeChannelTypeUsage.class)
                .find("LOADPROFILETYPEID", loadProfileType.getId());
        assertThat(usages).as("Was not expecting to find any register mapping usages for LoadProfileType {0} after deletion", loadProfileType.getName()).isEmpty();
    }

    private void setupPhenomenaInExistingTransaction() {
        this.unit = Unit.get("kWh");
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        Optional<Phenomenon> xPhenomenon = masterDataService.findPhenomenonByUnit(this.unit);
        if (!xPhenomenon.isPresent()) {
            this.phenomenon = masterDataService.newPhenomenon(LoadProfileTypeImplTest.class.getSimpleName(), this.unit);
            this.phenomenon.save();
        }
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FORWARD)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .period(TimeAttribute.NOTAPPLICABLE)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        this.readingType = PersistenceTest.inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}
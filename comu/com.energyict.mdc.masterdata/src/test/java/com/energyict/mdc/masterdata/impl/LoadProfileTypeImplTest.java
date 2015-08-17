package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesNotMappableToLoadProfileTypeIntervalException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoadProfileTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-13 (14:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileTypeImplTest extends PersistenceTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
    private static final ObisCode OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");

    private ReadingType readingType;
    private Unit unit = Unit.get("kWh");

    @Test
    @Transactional
    public void testCreateWithoutSave() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testCreateWithoutViolations";
        setupReadingTypeInExistingTransaction();
        // Business method
        TimeDuration interval = INTERVAL_15_MINUTES;
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
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
        setupReadingTypeInExistingTransaction();

        LoadProfileType loadProfileType;
        // Business method
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
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
        setupReadingTypeInExistingTransaction();

        // Setup first LoadProfileType
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        LoadProfileType loadProfileType2 = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType2.setDescription("For testing purposes only");
        loadProfileType2.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", strict = false)
    public void testCreateWithoutName() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        setupReadingTypeInExistingTransaction();
        TimeDuration interval = INTERVAL_15_MINUTES;
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(null, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));

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
        setupReadingTypeInExistingTransaction();
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("", OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));

        // Business method
        loadProfileType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testCreateWithRegisterTypeThatDoestHaveCorrespondingIntervalReadingType() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = INTERVAL_15_MINUTES;
        Currency currency = Currency.getInstance("XXX");    // No currency
        ObisCode badObisCode = ObisCode.fromString("1.1.1.1.1.1");
        String myBadReadingTypeAlias = "myBadReadingTypeAlias";
        ReadingType badReadingType = mock(ReadingType.class);
        when(badReadingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(badReadingType.getAccumulation()).thenReturn(Accumulation.NOTAPPLICABLE);
        when(badReadingType.getAggregate()).thenReturn(Aggregate.NOTAPPLICABLE);
        when(badReadingType.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(badReadingType.getCpp()).thenReturn(0);
        when(badReadingType.getCurrency()).thenReturn(currency);
        when(badReadingType.getFlowDirection()).thenReturn(FlowDirection.NOTAPPLICABLE);
        when(badReadingType.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(badReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(badReadingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(badReadingType.getMeasurementKind()).thenReturn(MeasurementKind.NOTAPPLICABLE);
        when(badReadingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(badReadingType.getConsumptionTier()).thenReturn(0);
        when(badReadingType.getTou()).thenReturn(0);
        when(badReadingType.getAliasName()).thenReturn(myBadReadingTypeAlias);
        String expectedReadingType_mRID = "0.0.2.0.0.2.0.0.0.0.0.0.0.0.0.0.0.0";
        RegisterType badRegisterType = mock(RegisterType.class);
        /* Mock bad register type (with bad reading type and obis code) for which:
         * MasterDataService#findChannelTypeByTemplateRegisterAndInterval(RegisterType, TimeDuration) returns empty Optional
         * MdcReadingTypeUtilService#getIntervalAppliedReadingType(ReadingType, TimeDuration, ObisCode) returns empty Optional
         *    because the RegisterType's obiscode cannot be mapped to the LoadProfile's interval.
         */
        when(badRegisterType.getId()).thenReturn(-1L); // Make sure that MasterDataService#findChannelTypeByTemplateRegisterAndInterval(RegisterType, TimeDuration) returns empty Optional
        when(badRegisterType.getReadingType()).thenReturn(badReadingType);
        when(badRegisterType.getObisCode()).thenReturn(badObisCode);
        // Now setup a proper reading type
        setupReadingTypeInExistingTransaction();
        RegisterType goodRegisterType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Business method
        masterDataService.newLoadProfileType("Test", OBIS_CODE, interval, Arrays.asList(goodRegisterType, badRegisterType));

        Optional<ReadingType> newReadingType = PersistenceTest.inMemoryPersistence.getMeteringService().getReadingType(expectedReadingType_mRID);
        assertThat(newReadingType.isPresent()).isTrue();
    }

    @Test(expected = RegisterTypesNotMappableToLoadProfileTypeIntervalException.class)
    @Transactional
    public void testCreateWithNonApplicableCommodityReadingType() {
        this.createWithInCompatibleCommodityReadingType(Commodity.NOTAPPLICABLE);
    }

    @Test(expected = RegisterTypesNotMappableToLoadProfileTypeIntervalException.class)
    @Transactional
    public void testCreateWithDeviceCommodityReadingType() {
        this.createWithInCompatibleCommodityReadingType(Commodity.DEVICE);
    }

    @Test(expected = RegisterTypesNotMappableToLoadProfileTypeIntervalException.class)
    @Transactional
    public void testCreateWithCommunicationCommodityReadingType() {
        this.createWithInCompatibleCommodityReadingType(Commodity.COMMUNICATION);
    }

    private void createWithInCompatibleCommodityReadingType(Commodity commodity) {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = INTERVAL_15_MINUTES;
        Currency currency = Currency.getInstance("XXX");    // No currency
        ObisCode badObisCode = ObisCode.fromString("1.1.1.1.1.1");
        String myBadReadingTypeAlias = "NOTAPPLICABLE_ReadingTypeAlias";
        ReadingType badReadingType = mock(ReadingType.class);
        when(badReadingType.getCommodity()).thenReturn(commodity);
        when(badReadingType.getAccumulation()).thenReturn(Accumulation.NOTAPPLICABLE);
        when(badReadingType.getAggregate()).thenReturn(Aggregate.NOTAPPLICABLE);
        when(badReadingType.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(badReadingType.getCpp()).thenReturn(0);
        when(badReadingType.getCurrency()).thenReturn(currency);
        when(badReadingType.getFlowDirection()).thenReturn(FlowDirection.NOTAPPLICABLE);
        when(badReadingType.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(badReadingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(badReadingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(badReadingType.getMeasurementKind()).thenReturn(MeasurementKind.NOTAPPLICABLE);
        when(badReadingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(badReadingType.getConsumptionTier()).thenReturn(0);
        when(badReadingType.getTou()).thenReturn(0);
        when(badReadingType.getAliasName()).thenReturn(myBadReadingTypeAlias);
        RegisterType badRegisterType = mock(RegisterType.class);
        /* Mock bad register type (with bad reading type and obis code) for which:
         * MasterDataService#findChannelTypeByTemplateRegisterAndInterval(RegisterType, TimeDuration) returns empty Optional
         * MdcReadingTypeUtilService#getIntervalAppliedReadingType(ReadingType, TimeDuration, ObisCode) returns empty Optional
         *    because the RegisterType's obiscode cannot be mapped to the LoadProfile's interval.
         */
        when(badRegisterType.getId()).thenReturn(-1L); // Make sure that MasterDataService#findChannelTypeByTemplateRegisterAndInterval(RegisterType, TimeDuration) returns empty Optional
        when(badRegisterType.getReadingType()).thenReturn(badReadingType);
        when(badRegisterType.getObisCode()).thenReturn(badObisCode);

        // Business method
        masterDataService.newLoadProfileType("Test", OBIS_CODE, interval, Arrays.asList(badRegisterType));

        // Asserts: see expected exception rule on calling methods
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_REGISTER_TYPE_REQUIRED + "}")
    public void testCreateWithoutRegisterType() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        TimeDuration interval = INTERVAL_15_MINUTES;

        setupReadingTypeInExistingTransaction();
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("Test", OBIS_CODE, interval, Collections.emptyList());

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
        setupReadingTypeInExistingTransaction();
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("testCreateWithoutObisCode", null, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));

        // Business method
        loadProfileType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CHANNEL_TYPE_INTERVAL_IS_REQUIRED + "}", strict = false)
    public void testCreateWithoutInterval() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();

        setupReadingTypeInExistingTransaction();
        // Business method
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType("testCreateWithoutInterval", OBIS_CODE, null, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType.save();
    }

    @Test
    @Transactional
    public void testUpdateInterval() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testUpdateInterval";
        TimeDuration interval = INTERVAL_15_MINUTES;
        setupReadingTypeInExistingTransaction();
        LoadProfileType loadProfileType;
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        // Business method
        TimeDuration updatedInterval = new TimeDuration(1, TimeDuration.TimeUnit.HOURS);
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
        this.setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType.setDescription("For testing purposes only");

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
        this.setupReadingTypeInExistingTransaction();

        // Setup LoadProfileType without RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType.setDescription("For testing purposes only");
        loadProfileType.save();

        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED)
                .flow(FlowDirection.REVERSE)
                .measure(ENERGY)
                .in(KILO, WATTHOUR)
                .period(TimeAttribute.NOTAPPLICABLE)
                .accumulate(Accumulation.BULKQUANTITY)
                .code();
        ReadingType readingType2 = PersistenceTest.inMemoryPersistence.getMeteringService().getReadingType(code).get();

        // Business method
        RegisterType templateRegister = masterDataService.findRegisterTypeByReadingType(readingType2).get();
        loadProfileType.createChannelTypeForRegisterType(templateRegister);

        // Asserts
        assertThat(loadProfileType).isNotNull();
        Optional<ChannelType> channelTypeByTemplateRegisterAndInterval = masterDataService.findChannelTypeByTemplateRegisterAndInterval(templateRegister, interval);
        assertThat(loadProfileType.getChannelTypes()).hasSize(2);
        assertThat(loadProfileType.getChannelTypes().get(1).getId()).isEqualTo(channelTypeByTemplateRegisterAndInterval.get().getId());
    }

    @Test
    @Transactional
    public void testRemoveRegisterType() {
        MasterDataServiceImpl masterDataService = PersistenceTest.inMemoryPersistence.getMasterDataService();
        String loadProfileTypeName = "testRemoveRegisterType";
        TimeDuration interval = INTERVAL_15_MINUTES;

        LoadProfileType loadProfileType;
        this.setupReadingTypeInExistingTransaction();

        // Setup RegisterType
        RegisterType registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(registerType));
        loadProfileType.setDescription("For testing purposes only");
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
        setupReadingTypeInExistingTransaction();
        LoadProfileType loadProfileType;
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
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

        // Setup RegisterType
        registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(masterDataService.findRegisterTypeByReadingType(readingType).get()));
        loadProfileType.setDescription("For testing purposes only");
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

        // Setup RegisterType
        registerType = masterDataService.findRegisterTypeByReadingType(readingType).get();

        // Setup LoadProfileType with RegisterType
        loadProfileType = masterDataService.newLoadProfileType(loadProfileTypeName, OBIS_CODE, interval, Arrays.asList(registerType));
        loadProfileType.setDescription("For testing purposes only");
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
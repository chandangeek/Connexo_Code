package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.google.common.base.Optional;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the peristence aspects of the {@link RegisterTypeImpl} component
 * as provided by the {@link MasterDataServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterTypeImplTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.MINUTES);

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    private LoadProfileType loadProfileType;
    private ReadingType readingType1;
    private ReadingType readingType2;
    private Phenomenon phenomenon1;
    private Phenomenon phenomenon2;
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private Unit unit1;
    private Unit unit2;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("PersistenceTest.mdc.masterdata", false, false);
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() {
        String registerTypeName = "testCreateWithoutViolations";
        MeasurementType measurementType;
        this.setupProductSpecsInExistingTransaction();

        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts
        assertThat(measurementType).isNotNull();
        assertThat(measurementType.getId()).isGreaterThan(0);
        assertThat(measurementType.getName()).isEqualTo(registerTypeName);
        assertThat(measurementType.getDescription()).isNotEmpty();
        assertThat(measurementType.getReadingType()).isEqualTo(this.readingType1);
        assertThat(measurementType.getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    public void testFindAfterCreation() {
        String registerTypeName = "testFindAfterCreation";
        this.setupProductSpecsInExistingTransaction();

        RegisterType registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
        registerType.setDescription("For testing purposes only");
        registerType.save();

        // Business method
        Optional<RegisterType> registerType2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);

        // Asserts
        assertThat(registerType2.isPresent()).isTrue();
        assertThat(registerType2.get().getName()).isEqualTo(registerTypeName);
        assertThat(registerType2.get().getDescription()).isNotEmpty();
        assertThat(registerType2.get().getReadingType()).isEqualTo(this.readingType1);
        assertThat(registerType2.get().getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", strict = false)
    public void testCreateWithoutName() {
        this.setupProductSpecsInExistingTransaction();

        MeasurementType measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(null, obisCode1, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");

        // Business method
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testCreateWithEmptyName() {
        this.setupProductSpecsInExistingTransaction();

        MeasurementType measurementType = inMemoryPersistence.getMasterDataService().newRegisterType("", obisCode1, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");

        // Business method
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_DUPLICATE_READING_TYPE + "}")
    public void testCreateWithDuplicateReadingType() {
        this.setupProductSpecsInExistingTransaction();

        MeasurementType measurementType = inMemoryPersistence.getMasterDataService().newRegisterType("hello world", obisCode1, unit1, readingType1, 1);
        measurementType.save();
        MeasurementType measurementType2 = inMemoryPersistence.getMasterDataService().newRegisterType("hello again", obisCode1, unit1, readingType1, 2);
        measurementType2.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_OBIS_CODE_IS_REQUIRED + "}")
    public void testCreateWithoutObisCode() {
        String registerTypeName = "testCreateWithoutObisCode";
        this.setupProductSpecsInExistingTransaction();

        // Business method
        MeasurementType measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, null, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_UNIT_IS_REQUIRED + "}")
    public void testCreateWithoutUnit() {
        setupProductSpecsInExistingTransaction();
        String registerTypeName = "testCreateWithoutProductSpec";
        MeasurementType measurementType;
        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, null, readingType1, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_READING_TYPE_IS_REQUIRED + "}")
    public void testCreateWithoutReadingType() {
        setupProductSpecsInExistingTransaction();
        String registerTypeName = "testCreateWithoutProductSpec";
        MeasurementType measurementType;
        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, null, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_TIMEOFUSE_TOO_SMALL + "}")
    public void testCreateWithInvalidTimeOfUse() {
        setupProductSpecsInExistingTransaction();
        String registerTypeName = "testCreateWithoutProductSpec";
        MeasurementType measurementType;
        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, -1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testUpdateName() {
        String registerTypeName = "testUpdateObisCode";
        MeasurementType measurementType;
        this.setupProductSpecsInExistingTransaction();

        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Business method
        String updatedName = registerTypeName + "-Updated";
        measurementType.setName(updatedName);
        measurementType.save();

        // Asserts
        assertThat(measurementType.getName()).isEqualTo(updatedName);
    }

    @Test
    @Transactional
    public void testUpdateObisCodeAndUnit() {
        String registerTypeName = "testUpdateObisCode";
        MeasurementType measurementType;
        this.setupProductSpecsInExistingTransaction();

        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Business method
        measurementType.setObisCode(obisCode2);
        measurementType.setUnit(unit2);
        measurementType.save();

        // Asserts
        assertThat(measurementType.getObisCode()).isEqualTo(obisCode2);
        assertThat(measurementType.getName()).isEqualTo(registerTypeName);
        assertThat(measurementType.getDescription()).isNotEmpty();
        assertThat(measurementType.getPhenomenon()).isEqualToComparingOnlyGivenFields(this.phenomenon2, "name", "unit");
    }


    @Test
    @Transactional
    public void testDelete() {
        String registerTypeName = "testDelete";
        MeasurementType measurementType;
        this.setupProductSpecsInExistingTransaction();

        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        long id = measurementType.getId();
        measurementType.delete();

        // Asserts
        Optional<RegisterType> expectedNull = inMemoryPersistence.getMasterDataService().findRegisterType(id);
        assertThat(expectedNull.isPresent()).isFalse();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByLoadProfileType() {
        String registerTypeName = "testCannotDeleteWhenUsedByLoadProfileType";
        RegisterType registerType;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterType
        registerType = inMemoryPersistence.getMasterDataService().newRegisterType(registerTypeName, obisCode1, unit1, readingType1, 1);
        registerType.setDescription("For testing purposes only");
        registerType.save();

        // Use it in a LoadProfileType
        this.setupLoadProfileTypesInExistingTransaction(registerType);


        try {
        registerType.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            Assertions.assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE);
            throw e;
        }
    }

    private void setupProductSpecsInExistingTransaction() {
        this.setupReadingTypesInExistingTransaction();
        this.setupPhenomenaInExistingTransaction();
    }

    private void setupReadingTypesInExistingTransaction() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType1).getObisCode();
        String code2 = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        this.obisCode2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFor(readingType2).getObisCode();
    }

    private void setupLoadProfileTypesInExistingTransaction() {
        this.loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(RegisterTypeImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
    }

    private void setupLoadProfileTypesInExistingTransaction(RegisterType registerType) {
        this.setupLoadProfileTypesInExistingTransaction();
        this.loadProfileType.createChannelTypeForRegisterType(registerType);
    }

    private void setupPhenomenaInExistingTransaction() {
        this.unit1 = Unit.get("kWh");
        this.phenomenon1 = this.createPhenomenonIfMissing(this.unit1, RegisterTypeImplTest.class.getSimpleName() + "1");
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = this.createPhenomenonIfMissing(this.unit2, RegisterTypeImplTest.class.getSimpleName() + "2");
    }

    private Phenomenon createPhenomenonIfMissing(Unit unit, String name) {
        Optional<Phenomenon> phenomenonByUnit = inMemoryPersistence.getMasterDataService().findPhenomenonByUnit(unit);
        if (!phenomenonByUnit.isPresent()) {
            Phenomenon phenomenon = inMemoryPersistence.getMasterDataService().newPhenomenon(name, unit);
            phenomenon.save();
            return phenomenon;
        }
        else {
            return phenomenonByUnit.get();
        }
    }

}
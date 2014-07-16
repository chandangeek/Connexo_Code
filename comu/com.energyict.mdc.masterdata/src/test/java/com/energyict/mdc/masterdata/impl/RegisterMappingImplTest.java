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
import com.energyict.mdc.masterdata.RegisterMapping;
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
 * Tests the peristence aspects of the {@link RegisterMappingImpl} component
 * as provided by the {@link MasterDataServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterMappingImplTest {

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
        String registerMappingName = "testCreateWithoutViolations";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Business method
        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts
        assertThat(registerMapping).isNotNull();
        assertThat(registerMapping.getId()).isGreaterThan(0);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getReadingType()).isEqualTo(this.readingType1);
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    public void testFindAfterCreation() {
        String registerMappingName = "testFindAfterCreation";
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Business method
        Optional<RegisterMapping> registerMapping2 = inMemoryPersistence.getMasterDataService().findRegisterMappingByName(registerMappingName);

        // Asserts
        assertThat(registerMapping2.isPresent()).isTrue();
        assertThat(registerMapping2.get().getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping2.get().getDescription()).isNotEmpty();
        assertThat(registerMapping2.get().getReadingType()).isEqualTo(this.readingType1);
        assertThat(registerMapping2.get().getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", strict = false)
    public void testCreateWithoutName() {
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(null, obisCode1,  unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");

        // Business method
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testCreateWithEmptyName() {
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping("", obisCode1,  unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");

        // Business method
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_MAPPING_DUPLICATE_READING_TYPE + "}")
    public void testCreateWithDuplicateReadingType() {
        this.setupProductSpecsInExistingTransaction();

        RegisterMapping registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping("hello world", obisCode1,  unit1, readingType1, 1);
        registerMapping.save();
        RegisterMapping registerMapping2 = inMemoryPersistence.getMasterDataService().newRegisterMapping("hello again", obisCode1,  unit1, readingType1, 2);
        registerMapping2.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED + "}")
    public void testCreateWithoutObisCode() {
        String registerMappingName = "testCreateWithoutObisCode";
        this.setupProductSpecsInExistingTransaction();

        // Business method
        RegisterMapping registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, null,  unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_MAPPING_UNIT_IS_REQUIRED + "}")
    public void testCreateWithoutUnit() {
        setupProductSpecsInExistingTransaction();
        String registerMappingName = "testCreateWithoutProductSpec";
        RegisterMapping registerMapping;
        // Business method
        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, null, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_MAPPING_READING_TYPE_IS_REQUIRED + "}")
    public void testCreateWithoutReadingType() {
        setupProductSpecsInExistingTransaction();
        String registerMappingName = "testCreateWithoutProductSpec";
        RegisterMapping registerMapping;
        // Business method
        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, null, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_MAPPING_TIMEOFUSE_TOO_SMALL + "}")
    public void testCreateWithInvalidTimeOfUse() {
        setupProductSpecsInExistingTransaction();
        String registerMappingName = "testCreateWithoutProductSpec";
        RegisterMapping registerMapping;
        // Business method
        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, -1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testUpdateName() {
        String registerMappingName = "testUpdateObisCode";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1,  unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Business method
        String updatedName = registerMappingName + "-Updated";
        registerMapping.setName(updatedName);
        registerMapping.save();

        // Asserts
        assertThat(registerMapping.getName()).isEqualTo(updatedName);
    }

    @Test
    @Transactional
    public void testUpdateObisCodeAndUnit() {
        String registerMappingName = "testUpdateObisCode";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1,  unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Business method
        registerMapping.setObisCode(obisCode2);
        registerMapping.setUnit(unit2);
        registerMapping.save();

        // Asserts
        assertThat(registerMapping.getObisCode()).isEqualTo(obisCode2);
        assertThat(registerMapping.getName()).isEqualTo(registerMappingName);
        assertThat(registerMapping.getDescription()).isNotEmpty();
        assertThat(registerMapping.getPhenomenon()).isEqualToComparingOnlyGivenFields(this.phenomenon2, "name", "unit");
    }


    @Test
    @Transactional
    public void testDelete() {
        String registerMappingName = "testDelete";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Business method
        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1,  unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        long id = registerMapping.getId();
        registerMapping.delete();

        // Asserts
        Optional<RegisterMapping> expectedNull = inMemoryPersistence.getMasterDataService().findRegisterMapping(id);
        assertThat(expectedNull.isPresent()).isFalse();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByLoadProfileType() {
        String registerMappingName = "testCannotDeleteWhenUsedByLoadProfileType";
        RegisterMapping registerMapping;
        this.setupProductSpecsInExistingTransaction();

        // Create the RegisterMapping
        registerMapping = inMemoryPersistence.getMasterDataService().newRegisterMapping(registerMappingName, obisCode1, unit1, readingType1, 1);
        registerMapping.setDescription("For testing purposes only");
        registerMapping.save();

        // Use it in a LoadProfileType
        this.setupLoadProfileTypesInExistingTransaction(registerMapping);


        try {
        registerMapping.delete();
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
        this.loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(RegisterMappingImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES);
        this.loadProfileType.save();
    }

    private void setupLoadProfileTypesInExistingTransaction(RegisterMapping registerMapping) {
        this.setupLoadProfileTypesInExistingTransaction();
        this.loadProfileType.addRegisterMapping(registerMapping);
    }

    private void setupPhenomenaInExistingTransaction() {
        this.unit1 = Unit.get("kWh");
        this.phenomenon1 = this.createPhenomenonIfMissing(this.unit1, RegisterMappingImplTest.class.getSimpleName() + "1");
        this.unit2 = Unit.get("MWh");
        this.phenomenon2 = this.createPhenomenonIfMissing(this.unit2, RegisterMappingImplTest.class.getSimpleName() + "2");
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
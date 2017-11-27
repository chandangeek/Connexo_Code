/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
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
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.obis.ObisCode;
import com.energyict.cbo.Unit;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * Tests the persistence aspects of the {@link RegisterTypeImpl} component
 * as provided by the {@link MasterDataServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterTypeImplTest {

    private static final TimeDuration INTERVAL_15_MINUTES = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
    private static final String[] availableReadingTypes = new String[]{"0.0.2.0.0.2.0.0.0.0.0.0.0.0.0.0.0.0",
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",
            "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0",
            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.111.0",
            "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0",
            "0.0.0.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0",
            "11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
            "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.840"};

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
    private ObisCode obisCode1;
    private ObisCode obisCode2;
    private final Unit unit1 = Unit.get("kWh");
    private final Unit unit2 = Unit.get("MWh");

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("mdc.masterdata.registertype", false, false,
                availableReadingTypes);
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
    public void testFindAfterCreation() {
        this.setupReadingTypesInExistingTransaction();

        RegisterType registerType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1).get();
        registerType.setDescription("For testing purposes only");
        registerType.save();

        // Business method
        Optional<RegisterType> registerType2 = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1);

        // Asserts
        assertThat(registerType2.isPresent()).isTrue();
        assertThat(registerType2.get().getDescription()).isNotEmpty();
        assertThat(registerType2.get().getReadingType()).isEqualTo(this.readingType1);
        assertThat(registerType2.get().getObisCode()).isEqualTo(obisCode1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_DUPLICATE_READING_TYPE + "}")
    public void testCreateWithDuplicateReadingType() {
        this.setupReadingTypesInExistingTransaction();

        MeasurementType measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType1, obisCode1);
        measurementType.save();
        MeasurementType measurementType2 = inMemoryPersistence.getMasterDataService().newRegisterType(readingType1, obisCode1);
        measurementType2.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", strict = false)
    public void testCreateWithoutObisCode() {
        String registerTypeName = "testCreateWithoutObisCode";
        ReadingType readingType = inMemoryPersistence.getMeteringService()
                .getReadingType("0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.840")
                .get();

        // Business method
        MeasurementType measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, null);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_READING_TYPE_IS_REQUIRED + "}")
    public void testCreateWithoutReadingType() {
        setupReadingTypesInExistingTransaction();
        String registerTypeName = "testCreateWithoutProductSpec";
        MeasurementType measurementType;
        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().newRegisterType(null, obisCode1);
        measurementType.setDescription("For testing purposes only");
        measurementType.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void testUpdateObisCode() {
        MeasurementType measurementType;
        this.setupReadingTypesInExistingTransaction();

        measurementType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1).get();

        // Business method
        measurementType.setObisCode(obisCode2);
        measurementType.save();

        // Asserts
        assertThat(measurementType.getObisCode()).isEqualTo(obisCode2);
    }


    @Test
    @Transactional
    public void testDelete() {
        String registerTypeName = "testDelete";
        MeasurementType measurementType;
        this.setupReadingTypesInExistingTransaction();

        // Business method
        measurementType = inMemoryPersistence.getMasterDataService().findMeasurementTypeByReadingType(readingType1).get();

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
        this.setupReadingTypesInExistingTransaction();

        // Create the RegisterType
        registerType = inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(readingType1).get();

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

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.REGISTER_TYPE_SHOULD_NOT_HAVE_INTERVAL_READINGTYPE + "}")
    public void registerTypeShouldNotHaveReadingTypeWithIntervalTest() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .period(MacroPeriod.DAILY)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        ObisCode obisCode = ObisCode.fromString("1.0.1.8.0.255");


        // Create the RegisterType
        RegisterType registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, obisCode);
        registerType.setDescription("For testing purposes only");
        registerType.save();
    }


    private void setupReadingTypesInExistingTransaction() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        this.obisCode1 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFrom(readingType1).get().getObisCode();
        String code2 = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
        this.obisCode2 = inMemoryPersistence.getReadingTypeUtilService().getReadingTypeInformationFrom(readingType2).get().getObisCode();
    }

    private void setupLoadProfileTypesInExistingTransaction(RegisterType registerType) {
        this.loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(RegisterTypeImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), INTERVAL_15_MINUTES, Arrays.asList(registerType));
        this.loadProfileType.save();
    }

}
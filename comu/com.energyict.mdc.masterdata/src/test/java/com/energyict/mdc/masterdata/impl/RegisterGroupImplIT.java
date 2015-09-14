package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.exceptions.RegisterTypesRequiredException;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the persistence aspects of the {@link RegisterGroupImpl} component
 * as provided by the {@link MasterDataServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:35)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterGroupImplIT {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    private ReadingType readingType1;
    private ReadingType readingType2;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("mdc.masterdata.registergroup", false, false, "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0");
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
    public void testCreateEmptyWithoutViolations() {
        // Business method
        this.setupProductSpecsInExistingTransaction();
        String expectedName = "No violations";
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();
        RegisterGroup registerGroup = inMemoryPersistence.getMasterDataService().newRegisterGroup(expectedName);
        registerGroup.updateRegisterTypes(Arrays.asList(registerType1, registerType2));
        registerGroup.save();

        // Asserts
        assertThat(registerGroup.getId()).isGreaterThan(0);
        assertThat(registerGroup.getName()).isEqualTo(expectedName);
        List<Long> registerTypeIds = registerGroup.getRegisterTypes().stream().map(RegisterType::getId).collect(Collectors.toList());
        assertThat(registerTypeIds).containsOnly(registerType1.getId(), registerType2.getId());
    }

    @Test
    @Transactional
    public void testFindAfterCreation() {
        String expectedName = "No violations";
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterGroup registerGroup = inMemoryPersistence.getMasterDataService().newRegisterGroup(expectedName);
        registerGroup.addRegisterType(registerType1);
        registerGroup.save();

        // Business method
        Optional<RegisterGroup> found = inMemoryPersistence.getMasterDataService().findRegisterGroup(registerGroup.getId());

        // Asserts
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(registerGroup.getId());
    }

    @Test
    @Transactional
    public void testCreateWithRegisterTypesWithoutViolations() {
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();

        // Business method
        String expectedName = "With types and no violations";
        RegisterGroup registerGroup = masterDataService.newRegisterGroup(expectedName);
        registerGroup.addRegisterType(registerType1);
        registerGroup.addRegisterType(registerType2);
        registerGroup.save();

        // Asserts
        assertThat(registerGroup.getId()).isGreaterThan(0);
        assertThat(registerGroup.getName()).isEqualTo(expectedName);
        List<Long> registerTypeIds = registerGroup.getRegisterTypes().stream().map(RegisterType::getId).collect(Collectors.toList());
        assertThat(registerTypeIds).containsOnly(registerType1.getId(), registerType2.getId());
    }

    @Test
    @Transactional
    public void testAddRegisterType() {
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();
        RegisterGroup registerGroup = masterDataService.newRegisterGroup("testAddRegisterType");

        // Business method
        registerGroup.addRegisterType(registerType1);
        registerGroup.addRegisterType(registerType2);
        registerGroup.save();

        // Asserts
        List<Long> registerTypeIds = registerGroup.getRegisterTypes().stream().map(RegisterType::getId).collect(Collectors.toList());
        assertThat(registerTypeIds).containsOnly(registerType1.getId(), registerType2.getId());
    }

    @Test
    @Transactional
    public void testRemoveRegisterType() {
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();
        RegisterGroup registerGroup = masterDataService.newRegisterGroup("testRemoveRegisterType");
        registerGroup.addRegisterType(registerType1);
        registerGroup.addRegisterType(registerType2);
        registerGroup.save();

        // Business method
        registerGroup.removeRegisterType(registerType1);

        // Asserts
        List<Long> registerTypeIds = registerGroup.getRegisterTypes().stream().map(RegisterType::getId).collect(Collectors.toList());
        assertThat(registerTypeIds).containsOnly(registerType2.getId());
    }

    @Test(expected = RegisterTypesRequiredException.class)
    @Transactional
    public void testRemoveAllOneByOneRegisterType() {
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();
        RegisterGroup registerGroup = masterDataService.newRegisterGroup("testRemoveAllOneByOneRegisterType");
        registerGroup.addRegisterType(registerType1);
        registerGroup.addRegisterType(registerType2);
        registerGroup.save();

        // Business method
        registerGroup.removeRegisterType(registerType1);
        registerGroup.removeRegisterType(registerType2);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testUpdateRegisterTypes() {
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();
        RegisterGroup registerGroup = inMemoryPersistence.getMasterDataService().newRegisterGroup("testUpdateRegisterTypes");

        // Business method
        registerGroup.updateRegisterTypes(Arrays.asList(registerType1));
        registerGroup.save();
        // Asserts
        List<Long> registerTypeIds = registerGroup.getRegisterTypes().stream().map(RegisterType::getId).collect(Collectors.toList());
        assertThat(registerTypeIds).containsOnly(registerType1.getId());

        registerGroup.updateRegisterTypes(Arrays.asList(registerType1, registerType2));
        registerGroup.save();

        registerTypeIds = registerGroup.getRegisterTypes().stream().map(RegisterType::getId).collect(Collectors.toList());
        assertThat(registerTypeIds).containsOnly(registerType1.getId(), registerType2.getId());
    }

    @Test
    @Transactional
    public void testDelete() {
        this.setupProductSpecsInExistingTransaction();
        MasterDataServiceImpl masterDataService = inMemoryPersistence.getMasterDataService();
        RegisterType registerType1 = masterDataService.newRegisterType(this.readingType1, ObisCode.fromString("1.1.1.1.1.1"));
        registerType1.save();
        RegisterType registerType2 = masterDataService.newRegisterType(this.readingType2, ObisCode.fromString("2.2.2.2.2.2"));
        registerType2.save();
        RegisterGroup registerGroup = inMemoryPersistence.getMasterDataService().newRegisterGroup("testUpdateRegisterTypes");
        registerGroup.addRegisterType(registerType1);
        registerGroup.addRegisterType(registerType2);
        registerGroup.save();

        // Business method
        registerGroup.delete();

        // Asserts
        Optional<RegisterGroup> shouldBeEmpty = masterDataService.findRegisterGroup(registerGroup.getId());
        assertThat(shouldBeEmpty).isEmpty();
    }

    private void setupProductSpecsInExistingTransaction() {
        this.setupReadingTypesInExistingTransaction();
    }

    private void setupReadingTypesInExistingTransaction() {
        String code = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType1 = inMemoryPersistence.getMeteringService().getReadingType(code).get();
        String code2 = ReadingTypeCodeBuilder
                .of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.REVERSE)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).code();
        this.readingType2 = inMemoryPersistence.getMeteringService().getReadingType(code2).get();
    }

}
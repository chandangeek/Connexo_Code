package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateReadingTypeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.ReadingTypeIsRequiredException;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the peristence aspects of the {@link ProductSpecImpl} component
 * as provided by the {@link DeviceConfigurationServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-17 (16:05)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductSpecImplTest {

    private InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();
    private ReadingType readingType;

    @Before
    public void initializeDatabaseAndMocks() {
        this.inMemoryPersistence = new InMemoryPersistence();
        this.inMemoryPersistence.initializeDatabase("ProductSpecImplTest.mdc.device.config");
        this.initializeMocks();
    }

    private void initializeMocks() {
    }

    @After
    public void cleanUpDataBase() throws SQLException {
        this.inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testCreationWithoutViolations () {
        ProductSpec productSpec;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            // Business method
            productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
            productSpec.save();
            ctx.commit();
        }

        // Asserts
        assertThat(productSpec).isNotNull();
        assertThat(productSpec.getReadingType()).isEqualTo(this.readingType);
        assertThat(productSpec.getDescription()).isNotEmpty();
    }

    @Test
    public void testFindAfterCreation () {
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            ProductSpec productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
            productSpec.save();
            ctx.commit();
        }

        // Business method
        ProductSpec productSpec = this.inMemoryPersistence.getDeviceConfigurationService().findProductSpecByReadingType(this.readingType);

        // Asserts
        assertThat(productSpec).isNotNull();
    }

    @Test(expected = ReadingTypeIsRequiredException.class)
    public void testCreationWithoutReadingType () {
        ProductSpec productSpec;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(null);
            productSpec.save();
            ctx.commit();
        }

        // Asserts: expected ReadingTypeIsRequiredException
    }

    @Test(expected = DuplicateReadingTypeException.class)
    public void testDuplicateReadingType () {
        ProductSpec productSpec;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            // Setup first ProductSpec
            productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
            productSpec.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
            productSpec.save();
            ctx.commit();
        }

        // Asserts: expected DuplicateReadingTypeException
    }

    @Test
    public void testDelete () {
        ProductSpec productSpec;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            this.setupReadingTypeInExistingTransaction();

            productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
            productSpec.save();
            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            productSpec.delete();
            ctx.commit();
        }

        ProductSpec expectedNull = this.inMemoryPersistence.getDeviceConfigurationService().findProductSpecByReadingType(this.readingType);

        // Asserts
        assertThat(expectedNull).isNull();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    public void testCannotDeleteWhenUsedByRegisterMapping () {
        ProductSpec productSpec;
        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {

            this.setupReadingTypeInExistingTransaction();

            productSpec = this.inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
            productSpec.save();

            RegisterMapping registerMapping = this.inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(ProductSpecImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), productSpec);
            registerMapping.save();

            ctx.commit();
        }

        try (TransactionContext ctx = this.inMemoryPersistence.getTransactionService().getContext()) {
            // Business method
            productSpec.delete();
            ctx.commit();
        }
        catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.PRODUCT_SPEC_STILL_IN_USE);
            throw e;
        }
    }

    private void setupReadingTypeInExistingTransaction () {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType = this.inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}
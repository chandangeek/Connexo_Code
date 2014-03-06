package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
public class ProductSpecImplTest extends PersistenceTest {

    private ReadingType readingType;

    @Test
    @Transactional
    public void testCreationWithoutViolations() {
        ProductSpec productSpec;
        this.setupReadingTypeInExistingTransaction();

        // Business method
        productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        productSpec.save();

        // Asserts
        assertThat(productSpec).isNotNull();
        assertThat(productSpec.getReadingType()).isEqualTo(this.readingType);
        assertThat(productSpec.getDescription()).isNotEmpty();
    }

    @Test
    @Transactional
    public void testFindAfterCreation() {
        this.setupReadingTypeInExistingTransaction();

        ProductSpec productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        productSpec.save();

        // Business method
        ProductSpec productSpec2 = inMemoryPersistence.getDeviceConfigurationService().findProductSpecByReadingType(this.readingType);

        // Asserts
        assertThat(productSpec2).isNotNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.READING_TYPE_IS_REQUIRED_KEY + "}", property = "readingType")
    public void testCreationWithoutReadingType() {
        ProductSpec productSpec;
        // Business method
        productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(null);
        productSpec.save();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.READING_TYPE_ALREADY_EXISTS_KEY + "}", property = "readingType")
    public void testDuplicateReadingType() {
        ProductSpec productSpec;
        this.setupReadingTypeInExistingTransaction();

        // Setup first ProductSpec
        productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        productSpec.save();

        // Business method
        productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        productSpec.save();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void testDelete() {
        ProductSpec productSpec;
        this.setupReadingTypeInExistingTransaction();

        productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        productSpec.save();

        // Business method
        productSpec.delete();

        ProductSpec expectedNull = inMemoryPersistence.getDeviceConfigurationService().findProductSpecByReadingType(this.readingType);

        // Asserts
        assertThat(expectedNull).isNull();
    }

    @Test(expected = CannotDeleteBecauseStillInUseException.class)
    @Transactional
    public void testCannotDeleteWhenUsedByRegisterMapping() {
        ProductSpec productSpec;

        this.setupReadingTypeInExistingTransaction();

        productSpec = inMemoryPersistence.getDeviceConfigurationService().newProductSpec(this.readingType);
        productSpec.save();

        RegisterMapping registerMapping = inMemoryPersistence.getDeviceConfigurationService().newRegisterMapping(ProductSpecImplTest.class.getSimpleName(), ObisCode.fromString("1.0.99.1.0.255"), productSpec);
        registerMapping.save();


        try {
            // Business method
            productSpec.delete();
        } catch (CannotDeleteBecauseStillInUseException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.PRODUCT_SPEC_STILL_IN_USE);
            throw e;
        }
    }

    private void setupReadingTypeInExistingTransaction() {
        String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO, WATTHOUR).period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        this.readingType = inMemoryPersistence.getMeteringService().getReadingType(code).get();
    }

}
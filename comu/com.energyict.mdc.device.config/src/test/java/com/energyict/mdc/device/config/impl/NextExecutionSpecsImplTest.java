package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import org.junit.*;
import org.junit.rules.*;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link NextExecutionSpecsImpl} component.
 * Tests for the getNextTimestamp method are not included
 * as the implementation currently completely relies on
 * other core EIServer object that are unit tested.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-12 (15:23)
 */
public class NextExecutionSpecsImplTest extends PersistenceTest {

    private static final TimeDuration ZERO_OFFSET = new TimeDuration(0, TimeDuration.SECONDS);

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private TimeDuration frequency = new TimeDuration(1, TimeDuration.DAYS);
    private TimeDuration offset = new TimeDuration(2, TimeDuration.HOURS);

    @Test
    @Transactional
    public void testCreateWithOnlyFrequencyWithoutViolations () throws BusinessException, SQLException {
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();

        // Business method
        specs.save();

        // Asserts
        assertThat(specs.getTemporalExpression()).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(ZERO_OFFSET);
    }

    @Test
    @Transactional
    public void testCreateWithFrequencyAndOfsetWithoutViolations () throws BusinessException, SQLException {
        NextExecutionSpecs specs = this.createWithFrequencyAndOffsetWithoutViolations();

        // Asserts
        assertThat(specs.getTemporalExpression()).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(this.offset);
    }

    @Test
    @Transactional
    public void testFindAfterCreate () throws BusinessException, SQLException {
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();
        specs.save();

        // Business method
        NextExecutionSpecs found = inMemoryPersistence.getDeviceConfigurationService().findNextExecutionSpecs(specs.getId());

        // Asserts
        assertThat(found).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(ZERO_OFFSET);
    }

    @Test
    @Transactional
    public void testUpdateFrequencyOnlyToFrequencyAndOffset () throws BusinessException, SQLException {
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();

        // Business method
        specs.setTemporalExpression(new TemporalExpression(this.frequency, this.offset));
        specs.save();

        // Asserts
        assertThat(specs.getTemporalExpression()).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(this.offset);
    }

    @Test
    @Transactional
    public void testUpdateFrequencyAndOffsetToFrequencyOnly () throws BusinessException, SQLException {
        NextExecutionSpecs specs = this.createWithFrequencyAndOffsetWithoutViolations();

        // Business method
        specs.setTemporalExpression(new TemporalExpression(this.frequency));
        specs.save();

        // Asserts
        assertThat(specs.getTemporalExpression()).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(ZERO_OFFSET);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY + "}")
    public void testCreateWithoutTemporalExpression () throws BusinessException, SQLException {
        NextExecutionSpecs specs = inMemoryPersistence.getDeviceConfigurationService().newNextExecutionSpecs(null);

        // Business method
        specs.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY + "}")
    public void testRemoveTemporalExpression () throws BusinessException, SQLException {
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();
        specs.save();

        // Business method
        specs.setTemporalExpression(null);
        specs.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY + "}")
    public void testCreateWithZeroFrequency () throws BusinessException, SQLException {
        DeviceConfigurationService service = inMemoryPersistence.getDeviceConfigurationService();
        NextExecutionSpecs specs = service.newNextExecutionSpecs(new TemporalExpression(new TimeDuration(0, TimeDuration.MINUTES)));

        // Business method
        specs.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY + "}")
    public void testCreateWithNegativeFrequency () throws BusinessException, SQLException {
        DeviceConfigurationService service = inMemoryPersistence.getDeviceConfigurationService();
        NextExecutionSpecs specs = service.newNextExecutionSpecs(new TemporalExpression(new TimeDuration(-1, TimeDuration.MINUTES)));

        // Business method
        specs.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY + "}")
    public void testCreateWithNegativeOffset () throws BusinessException, SQLException {
        DeviceConfigurationService service = inMemoryPersistence.getDeviceConfigurationService();
        NextExecutionSpecs specs = service.newNextExecutionSpecs(new TemporalExpression(this.frequency, new TimeDuration(-1, TimeDuration.HOURS)));

        // Business method
        specs.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    private NextExecutionSpecs createWithOnlyFrequencyWithoutViolations () {
        return inMemoryPersistence.getDeviceConfigurationService().newNextExecutionSpecs(new TemporalExpression(this.frequency));
    }

    private NextExecutionSpecs createWithFrequencyAndOffsetWithoutViolations () {
        return inMemoryPersistence.getDeviceConfigurationService().newNextExecutionSpecs(new TemporalExpression(this.frequency, this.offset));
    }

}
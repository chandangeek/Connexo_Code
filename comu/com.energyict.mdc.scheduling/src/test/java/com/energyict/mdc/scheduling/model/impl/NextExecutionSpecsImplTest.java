/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;

import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.scheduling.model.impl.NextExecutionSpecsImpl} component.
 * Tests for the getNextTimestamp method are not included
 * as the implementation currently completely relies on
 * other core EIServer object that are unit tested.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-12 (15:23)
 */
public class NextExecutionSpecsImplTest extends PersistenceTest {

    private static final TimeDuration ZERO_OFFSET = new TimeDuration(0, TimeDuration.TimeUnit.SECONDS);

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    private TimeDuration offset = new TimeDuration(2, TimeDuration.TimeUnit.HOURS);

    @Test
    @Transactional
    public void testCreateWithOnlyFrequencyWithoutViolations() throws SQLException {
        // Business method
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();

        // Asserts
        assertThat(specs).isNotNull();
        assertThat(specs.getTemporalExpression()).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(ZERO_OFFSET);
    }

    @Test
    @Transactional
    public void testCreateWithFrequencyAndOffsetWithoutViolations() throws SQLException {
        NextExecutionSpecs specs = this.createWithFrequencyAndOffsetWithoutViolations();

        // Asserts
        assertThat(specs.getTemporalExpression()).isNotNull();
        assertThat(specs.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(specs.getTemporalExpression().getOffset()).isEqualTo(this.offset);
    }

    @Test
    @Transactional
    public void testFindAfterCreate() throws SQLException {
        NextExecutionSpecs specs = this.createWithFrequencyAndOffsetWithoutViolations();

        // Business method
        NextExecutionSpecs found = PersistenceTest.inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(specs.getId());

        // Asserts
        assertThat(found).isNotNull();
        assertThat(found.getTemporalExpression()).isNotNull();
        assertThat(found.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(found.getTemporalExpression().getOffset()).isEqualTo(this.offset);
    }

    @Test
    @Transactional
    public void testUpdateFrequencyOnlyToFrequencyAndOffset() throws SQLException {
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();

        // Business method
        specs.setTemporalExpression(new TemporalExpression(this.frequency, this.offset));
        specs.update();

        // Asserts
        NextExecutionSpecs found = PersistenceTest.inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(specs.getId());

        assertThat(found.getTemporalExpression()).isNotNull();
        assertThat(found.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(found.getTemporalExpression().getOffset()).isEqualTo(this.offset);
    }

    @Test
    @Transactional
    public void testUpdateFrequencyAndOffsetToFrequencyOnly() throws SQLException {
        NextExecutionSpecs specs = this.createWithFrequencyAndOffsetWithoutViolations();

        // Business method
        specs.setTemporalExpression(new TemporalExpression(this.frequency));
        specs.update();

        // Asserts
        NextExecutionSpecs found = PersistenceTest.inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(specs.getId());
        assertThat(found.getTemporalExpression()).isNotNull();
        assertThat(found.getTemporalExpression().getEvery()).isEqualTo(this.frequency);
        assertThat(found.getTemporalExpression().getOffset()).isEqualTo(ZERO_OFFSET);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY + "}")
    public void testCreateWithoutTemporalExpression() throws SQLException {
        // Business method
        PersistenceTest.inMemoryPersistence.getSchedulingService().newNextExecutionSpecs(null);

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY + "}")
    public void testRemoveTemporalExpression() throws SQLException {
        NextExecutionSpecs specs = this.createWithOnlyFrequencyWithoutViolations();

        // Business method
        specs.setTemporalExpression(null);
        specs.update();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY + "}")
    public void testCreateWithZeroFrequency() throws SQLException {
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(new TemporalExpression(new TimeDuration(0, TimeDuration.TimeUnit.MINUTES)));

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY + "}", strict = false)
    public void testCreateWithNegativeFrequency() throws SQLException {
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(new TemporalExpression(new TimeDuration(-1, TimeDuration.TimeUnit.MINUTES)));

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY + "}")
    public void testCreateWithNegativeOffset() throws SQLException {
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        NextExecutionSpecs specs = service.newNextExecutionSpecs(new TemporalExpression(this.frequency, new TimeDuration(-1, TimeDuration.TimeUnit.HOURS)));

        // Asserts: see ExpectedConstraintViolation rule
    }

    private NextExecutionSpecs createWithOnlyFrequencyWithoutViolations() {
        return PersistenceTest.inMemoryPersistence.getSchedulingService().newNextExecutionSpecs(new TemporalExpression(this.frequency));
    }

    private NextExecutionSpecs createWithFrequencyAndOffsetWithoutViolations() {
        return PersistenceTest.inMemoryPersistence.getSchedulingService().newNextExecutionSpecs(new TemporalExpression(this.frequency, this.offset));
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everySevenHoursTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(7, TimeDuration.TimeUnit.HOURS));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everyThirteenMinutesTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(13, TimeDuration.TimeUnit.MINUTES));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everyNineSecondsTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(9, TimeDuration.TimeUnit.SECONDS));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everyElevenMonthsTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(11, TimeDuration.TimeUnit.MONTHS));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everyFiftyDaysTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(50, TimeDuration.TimeUnit.DAYS));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    public void everOneDayTest() {
        TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        TemporalExpression temporalExpression = new TemporalExpression(frequency);
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        NextExecutionSpecs specs = service.newNextExecutionSpecs(temporalExpression);
        NextExecutionSpecs reloadedSpec = service.findNextExecutionSpecs(specs.getId());
        assertThat(reloadedSpec.getTemporalExpression().getEvery()).isEqualTo(frequency);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everyTwoWeeksTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(2, TimeDuration.TimeUnit.WEEKS));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    public void everyOneWeeksTest() {
        TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.WEEKS);
        TemporalExpression temporalExpression = new TemporalExpression(frequency);
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        NextExecutionSpecs specs = service.newNextExecutionSpecs(temporalExpression);
        NextExecutionSpecs reloadedSpec = service.findNextExecutionSpecs(specs.getId());
        assertThat(reloadedSpec.getTemporalExpression().getEvery()).isEqualTo(frequency);
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}")
    public void everyTwentyYearsTest() {
        TemporalExpression temporalExpression = new TemporalExpression(new TimeDuration(20, TimeDuration.TimeUnit.YEARS));
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        service.newNextExecutionSpecs(temporalExpression);
    }

    @Test
    @Transactional
    public void everyOneYearsTest() {
        TimeDuration frequency = new TimeDuration(1, TimeDuration.TimeUnit.YEARS);
        TemporalExpression temporalExpression = new TemporalExpression(frequency);
        SchedulingService service = PersistenceTest.inMemoryPersistence.getSchedulingService();

        // Business method
        NextExecutionSpecs specs = service.newNextExecutionSpecs(temporalExpression);
        NextExecutionSpecs reloadedSpec = service.findNextExecutionSpecs(specs.getId());
        assertThat(reloadedSpec.getTemporalExpression().getEvery()).isEqualTo(frequency);
    }
}
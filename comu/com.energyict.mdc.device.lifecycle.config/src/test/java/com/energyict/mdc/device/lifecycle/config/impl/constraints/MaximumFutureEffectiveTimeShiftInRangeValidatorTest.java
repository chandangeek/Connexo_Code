package com.energyict.mdc.device.lifecycle.config.impl.constraints;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.elster.jupiter.time.TimeDuration;

import javax.validation.ConstraintValidatorContext;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MaximumFutureEffectiveTimeShiftInRangeValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-24 (11:20)
 */
@RunWith(MockitoJUnitRunner.class)
public class MaximumFutureEffectiveTimeShiftInRangeValidatorTest {

    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext;
    @Mock
    private DeviceLifeCycle deviceLifeCycle;

    @Before
    public void initializeMocks() {
        TimeDuration maximum = TimeDuration.days(3);
        when(this.deviceLifeCycleConfigurationService.getMaximumFutureEffectiveTimeShift()).thenReturn(maximum);
        when(this.context.getDefaultConstraintMessageTemplate()).thenReturn("{0}");
        when(this.context.buildConstraintViolationWithTemplate(anyString())).thenReturn(this.constraintViolationBuilder);
        when(this.constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(this.nodeBuilderCustomizableContext);
    }

    @Test
    public void nullIsValid() {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(null);

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroMonthsAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.months(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroWeeksAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.weeks(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroDaysAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.days(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroHoursAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.hours(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroMinutesAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.minutes(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroSecondsAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.seconds(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void zeroMillisAreValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.millis(0));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void maximumIsValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        TimeDuration maximum = TimeDuration.days(3);
        when(this.deviceLifeCycleConfigurationService.getMaximumFutureEffectiveTimeShift()).thenReturn(maximum);
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(maximum);

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void valueSmallerThanMaximumIsValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        TimeDuration maximum = TimeDuration.days(3);
        when(this.deviceLifeCycleConfigurationService.getMaximumFutureEffectiveTimeShift()).thenReturn(maximum);
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.days(1));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isTrue();
    }

    @Test
    public void valueBiggerThanMaximumIsNotValid () {
        MaximumFutureEffectiveTimeShiftInRangeValidator validator = this.getTestInstance();
        TimeDuration maximum = TimeDuration.days(1);
        when(this.deviceLifeCycleConfigurationService.getMaximumFutureEffectiveTimeShift()).thenReturn(maximum);
        when(this.deviceLifeCycle.getMaximumFutureEffectiveTimeShift()).thenReturn(TimeDuration.days(3));

        // Business method
        boolean validationResult = validator.isValid(this.deviceLifeCycle, this.context);

        // Asserts
        assertThat(validationResult).isFalse();
        verify(this.context).getDefaultConstraintMessageTemplate();
        verify(this.context).buildConstraintViolationWithTemplate(anyString());
        verify(this.nodeBuilderCustomizableContext).addConstraintViolation();
    }

    private MaximumFutureEffectiveTimeShiftInRangeValidator getTestInstance() {
        return new MaximumFutureEffectiveTimeShiftInRangeValidator(this.deviceLifeCycleConfigurationService);
    }

}
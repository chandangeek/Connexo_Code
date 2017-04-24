package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapter between Connexo TimeDuration property spec builder
 * and upl property spec builder for TemporalAmount values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:51)
 */
class TemporalAmountPropertySpecBuilderAdapter implements PropertySpecBuilder<TemporalAmount> {
    private static final int MONTHS_IN_YEAR = 12;
    private static final BigDecimal AVERAGE_DAYS_IN_MONTH = new BigDecimal("30.4375");  // average days in year // 12

    private final com.elster.jupiter.properties.PropertySpecBuilder<TimeDuration> actual;

    TemporalAmountPropertySpecBuilderAdapter(com.elster.jupiter.properties.PropertySpecBuilder<TimeDuration> actual) {
        this.actual = actual;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> setDefaultValue(TemporalAmount defaultValue) {
        this.actual.setDefaultValue(this.fromUpl(defaultValue));
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> markExhaustive() {
        this.actual.markExhaustive();
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> markExhaustive(PropertySelectionMode selectionMode) {
        this.actual.markExhaustive(PropertySelectionModeConverter.fromUpl(selectionMode));
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> markEditable() {
        this.actual.markEditable();
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> markMultiValued() {
        this.actual.markMultiValued();
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> markMultiValued(String separator) {
        this.actual.markMultiValued(separator);
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> markRequired() {
        this.actual.markRequired();
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> addValues(TemporalAmount... values) {
        this.actual.addValues(Stream.of(values).map(this::fromUpl).collect(Collectors.toList()));
        return this;
    }

    @Override
    public PropertySpecBuilder<TemporalAmount> addValues(List<TemporalAmount> values) {
        this.actual.addValues(values.stream().map(this::fromUpl).collect(Collectors.toList()));
        return this;
    }

    private TimeDuration fromUpl(TemporalAmount temporalAmount) {
        if (temporalAmount instanceof Duration) {
            return this.fromUpl((Duration) temporalAmount);
        } else {
            return this.fromUpl((Period) temporalAmount);
        }
    }

    private TimeDuration fromUpl(Duration duration) {
        long remainingMillis = duration.toMillis() % 1000;
        if (remainingMillis == 0) {
            return TimeDuration.seconds((int) duration.getSeconds());
        } else {
            return TimeDuration.millis((int) duration.toMillis());
        }
    }

    private TimeDuration fromUpl(Period period) {
        if (period.getDays() != 0) {
            // Ok, smallest unit is days, convert all to days
            return TimeDuration.days(this.getDays(period));
        } else if (period.getMonths() != 0) {
            // Ok, smallest unit is months, convert all to months
            return TimeDuration.days(this.getMonths(period));
        } else {
            return TimeDuration.years(period.getYears());
        }
    }

    private int getDays(Period period) {
        return period.getDays() + AVERAGE_DAYS_IN_MONTH.multiply(BigDecimal.valueOf(this.getMonths(period))).intValue();
    }

    private int getMonths(Period period) {
        return period.getMonths() + period.getYears() * MONTHS_IN_YEAR;
    }

    @Override
    public PropertySpec finish() {
        return new ConnexoToUPLPropertSpecAdapter(this.actual.finish());
    }
}
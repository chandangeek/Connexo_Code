package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapter between Connexo TimeDuration property spec builder
 * and upl property spec builder for Duration values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:52)
 */
class DurationPropertySpecBuilderAdapter implements PropertySpecBuilder<Duration> {
    private final com.elster.jupiter.properties.PropertySpecBuilder<TimeDuration> actual;

    DurationPropertySpecBuilderAdapter(com.elster.jupiter.properties.PropertySpecBuilder<TimeDuration> actual) {
        this.actual = actual;
    }

    @Override
    public PropertySpecBuilder<Duration> setDefaultValue(Duration defaultValue) {
        this.actual.setDefaultValue(this.fromUpl(defaultValue));
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> markExhaustive() {
        this.actual.markExhaustive();
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> markExhaustive(PropertySelectionMode selectionMode) {
        this.actual.markExhaustive(PropertySelectionModeConverter.fromUpl(selectionMode));
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> markEditable() {
        this.actual.markEditable();
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> markMultiValued() {
        this.actual.markMultiValued();
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> markMultiValued(String separator) {
        this.actual.markMultiValued(separator);
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> markRequired() {
        this.actual.markRequired();
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> addValues(Duration... values) {
        this.actual.addValues(Stream.of(values).map(this::fromUpl).collect(Collectors.toList()));
        return this;
    }

    @Override
    public PropertySpecBuilder<Duration> addValues(List<Duration> values) {
        this.actual.addValues(values.stream().map(this::fromUpl).collect(Collectors.toList()));
        return this;
    }

    private TimeDuration fromUpl(Duration duration) {
        long remainingMillis = duration.toMillis() % 1000;
        if (remainingMillis == 0) {
            return TimeDuration.seconds((int) duration.getSeconds());
        } else {
            return TimeDuration.millis((int) duration.toMillis());
        }
    }

    @Override
    public PropertySpec finish() {
        return new ConnexoToUPLPropertSpecAdapter(this.actual.finish());
    }
}
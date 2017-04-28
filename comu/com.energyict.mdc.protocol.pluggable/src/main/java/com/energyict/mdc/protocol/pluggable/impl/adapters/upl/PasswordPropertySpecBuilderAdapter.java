package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapter between {@link com.energyict.mdc.common.Password Connexo} Password
 * property spec builder and {@link Password upl} property spec builder.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:53)
 */
class PasswordPropertySpecBuilderAdapter implements PropertySpecBuilder<Password> {
    private final com.elster.jupiter.properties.PropertySpecBuilder<com.energyict.mdc.common.Password> actual;

    PasswordPropertySpecBuilderAdapter(com.elster.jupiter.properties.PropertySpecBuilder<com.energyict.mdc.common.Password> actual) {
        this.actual = actual;
    }

    @Override
    public PropertySpecBuilder<Password> setDefaultValue(Password defaultValue) {
        this.actual.setDefaultValue(this.fromUpl(defaultValue));
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> markExhaustive() {
        this.actual.markExhaustive();
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> markExhaustive(PropertySelectionMode selectionMode) {
        this.actual.markExhaustive(PropertySelectionModeConverter.fromUpl(selectionMode));
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> markEditable() {
        this.actual.markEditable();
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> markMultiValued() {
        this.actual.markMultiValued();
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> markMultiValued(String separator) {
        this.actual.markMultiValued(separator);
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> markRequired() {
        this.actual.markRequired();
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> addValues(Password... values) {
        this.actual.addValues(Stream.of(values).map(this::fromUpl).collect(Collectors.toList()));
        return this;
    }

    @Override
    public PropertySpecBuilder<Password> addValues(List<Password> values) {
        this.actual.addValues(values.stream().map(this::fromUpl).collect(Collectors.toList()));
        return this;
    }

    private com.energyict.mdc.common.Password fromUpl(Password password) {
        return new com.energyict.mdc.common.Password(password.getValue());
    }

    @Override
    public PropertySpec finish() {
        return new ConnexoToUPLPropertSpecAdapter(this.actual.finish());
    }
}
package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.properties.InvalidValueException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.ValueFactory;

import java.util.Optional;

/**
 * Adapter between {@link com.elster.jupiter.properties.PropertySpec Connexo}
 * and {@link PropertySpec upl} PropertySpec interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:45)
 */
public class ConnexoToUPLPropertSpecAdapter implements PropertySpec {
    private final com.elster.jupiter.properties.PropertySpec actual;

    public ConnexoToUPLPropertSpecAdapter(com.elster.jupiter.properties.PropertySpec actual) {
        this.actual = actual;
    }

    com.elster.jupiter.properties.PropertySpec getConnexoPropertySpec() {
        return actual;
    }

    @Override
    public String getName() {
        return this.actual.getName();
    }

    @Override
    public String getDisplayName() {
        return this.actual.getDisplayName();
    }

    @Override
    public String getDescription() {
        return this.actual.getDescription();
    }

    @Override
    public boolean isRequired() {
        return this.actual.isRequired();
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        try {
            return this.actual.validateValue(value);
        } catch (InvalidValueException e) {
            throw new InvalidPropertyException(e, e.getMessage());
        }
    }

    @Override
    public Optional<?> getDefaultValue() {
        com.elster.jupiter.properties.PropertySpecPossibleValues possibleValues = this.actual.getPossibleValues();
        if (possibleValues == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(possibleValues.getDefault());
        }
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        return new PossibleValuesAdapter(this.actual.getPossibleValues());
    }

    @Override
    public boolean supportsMultiValues() {
        return this.actual.supportsMultiValues();
    }

    @Override
    public ValueFactory getValueFactory() {
        return ConnexoToUPLValueFactoryAdapter.adapt(this.actual.getValueFactory());
    }

}
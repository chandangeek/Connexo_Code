package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.ValueRequiredException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

/**
 * Adapter between {@link com.energyict.mdc.upl.properties.PropertySpec upl}
 * and {@link com.elster.jupiter.properties.PropertySpec Connexo} PropertySpec interfaces.
 * Think carefully before you use this class since the PropertySpec that you are
 * adapting is likely one that was produced by this very same bundle.
 * In case the PropertySpec is an instance of {@link ConnexoToUPLPropertSpecAdapter}
 * then please cast it and return the actual UPL PropertySpec instead of adapting an adapter.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see ConnexoToUPLPropertSpecAdapter#getConnexoPropertySpec()
 * @since 2016-12-12 (13:27)
 */
public class UPLToConnexoPropertySpecAdapter implements PropertySpec {
    private final com.energyict.mdc.upl.properties.PropertySpec actual;

    public UPLToConnexoPropertySpecAdapter(com.energyict.mdc.upl.properties.PropertySpec actual) {
        this.actual = actual;
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
    public ValueFactory getValueFactory() {
        return new UPLToConnexoValueFactoryAdapter(this.actual.getValueFactory());
    }

    @Override
    public boolean isRequired() {
        return this.actual.isRequired();
    }

    @Override
    public boolean isReference() {
        return ValueType.fromClassName(this.actual.getValueFactory().getValueTypeName()).isReference();
    }

    @Override
    public boolean supportsMultiValues() {
        return this.actual.supportsMultiValues();
    }

    @Override
    public boolean validateValue(Object value) throws InvalidValueException {
        try {
            return this.actual.validateValue(value);
        } catch (MissingPropertyException e) {
            throw new ValueRequiredException(this.getName());
        } catch (PropertyValidationException e) {
            throw new InvalidValueException("XisNotValidValueForAttributeY", "The value \\\"{1}\\\" is not valid for the attribute specification {0}.", this.getName(), value);
        }
    }

    @Override
    public boolean validateValueIgnoreRequired(Object value) throws InvalidValueException {
        try {
            return this.validateValue(value);
        } catch (ValueRequiredException e) {
            // Let's hope the actual property spec did not check required first and did not validate the value at all
            return true;
        }
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        com.energyict.mdc.upl.properties.PropertySpecPossibleValues possibleValues = this.actual.getPossibleValues();
        if (possibleValues == null) {
            return null;
        } else {
            return new UPLToConnexoPropetySpecPossibleValuesAdapter(this.actual.getPossibleValues());
        }
    }

}
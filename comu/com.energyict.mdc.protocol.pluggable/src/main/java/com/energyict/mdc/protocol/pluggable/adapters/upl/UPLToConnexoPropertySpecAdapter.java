package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.ValueRequiredException;
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

    public static PropertySpec adaptTo(com.energyict.mdc.upl.properties.PropertySpec actual) {
        if (actual instanceof ConnexoToUPLPropertSpecAdapter) {
            return ((ConnexoToUPLPropertSpecAdapter) actual).getConnexoPropertySpec();
        } else {
            return new UPLToConnexoPropertySpecAdapter(actual);
        }
    }

    private UPLToConnexoPropertySpecAdapter(com.energyict.mdc.upl.properties.PropertySpec actual) {
        this.actual = actual;
    }

    public com.energyict.mdc.upl.properties.PropertySpec getUplPropertySpec() {
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
    public ValueFactory getValueFactory() {
        return UPLToConnexoValueFactoryAdapter.adaptTo(this.actual.getValueFactory());
    }

    @Override
    public boolean isRequired() {
        return this.actual.isRequired();
    }

    @Override
    public boolean isReference() {
        return ValueType.fromUPLClassName(this.actual.getValueFactory().getValueTypeName()).isReference();
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
            throw new InvalidValueException("XisNotValid", "{0} is not valid.", this.getDisplayName());
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
            return UPLToConnexoPropertySpecPossibleValuesAdapter.adaptTo(this.actual.getPossibleValues());
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UPLToConnexoPropertySpecAdapter) {
            return actual.equals(((UPLToConnexoPropertySpecAdapter) obj).actual);
        } else {
            return actual.equals(obj);
        }
    }
}
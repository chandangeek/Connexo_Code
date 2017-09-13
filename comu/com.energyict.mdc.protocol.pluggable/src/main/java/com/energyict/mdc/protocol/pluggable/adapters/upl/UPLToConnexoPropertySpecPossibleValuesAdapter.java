package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpecPossibleValues;

import java.util.List;

/**
 * Adapter between {@link com.energyict.mdc.upl.properties.PropertySpec upl}
 * and {@link PropertySpecPossibleValues Connexo} interfaces that model possible values
 * for property specifications.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-12 (13:41)
 */
class UPLToConnexoPropertySpecPossibleValuesAdapter implements PropertySpecPossibleValues {

    private final com.energyict.mdc.upl.properties.PropertySpecPossibleValues actual;

    public static PropertySpecPossibleValues adaptTo(com.energyict.mdc.upl.properties.PropertySpecPossibleValues actual) {
        if (actual instanceof ConnexoToUPLPropertySpecPossibleValuesAdapter) {
            return ((ConnexoToUPLPropertySpecPossibleValuesAdapter) actual).getConnexoPropertySpecPossibleValues();
        } else {
            return new UPLToConnexoPropertySpecPossibleValuesAdapter(actual);
        }

    }

    private UPLToConnexoPropertySpecPossibleValuesAdapter(com.energyict.mdc.upl.properties.PropertySpecPossibleValues actual) {
        this.actual = actual;
    }

    public com.energyict.mdc.upl.properties.PropertySpecPossibleValues getUplPropertySpecPossibleValues() {
        return actual;
    }

    @Override
    public PropertySelectionMode getSelectionMode() {
        switch (this.actual.getSelectionMode()) {
            case NONE: {
                return PropertySelectionMode.UNSPECIFIED;
            }
            case COMBOBOX: {
                return PropertySelectionMode.COMBOBOX;
            }
            case SEARCH_AND_SELECT: {
                return PropertySelectionMode.SEARCH_AND_SELECT;
            }
            case LIST: {
                return PropertySelectionMode.LIST;
            }
            default: {
                return PropertySelectionMode.UNSPECIFIED;
            }
        }
    }

    @Override
    public List getAllValues() {
        return this.actual.getAllValues();
    }

    @Override
    public boolean isExhaustive() {
        return this.actual.isExhaustive();
    }

    @Override
    public boolean isEditable() {
        return this.actual.isEditable();
    }

    @Override
    public Object getDefault() {
        return this.actual.getDefault();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UPLToConnexoPropertySpecPossibleValuesAdapter) {
            return actual.equals(((UPLToConnexoPropertySpecPossibleValuesAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}
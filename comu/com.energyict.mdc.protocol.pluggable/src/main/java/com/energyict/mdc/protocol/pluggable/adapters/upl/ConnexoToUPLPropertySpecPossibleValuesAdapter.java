package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;

import java.util.List;

/**
 * Provides an implementation for the {@link PropertySpecPossibleValues} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (11:44)
 */
public class ConnexoToUPLPropertySpecPossibleValuesAdapter implements PropertySpecPossibleValues {

    private final com.elster.jupiter.properties.PropertySpecPossibleValues actual;

    public static PropertySpecPossibleValues adaptTo(com.elster.jupiter.properties.PropertySpecPossibleValues actual) {
        if (actual instanceof UPLToConnexoPropertySpecPossibleValuesAdapter) {
            return ((UPLToConnexoPropertySpecPossibleValuesAdapter) actual).getUplPropertySpecPossibleValues();
        } else {
            return new ConnexoToUPLPropertySpecPossibleValuesAdapter(actual);
        }
    }

    private ConnexoToUPLPropertySpecPossibleValuesAdapter(com.elster.jupiter.properties.PropertySpecPossibleValues actual) {
        this.actual = actual;
    }

    public com.elster.jupiter.properties.PropertySpecPossibleValues getConnexoPropertySpecPossibleValues() {
        return actual;
    }

    @Override
    public PropertySelectionMode getSelectionMode() {
        return this.toUpl(this.actual.getSelectionMode());
    }

    @Override
    public List<?> getAllValues() {
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

    private PropertySelectionMode toUpl(com.elster.jupiter.properties.PropertySelectionMode selectionMode) {
        switch (selectionMode) {
            case UNSPECIFIED: {
                return PropertySelectionMode.NONE;
            }
            case LIST: {
                return PropertySelectionMode.LIST;
            }
            case COMBOBOX: {
                return PropertySelectionMode.COMBOBOX;
            }
            case SEARCH_AND_SELECT: {
                return PropertySelectionMode.SEARCH_AND_SELECT;
            }
            default: {
                throw new IllegalArgumentException("Unexpected property selection mode: " + selectionMode.name());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConnexoToUPLPropertySpecPossibleValuesAdapter) {
            return actual.equals(((ConnexoToUPLPropertySpecPossibleValuesAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}
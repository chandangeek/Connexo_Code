package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.energyict.mdc.upl.properties.ValueFactory;

/**
 * Adapter between {@link com.elster.jupiter.properties.ValueFactory Connexo}
 * and {@link ValueFactory upl} value factory interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-12 (11:47)
 */
public class ConnexoToUPLValueFactoryAdapter implements ValueFactory {
    private final com.elster.jupiter.properties.ValueFactory actual;

    public static ValueFactory adaptTo(com.elster.jupiter.properties.ValueFactory actual) {
        if (actual instanceof UPLToConnexoValueFactoryAdapter) {
            return ((UPLToConnexoValueFactoryAdapter) actual).getUplValueFactory();
        } else {
            return new ConnexoToUPLValueFactoryAdapter(actual);
        }
    }

    private ConnexoToUPLValueFactoryAdapter(com.elster.jupiter.properties.ValueFactory actual) {
        this.actual = actual;
    }

    public com.elster.jupiter.properties.ValueFactory getConnexoValueFactory() {
        return actual;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isNull(Object value) {
        return this.actual.isNull(value);
    }

    @Override
    public String getValueTypeName() {
        return ValueType.fromCXOClassName(this.actual.getValueType().getName()).getUplClassName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isValid(Object value) {
        return this.actual.isValid(value);
    }

    @Override
    public Object fromStringValue(String stringValue) {
        return this.actual.fromStringValue(stringValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toStringValue(Object object) {
        return this.actual.toStringValue(object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object valueToDatabase(Object object) {
        return this.actual.valueToDatabase(object);
    }

    @Override
    public Object valueFromDatabase(Object databaseValue) {
        return this.actual.valueFromDatabase(databaseValue);
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConnexoToUPLValueFactoryAdapter) {
            return actual.equals(((ConnexoToUPLValueFactoryAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }
}
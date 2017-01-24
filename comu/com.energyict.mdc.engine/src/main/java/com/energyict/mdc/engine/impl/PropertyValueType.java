package com.energyict.mdc.engine.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.security.CertificateAlias;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Models the different types of property values.
 *
 * @author sva
 * @since 2017-01-23 (10:02)
 */
public enum PropertyValueType {

    STRING {
        @Override
        public Class getValueTypeClass() {
            return String.class;
        }

        @Override
        public Object doConvertValue(Object value) throws ClassCastException {
            return value.toString();
        }
    },

    BIG_DECIMAL {
        @Override
        public Class getValueTypeClass() {
            return BigDecimal.class;
        }

        @Override
        public Object doConvertValue(Object value) throws ClassCastException {
            BigDecimal result = null;
            if (value != null) {
                if (value instanceof BigDecimal) {
                    result = (BigDecimal) value;
                } else if (value instanceof String) {
                    result = new BigDecimal((String) value);
                } else if (value instanceof BigInteger) {
                    result = new BigDecimal((BigInteger) value);
                } else if (value instanceof Number) {
                    result = new BigDecimal(((Number) value).doubleValue());
                } else {
                    throw new ClassCastException("Not possible to convert [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
                }
            }
            return result;
        }
    },

    CERTIFICATE_ALIAS {
        @Override
        public Class getValueTypeClass() {
            return CertificateAlias.class;
        }

        @Override
        public Object doConvertValue(Object value) throws ClassCastException {
            throw new ClassCastException("Not possible to convert [" + value + "] from class " + value.getClass() + " into a CertificateAlias.");
        }
    },

    CERTIFICATE_WRAPPER_ID {
        @Override
        public Class getValueTypeClass() {
            return CertificateWrapperId.class;
        }

        @Override
        public Object doConvertValue(Object value) throws ClassCastException {
            throw new ClassCastException("Not possible to convert [" + value + "] from class " + value.getClass() + " into a CertificateWrapperId.");
        }
    },

    UNSUPPORTED {
        @Override
        public Class getValueTypeClass() {
            return null;
        }

        @Override
        public Object doConvertValue(Object value) throws ClassCastException {
            return null;
        }
    };

    public static PropertyValueType from(PropertySpec propertySpec) {
        for (PropertyValueType propertyValueType : values()) {
            if (propertySpec.getValueFactory().getValueType().equals(propertyValueType.getValueTypeClass())) {
                return propertyValueType;
            }
        }
        return UNSUPPORTED;
    }

    public abstract Class getValueTypeClass();

    /**
     * Converts the specified value to a value that is compatible
     * with this PropertyValueType or throws a ClassCastException
     * if that is absolutely impossible.
     *
     * @param value The Value
     * @return The converted value
     * @throws ClassCastException Thrown when the specified value is even after conversion absolutely not assignment compatible with this PropertyValueType
     */
    public Object convertValue(Object value) throws ClassCastException {
        if (this.getValueTypeClass().isAssignableFrom(value.getClass())) {
            return value;
        } else {
            return this.doConvertValue(value);
        }
    }

    protected abstract Object doConvertValue(Object value) throws ClassCastException;

}
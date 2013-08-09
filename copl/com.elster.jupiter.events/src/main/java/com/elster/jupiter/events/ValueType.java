package com.elster.jupiter.events;

public enum ValueType {

    STRING(String.class), BYTE(Byte.class), SHORT(Short.class), INTEGER(Integer.class), LONG(Long.class), FLOAT(Float.class), DOUBLE(Double.class), BOOLEAN(Boolean.class), CHARACTER(Character.class),
    PRIMITIVE_BYTE(Byte.TYPE), PRIMITIVE_SHORT(Short.TYPE), PRIMITIVE_INT(Integer.TYPE), PRIMITIVE_LONG(Long.TYPE), PRIMITIVE_FLOAT(Float.TYPE), PRIMITIVE_DOUBLE(Double.TYPE), PRIMITIVE_BOOLEAN(Boolean.TYPE), PRIMITIVE_CHAR(Character.TYPE);

    private final Class<?> type;

    ValueType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}

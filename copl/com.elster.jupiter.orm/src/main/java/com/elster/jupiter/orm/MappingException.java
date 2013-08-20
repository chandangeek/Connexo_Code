package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.ExceptionType;

import java.text.MessageFormat;

public class MappingException extends PersistenceException {

    public MappingException(IllegalAccessException cause) {
        super(ExceptionTypes.MAPPING_INTROSPECTION_FAILED, cause);
    }

    public MappingException(ReflectiveOperationException cause) {
        super(ExceptionTypes.MAPPING_INTROSPECTION_FAILED, cause);
    }

    public MappingException(Class<?> unmappedClass) {
        super(ExceptionTypes.MAPPING_MISMATCH, MessageFormat.format("No mapping found for class {0}", unmappedClass.getName()));
    }

    public MappingException(Class<?> clazz, String fieldName) {
        super(ExceptionTypes.MAPPING_MISMATCH, MessageFormat.format("No mapping found for field {1} on class {0}", clazz.getName(), fieldName));
        set("class", clazz);
        set("fieldName", fieldName);
    }

    public static MappingException noMappingForSqlType(String sqlType) {
        return new MappingException(ExceptionTypes.MAPPING_MISMATCH, MessageFormat.format("No mapping found for SQL type {0}", sqlType));
    }

    public static MappingException noDiscriminatorColumn() {
        return new MappingException(ExceptionTypes.MAPPING_NO_DISCRIMINATOR_COLUMN, "No discriminator column found.");
    }

    private MappingException(ExceptionType type, String message) {
        super(type, message);
    }
}

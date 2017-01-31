/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Thrown when ORM mapping of persistent Objects fails.
 */
public class MappingException extends PersistenceException {
	private static final long serialVersionUID = 1L;
	
    public MappingException(IllegalAccessException cause) {
        super(MessageSeeds.MAPPING_INTROSPECTION_FAILED, cause);
    }

    public MappingException(ReflectiveOperationException cause) {
        super(MessageSeeds.MAPPING_INTROSPECTION_FAILED, cause);
    }

    public MappingException(Class<?> unmappedClass) {
        super(MessageSeeds.MAPPING_MISMATCH_FOR_CLASS, unmappedClass.getName());
        set("class", unmappedClass);
    }

    public MappingException(Class<?> clazz, String fieldName) {
        super(MessageSeeds.MAPPING_MISMATCH_FOR_FIELD, clazz.getName(), fieldName);
        set("class", clazz);
        set("fieldName", fieldName);
    }

    public static MappingException noMappingForSqlType(String sqlType) {
        return new MappingException(MessageSeeds.NO_MAPPING_FOR_SQL_TYPE, sqlType);
    }

    public static MappingException noDiscriminatorColumn() {
        return new MappingException(MessageSeeds.MAPPING_NO_DISCRIMINATOR_COLUMN);
    }

    private MappingException(MessageSeed messageSeed, Object... args) {
        super(messageSeed, args);
    }
}

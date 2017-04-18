/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.properties;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.AbstractValueFactory;

import java.util.function.Predicate;

import static com.elster.jupiter.util.streams.Predicates.not;

public class ReadingTypeValueFactory extends AbstractValueFactory<ReadingTypeReference> {

    public enum Mode {
        ALL(PropertyType.READING_TYPE, readingType -> true),
        ONLY_REGULAR(PropertyType.REGULAR_READINGTYPE, ReadingType::isRegular),
        ONLY_IRREGULAR(PropertyType.IRREGULAR_READINGTYPE, not(ReadingType::isRegular));

        private PropertyType propertyType;
        private Predicate<ReadingType> accept;

        Mode(PropertyType propertyType, Predicate<ReadingType> accept) {
            this.propertyType = propertyType;
            this.accept = accept;
        }

        PropertyType getPropertyType() {
            return propertyType;
        }
    }

    private final MeteringService meteringService;
    private final Mode mode;

    public ReadingTypeValueFactory(MeteringService meteringService, Mode mode) {
        this.meteringService = meteringService;
        this.mode = mode;
    }

    Mode getMode() {
        return mode;
    }

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public ReadingTypeReference fromStringValue(String stringValue) {
        return meteringService.getReadingType(stringValue).map(ReadingTypeReference::new).orElse(null);
    }

    @Override
    public String toStringValue(ReadingTypeReference object) {
        return object.getReadingType().getMRID();
    }

    @Override
    public Class<ReadingTypeReference> getValueType() {
        return ReadingTypeReference.class;
    }

    @Override
    public ReadingTypeReference valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(ReadingTypeReference object) {
        return this.toStringValue(object);
    }

    @Override
    public boolean isValid(ReadingTypeReference value) {
        return this.mode.accept.test(value.getReadingType());
    }
}

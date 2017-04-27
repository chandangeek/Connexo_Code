/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.properties.AbstractValueFactory;

import java.util.function.Predicate;

import static com.elster.jupiter.util.streams.Predicates.not;

public class ReadingTypeValueFactory extends AbstractValueFactory<ReadingTypeValueFactory.ReadingTypeReference> {

    public enum Mode {
        ALL(readingType -> true),
        ONLY_REGULAR(ReadingType::isRegular),
        ONLY_IRREGULAR(not(ReadingType::isRegular));

        private Predicate<ReadingType> accept;

        Mode( Predicate<ReadingType> accept) {
            this.accept = accept;
        }

    }

    private final MeteringService meteringService;
    private final Mode mode;

    public ReadingTypeValueFactory(MeteringService meteringService, Mode mode) {
        this.meteringService = meteringService;
        this.mode = mode;
    }

    public Mode getMode() {
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

    public static class ReadingTypeReference {

        private final ReadingType readingType;

        public ReadingTypeReference(ReadingType readingType) {
            this.readingType = readingType;
        }

        public ReadingType getReadingType() {
            return readingType;
        }
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.fields;

import com.elster.jupiter.metering.imports.impl.FieldParser;

public final class CommonField<R> implements FileImportField<R> {

    private String fieldName;
    private FieldParser<R> parser;
    private FieldSetter<R> setter;
    private boolean mandatory;
    private boolean repetitive;

    private CommonField() {
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    @Override
    public FieldSetter<R> getSetter() {
        return this.setter;
    }

    @Override
    public FieldParser<R> getParser() {
        return this.parser;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public boolean isRepetitive() {
        return this.repetitive;
    }

    public static <T> FieldBuilder<T> withParser(FieldParser<T> parser) {
        FieldBuilder<T> builder = new FieldBuilder<>();
        builder.field.parser = parser;
        return builder;
    }

    public static final class FieldBuilder<R> {

        private CommonField<R> field;

        private FieldBuilder() {
            this.field = new CommonField<>();
        }

        public FieldBuilder<R> markMandatory() {
            this.field.mandatory = true;
            return this;
        }

        public FieldBuilder<R> markRepetitive() {
            this.field.repetitive = true;
            return this;
        }

        public FieldBuilder<R> withSetter(FieldSetter<R> fieldSetter) {
            this.field.setter = fieldSetter;
            return this;
        }

        public FieldBuilder<R> withName(String fieldName) {
            this.field.fieldName = fieldName;
            return this;
        }

        public CommonField<R> build() {
            return this.field;
        }
    }

}
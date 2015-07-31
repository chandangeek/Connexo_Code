package com.energyict.mdc.device.data.importers.impl.fields;

import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

public class CommonField<R> implements FileImportField<R> {

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
    public boolean isRepetitive() {
        return this.repetitive;
    }

    public static <T> FieldBuilder<T> withParser(FieldParser<T> parser) {
        FieldBuilder<T> builder = new FieldBuilder<>();
        builder.field.parser = parser;
        return builder;
    }

    public static class FieldBuilder<R> {

        private CommonField field;

        private FieldBuilder() {
            this.field = new CommonField();
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

        public CommonField<R> build() {
            return this.field;
        }
    }
}

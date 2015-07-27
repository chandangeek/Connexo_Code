package com.energyict.mdc.device.data.importers.impl.fields;

import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

import java.util.function.Consumer;

public class CommonField<R> implements FileImportField<R>{

    private boolean mandatory;
    private Consumer<R> consumer;
    private FieldParser<R> parser;
    private boolean repetitive;

    private CommonField(){}

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    @Override
    public Consumer<R> getResultConsumer() {
        return this.consumer;
    }

    @Override
    public FieldParser<R> getParser() {
        return this.parser;
    }

    @Override
    public boolean isRepetitive() {
        return this.repetitive;
    }

    public void setRepetitive(boolean repetitive) {
        this.repetitive = repetitive;
    }

    public static <T> FieldBuilder<T> withParser(FieldParser<T> parser){
        FieldBuilder<T> builder = new FieldBuilder<>();
        builder.field.parser = parser;
        return builder;
    }

    public static class FieldBuilder<R> {
        private CommonField field;

        private FieldBuilder() {
            this.field = new CommonField();
        }

        public FieldBuilder<R> markMandatory(){
            this.field.mandatory = true;
            return this;
        }

        public FieldBuilder<R> markRepetitive(){
            this.field.repetitive = true;
            return this;
        }

        public FieldBuilder<R> withConsumer(Consumer<R> resultConsumer){
            this.field.consumer = resultConsumer;
            return this;
        }

        public CommonField<R> build(){
            return this.field;
        }
    }
}

package com.energyict.mdc.device.data.importers.impl.fields;

import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

import java.util.function.Consumer;

public class CommonField<R> implements FileImportField<R>{

    private String title;
    private boolean mandatory;
    private Consumer<R> consumer;
    private FieldParser<R> parser;

    private CommonField(){}

    @Override
    public String getTitle() {
        return this.title;
    }

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

        public FieldBuilder<R> withConsumer(Consumer<R> resultConsumer){
            this.field.consumer = resultConsumer;
            return this;
        }

        public FieldBuilder<R> onColumn(String title){
            this.field.title = title;
            return this;
        }

        public CommonField<R> build(){
            return this.field;
        }
    }
}

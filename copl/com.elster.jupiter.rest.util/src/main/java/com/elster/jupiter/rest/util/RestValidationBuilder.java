package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class RestValidationBuilder {
    private RestValidationException validationError;

    private RestValidationException getValidationError() {
        if (this.validationError == null){
            this.validationError = new RestValidationException();
        }
        return this.validationError;
    }

    public<T> RestValidationBuilder notEmpty(T object, String field){
        new ValidationBuilder<T>(object).field(field).check(o -> o != null).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test();
        return this;
    }

    public RestValidationBuilder notEmpty(String string, String field){
        new ValidationBuilder<String>(string).field(field).check(o -> !Checks.is((String) o).emptyOrOnlyWhiteSpace()).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test();
        return this;
    }

    public RestValidationBuilder isCorrectId(Long id, String field){
        new ValidationBuilder<>(id).field(field).check(obj -> obj != null && obj > 0).message(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY).test();
        return this;
    }

    public <T> ValidationBuilder<T> on(T object){
        return new ValidationBuilder<>(object);
    }

    public RestValidationBuilder addValidationError(LocalizedFieldValidationException error){
        getValidationError().addError(error);
        return this;
    }

    public void validate(){
        if (this.validationError != null) {
            throw this.validationError;
        }
    }

    public class ValidationBuilder<T> {
        private T obj;
        private String field = "";
        private MessageSeed messageSeed = MessageSeeds.INVALID_VALUE;
        private Object[] args;
        private Predicate<T> check;

        ValidationBuilder(T obj){
            this.obj = obj;
        }

        public ValidationBuilder<T> field(String field){
            this.field = field;
            return this;
        }

        public ValidationBuilder<T> check(Predicate<T> check){
            this.check = check;
            return this;
        }

        public ValidationBuilder<T> message(MessageSeed messageSeed, Object... args){
            this.messageSeed = messageSeed;
            this.args = args;
            return this;
        }

        public RestValidationBuilder test(){
            if (this.check == null){
                throw new IllegalStateException("You must specify check for test");
            }
            RestValidationBuilder builder = RestValidationBuilder.this;
            if (!this.check.test(this.obj)){
                builder.getValidationError().addError(new LocalizedFieldValidationException(this.messageSeed, this.field, this.args));
            }
            return builder;
        }
    }

    public static class RestValidationException extends RuntimeException{
        private List<LocalizedFieldValidationException> errors;

        RestValidationException() {
            this.errors = new ArrayList<>();
        }

        void addError(LocalizedFieldValidationException error){
            this.errors.add(error);
        }

        public List<LocalizedFieldValidationException> getErrors() {
            return errors;
        }
    }
}

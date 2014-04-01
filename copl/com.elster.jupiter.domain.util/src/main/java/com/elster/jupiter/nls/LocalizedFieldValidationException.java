package com.elster.jupiter.nls;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * LocalizedException that can be linked to a specific field of an Impl
 */
public class LocalizedFieldValidationException extends LocalizedException {
    public static final String VIOLATING_PROPERTY_NAME = "violatingPropertyName";

    protected LocalizedFieldValidationException(Thesaurus thesaurus, MessageSeed messageSeed, String javaFieldName) {
        super(thesaurus, messageSeed);
        this.set(VIOLATING_PROPERTY_NAME, javaFieldName);
    }

    protected LocalizedFieldValidationException(Thesaurus thesaurus, MessageSeed messageSeed, String javaFieldName, Object... args) {
        super(thesaurus, messageSeed, args);
        this.set(VIOLATING_PROPERTY_NAME, javaFieldName);
    }

    public String getViolatingProperty(){
        return (String)this.get(VIOLATING_PROPERTY_NAME);
    }

}

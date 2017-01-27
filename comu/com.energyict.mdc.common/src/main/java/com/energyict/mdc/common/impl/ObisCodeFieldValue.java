package com.energyict.mdc.common.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * @author Koen
 */
class ObisCodeFieldValue {

    private final Thesaurus thesaurus;
    private final int code;
    private final StringBuilder descriptionBuilder;

    ObisCodeFieldValue(int code, TranslationKey descriptionKey, Thesaurus thesaurus) {
        this(code, thesaurus.getFormat(descriptionKey).format(code), thesaurus);
    }

    ObisCodeFieldValue(int code, String description, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.code = code;
        this.descriptionBuilder = new StringBuilder(description);
    }

    int getCode() {
        return code;
    }

    String getDescription() {
        return this.descriptionBuilder.toString();
    }

    public String toString() {
        return getDescription();
    }

    void add2Description(TranslationKey descriptionKey) {
        this.descriptionBuilder.append(this.thesaurus.getFormat(descriptionKey).format(code));
    }

    void add2Description(TranslationKey descriptionKey, int code) {
        this.descriptionBuilder.append(this.thesaurus.getFormat(descriptionKey).format(code));
    }

    void addSpaceDescription() {
        this.descriptionBuilder.append(" ");
    }

}

package com.elster.jupiter.validation;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.impl.MessageSeeds;

public class InvalidReadingTypeException extends LocalizedException {

    public InvalidReadingTypeException(Thesaurus thesaurus, String mRID) {
        super(thesaurus, MessageSeeds.NO_SUCH_READINGTYPE, mRID);
        set("mRID", mRID);
    }

}

package com.elster.jupiter.validators;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.validation.*;

public class MissingRequiredProperty extends BaseException {

    public MissingRequiredProperty(Thesaurus thesaurus, String missingKey) {
        super(ExceptionTypes.MISSING_PROPERTY, buildMessage(thesaurus, missingKey));
        set("missingKey", missingKey);
    }

    private static String buildMessage(Thesaurus thesaurus, String missingKey) {
        return thesaurus.getFormat(MessageSeeds.MISSING_PROPERTY).format(missingKey);
    }
}

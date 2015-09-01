package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ImportSchedulePropertyNotFoundException extends LocalizedException {

    public ImportSchedulePropertyNotFoundException(Thesaurus thesaurus, String implementation) {
        super(thesaurus, MessageSeeds.NO_SUCH_IMPORTER, implementation);
        set("implementation", implementation);
    }
}

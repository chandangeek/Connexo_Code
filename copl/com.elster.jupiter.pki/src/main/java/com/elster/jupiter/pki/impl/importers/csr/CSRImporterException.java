package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CSRImporterException extends LocalizedException {

    protected CSRImporterException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    protected CSRImporterException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }
}

package com.energyict.mdc.device.data.importers.impl.certificatesimport.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class ZipFieldParserException extends LocalizedException {

    public ZipFieldParserException(Thesaurus thesaurus, MessageSeed messageSeed, String fileName) {
        super(thesaurus, messageSeed, fileName);
    }
}

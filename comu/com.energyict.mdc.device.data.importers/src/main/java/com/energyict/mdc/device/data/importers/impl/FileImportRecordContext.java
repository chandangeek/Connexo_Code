package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class FileImportRecordContext {
    private Thesaurus thesaurus;
    private Logger logger;
    private boolean hasWarnings = false;

    FileImportRecordContext(Thesaurus thesaurus, Logger logger) {
        this.thesaurus = thesaurus;
        this.logger = logger;
    }

    boolean hasWarnings() {
        return this.hasWarnings;
    }

    public void warning(TranslationKey message, Object... arguments) {
        if (!hasWarnings) {
            hasWarnings = true;
        }
        String msg = thesaurus.getString(message.getKey(), message.getDefaultFormat());
        if (arguments != null && arguments.length > 0) {
            msg = MessageFormat.format(msg, arguments);
        }
        logger.info(msg);
    }
}

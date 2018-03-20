package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;

public class CSRImporterLogger {

    private FileImportOccurrence fileImportOccurrence;
    private Thesaurus thesaurus;

    public CSRImporterLogger(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus) {
        this.fileImportOccurrence = fileImportOccurrence;
        this.thesaurus = thesaurus;
    }

    public void log(LocalizedException exception) {
        Throwable cause = exception.getCause();
        if (cause == null) {
            fileImportOccurrence.getLogger().log(exception.getMessageSeed().getLevel(), exception.getLocalizedMessage());
        } else {
            fileImportOccurrence.getLogger().log(exception.getMessageSeed().getLevel(), exception.getLocalizedMessage(), cause);
        }
    }

    public void log(Throwable exception) {
        log(MessageSeeds.CSR_IMPORT_EXCEPTION, exception.getLocalizedMessage());
    }

    public void log(MessageSeeds messageSeeds, Object... args) {
        fileImportOccurrence.getLogger().log(messageSeeds.getLevel(), thesaurus.getFormat(messageSeeds).format(args));
    }

    public void markFailure() {
        fileImportOccurrence.markFailure(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_FAILED).format());
    }

    public void markSuccess() {
        fileImportOccurrence.markSuccess(thesaurus.getFormat(TranslationKeys.CSR_IMPORT_SUCCESS).format());
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}

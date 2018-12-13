/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.syntheticloadprofile;

import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.slp.importers.impl.FileImportLoggerImpl;
import com.elster.jupiter.slp.importers.impl.SyntheticLoadProfileDataImporterContext;
import com.elster.jupiter.slp.importers.impl.TranslationKeys;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class SyntheticLoadProfileImportLogger extends FileImportLoggerImpl<FileImportRecord> {

    private int linesProcessed = 0;

    private List<String> syntheticLoadProfiles = new ArrayList<>();

    public SyntheticLoadProfileImportLogger(SyntheticLoadProfileDataImporterContext context) {
        super(context);
    }

    public void addImportedSyntheticLoadProfiles(Collection<String> syntheticLoadProfileNames){
        syntheticLoadProfiles.addAll(syntheticLoadProfileNames);
    }

    @Override
    public void warning(MessageSeed message, Object... arguments) {
        super.warning(message, arguments);
    }

    @Override
    public void warning(TranslationKey message, Object... arguments) {
        super.warning(message, arguments);
    }

    @Override
    public void importLineFinished(FileImportRecord data) {
        super.importLineFinished(data);
        linesProcessed++;
    }

    @Override
    public void importLineFailed(FileImportRecord data, Exception exception) {
        super.importLineFailed(data.getLineNumber(), exception);
    }

    @Override
    public void importLineFailed(long lineNumber, Exception exception) {
        super.importLineFailed(lineNumber, exception);
    }

    protected void summarizeFailedImport() {
            // At least one the failures has occured
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.CF_IMPORT_RESULT_FAILED)
                    .format());

    }

    protected void summarizeSuccessImport() {
            // No failures. All data is imported from the file for all of the synthetic load profiles
            fileImportOccurrence.markSuccess(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.CF_IMPORT_RESULT_SUCCESS)
                    .format(linesProcessed, String.join(", ", syntheticLoadProfiles)));
    }
}

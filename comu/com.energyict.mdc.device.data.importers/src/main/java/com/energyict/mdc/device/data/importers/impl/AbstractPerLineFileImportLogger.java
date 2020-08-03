/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

public abstract class AbstractPerLineFileImportLogger extends FileImportLoggerImpl<FileImportRecord> {

    protected int linesWithError = 0;
    protected int linesProcessed = 0;
    protected int linesWithWarnings = 0;
    protected boolean hasWarnings = false;

    public AbstractPerLineFileImportLogger(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void warning(MessageSeed message, Object... arguments) {
        super.warning(message, arguments);
        hasWarnings = true;
    }

    @Override
    public void warning(TranslationKey message, Object... arguments) {
        super.warning(message, arguments);
        hasWarnings = true;
    }

    @Override
    public void importLineFinished(FileImportRecord data) {
        super.importLineFinished(data);
        linesProcessed++;
        if (hasWarnings) {
            linesWithWarnings++;
        }
    }

    @Override
    public void importLineFailed(FileImportRecord data, Exception exception) {
        super.importLineFailed(data, exception);
        this.linesWithError++;
    }

    @Override
    public void importLineFailed(long lineNumber, Exception exception) {
        super.importLineFailed(lineNumber, exception);
        this.linesWithError++;
    }

    protected abstract void summarizeFailedImport();

    protected abstract void summarizeSuccessImport();

}

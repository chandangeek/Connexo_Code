/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Creates {@link NlsEntry NlsEntries} in batch from an InputStream.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-19 (10:46)
 */
class TranslationBatchCreator extends TranslationBatchExecutor {

    TranslationBatchCreator(Locale locale, NlsServiceImpl nlsService) {
        super(nlsService, locale);
    }

    @Override
    protected void addTranslations(BufferedReader reader, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(BATCH_INSERT_SQL)) {
            reader.lines().forEach(each -> this.insertNlsEntry(each, statement));
            statement.executeBatch();
        }
    }

    private void insertNlsEntry(String csvLine, PreparedStatement statement) throws UnderlyingSQLFailedException {
        try {
            TranslationCsvEntry entry = TranslationCsvEntry.parseFrom(csvLine, this.getLocale());
            entry.bindToInsertBatch(statement);
            this.addImpactedThesaurus(entry);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
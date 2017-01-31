/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

/**
 * Creates or updates {@link NlsEntry NlsEntries} in batch from an InputStream.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-19 (10:46)
 */
class TranslationBatchUpdater extends TranslationBatchExecutor {

    private PreparedStatement insertStatement;
    private long insertCount;
    private PreparedStatement updateStatement;
    private long updateCount;

    TranslationBatchUpdater(Locale locale, NlsServiceImpl nlsService) {
        super(nlsService, locale);
    }

    @Override
    protected void addTranslations(BufferedReader reader, Connection connection) throws SQLException {
        this.cacheAllNlsKeys();
        try (PreparedStatement insertStatement = connection.prepareStatement(BATCH_INSERT_SQL);
             PreparedStatement updateStatement = connection.prepareStatement(BATCH_UPDATE_SQL)) {
            this.insertStatement = insertStatement;
            this.updateStatement = updateStatement;
            reader.lines().forEach(this::upsertNlsEntry);
            if (this.insertCount > 0) {
                insertStatement.executeBatch();
            }
            if (this.updateCount > 0) {
                updateStatement.executeBatch();
            }
        }
    }

    private void upsertNlsEntry(String csvLine) {
        TranslationCsvEntry entry = TranslationCsvEntry.parseFrom(csvLine, this.getLocale(), this.getCsvSeparator());
        // We ignore translation for NlsKeys that don't exist
        if (this.keyExists(entry)) {
            this.upsertNlsEntry(entry);
        }
    }

    private void upsertNlsEntry(TranslationCsvEntry csvEntry) {
        Optional<NlsEntry> nlsEntry = csvEntry.findEntry(this.nlsEntryDataMapper());
        try {
            if (nlsEntry.isPresent()) {
                csvEntry.bindToUpdateBatch(this.updateStatement);
                this.updateCount++;
            } else {
                csvEntry.bindToInsertBatch(this.insertStatement);
                this.insertCount++;
            }
            this.addImpactedThesaurus(csvEntry);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
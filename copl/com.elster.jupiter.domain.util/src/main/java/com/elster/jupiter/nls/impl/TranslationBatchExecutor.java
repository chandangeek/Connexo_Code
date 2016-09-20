/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-19 (11:19)
 */
abstract class TranslationBatchExecutor {

    static final String BATCH_INSERT_SQL = "INSERT INTO " + TableSpecs.NLS_ENTRY.name() + " (component, layer, key, translation, languagetag) VALUES (?, ?, ?, ?, ?)";
    static final String BATCH_UPDATE_SQL = "UPDATE " + TableSpecs.NLS_ENTRY.name() + " set translation = ? where component = ? and layer = ? and key = ? and languagetag = ?";

    private final NlsServiceImpl nlsService;
    private final Locale locale;
    private Set<Pair<String, Layer>> impactedThesauri = new HashSet<>();

    TranslationBatchExecutor(NlsServiceImpl nlsService, Locale locale) {
        this.nlsService = nlsService;
        this.locale = locale;
    }

    // Intended for subclasses only
    protected Locale getLocale() {
        return locale;
    }

    // Intended for subclasses only
    protected DataMapper<NlsKey> nlsKeyDataMapper() {
        return this.nlsService.getDataModel().mapper(NlsKey.class);
    }

    // Intended for subclasses only
    protected DataMapper<NlsEntry> nlsEntryDataMapper() {
        return this.nlsService.getDataModel().mapper(NlsEntry.class);
    }

    void addTranslations(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            this.addTranslations(reader);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }
        this.invalidateImpactedThesauri();
    }

    private void addTranslations(BufferedReader reader) {
        try (Connection connection = this.nlsService.getDataModel().getConnection(true)) {
            this.addTranslations(reader, connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    protected abstract void addTranslations(BufferedReader reader, Connection connection) throws SQLException;

    void addImpactedThesaurus(TranslationCsvEntry csvEntry) {
        this.impactedThesauri.add(csvEntry.componentAndLayer());
    }

    protected void invalidateImpactedThesauri() {
        this.impactedThesauri.forEach(this::invalidateImpactedThesaurus);
    }

    protected void invalidateImpactedThesaurus(Pair<String, Layer> componentAndLayer) {
        this.nlsService.invalidate(componentAndLayer.getFirst(), componentAndLayer.getLast());
    }

}
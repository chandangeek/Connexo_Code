/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.Pair;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

/**
 * Models an entry from a CSV file that contains translations.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-19 (11:02)
 */
class TranslationCsvEntry {
    private final Locale locale;
    private final String component;
    private final String layerAsString;
    private final Layer layer;
    private final String key;
    private final String translation;

    static TranslationCsvEntry parseFrom(String csvLine, Locale locale) {
        String[] values = csvLine.split(",");
        return new TranslationCsvEntry(locale, values[0], values[1], values[2], values[3]);
    }

    private TranslationCsvEntry(Locale locale, String component, String layer, String key, String translation) {
        this.locale = locale;
        this.component = component;
        this.layerAsString = layer;
        this.layer = Layer.valueOf(layer);
        this.key = key;
        this.translation = translation;
    }

    void bindToInsertBatch(PreparedStatement statement) throws SQLException {
        statement.setString(1, this.component);
        statement.setString(2, this.layerAsString);
        statement.setString(3, this.key);
        statement.setString(4, this.translation);
        statement.setString(5, this.locale.toLanguageTag());
        statement.addBatch();
    }

    void bindToUpdateBatch(PreparedStatement statement) throws SQLException {
        statement.setString(1, this.translation);
        statement.setString(2, this.component);
        statement.setString(3, this.layerAsString);
        statement.setString(4, this.key);
        statement.setString(5, this.locale.toLanguageTag());
        statement.addBatch();
    }

    Pair<String, Layer> componentAndLayer() {
        return Pair.of(this.component, this.layer);
    }

    Optional<NlsKey> findKey(DataMapper<NlsKey> dataMapper) {
        return dataMapper.getOptional(this.component, this.layerAsString, this.key);
    }

    Optional<NlsEntry> findEntry(DataMapper<NlsEntry> dataMapper) {
        return dataMapper.getOptional(this.component, this.layerAsString, this.key, this.locale.toLanguageTag());
    }

}
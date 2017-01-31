/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;

import com.google.common.base.MoreObjects;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

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

    static TranslationCsvEntry parseFrom(String csvLine, Locale locale, String separator) {
        String[] values = csvLine.split(separator, 5);
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

    String component() {
        return this.component;
    }

    String layerAsString() {
        return this.layerAsString;
    }

    String key() {
        return this.key;
    }

    String translation() {
        return this.translation;
    }

    Pair<String, Layer> componentAndLayer() {
        return Pair.of(this.component, this.layer);
    }

    Optional<NlsEntry> findEntry(DataMapper<NlsEntry> dataMapper) {
        return dataMapper.getOptional(this.component, this.layerAsString, this.key, this.locale.toLanguageTag());
    }

    boolean validate(Logger logger) {
        if (Checks.is(this.translation).emptyOrOnlyWhiteSpace()) {
            logger.severe(() -> "translation for " + this.toString() + " is empty");
            return false;
        }
        if (this.translation.length() > Table.MAX_STRING_LENGTH) {
            logger.severe(() -> "translation for " + this.toString() + " is too long. Expected at most " + Table.MAX_STRING_LENGTH + " but got " + this.translation.length());
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("component", this.component)
                .add("layerAsString", this.layerAsString)
                .add("key", this.key)
                .toString();
    }

    public static void main(String[] args) {
        TranslationCsvEntry csvEntry = parseFrom("APR;REST;appServers.configureWebServicesSuccess;Webservice-Endpunkte gespeichert;", Locale.GERMANY, ";");
        System.out.println("csvEntry = " + csvEntry);
    }

}
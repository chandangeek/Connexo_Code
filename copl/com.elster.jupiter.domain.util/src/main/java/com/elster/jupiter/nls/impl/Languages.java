/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Currying;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.framework.BundleContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;

/**
 * Models the list of languages to which new NlsKeys will automatically
 * be translated from a list of CSV files that are found in the nls configuration directory.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-21 (14:25)
 */
final class Languages {

    private static final String LANGUAGES_LIST_PROPERTY_NAME = "com.elster.jupiter.nls.install";

    private static final String CONFIGURATION_DIRECTORY_PROPERTY_NAME = "com.elster.jupiter.nls.config.directory";
    private static final String DEFAULT_CONFIGURATION_DIRECTORY = "language-packs";

    private static final String CSV_SEPARATOR_CHAR_PROPERTY_NAME = "com.elster.jupiter.nls.csv.separator";
    private static final String DEFAULT_CSV_SEPARATOR = ",";

    private static final Logger LOGGER = Logger.getLogger(Languages.class.getName());

    private ConfigurationDirectory configurationDirectory;
    private String csvSeparator;
    private Map<String, Language> languages;

    static Languages withSettingsOf(NlsServiceImpl nlsService) {
        BundleContext context = nlsService.getBundleContext();
        String property = context.getProperty(LANGUAGES_LIST_PROPERTY_NAME);
        Languages languages = new Languages();
        languages.configurationDirectory = new ConfigurationDirectory(nlsService.getFileSystem(), context);
        languages.csvSeparator = context.getProperty(CSV_SEPARATOR_CHAR_PROPERTY_NAME);
        if (Checks.is(languages.csvSeparator).emptyOrOnlyWhiteSpace()) {
            LOGGER.warning(() -> "No value found for configuration property " + CSV_SEPARATOR_CHAR_PROPERTY_NAME + ", using default value: ','");
            languages.csvSeparator = DEFAULT_CSV_SEPARATOR;
        }
        if (!is(property).emptyOrOnlyWhiteSpace()) {
            languages.addLanguagesFromProperty(property);
        }
        return languages;
    }

    private void addLanguagesFromProperty(String property) {
        this.languages =
                Stream
                    .of(property.split(","))
                    .map((tag) -> new Language(tag, this.csvSeparator, this.configurationDirectory))
                    .collect(Collectors.toMap(
                            Language::getTag,
                            Function.identity()));
    }

    void addTranslationsTo(List<NlsKeyImpl> nlsKeys) {
        this.languages.values().forEach(language -> language.addTranslationsTo(nlsKeys));
    }

    private static final class ConfigurationDirectory {
        private final Path path;

        private ConfigurationDirectory(FileSystem fileSystem, BundleContext bundleContext) {
            String property = bundleContext.getProperty(CONFIGURATION_DIRECTORY_PROPERTY_NAME);
            if (is(property).emptyOrOnlyWhiteSpace()) {
                property = DEFAULT_CONFIGURATION_DIRECTORY;
            }
            this.path = fileSystem.getPath(property);
        }

        Stream<Path> filesForLanguageTag(String tag) {
            try {
                return Files.list(this.path).filter(subPath -> subPath.getFileName().toString().endsWith("_" + tag));
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }
    }

    private static final class SimpleTranslation {
        private final String key;
        private final String translation;

        public static SimpleTranslation from(TranslationCsvEntry csvEntry) {
            return new SimpleTranslation(csvEntry.key(), csvEntry.translation());
        }

        SimpleTranslation(String key, String translation) {
            this.key = key;
            this.translation = translation;
        }

        boolean matches(NlsKey nlsKey) {
            return this.key.equals(nlsKey.getKey());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SimpleTranslation that = (SimpleTranslation) o;
            return Objects.equals(key, that.key) &&
                    Objects.equals(translation, that.translation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, translation);
        }

        void addTo(NlsKeyImpl nlsKey, Locale locale) {
            nlsKey.add(locale, this.translation);
        }
    }

    static final class Language {
        private final ConfigurationDirectory configurationDirectory;
        private final String csvSeparator;
        private final String tag;
        private final Locale locale;
        private final SetMultimap<ComponentAndLayer, SimpleTranslation> translations = HashMultimap.create();

        private Language(String tag, String csvSeparator, ConfigurationDirectory configurationDirectory) {
            this.configurationDirectory = configurationDirectory;
            this.csvSeparator = csvSeparator;
            this.tag = tag;
            this.locale = Locale.forLanguageTag(tag);
            this.loadTranslations();
        }

        private void loadTranslations() {
            this.configurationDirectory
                    .filesForLanguageTag(this.tag)
                    .forEach(this::loadTranslationsFrom);
        }

        private void loadTranslationsFrom(Path path) {
            try {
                this.loadTranslationsFrom(Files.newInputStream(path));
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }

        private void loadTranslationsFrom(InputStream in) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                this.loadTranslationsWith(reader);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new UnderlyingIOException(e);
                }
            }
        }

        private void loadTranslationsWith(BufferedReader reader) {
            reader
                .lines()
                .map(csvLine -> TranslationCsvEntry.parseFrom(csvLine, this.locale, this.csvSeparator))
                .forEach(this::add);
        }

        private void add(TranslationCsvEntry entry) {
            ComponentAndLayer componentAndLayer = ComponentAndLayer.from(entry);
            this.translations.put(componentAndLayer, SimpleTranslation.from(entry));
        }

        String getTag() {
            return tag;
        }

        void addTranslationsTo(List<NlsKeyImpl> nlsKeys) {
            nlsKeys.forEach(this::addTranslationsTo);
        }

        private void addTranslationsTo(NlsKeyImpl nlsKey) {
            ComponentAndLayer componentAndLayer = ComponentAndLayer.from(nlsKey);
            this.translations
                    .get(componentAndLayer)
                    .stream()
                    .filter(Currying.test(SimpleTranslation::matches).with(nlsKey))
                    .findFirst()
                    .ifPresent(simpleTranslation -> {
                        simpleTranslation.addTo(nlsKey, this.locale);
                        this.translations.remove(componentAndLayer, simpleTranslation);
                    });
        }

    }

}
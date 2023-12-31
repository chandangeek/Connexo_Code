/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NlsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NlsService.class).to(NlsServiceImpl.class).in(Scopes.SINGLETON);
    }

    private static class FakeNlsMessageFormat implements NlsMessageFormat {
        private final String defaultFormat;

        FakeNlsMessageFormat(MessageSeed seed) {
            this.defaultFormat = seed.getDefaultFormat();
        }

        FakeNlsMessageFormat(TranslationKey key) {
            this.defaultFormat = key.getDefaultFormat();
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format(defaultFormat, args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return MessageFormat.format(defaultFormat, args);
        }
    }

    public enum FakeThesaurus implements Thesaurus {
        INSTANCE;

        @Override
        public String getString(String key, String defaultMessage) {
            return defaultMessage;
        }

        @Override
        public String getString(Locale locale, String key, String defaultMessage) {
            return defaultMessage;
        }

        @Override
        public NlsMessageFormat getFormat(MessageSeed seed) {
            return new FakeNlsMessageFormat(seed);
        }

        @Override
        public NlsMessageFormat getFormat(TranslationKey key) {
            return new FakeNlsMessageFormat(key);
        }

        @Override
        public NlsMessageFormat getSimpleFormat(MessageSeed seed) {
            return getFormat(seed);
        }

        @Override
        public Map<String, String> getTranslationsForCurrentLocale() {
            return Collections.emptyMap();
        }

        @Override
        public boolean hasKey(String key) {
            return false;
        }

        @Override
        public Thesaurus join(Thesaurus thesaurus) {
            return thesaurus instanceof SimpleThesaurus ? thesaurus : this;
        }

        @Override
        public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
            return DateTimeFormatter.ISO_DATE_TIME;
        }

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return messageTemplate;
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return messageTemplate;
        }
    }

    public static class SimpleThesaurus implements Thesaurus {
        private Map<String, String> translations;

        private SimpleThesaurus(Map<String, String> translations) {
            this.translations = translations;
        }

        private SimpleThesaurus(Collection<TranslationKey> translationKeys) {
            this(translationKeys.stream()
                    .collect(Collectors.toMap(TranslationKey::getKey, TranslationKey::getDefaultFormat)));
        }

        public static SimpleThesaurus from(Collection<TranslationKey> translationKeys) {
            return new SimpleThesaurus(translationKeys);
        }

        @Override
        public String getString(String key, String defaultMessage) {
            return Optional.ofNullable(translations.get(key)).orElse(defaultMessage);
        }

        @Override
        public String getString(Locale locale, String key, String defaultMessage) {
            return Optional.ofNullable(translations.get(key)).orElse(defaultMessage);
        }

        @Override
        public NlsMessageFormat getFormat(MessageSeed seed) {
            return new FakeNlsMessageFormat(seed);
        }

        @Override
        public NlsMessageFormat getFormat(TranslationKey key) {
            return new FakeNlsMessageFormat(key);
        }

        @Override
        public NlsMessageFormat getSimpleFormat(MessageSeed seed)  {
            return new FakeNlsMessageFormat(seed);
        }

        @Override
        public Map<String, String> getTranslationsForCurrentLocale() {
            return translations;
        }

        @Override
        public boolean hasKey(String key) {
            return translations.containsKey(key);
        }

        @Override
        public Thesaurus join(Thesaurus thesaurus) {
            return thesaurus instanceof SimpleThesaurus ?
                    new SimpleThesaurus(Stream.concat(translations.entrySet().stream(), ((SimpleThesaurus) thesaurus).translations.entrySet().stream())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a))) :
                    this;
        }

        @Override
        public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
            return DateTimeFormatter.ISO_DATE_TIME;
        }

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return messageTemplate;
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return messageTemplate;
        }
    }
}

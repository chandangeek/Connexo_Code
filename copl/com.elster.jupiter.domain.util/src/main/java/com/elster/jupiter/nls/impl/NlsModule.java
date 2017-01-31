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
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class NlsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NlsService.class).to(NlsServiceImpl.class).in(Scopes.SINGLETON);
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
        public Map<String, String> getTranslationsForCurrentLocale() {
            return Collections.emptyMap();
        }

        @Override
        public boolean hasKey(String key) {
            return false;
        }


        @Override
        public Thesaurus join(Thesaurus thesaurus) {
            return this;
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

    }

}
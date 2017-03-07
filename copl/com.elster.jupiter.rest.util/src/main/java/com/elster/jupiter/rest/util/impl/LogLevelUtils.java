/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

final class LogLevelUtils {

    private Thesaurus thesaurus;

    public LogLevelUtils(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    Level[] getUsedLogLevels() {
        return new Level[] {Level.SEVERE, Level.WARNING, Level.CONFIG, Level.FINER, Level.FINEST};
    }

    String getTranslation(Level level, Thesaurus thesaurus) {
        return thesaurus.getString(getKey(level), getDefaultTranslation(level));
    }

    String getKey(Level level) {
        return "loglevel." + level.getName();
    }

    String getDefaultTranslation(Level level) {
        if (Level.SEVERE.equals(level)) {
            return "Error";
        } else if (Level.WARNING.equals(level)) {
            return "Warning";
        } else if (Level.CONFIG.equals(level)) {
            return "Information";
        } else if (Level.FINER.equals(level)) {
            return "Debug";
        } else if (Level.FINEST.equals(level)) {
            return "Trace";
        }
        return "Unknown";
    }

    List<TranslationKey> getUsedLogLevelsAsTranslationKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        for (Level level : getUsedLogLevels()) {
            translationKeys.add(new LogLevelTranslationKey(getKey(level), getTranslation(level, thesaurus)));
        }
        return translationKeys;
    }

    class LogLevelTranslationKey implements TranslationKey {

        private String key;
        private String defaultFormat;

        LogLevelTranslationKey(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }
}

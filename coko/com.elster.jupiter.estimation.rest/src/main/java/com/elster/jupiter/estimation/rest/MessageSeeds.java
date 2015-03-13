package com.elster.jupiter.estimation.rest;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return EstimationService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public enum Keys {
        ;
        private static final String KEY_PREFIX = EstimationService.COMPONENTNAME + '.';
        public static final String DELETE_TASK_STATUS_BUSY = "DeleteTaskStatusBusy";
        public static final String DELETE_TASK_SQL_EXCEPTION = "DeleteTaskSqlException";
    }

    public enum Labels {
        SCHEDULED("estimation.occurrence.scheduled", "Scheduled"),
        ON_REQUEST("estimation.occurrence.onrequest", "On Request");

        private final String key;
        private final String defaultTranslation;

        Labels(String key, String defaultTranslation) {
            this.key = key;
            this.defaultTranslation = defaultTranslation;
        }

        public Translation toDefaultTransation() {
            return SimpleTranslation.translation(SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.REST, key), Locale.ENGLISH, defaultTranslation);
        }

        public String translate(Thesaurus thesaurus) {
            return thesaurus.getString(key, defaultTranslation);
        }

        public String translate(Thesaurus thesaurus, Locale locale) {
            return thesaurus.getString(locale, key, defaultTranslation);
        }
    }
}

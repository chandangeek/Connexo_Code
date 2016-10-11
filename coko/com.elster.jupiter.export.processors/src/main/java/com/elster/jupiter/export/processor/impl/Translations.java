package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 13:07
 */
@Component(name = "com.elster.jupiter.export.processor.translations", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
public class Translations implements TranslationKeyProvider, MessageSeedProvider {

    @Override
    public String getComponentName() {
        return DataExportService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>(Arrays.asList(Labels.values()));
        translationKeys.addAll(Arrays.asList(FormatterProperties.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    enum Labels implements TranslationKey {
        CSV_FORMATTER(StandardCsvDataFormatterFactory.NAME, "CSV formatter"),
        CSV_EVENTS_FORMATTER(StandardCsvEventDataFormatterFactory.NAME, "CSV formatter"),
        AND("des.processor.and", "and");

        private final String key;
        private final String defaultFormat;

        Labels(String key, String defaultFormat) {
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
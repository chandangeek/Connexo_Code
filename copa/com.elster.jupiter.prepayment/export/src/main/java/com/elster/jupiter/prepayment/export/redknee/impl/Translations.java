package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.export.DataExportService;
// import com.elster.jupiter.export.processor.impl.FormatterProperties;
// import com.elster.jupiter.export.processor.impl.MessageSeeds;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 13:07
 */
@Component(name = "com.elster.jupiter.export.redknee.translations", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
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
 //       translationKeys.addAll(Arrays.asList(FormatterProperties.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Collections.emptyList(); //Arrays.asList(MessageSeeds.values());
    }

    static enum Labels implements TranslationKey {
        CSV_PROCESSSOR(ReadingDataFormatterFactory.NAME, "RedKnee reading formatter");

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

        public String translate(Thesaurus thesaurus) {
            return thesaurus.getString(key, defaultFormat);
        }
    }
}

package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// import com.elster.jupiter.export.processor.impl.FormatterProperties;
// import com.elster.jupiter.export.processor.impl.MessageSeeds;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 13:07
 */
@Component(name = "com.elster.jupiter.export.redknee.translations", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
public class Translations implements TranslationKeyProvider, MessageSeedProvider {

    @Override
    public String getComponentName() {
        return "RKN";
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return new ArrayList<>(Arrays.asList(Labels.values()));
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Collections.emptyList(); //Arrays.asList(MessageSeeds.values());
    }

    enum Labels implements TranslationKey {
        CSV_PROCESSSOR(ReadingDataFormatterFactory.NAME, "Prepayment specific formatter"),
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

        public String translate(Thesaurus thesaurus) {
            return thesaurus.getString(key, defaultFormat);
        }
    }
}

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.imports.impl.translations", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
public class Translations implements TranslationKeyProvider, MessageSeedProvider {

    @Override
    public String getComponentName() {
        return FileImportService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Labels.values());
    }

    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    enum Labels implements TranslationKey {
        USAGEPOINT_FILE_IMPORTER(UsagePointFileImporterFactory.NAME, "Usage Point Importer");
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
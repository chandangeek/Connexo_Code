/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepoint.translations.UsagePointRecord", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
public class TranslationKeysUsagePointReadingImporter implements TranslationKeyProvider, MessageSeedProvider {
    @Override
    public List<MessageSeed> getSeeds() {
        return null;
    }

    @Override
    public String getComponentName() {
        return UsagePointReadingMessageHandlerFactory.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Labels.values());
    }

    public enum Labels implements TranslationKey {
        USAGEPOINT_READING_IMPORTER(UsagePointReadingImporterFactory.NAME, "Usage point Reading importer [STD]"),
        USAGEPOINT_MESSAGE_SUBSCRIBER(UsagePointReadingMessageHandlerFactory.SUBSCRIBER_NAME, "Handle usage point reading import");

        private String key;
        private String defaultFormat;

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

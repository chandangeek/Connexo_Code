/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.export.processor.translations", service = {TranslationKeyProvider.class}, immediate = true)
public class Translations implements TranslationKeyProvider {

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
        return Stream.of(labels())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<MessageSeeds> messageSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private List<TranslationKey> labels() {
        return Arrays.asList(Labels.values());
    }

    static enum Labels implements TranslationKey {
        AND("des.and", "and");

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
            return thesaurus.getFormat(this).format();
        }
    }

}
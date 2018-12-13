/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo.impl;

import com.elster.jupiter.cbo.I18N;
import com.elster.jupiter.cbo.MessageSeeds;
import com.elster.jupiter.cbo.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.cbo.impl.CboMessageSeedProvider",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + I18N.COMPONENT_NAME},
        immediate = true)
public class CboTranslationProvider implements TranslationKeyProvider, MessageSeedProvider {

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return I18N.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

}
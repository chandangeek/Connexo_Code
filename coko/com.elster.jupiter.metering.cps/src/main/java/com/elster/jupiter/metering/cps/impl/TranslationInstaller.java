/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(
        name = "com.elster.jupiter.metering.cps.impl.TranslationInstaller",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + TranslationInstaller.COMPONENT_NAME},
        immediate = true)
public class TranslationInstaller implements TranslationKeyProvider, MessageSeedProvider {
    public static final String COMPONENT_NAME = "CPM";

    public volatile NlsService nlsService;

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

}


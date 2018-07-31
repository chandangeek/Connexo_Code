/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "om.elster.jupiter.metering.cps.doa.impl.TranslationInstaller",
        service = {TranslationKeyProvider.class},
        property = {"name=" + TranslationInstaller.COMPONENT_NAME},
        immediate = true)
public class TranslationInstaller implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "DOA";

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
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }
}


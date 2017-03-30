/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.energyict.mdc.device.config.cps.CustomPropertySetsDemoInstaller",
        service = {CustomPropertySetsDemoInstaller.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = "name=CPD", immediate = true)
public class CustomPropertySetsDemoInstaller implements TranslationKeyProvider, MessageSeedProvider {
    public static final String COMPONENT_NAME = "CPD";

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
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
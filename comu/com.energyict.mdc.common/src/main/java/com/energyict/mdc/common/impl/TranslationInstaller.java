package com.energyict.mdc.common.impl;


import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.common.impl.TranslationInstaller",
        service = {MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + MessageSeeds.COMPONENT_NAME,
        immediate = true)
public class TranslationInstaller implements MessageSeedProvider, TranslationKeyProvider {

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
        return MessageSeeds.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(ObisCodeTranslationKeys.values());
    }

}
package com.energyict.mdc.common.impl;


import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.common.impl.TranslationInstaller",
        service = {TranslationKeyProvider.class},
        property = "name=" + MessageSeeds.COMPONENT_NAME,
        immediate = true)
public class TranslationInstaller implements TranslationKeyProvider {

    public TranslationInstaller() {
    }

    @Override
    public String getComponentName() {
        return MessageSeeds.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
}

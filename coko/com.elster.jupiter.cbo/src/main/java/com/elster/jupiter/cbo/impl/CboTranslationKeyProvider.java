package com.elster.jupiter.cbo.impl;

import com.elster.jupiter.cbo.MessageSeeds;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.cbo.impl.CboTranslationKeyProvider",
        service = {TranslationKeyProvider.class},
        property = {"name=" + CboTranslationKeyProvider.COMPONENT_NAME},
        immediate = true)
public class CboTranslationKeyProvider implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "CBO";

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
        return Arrays.asList(MessageSeeds.values());
    }
}

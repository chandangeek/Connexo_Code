package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.metering.cps.impl.TranslationInstaller",
        service = {TranslationKeyProvider.class},
        property = {"name=" + TranslationInstaller.COMPONENT_NAME},
        immediate = true)
public class TranslationInstaller implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "CPM";

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

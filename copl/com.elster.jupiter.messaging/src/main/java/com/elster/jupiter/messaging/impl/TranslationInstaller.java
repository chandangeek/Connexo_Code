package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.messaging.impl.TranslationInstaller",
        service = {TranslationKeyProvider.class},
        property = "name=" + MessageService.COMPONENTNAME,
        immediate = true)
public class TranslationInstaller implements TranslationKeyProvider {

    public TranslationInstaller() {
    }

    @Override
    public String getComponentName() {
        return MessageService.COMPONENTNAME;
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

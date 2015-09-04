package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.MessageSeeds;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.messaging.impl.TranslationInstaller",
        service = {MessageSeedProvider.class},
        property = "name=" + MessageService.COMPONENTNAME,
        immediate = true)
public class TranslationInstaller implements MessageSeedProvider {

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
package com.elster.jupiter.cbo.impl;

import com.elster.jupiter.cbo.MessageSeeds;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.cbo.impl.CboMessageSeedProvider",
        service = {MessageSeedProvider.class},
        property = {"name=" + CboMessageSeedProvider.COMPONENT_NAME},
        immediate = true)
public class CboMessageSeedProvider implements MessageSeedProvider {
    public static final String COMPONENT_NAME = "CBO";

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
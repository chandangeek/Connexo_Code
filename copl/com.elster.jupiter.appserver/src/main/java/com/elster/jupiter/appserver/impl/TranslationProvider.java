package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 6/10/2014
 * Time: 12:03
 */
@Component(name = "com.elster.jupiter.appserver.translations", service = {MessageSeedProvider.class}, property = {"name=" + AppService.COMPONENT_NAME}, immediate = true)
public class TranslationProvider implements MessageSeedProvider {

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
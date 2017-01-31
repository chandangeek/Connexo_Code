/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.impl;


import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.energyict.mdc.common.impl.TranslationInstaller",
        service = {MessageSeedProvider.class},
        property = "name=" + MessageSeeds.COMPONENT_NAME,
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
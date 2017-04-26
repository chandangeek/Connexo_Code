/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.upl.io.SerialComponentService;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Installs the {@link MessageSeeds} and TranslationKeys of this mdc.io bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (17:42)
 */
@Component(name = "com.energyict.mdc.io.nls.installer", service = {MessageSeedProvider.class, TranslationKeyProvider.class}, property = {"name=" + SerialComponentService.COMPONENT_NAME})
@SuppressWarnings("unused")
public class NlsInstaller implements MessageSeedProvider, TranslationKeyProvider {

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return
                Stream
                        .concat(
                                Stream.of(MessageSeeds.values()),
                                Stream.of(ModemException.MessageSeeds.values()))
                        .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return SerialComponentService.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
    }

}
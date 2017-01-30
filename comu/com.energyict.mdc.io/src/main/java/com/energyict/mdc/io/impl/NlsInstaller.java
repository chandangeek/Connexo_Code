package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.io.SerialComponentService;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Installs the {@link MessageSeeds} and {@link TranslationKeys} of this mdc.io bundle.
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
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return SerialComponentService.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

}
package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SerialComponentService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Installs the {@link MessageSeeds} of this mdc.io bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (17:42)
 */
@Component(name = "com.energyict.mdc.io.nls.installer", service = TranslationKeyProvider.class, property = {"name=" + SerialComponentService.COMPONENT_NAME})
public class MessageSeedsInstaller implements TranslationKeyProvider {

    @Override
    public String getComponentName() {
        return SerialComponentService.COMPONENT_NAME;
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
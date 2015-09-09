package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SerialComponentService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Installs the {@link MessageSeeds} of this mdc.io bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (17:42)
 */
@Component(name = "com.energyict.mdc.io.nls.installer", service = MessageSeedProvider.class, property = {"name=" + SerialComponentService.COMPONENT_NAME})
public class MessageSeedsInstaller implements MessageSeedProvider {

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
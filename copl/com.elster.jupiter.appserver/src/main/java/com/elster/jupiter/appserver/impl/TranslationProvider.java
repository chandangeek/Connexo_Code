package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.appserver.security.Privileges;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 6/10/2014
 * Time: 12:03
 */
@Component(name = "com.elster.jupiter.appserver.translations", service = {TranslationKeyProvider.class}, property = {"name=" + AppService.COMPONENT_NAME}, immediate = true)
public class TranslationProvider implements TranslationKeyProvider {

    @Override
    public String getComponentName() {
        return AppService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(MessageSeeds.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }
}

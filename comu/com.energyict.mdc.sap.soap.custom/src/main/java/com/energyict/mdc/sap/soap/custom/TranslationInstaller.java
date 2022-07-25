/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler;
import com.energyict.mdc.sap.soap.custom.export.MessageSeeds;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.sap.soap.custom.TranslationInstaller",
        service = {MessageSeedProvider.class, TranslationKeyProvider.class, TranslationInstaller.class},
        immediate = true)
public class TranslationInstaller implements MessageSeedProvider, TranslationKeyProvider {
    private volatile Thesaurus thesaurus;

    @Override
    public String getComponentName() {
        return CustomSAPDeviceEventHandler.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                com.energyict.mdc.sap.soap.custom.export.TranslationKeys.values(),
                com.energyict.mdc.sap.soap.custom.meterreadingdocument.TranslationKeys.values(),
                com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.TranslationKeys.values())
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Stream.of(
                MessageSeeds.values(),
                com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.MessageSeeds.values())
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}

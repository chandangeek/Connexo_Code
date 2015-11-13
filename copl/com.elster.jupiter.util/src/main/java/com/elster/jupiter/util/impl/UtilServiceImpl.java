package com.elster.jupiter.util.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.UtilService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.util",
        service = {UtilService.class, TranslationKeyProvider.class},
        property = "name=" + UtilService.COMPONENT_NAME,
        immediate = true)
public class UtilServiceImpl implements UtilService, TranslationKeyProvider {

    private volatile Thesaurus thesaurus;

    public UtilServiceImpl() {
    }

    @Inject
    public UtilServiceImpl(NlsService nlsService) {
        this();
        this.setThesaurus(nlsService);
    }


    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UtilService.COMPONENT_NAME, Layer.DOMAIN);
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        Arrays.stream(TranslationKeys.values()).forEach(translationKeys::add);
        return translationKeys;
    }



}

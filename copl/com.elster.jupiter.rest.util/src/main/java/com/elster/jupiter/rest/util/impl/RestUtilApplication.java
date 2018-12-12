/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.jupiter.tasks.rest", service = {Application.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/rut", "app=SYS", "name=" + RestUtilApplication.COMPONENT_NAME})
public class RestUtilApplication extends Application implements TranslationKeyProvider {

    public static final String COMPONENT_NAME = "RUT";

    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(LogLevelResource.class);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public String getComponentName() {
        return this.COMPONENT_NAME;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.addAll(super.getSingletons());
        singletons.add(new HK2Binder());
        return singletons;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(thesaurus).to(Thesaurus.class);
        }
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return new LogLevelUtils(this.thesaurus).getUsedLogLevelsAsTranslationKeys();
    }
}

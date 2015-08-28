package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.InfoService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.rest.MessageSeeds;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 6/1/15.
 */
@Component(name = "com.elster.jupiter.search.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/jsr", "app=MDC", "name=" + SearchApplication.COMPONENT_NAME})
public class SearchApplication extends Application implements TranslationKeyProvider{

    private final Logger logger = Logger.getLogger(SearchApplication.class.getName());

    public static final String COMPONENT_NAME = "JSR";

    private volatile SearchService searchService;
    private volatile Thesaurus thesaurus;
    private volatile InfoService infoService;


    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                SearchResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }


    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setInfoService(InfoService infoService) {
        this.infoService = infoService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(searchService).to(SearchService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(infoService).to(InfoService.class);
            bind(PropertyInfoFactory.class).to(PropertyInfoFactory.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
        }
    }

}

package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.autotests.rest",
        service = {Application.class},
        immediate = true,
        property = {"alias=/test", "app=INS", "name=" + TestUsagePointResourceApplication.COMPONENT_NAME})
public class TestUsagePointResourceApplication extends Application {
    public static final String COMPONENT_NAME = "TEST";

    private volatile MeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService){
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Activate
    public void activate() {
        System.out.println("Activated.");
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(UsagePointTestResource.class,
                MetrologyConfigurationTestResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        public void configure() {
            bind(meteringService).to(MeteringService.class);
            bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
            bind(ormService).to(OrmService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }
}

package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.layer.LinkQualityLayer;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.topology.graph", service = {Application.class}, immediate = true, property = {"alias=/dtg", "app="+TopologyGraphApplication.APP_KEY, "name=" + TopologyGraphApplication.COMPONENT_NAME})
public class TopologyGraphApplication extends Application implements TranslationKeyProvider,MessageSeedProvider {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DTG";

    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile GraphLayerService graphLayerService;
    private volatile Clock clock;

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TopologyGraphResource.class
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setGraphLayerService(GraphLayerService graphLayerService) {
        this.graphLayerService = graphLayerService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    public Clock getClock() {
        return clock;
    }
    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>(Arrays.asList(LinkQualityLayer.PropertyNames.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds(){
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(topologyService).to(TopologyService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(deviceService).to(DeviceService.class);
            bind(graphLayerService).to(GraphLayerService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(clock).to(Clock.class);
        }
    }
}



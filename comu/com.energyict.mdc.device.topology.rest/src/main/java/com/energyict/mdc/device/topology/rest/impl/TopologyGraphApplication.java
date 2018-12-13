package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.*;

@Component(name = "com.energyict.mdc.device.topology.graph", service = {Application.class}, immediate = true, property = {"alias=/dtg", "app="+TopologyGraphApplication.APP_KEY, "name=" + TopologyGraphApplication.COMPONENT_NAME})
public class TopologyGraphApplication extends Application implements MessageSeedProvider {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DTG";

    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile GraphLayerService graphLayerService;
    private volatile Clock clock;

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceGraphFactory deviceGraphFactory;
    private volatile BundleContext context;

    @Activate
    public void activate(BundleContext context) {
        // DeviceGraphFactory has a cache of GraphInfo's
        setDeviceGraphFactory(new DeviceGraphFactory(topologyService, graphLayerService, clock));
        setContext(context);
    }

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

    public void setDeviceGraphFactory(DeviceGraphFactory deviceGraphFactory) {
        this.deviceGraphFactory = deviceGraphFactory;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds(){
        return Arrays.asList(MessageSeeds.values());
    }

    public void setContext(BundleContext context) {
        this.context = context;
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
            bind(deviceGraphFactory).to(DeviceGraphFactory.class);
            bind(context).to(BundleContext.class);
        }
    }
}



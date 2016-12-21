package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.topology.rest", service = {Application.class}, immediate = true, property = {"alias=/"+TopologyGraphApplication.COMPONENT_NAME, "app="+TopologyGraphApplication.APP_KEY, "name=" + TopologyGraphApplication.COMPONENT_NAME})
public class TopologyGraphApplication extends Application  {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DTG";

    private volatile Provider<GraphFactory> graphFactory;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;

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
    public void setDeviceService(DeviceService deviceServicee) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

/*  MessageSeedProvider
    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
*/

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(topologyService).to(TopologyService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
        }
    }
}



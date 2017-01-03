package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.concurrent.OptionalServiceContainer;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerService;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 16:11
 */
@Component(name = "com.energyict.mdc.device.topology.graphlayer.service", service = {GraphLayerService.class}, property = "name=" + GraphLayerService.COMPONENT_NAME)
public class GraphLayerServiceImpl implements GraphLayerService {
    private final OptionalServiceContainer<GraphLayer> graphLayerServices = new CopyOnWriteServiceContainer<>();

    @Override
    @Reference(name = "DTG-layer", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void register(GraphLayer graphLayer) {
        this.graphLayerServices.register(graphLayer);
    }

    @Override
    public void unregister(GraphLayer graphLayer) {
        this.graphLayerServices.unregister(graphLayer);
    }

    @Override
    public List<GraphLayer> getGraphLayers() {
        return graphLayerServices.getServices();
    }

    @Override
    public Optional<GraphLayer> getGraphLayer(GraphLayerType type, String name) {
        return this.getGraphLayers().stream().filter(each -> each.getType() == type && each.getName().equals(name)).findFirst();
    }
}

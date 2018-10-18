/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerCalculationMode;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.device.topology.PLCDetailsLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class PLCDetailsLayer extends AbstractGraphLayer<Device> {

    private TopologyService topologyService;
    private volatile Thesaurus thesaurus;

    public enum PropertyNames implements TranslationKey {
        STATE("state", "State"),
        PHASE_INFO("phaseInfo", "Phase info"),
        MODULATION("modulation", "Modulation"),
        LINKQUALITYINDICATOR("linkQualityIndicator", "Link quality indicator"),
        NODE_ADDRESS("nodeAddress", "Node address"),
        LINK_COST("linkCost", "Link cost"),
        ROUND_TRIP("roundTrip", "Round trip");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat) {
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return LayerNames.PLCDetailsLayer.fullName() + ".node." + propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return LayerNames.DeviceInfoLayer.fullName();
    }

    @Override
    public GraphLayerCalculationMode getCalculationMode() {
        return GraphLayerCalculationMode.IMMEDIATE;
    }

    @Reference
    public void setTopologyServiceService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public void setPhaseInfo(String value) {
        setProperty(PropertyNames.PHASE_INFO.getPropertyName(), value);
    }

    public void setModulation(String value) {
        setProperty(PropertyNames.MODULATION.getPropertyName(), value);
    }

    public void setState(String value) {
        setProperty(PropertyNames.STATE.getPropertyName(), value);
    }

    public void setLinkQualityIndicator(long value) {
        setProperty(PropertyNames.LINKQUALITYINDICATOR.getPropertyName(), value);
    }

    public void setNodeAddress(String value) {
        setProperty(PropertyNames.NODE_ADDRESS.getPropertyName(), value);
    }

    public void setLinkCost(long value) {
        setProperty(PropertyNames.LINK_COST.getPropertyName(), value);
    }

    public void setRoundTrip(long value) {
        setProperty(PropertyNames.ROUND_TRIP.getPropertyName(), value);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

    public Map<String, Object> getProperties(NodeInfo<Device> nodeInfo) {
        Device device = ((DeviceNodeInfo) nodeInfo).getDevice();
        Device parentNodeInfo = ((DeviceNodeInfo) nodeInfo).getParent();
        if (parentNodeInfo == null) {
            return propertyMap();
        }
        List<G3Neighbor> neighbors = topologyService.findG3Neighbors(parentNodeInfo);
        neighbors.stream().filter(g3Neighbor -> g3Neighbor.getNeighbor().getmRID().equals(device.getmRID()))
                .findFirst()
                .ifPresent(g3Neighbor -> {
                    this.setPhaseInfo(g3Neighbor.getPhaseInfo().toString());
                    this.setModulation(g3Neighbor.getModulation().toString());
                    this.setState(thesaurus.getFormat(g3Neighbor.getState()).format());
                    this.setLinkQualityIndicator(g3Neighbor.getLinkQualityIndicator());
                    this.setNodeAddress(g3Neighbor.getNodeAddress());
                    this.setLinkCost(g3Neighbor.getLinkCost());
                    this.setRoundTrip(g3Neighbor.getRoundTrip());
                });
        return propertyMap();
    }

}

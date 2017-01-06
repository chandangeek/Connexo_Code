package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * GraphLayer - Link quality properties
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:13
 */
@Component(name = "com.energyict.mdc.device.topology.LinkQualityLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class LinkQualityLayer extends AbstractGraphLayer<Device> {

    private TopologyService topologyService;

    private final static String NAME = "topology.GraphLayer.linkQuality";

    public enum PropertyNames implements TranslationKey{
        LINK_QUALITY("linkQuality", "Link quality");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat){
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return NAME + ".link." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
        }

        public String getPropertyName(){
            return propertyName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.LINK;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(TopologyService topologyService){
        this.topologyService = topologyService;
    }

    public void calculateLinkQuality(NodeInfo<Device> info){
//        Random random = new Random();
//        this.setLinkQuality(random.nextInt(100));
        getNeighbor((DeviceNodeInfo) info).ifPresent((x) -> setLinkQuality(x.getLinkQualityIndicator()));
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> info) {
        calculateLinkQuality(info);
        return propertyMap();
    }

    public void setLinkQuality(int quality){
        this.setProperty(PropertyNames.LINK_QUALITY.getPropertyName(), "" + quality);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

    private Optional<G3Neighbor> getNeighbor(DeviceNodeInfo info){
        // downstream link
        return topologyService.findG3Neighbors(((DeviceNodeInfo)info.getParent()).getDevice()).stream().filter((n) -> n.getNeighbor().getId() == info.getId()).findFirst();
    }
}

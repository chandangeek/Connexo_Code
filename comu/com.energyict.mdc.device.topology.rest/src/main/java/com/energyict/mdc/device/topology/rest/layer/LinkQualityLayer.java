package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * GraphLayer - Link quality properties
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:13
 */
@Component(name = "com.energyict.mdc.device.topology.LinkQualityLayer", service = GraphLayer.class, immediate = true)
public class LinkQualityLayer extends AbstractGraphLayer {

    private final static String NAME = "topology.GraphLayer.Links.linkQuality";

    public enum PropertyNames implements TranslationKey{
        LINK_QUALITY("linkQuality", "link Quality");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat){
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return NAME + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
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

    public void calculateLinkQuality(NodeInfo info){
        Random random = new Random();
        this.setLinkQuality(random.nextInt(100));
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo info) {
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
}

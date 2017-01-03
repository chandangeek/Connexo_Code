package com.energyict.mdc.device.topology.rest.info;


import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

/**
 * Default GraphLayer - Properties of links
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:13
 */
public class LinkQualityLayer extends AbstractGraphLayer {

    private final static String NAME = PropertyNames.LINK_QUALITY.key;

    public enum PropertyNames implements TranslationKey{
        LINK_QUALITY("graphLayer.Links.linkQuality", "linkQuality");

        private String key;
        private String defaultFormat;

        PropertyNames(String key, String defaultFormat){
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    public LinkQualityLayer(){}

    public LinkQualityLayer(int quality){
        super();
        this.setLinkQuality(quality);
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.LINK;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void setLinkQuality(int quality){
        this.setProperty(PropertyNames.LINK_QUALITY.getDefaultFormat(), "" + quality);
    }
}

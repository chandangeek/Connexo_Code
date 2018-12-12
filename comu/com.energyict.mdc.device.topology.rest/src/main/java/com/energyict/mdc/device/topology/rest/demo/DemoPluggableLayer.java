package com.energyict.mdc.device.topology.rest.demo;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerCalculationMode;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;
import com.energyict.mdc.device.topology.rest.layer.AbstractGraphLayer;

import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 17:00
 */
@Component(name = "com.energyict.mdc.demo.DemoPluggableLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class DemoPluggableLayer extends AbstractGraphLayer<Device> {

    private final static String NAME = "Demo Pluggable";
    private final static String DEFAULT_FORMAT = "Demo pluggability of 'Graph layers'";

    public DemoPluggableLayer(){
        super();
        setProperty("usagePoint", "The device's usagepoint name");
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return NAME ;
    }

    public String getDisplayName(Thesaurus thesaurus){
        return thesaurus.getString(NAME,DEFAULT_FORMAT);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
    }

    public Map<String, Object> getProperties(NodeInfo<Device> nodeInfo) {
        return propertyMap();
    }

}

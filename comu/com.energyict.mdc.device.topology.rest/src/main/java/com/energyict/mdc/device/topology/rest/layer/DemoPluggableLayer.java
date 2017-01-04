package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 17:00
 */
@Component(name = "com.energyict.mdc.demo.DemoPluggableLayer", service = GraphLayer.class, immediate = true)
public class DemoPluggableLayer extends AbstractGraphLayer {

    private final static String NAME = "Demo Pluggable";


    public DemoPluggableLayer(){
        super();
        setProperty("is Demo", ""+Boolean.TRUE);
    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return NAME ;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
    }

}

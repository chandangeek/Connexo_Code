package com.energyict.mdc.device.topology.rest.layer;

import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;

import org.osgi.service.component.annotations.Component;

import java.util.Map;

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


}

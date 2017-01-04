package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.impl.TopologyGraphApplication;


import java.util.HashMap;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:18
 */
public abstract class AbstractGraphLayer implements GraphLayer, TranslationKeyProvider {

    private Properties properties = new Properties();

    public void setProperty(String propertyName, String propertyValue){
        this.properties.put(propertyName, propertyValue);
    }

    public void getProperty(String propertyName){
        this.properties.getProperty(propertyName);
    }

    @Override
    public String getComponentName() {
        return TopologyGraphApplication.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public boolean equals(Object o){
        return (o instanceof GraphLayer) && getName().equals(((GraphLayer) o).getName()) && getType().equals(((GraphLayer) o).getType());
    }

    @Override
    public int hashCode(){
        return getType().ordinal() * getName().hashCode();
    }

    protected HashMap<String, Object> propertyMap(){
        HashMap<String, Object> result = new HashMap<>();
        properties.keySet().stream().forEach(key -> result.put((String) key, properties.getProperty((String) key)));
        return result;
    }

}

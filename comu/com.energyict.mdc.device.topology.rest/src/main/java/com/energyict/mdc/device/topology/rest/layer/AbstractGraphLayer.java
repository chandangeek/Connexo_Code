package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerCalculationMode;
import com.energyict.mdc.device.topology.rest.impl.TopologyGraphApplication;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * Base class for providing additional information about a NodeInfo
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:18
 */
public abstract class AbstractGraphLayer<T extends HasId> implements GraphLayer<T>, TranslationKeyProvider {

    private Properties properties = new Properties();
    private boolean active ;

    public void setProperty(String propertyName, Object propertyValue){
        if (propertyValue == null){
            this.properties.remove(propertyName);
        }else {
            this.properties.put(propertyName, propertyValue);
        }
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive(){
        return this.getCalculationMode() == GraphLayerCalculationMode.IMMEDIATE || this.active;
    }

    @Override
    public Object getProperty(String propertyName){
        return this.properties.get(propertyName);
    }

    // TranslationKeyProvider
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
        for (Enumeration<?> e = properties.keys(); e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            result.put(key, properties.get(key));
        }
        properties.clear();
        return result;
    }

}

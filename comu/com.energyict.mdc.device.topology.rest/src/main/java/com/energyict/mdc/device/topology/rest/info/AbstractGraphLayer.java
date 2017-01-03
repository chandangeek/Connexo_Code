package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.topology.rest.GraphLayer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 3/01/2017
 * Time: 11:18
 */
public abstract class AbstractGraphLayer implements GraphLayer{

    private Properties properties = new Properties();

    public Map<String, Object> getProperties() {
        HashMap<String, Object> result = new HashMap<>();
        properties.keySet().stream().forEach(key -> result.put((String) key, properties.getProperty((String) key)));
        return result;
    }

    public void setProperty(String propertyName, String propertyValue){
        this.properties.put(propertyName, propertyValue);
    }

    public void getProperty(String propertyName){
        this.properties.getProperty(propertyName);
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof GraphLayer){
            return this.getName().equals(((GraphLayer) o).getName()) && this.getType().equals(((GraphLayer) o).getType());
        }
        return false;
    }

    @Override
    public int hashCode(){
        return getType().ordinal() * getName().hashCode();
    }

}

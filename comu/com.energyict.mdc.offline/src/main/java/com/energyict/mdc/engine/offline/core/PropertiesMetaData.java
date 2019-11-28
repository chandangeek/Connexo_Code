package com.energyict.mdc.engine.offline.core;

import java.io.Serializable;
import java.util.*;

/**
 * @author Karel
 */
public class PropertiesMetaData implements Serializable {

    private List keys = new ArrayList();
    private Map possibleValuesMap = new HashMap();

    /**
     * Creates a new instance of PropertiesMetaData
     */
    public PropertiesMetaData() {
    }

    public List getKeys() {
        return keys;
    }

    public List getPossibleValues(String key) {
        return (List) possibleValuesMap.get(key);
    }

    public void setKeys(List keys) {
        this.keys = keys;
    }

    public void addKey(String key) {
        this.keys.add(key);
    }

    public void setPossibleValues(String key, List possibleValues) {
        this.possibleValuesMap.put(key, possibleValues);
    }

    public void sort() {
        Collections.sort(keys);
    }

    public String getDescription(String key, String value) {
        List possibleValues = getPossibleValues(key);
        if (possibleValues == null) {
            return null;
        }
        Iterator it = possibleValues.iterator();
        while (it.hasNext()) {
            Association association = (Association) it.next();
            if (association.getKey().equals(value)) {
                return association.getValue();
            }
        }
        return null;
    }
}

package com.energyict.mdc.rest.impl.comserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides simple one on one translation of javascript value to server values
 * @param <V>
 */
public class MapBasedXmlAdapter<V> extends XmlAdapter<String, V>{

    private final Map<String, V> map = new HashMap<>();

    protected final void register(String displayValue, V serverValue) {
        map.put(displayValue, serverValue);
    }

    public final Set<String> getClientSideValues() {
        return map.keySet();
    }

    @Override
    public V unmarshal(String text) throws Exception {
        if (map.containsKey(text)) {
            return map.get(text);
        }
        throw new IllegalArgumentException("'"+text+"' could not be mapped to an known value");
    }

    @Override
    public String marshal(V value) throws Exception {
        if (value!=null) {
            for (Map.Entry<String, V> entry : map.entrySet()) {
                if (value.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        throw new IllegalStateException(value+" is not a known server value ");
    }

}

package com.energyict.mdc.engine.impl.core.remote;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

/**
 * Provides mapping services for the JAXB marshalling mechanism
 * for the {@link java.util.HashMap} component.
 *
 * @author sva
 * @since 24/07/2014 - 15:39
 */
public class MapXmlMarshallAdapter extends XmlAdapter<MapXmlAdaptation, Map> {

    @Override
    public Map unmarshal(MapXmlAdaptation v) throws Exception {
        return v.unmarshallHashMap();
    }

    @Override
    public MapXmlAdaptation marshal(Map v) throws Exception {
        return new MapXmlAdaptation(v);
    }
}

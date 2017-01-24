package com.energyict.xml;


import com.energyict.mdc.protocol.api.DeviceProtocolCache;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides mapping services for the JAXB marshalling mechanism
 * for the {@link DeviceProtocolCache} component.
 *
 * @author sva
 * @since 12/0514 - 15:44
 */
public class DeviceProtocolCacheXmlMarshallAdapter extends XmlAdapter<DeviceProtocolCacheXmlAdaptation, DeviceProtocolCache> {

    @Override
    public DeviceProtocolCache unmarshal (DeviceProtocolCacheXmlAdaptation v) throws Exception {
        return v.unmarshallDeviceProtocolCache();
    }

    @Override
    public DeviceProtocolCacheXmlAdaptation marshal (DeviceProtocolCache v) throws Exception {
        return new DeviceProtocolCacheXmlAdaptation(v);
    }

}
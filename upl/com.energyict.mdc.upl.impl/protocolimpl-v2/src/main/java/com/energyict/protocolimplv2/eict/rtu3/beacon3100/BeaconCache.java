package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;
import com.energyict.protocol.support.FrameCounterCache;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Extension of the {@link DLMSCache}, adding support to store frame counters of the Beacon3100 device.
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/11/2016 - 13:07
 */
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class BeaconCache extends DLMSCache implements DeviceProtocolCache, FrameCounterCache, Serializable {

    protected Map<Integer, Long> frameCounters = new HashMap<>();

    public BeaconCache() {
    }

    @Override
    public void setTXFrameCounter(final int clientId, long frameCounter) {
        frameCounters.put(clientId, frameCounter);
        setChanged(true);
    }

    @Override
    public long getTXFrameCounter(final int clientId) {
        if (frameCounters.containsKey(clientId)) {
            return frameCounters.get(clientId);
        } else {
            return -1;
        }
    }
}
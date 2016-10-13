package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.mdc.protocol.DeviceProtocolCacheXmlMarshallAdapter;
import com.energyict.mdc.protocol.ServerDeviceProtocolCache;
import com.energyict.protocol.support.FrameCounterCache;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sva
 * @since 27/08/2015 - 11:54
 */
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class AM540Cache extends DLMSCache implements ServerDeviceProtocolCache, FrameCounterCache, Serializable {

    private boolean connectionToBeaconMirror;

    UniversalObject[] mirrorObjectList;
    UniversalObject[] gatewayObjectList;

    protected Map<Integer, Long> frameCountersGateway = new HashMap<>();
    protected Map<Integer, Long> frameCountersMirror = new HashMap<>();

    public AM540Cache(boolean connectionToBeaconMirror) {
        this.connectionToBeaconMirror = connectionToBeaconMirror;
    }

    @Override
    public void saveObjectList(UniversalObject[] objectList) {
        if (isConnectionToBeaconMirror()) {
            this.mirrorObjectList = objectList;
        } else {
            this.gatewayObjectList = objectList;
        }
        setChanged(true);
    }

    @Override
    public UniversalObject[] getObjectList() {
        return isConnectionToBeaconMirror() ? mirrorObjectList : gatewayObjectList;
    }

    public boolean isConnectionToBeaconMirror() {
        return connectionToBeaconMirror;
    }

    public void setConnectionToBeaconMirror(boolean connectionToBeaconMirror) {
        this.connectionToBeaconMirror = connectionToBeaconMirror;
    }

    @Override
    @Deprecated // The AM540 meter doesn't have this counter - so method should not be used
    public void setConfProgChange(int confProgChange) {
        super.setConfProgChange(confProgChange);
    }

    @Override
    @Deprecated // The AM540 meter doesn't have this counter - so method should not be used
    public int getConfProgChange() {
        return super.getConfProgChange();
    }

    public void setTXFrameCounter(final int clientId, long frameCounter){
        if (isConnectionToBeaconMirror()) {
            frameCountersMirror.put(clientId, frameCounter);
        } else {
            frameCountersGateway.put(clientId, frameCounter);
        }
        setChanged(true);
    }

    @Override
    public void setTXFrameCounter(int clientId, int frameCounter) {

    }

    @Override
    public long getTXFrameCounter(final int clientId){
        if (isConnectionToBeaconMirror()) {
            if (frameCountersMirror.containsKey(clientId)) {
                return frameCountersMirror.get(clientId);
            } else {
                return -1;
            }
        } else {
            if (frameCountersGateway.containsKey(clientId)) {
                return frameCountersGateway.get(clientId);
            } else {
                return -1;
            }
        }
    }

}
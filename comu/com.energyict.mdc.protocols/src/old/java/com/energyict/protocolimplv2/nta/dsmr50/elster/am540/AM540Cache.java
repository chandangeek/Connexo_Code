package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.xml.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 29.09.15
 * Time: 10:41
 */
@XmlRootElement
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class AM540Cache extends DLMSCache implements Serializable {

    UniversalObject[] mirrorObjectList;
    UniversalObject[] gatewayObjectList;
    private long frameCounter = 1;
    private boolean connectionToBeaconMirror;

    /*Default constructor for de/marshalling*/
    public AM540Cache() {
    }

    public AM540Cache(boolean connectionToBeaconMirror) {
        this.connectionToBeaconMirror = connectionToBeaconMirror;
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setFrameCounter(long frameCounter) {
        this.frameCounter = frameCounter;
    }

    @Override
    public void saveObjectList(UniversalObject[] objectList) {
        if (isConnectionToBeaconMirror()) {
            this.mirrorObjectList = objectList;
        } else {
            this.gatewayObjectList = objectList;
        }
        markDirty();
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
    public int getConfProgChange() {
        return super.getConfProgChange();
    }

    @Override
    @Deprecated // The AM540 meter doesn't have this counter - so method should not be used
    public void setConfProgChange(int confProgChange) {
        super.setConfProgChange(confProgChange);
    }
}
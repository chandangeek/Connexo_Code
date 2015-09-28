package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.DLMSCache;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Extension of the normal DLMSCache, it also remembers our last frame counter used in the previous session
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/06/2014 - 15:42
 */
@XmlRootElement
public class AM540Cache extends DLMSCache {

    UniversalObject[] mirrorObjectList;
    UniversalObject[] gatewayObjectList;
    private long frameCounter = 1;
    private boolean connectionToBeaconMirror;

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
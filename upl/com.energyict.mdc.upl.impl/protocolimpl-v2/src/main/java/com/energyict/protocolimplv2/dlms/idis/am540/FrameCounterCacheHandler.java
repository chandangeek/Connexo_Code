package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.mdc.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.protocol.support.FrameCounterCache;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by iulian on 9/2/2016.
 */
public class FrameCounterCacheHandler implements FrameCounterCache{

    private final boolean isBeaconMirrorConnection;
    private final DeviceIdentifier deviceIdentifier;
    protected Map<Integer, Long> frameCountersGateway = new HashMap<>();
    protected Map<Integer, Long> frameCountersMirror = new HashMap<>();
    protected AM540Properties properties;

    public FrameCounterCacheHandler(AM540Properties properties) {
        this.isBeaconMirrorConnection = properties.useBeaconMirrorDeviceDialect();
        parseCachedProperty(properties.getInitialFrameCounter());
        this.properties = properties;
        this.deviceIdentifier = new DeviceIdentifierBySerialNumber(properties.getSerialNumber());

    }

    private void parseCachedProperty(String initialFrameCounter) {
        if (initialFrameCounter == null){
            return;
        }

        String mirrorOrGateway[] = initialFrameCounter.split("|");
        boolean mirror = false;
        for (String conn : mirrorOrGateway) {
            String allPairs[] = conn.split(",");

            for (String pair : allPairs) {
                String subSet[] = pair.split(":");
                if (subSet.length == 2) {
                    int clientId = Integer.parseInt(subSet[0]);
                    int frameCounter = Integer.parseInt(subSet[2]);
                    if (clientId > 0 && clientId < 256) {
                        if (mirror){
                            frameCountersMirror.put(clientId, Long.valueOf(frameCounter));
                        } else {
                            frameCountersGateway.put(clientId, Long.valueOf(frameCounter));
                        }
                    }
                }
            }

            mirror = true; //second part is the mirror
        }

    }

    @Override
    public void setTXFrameCounter(final int clientId, int frameCounter) {
        if (isConnectionToBeaconMirror()) {
            frameCountersMirror.put(clientId, Long.valueOf(frameCounter));
        } else {
            frameCountersGateway.put(clientId, Long.valueOf(frameCounter));
        }

        save();
    }

    private CollectedDeviceInfo save() {
        String prop = toString();

        CollectedDeviceInfo collectedDeviceInfo =  MdcManager.getCollectedDataFactory().createCollectedDeviceProtocolProperty(deviceIdentifier, AM540ConfigurationSupport.INITIAL_FRAME_COUNTER, prop);
        return collectedDeviceInfo;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(generatePairs(frameCountersGateway)).append("|").append(generatePairs(frameCountersMirror)).append("-");

        return sb.toString().replace(";|","|").replace(";-","").replace(":|","");
    }


    private String generatePairs(Map<Integer, Long> theMap) {
        StringBuilder sb = new StringBuilder();

        for (Integer clientId : theMap.keySet()){
            Long frameCounter = theMap.get(clientId);
            sb.append(clientId).append("=").append(frameCounter).append(";");
        }

        return sb.toString();
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


    public boolean isConnectionToBeaconMirror() {
        return isBeaconMirrorConnection;
    }
}

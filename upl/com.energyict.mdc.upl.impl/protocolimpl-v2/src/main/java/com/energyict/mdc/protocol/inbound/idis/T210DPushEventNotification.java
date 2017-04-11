package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 7/13/2016.
 */
public class T210DPushEventNotification extends DataPushNotification {

    protected T210DEventPushNotificationParser parser;

    @Override
    public DiscoverResultType doDiscovery() {
        getEventPushNotificationParser().parseInboundFrame();
        return DiscoverResultType.DATA;
    }

    protected T210DEventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new T210DEventPushNotificationParser(comChannel, getContext());
        }
        return parser;
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDatas = new ArrayList<>();
        if (getEventPushNotificationParser().getCollectedDeviceIpAddres() != null) {
            collectedDatas.add(getEventPushNotificationParser().getCollectedDeviceIpAddres());
        }

        if (getEventPushNotificationParser().getCollectedLoadProfile() != null && getEventPushNotificationParser().getCollectedLoadProfile().size() > 0) {
            for (CollectedLoadProfile collectedLoadProfile : getEventPushNotificationParser().getCollectedLoadProfile()) {
                collectedDatas.add(collectedLoadProfile);
            }
        }

        if (getEventPushNotificationParser().getCollectedLogBooks() != null) {
            collectedDatas.addAll(getEventPushNotificationParser().getCollectedLogBooks());
        }

        if (getEventPushNotificationParser().getCollectedRegisters() != null) {
            collectedDatas.add(getEventPushNotificationParser().getCollectedRegisters());
        }

        return collectedDatas;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return getEventPushNotificationParser() != null ? getEventPushNotificationParser().getDeviceIdentifier() : null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-03-20 16:05:37 +0200 (Mon, 20 Mar 2017)$";
    }
}
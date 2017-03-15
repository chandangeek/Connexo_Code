package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 7/13/2016.
 */
public class T210DPushEventNotification extends DataPushNotification {

    protected T210DEventPushNotificationParser parser;

    @Override
    public DiscoverResultType doDiscovery() {
        //If no type is provided, use 'SocketComChannel', resulting in the TCP/IP connection layer.
        if (!comChannel.getProperties().hasValueFor(ComChannelType.TYPE)) {
            TypedProperties comChannelProperties = ConnectionTypeImpl.createTypeProperty(ComChannelType.SocketComChannel);
            comChannel.addProperties(comChannelProperties);
        }
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
        if(getEventPushNotificationParser().getCollectedDeviceIpAddres() != null){
            collectedDatas.add(getEventPushNotificationParser().getCollectedDeviceIpAddres());
        }

        if(getEventPushNotificationParser().getCollectedLoadProfile() != null && getEventPushNotificationParser().getCollectedLoadProfile().size() > 0){
            for(CollectedLoadProfile collectedLoadProfile: getEventPushNotificationParser().getCollectedLoadProfile()){
                collectedDatas.add(collectedLoadProfile);
            }
        }

        if(getEventPushNotificationParser().getCollectedLogBooks() != null){
            collectedDatas.addAll(getEventPushNotificationParser().getCollectedLogBooks());
        }

        if(getEventPushNotificationParser().getCollectedRegisters() != null){
            collectedDatas.add(getEventPushNotificationParser().getCollectedRegisters());
        }

        return collectedDatas;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return getEventPushNotificationParser() != null ? getEventPushNotificationParser().getDeviceIdentifier() : null;
    }
}

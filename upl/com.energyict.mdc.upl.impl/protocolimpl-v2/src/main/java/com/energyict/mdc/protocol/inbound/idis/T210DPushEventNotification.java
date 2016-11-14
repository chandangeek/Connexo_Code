package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.cpo.TypedProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 7/13/2016.
 */
public class T210DPushEventNotification extends DataPushNotification {

    private T210DEventPushNotificationParser parser;

    @Override
    public DiscoverResultType doDiscovery() {
        //If no type is provided, use 'SocketComChannel', resulting in the TCP/IP connection layer.
        if (!comChannel.getProperties().hasValueFor(ComChannelType.TYPE)) {
            TypedProperties comChannelProperties = ConnectionTypeImpl.createTypeProperty(ComChannelType.SocketComChannel);
            comChannel.addProperties(comChannelProperties);
        }
        parser = new T210DEventPushNotificationParser(comChannel, getContext());
        parser.parseInboundFrame();
        return DiscoverResultType.DATA;
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return true;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDatas = new ArrayList<>();
        if(parser.getCollectedDeviceIpAddres() != null){
            collectedDatas.add(parser.getCollectedDeviceIpAddres());
        }

        if(parser.getCollectedLoadProfile() != null && parser.getCollectedLoadProfile().size() > 0){
            for(CollectedLoadProfile collectedLoadProfile: parser.getCollectedLoadProfile()){
                collectedDatas.add(collectedLoadProfile);
            }
        }

        if(parser.getCollectedLogBooks() != null){
            collectedDatas.addAll(parser.getCollectedLogBooks());
        }

        return collectedDatas;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return parser != null ? parser.getDeviceIdentifier() : null;
    }
}

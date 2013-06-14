package com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 6/06/13
 * Time: 13:28
 * Author: khe
 */
public class WaveFlowV2 extends WaveFlow {

    private ObisCodeMapper obisCodeMapper;
    private WaveFlowMessages messaging;
    private ProfileDataReader profileDataReader;
    private EventReader eventReader;

    public WaveFlowV2() {
        isV1 = false;
        isV210 = false;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    protected ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this);
        }
        return obisCodeMapper;
    }

    protected WaveFlowMessages getMessaging() {
        if (messaging == null) {
            messaging = new WaveFlowMessages(this);
        }
        return messaging;
    }

    @Override
    protected DeviceLoadProfileSupport getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new ProfileDataReader(this);
        }
        return profileDataReader;
    }

    @Override
    protected DeviceLogBookSupport getEventReader() {
        if (eventReader == null) {
            eventReader = new EventReader(this);
        }
        return eventReader;
    }
}
package test.com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

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
        super(collectedDataFactory, issueFactory);
        isV1 = false;
        isV210 = false;
    }

    @Override
    public String getProtocolDescription() {
        return "Coronis WaveFlow V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-26 15:25:58 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    protected ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this, collectedDataFactory, issueFactory);
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
            profileDataReader = new ProfileDataReader(this, collectedDataFactory);
        }
        return profileDataReader;
    }

    @Override
    protected DeviceLogBookSupport getEventReader() {
        if (eventReader == null) {
            eventReader = new EventReader(this, collectedDataFactory);
        }
        return eventReader;
    }
}
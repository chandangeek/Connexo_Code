package test.com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.mdc.identifiers.DeviceIdentifierById;
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

    public WaveFlowV2(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService) {
        super(collectedDataFactory, issueFactory, propertySpecService, nlsService);
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
            obisCodeMapper = new ObisCodeMapper(this, this.getCollectedDataFactory(), this.getIssueFactory());
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
            profileDataReader = new ProfileDataReader(this, this.getCollectedDataFactory());
        }
        return profileDataReader;
    }

    @Override
    protected DeviceLogBookSupport getEventReader() {
        if (eventReader == null) {
            eventReader = new EventReader(this, this.getCollectedDataFactory());
        }
        return eventReader;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return this.getCollectedDataFactory().createCalendarCollectedData(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return this.getCollectedDataFactory().createBreakerStatusCollectedData(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        return this.getCollectedDataFactory().createCreditAmountCollectedData(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(getOfflineDevice().getId()));
    }
}
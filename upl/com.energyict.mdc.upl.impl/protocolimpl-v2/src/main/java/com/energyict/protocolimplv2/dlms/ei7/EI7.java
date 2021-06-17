package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.dlms.ei7.profiles.EI7LoadProfileDataReader;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class EI7 extends A2 {

    public EI7(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
               NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor,
               KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor, keyAccessorTypeExtractor);
    }

    public EI7DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsSessionProperties) {
        return new EI7DlmsSession(comChannel, dlmsSessionProperties, getHhuSignOnV2(), offlineDevice.getSerialNumber());
    }

    protected EI7Messaging createMessaging() {
        return new EI7Messaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor(), getKeyAccessorTypeExtractor());
    }

    @Override
    public String getProtocolDescription() {
        return "EI7 2021 ThemisLog2 DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "2021-06-17";
    }

    @Override
    protected A2ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new EI7LoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(),
                    getOfflineDevice(), getDlmsSessionProperties().getLimitMaxNrOfDays(), EI7LoadProfileDataReader.getSupportedLoadProfiles());
        }
        return profileDataReader;
    }

}

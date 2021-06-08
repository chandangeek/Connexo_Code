package com.energyict.protocolimplv2.dlms.ei6newspec;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei6newspec.messages.EI6NewSpecMessaging;
import com.energyict.protocolimplv2.dlms.ei6newspec.profiles.EI6NewSpecLoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei7.EI7;

public class EI6NewSpec extends EI7 {

    public EI6NewSpec(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor);
    }

    @Override
    public String getProtocolDescription() {
        return "EI6 2021 ThemisUno DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "2021-06-07";
    }

    protected EI6NewSpecMessaging createMessaging() {
        return new EI6NewSpecMessaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor());
    }

    @Override
    protected A2ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new EI6NewSpecLoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(),
                    getOfflineDevice(), getDlmsSessionProperties().getLimitMaxNrOfDays(), EI6NewSpecLoadProfileDataReader.getSupportedLoadProfiles());
        }
        return profileDataReader;
    }

}

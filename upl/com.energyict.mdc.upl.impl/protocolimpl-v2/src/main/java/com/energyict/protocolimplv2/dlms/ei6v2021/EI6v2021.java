package com.energyict.protocolimplv2.dlms.ei6v2021;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei6v2021.messages.EI6v2021Messaging;
import com.energyict.protocolimplv2.dlms.ei6v2021.profiles.EI6v2021LoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei6v2021.properties.EI6ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.ei6v2021.registers.EI6v2021RegisterFactory;
import com.energyict.protocolimplv2.dlms.ei7.EI7;

public class EI6v2021 extends EI7 {

    protected EI6v2021RegisterFactory registerFactory = null;

    public EI6v2021(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                    NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor,
                    KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getProtocolDescription() {
        return "EI6 2021 ThemisUno DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "2022-12-29";
    }

    protected EI6v2021Messaging createMessaging() {
        return new EI6v2021Messaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor(), getKeyAccessorTypeExtractor());
    }

    @Override
    public A2ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new EI6v2021LoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(),
                    getOfflineDevice(), getDlmsSessionProperties().getLimitMaxNrOfDays(), EI6v2021LoadProfileDataReader.getSupportedLoadProfiles());
        }
        return profileDataReader;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new EI6ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public EI6v2021RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new EI6v2021RegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return registerFactory;
    }
}

package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50ConfigurationSupport;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.protocolimplv2.nta.esmr50.messages.CryptoESMR50MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.messages.CryptoESMR50Messaging;

public class CryptoT210 extends T210 {

    private CryptoESMR50Messaging cryptoMessaging;
    private CryptoESMR50MessageExecutor cryptoMessageExecutor;
    private DeviceMasterDataExtractor deviceMasterDataExtractor;

    public CryptoT210(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                      PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                      DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor,
                      NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor,
                      KeyAccessorTypeExtractor keyAccessorTypeExtractor, DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, messageFileExtractor,
                calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor,
                deviceMasterDataExtractor);
        this.deviceMasterDataExtractor = deviceMasterDataExtractor;
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T210 Crypto Protocol DLMS (NTA ESMR5.0) V2";
    }

    @Override
    public String getVersion() {
        return "Crypto version: 2020-05-18";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        journal("Sagemcom T210 crypto protocol");
        super.init(offlineDevice,comChannel);
    }

    @Override
    protected DlmsSession newDlmsSession(ComChannel comChannel) {
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        if (getDlmsSessionProperties().useCryptoServer()) {
            //Uses the cryptoserver to encrypt requests and decrypt responses
            return new CryptoDlmsSession(comChannel, getDlmsSessionProperties());
        } else {
            //Normal session
            return super.newDlmsSession(comChannel);
        }
    }

    @Override
    public CryptoESMR50Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new CryptoESMR50Properties(getPropertySpecService());
        }
        return (CryptoESMR50Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if(dlmsConfigurationSupport == null){
            dlmsConfigurationSupport = new CryptoESMR50ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    protected ESMR50Messaging getMessaging() {
        if (this.cryptoMessaging == null) {
            this.cryptoMessaging = new CryptoESMR50Messaging(getMessageExecutor(), this.getPropertySpecService(), this.nlsService, this.converter, this.messageFileExtractor, this.calendarExtractor, this.numberLookupExtractor, this.loadProfileExtractor, this.keyAccessorTypeExtractor);
        }
        return this.cryptoMessaging;
    }

    @Override
    protected ESMR50MessageExecutor getMessageExecutor() {
        if (this.cryptoMessageExecutor == null) {
            this.cryptoMessageExecutor = new CryptoESMR50MessageExecutor(this, this.getCollectedDataFactory(),
                    this.getIssueFactory(), this.keyAccessorTypeExtractor, this.deviceMasterDataExtractor);
        }
        return this.cryptoMessageExecutor;
    }
}

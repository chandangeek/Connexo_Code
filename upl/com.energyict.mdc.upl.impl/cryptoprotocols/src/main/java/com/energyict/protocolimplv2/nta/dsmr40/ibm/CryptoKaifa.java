package com.energyict.protocolimplv2.nta.dsmr40.ibm;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
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

import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr40.messages.CryptoKaifaMessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.CryptoKaifaMessaging;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;
import com.energyict.protocolimplv2.nta.dsmr40.messages.KaifaDsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.KaifaDsmr40Messaging;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50ConfigurationSupport;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50Properties;

public class CryptoKaifa extends Kaifa {
    private CryptoKaifaMessaging cryptoMessaging;
    private CryptoKaifaMessageExecutor cryptoMessageExecutor;

    public CryptoKaifa(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
        setHasBreaker(false);
    }

    @Override
    public String getVersion() {
        return "Crypto version: 2019-02-27";
    }

    @Override
    public String getProtocolDescription() {
        return "Kaifa Crypto Protocol DLMS (NTA DSMR4.0) V2";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        setDlmsSession(newDlmsSession(comChannel));
    }

    protected HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }


    private DlmsSession newDlmsSession(ComChannel comChannel) {
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        if (getDlmsSessionProperties().useCryptoServer()) {
            //Uses the cryptoserver to encrypt requests and decrypt responses
            return new CryptoDlmsSession(comChannel, getDlmsSessionProperties());
        } else {
            //Normal session
            return super.getDlmsSession();
        }
    }

    @Override
    public CryptoKaifaProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new CryptoKaifaProperties();
        }
        return (CryptoKaifaProperties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new CryptoKaifaConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    protected Dsmr40Messaging getMessaging() {
        if (this.cryptoMessaging == null) {
            this.cryptoMessaging = new CryptoKaifaMessaging(getMessageExecutor(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(),
                    this.getDeviceMessageFileExtractor(), this.getTariffCalendarExtractor(), this.getNumberLookupExtractor(),
                    this.getLoadProfileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return this.cryptoMessaging;
    }

    @Override
    protected Dsmr40MessageExecutor getMessageExecutor() {
        if (this.cryptoMessageExecutor == null) {
            this.cryptoMessageExecutor = new CryptoKaifaMessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(),
                    this.getKeyAccessorTypeExtractor());
        }
        return this.cryptoMessageExecutor;
    }
}


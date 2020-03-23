package com.energyict.protocolimplv2.nta.dsmr23.iskra;

import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.Iskra.Mx382;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23Properties;
import com.energyict.protocolimplv2.nta.dsmr23.messages.CryptoDSMR23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.CryptoDSMR23Messaging;

public class CryptoMx382 extends Mx382 {
    private CryptoDSMR23Messaging cryptoMessaging;
    private CryptoDSMR23MessageExecutor cryptoMessageExecutor;
    private static long INITIAL_FRAME_COUNTER = 1L;

    public CryptoMx382(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, KeyAccessorTypeExtractor keyAccessorTypeExtractor, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, keyAccessorTypeExtractor, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor);
        setHasBreaker(false);
    }

    @Override
    public String getVersion() {
        return "Crypto version: 2020-03-09";
    }

    @Override
    public String getProtocolDescription() {
        return "Iskra Mx382 Crypto Protocol DLMS (NTA DSMR2.3) V2";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        journal("Iskra Mx382 protocol init V2");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        setDlmsSession(newDlmsSession(comChannel));
        getDlmsSession().getDLMSConnection().setSNRMType(1);//Uses a specific parameter length for the HDLC signon (SNRM request)
    }

    protected HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    @Override
    protected DlmsSession newDlmsSession(ComChannel comChannel) {
        if (!getDlmsSessionProperties().replayAttackPreventionEnabled()) {
            getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(INITIAL_FRAME_COUNTER);
        }
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        if (getDlmsSessionProperties().useCryptoServer()) {
            //Uses the cryptoserver to encrypt requests and decrypt responses
            return  new CryptoDlmsSession(comChannel, getDlmsSessionProperties());
        } else {
            //Normal session
            return super.newDlmsSession(comChannel);
        }
    }

    @Override
    public CryptoDSMR23Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new CryptoDSMR23Properties();
        }
        return (CryptoDSMR23Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if(dlmsConfigurationSupport == null){
            dlmsConfigurationSupport = new CryptoMx382ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    protected CryptoDSMR23MessageExecutor getMessageExecutor() {
        if (this.cryptoMessageExecutor == null) {
            this.cryptoMessageExecutor = new CryptoDSMR23MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getKeyAccessorTypeExtractor());
        }
        return this.cryptoMessageExecutor;
    }

    @Override
    protected CryptoDSMR23Messaging getDsmr23Messaging() {
        if (this.cryptoMessaging == null) {
            this.cryptoMessaging = new CryptoDSMR23Messaging(getMessageExecutor(), this.getPropertySpecService(), this.getNlsService(),
                    this.getConverter(), this.getDeviceMessageFileExtractor(), this.getTariffCalendarExtractor(),
                    this.getNumberLookupExtractor(), this.getLoadProfileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return this.cryptoMessaging;
    }
}
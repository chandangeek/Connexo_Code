package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

import com.energyict.mdc.protocol.ComChannel;
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
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50ConfigurationSupport;
import com.energyict.protocolimplv2.nta.esmr50.common.CryptoESMR50Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.protocolimplv2.nta.esmr50.messages.CryptoESMR50MessageExecutor;
import com.energyict.protocolimplv2.nta.esmr50.messages.CryptoESMR50Messaging;

public class CryptoT210 extends T210 {
    private CryptoESMR50Messaging cryptoMessaging;
    private CryptoESMR50MessageExecutor cryptoMessageExecutor;

    public CryptoT210(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T210 Crypto Protocol DLMS (NTA ESMR5.0) V2";
    }

    @Override
    public String getVersion() {
        return "Crypto version: 2019-02-13";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getLogger().info("Sagemcom T210 protocol init V2");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(getDlmsSessionProperties().getDeviceId());
        getLogger().info("Initialize communication with device identified by device ID: " + getDlmsSessionProperties().getDeviceId());
        if(!testCachedFrameCounter(comChannel)){
            readFrameCounter(comChannel);
            DlmsSession dlmsSession = newDlmsSession(comChannel);
            dlmsSession.getAso().getSecurityContext().setFrameCounter(getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter());
            setDlmsSession(dlmsSession);
        } else {
            //Framecounter was validated and DLMSSession set so go on
        }
        getLogger().info("Initialization phase has ended.");

    }

    private boolean testCachedFrameCounter(ComChannel comChannel){
        boolean validCachedFrameCounter = false;
        DlmsSession dlmsSession = newDlmsSession(comChannel);
        long cachedFramecounter = getDeviceCache().getFrameCounter();
        getLogger().info("Testing cached frame counter: " + cachedFramecounter );
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(cachedFramecounter);
        dlmsSession.getAso().getSecurityContext().setFrameCounter(cachedFramecounter);
        try {
            dlmsSession.getDlmsV2Connection().connectMAC();
            dlmsSession.createAssociation();
            if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
//                dlmsSession.disconnect();
                long frameCounter = dlmsSession.getAso().getSecurityContext().getFrameCounter();
                getLogger().info("This FrameCounter was validated: " + frameCounter);
                getDeviceCache().setFrameCounter(frameCounter);
                validCachedFrameCounter = true;
                setDlmsSession(dlmsSession);
            }
        } catch (CommunicationException ex) {
            getLogger().info("Association using cached frame counter has failed.");
        }
        return validCachedFrameCounter;
    }

    private DlmsSession newDlmsSession(ComChannel comChannel) {
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        if (getDlmsSessionProperties().useCryptoServer()) {
            //Uses the cryptoserver to encrypt requests and decrypt responses
            return  new CryptoDlmsSession(comChannel, getDlmsSessionProperties());
        } else {
            //Normal session
            return super.getDlmsSession();
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
            this.cryptoMessageExecutor = new CryptoESMR50MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.keyAccessorTypeExtractor);
        }
        return this.cryptoMessageExecutor;
    }
}

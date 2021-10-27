package com.energyict.protocolimplv2.dlms.idis.aec;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.idis.aec.events.AECLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.aec.messages.AECMessaging;
import com.energyict.protocolimplv2.dlms.idis.aec.profiledata.AECProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.aec.properties.AECConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.aec.properties.AECDlmsProperties;
import com.energyict.protocolimplv2.dlms.idis.aec.registers.AECRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.FIFOStoredValues;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class AEC extends AM540 {

    /**
     * OBIS code for the billing profile.
     */
    private static final ObisCode OBIS_BILLING_PROFILE = ObisCode.fromString("0.0.98.2.0.255");

    protected AECRegisterFactory registerFactory;
    private HHUSignOnV2 hhuSignOnV2;

    /**
     * For billing registers.
     */
    private StoredValues storedValues;

    public AEC(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AECMessaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new AECLogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return idisLogBookFactory;
    }

    @Override
    protected AECRegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AECRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public IDISProfileDataReader getIDISProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new AECProfileDataReader(this, this.getCollectedDataFactory(), this.getIssueFactory(), getDlmsSessionProperties().getLimitMaxNrOfDays());
        }
        return idisProfileDataReader;
    }

    @Override
    public StoredValues getStoredValues() {
        if (this.storedValues == null) {
            try {
                this.storedValues = new FIFOStoredValues(OBIS_BILLING_PROFILE,
                        this.getDlmsSession().getCosemObjectFactory(),
                        this.getDlmsSessionProperties(),
                        this.getIDISProfileDataReader(),
                        this.getTimeZone(),
                        this.getLogger());
            } catch (IOException e) {
                if (this.getLogger().isLoggable(Level.WARNING)) {
                    this.getLogger().log(Level.WARNING, "IO error when creating StoredValues : [" + e.getMessage() + "]", e);
                }

                throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
            }
        }

        return this.storedValues;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.singletonList(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        if (comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOnV2 = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: " + getVersion());
        setDlmsSession(new AECDlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOnV2, getDlmsSessionProperties().getDeviceId()));
    }

    @Override
    public void logOff() {
        getDlmsSession().getDlmsV2Connection().disconnectMAC();
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_MANUFACTURER_SPECIFIC_SEVCD);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    @Override
    public AECDlmsProperties getDlmsSessionProperties() {
        if(dlmsProperties == null) {
            dlmsProperties = new AECDlmsProperties(getPropertySpecService(), getNlsService());
        }
        return (AECDlmsProperties)dlmsProperties;
    }

    /**
     * A collection of general AEC properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = getNewInstanceOfConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    @Override
    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new AECSecuritySupport(this.getPropertySpecService());
        }
        return dlmsSecuritySupport;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(
                new TcpDeviceProtocolDialect(this.getPropertySpecService(), getNlsService()));
    }

    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new AECConfigurationSupport(this.getPropertySpecService());
    }

    @Override
    public String getProtocolDescription() {
        return "AEC DLMS Single phase";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-10-27$";
    }
}

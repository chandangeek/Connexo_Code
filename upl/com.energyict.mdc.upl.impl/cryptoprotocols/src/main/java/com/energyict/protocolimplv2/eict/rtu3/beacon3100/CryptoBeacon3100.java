package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.common.tls.TLSHSMConnectionType;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceGroupExtractor;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.crypto.HsmProtocolService;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.CryptoBeaconMessaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/10/2016 - 17:01
 */
public class CryptoBeacon3100 extends Beacon3100 {

    private final HsmProtocolService hsmProtocolService;

    public CryptoBeacon3100(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, ObjectMapperService objectMapperService, DeviceMasterDataExtractor extractor, DeviceGroupExtractor deviceGroupExtractor, CertificateWrapperExtractor certificateWrapperExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor, DeviceExtractor deviceExtractor, DeviceMessageFileExtractor deviceMessageFileExtractor, HsmProtocolService hsmProtocolService) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, objectMapperService, extractor, deviceGroupExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor, deviceExtractor, deviceMessageFileExtractor);
        this.hsmProtocolService = hsmProtocolService;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster EnergyICT Beacon3100 G3 DLMS crypto-protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-01-11 $" + "/" + super.getVersion();
    }

    @Override
    public Beacon3100Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new CryptoBeacon3100Properties(getCertificateWrapperExtractor());
        }
        return (CryptoBeacon3100Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new CryptoBeacon3100ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    protected Beacon3100Messaging getBeacon3100Messaging() {
        if (beacon3100Messaging == null) {
            beacon3100Messaging = new CryptoBeaconMessaging(this, getCollectedDataFactory(), getIssueFactory(), getObjectMapperService(), getPropertySpecService(), getNlsService(), getConverter(), getExtractor(), getDeviceGroupExtractor(), getDeviceExtractor(), getCertificateWrapperExtractor(), getKeyAccessorTypeExtractor(), getDeviceMessageFileExtractor(), getHsmProtocolService());
        }
        return beacon3100Messaging;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> supportedConnectionTypes = new ArrayList<>(super.getSupportedConnectionTypes());
        supportedConnectionTypes.add(new TLSHSMConnectionType(getPropertySpecService(), getCertificateWrapperExtractor()));
        return supportedConnectionTypes;
    }

    @Override
    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(getCryptoDlmsSession(comChannel));
    }

    @Override
    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return getCryptoDlmsSession(comChannel);
    }

    private DlmsSession getCryptoDlmsSession(ComChannel comChannel) {
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        return new CryptoDlmsSession(comChannel, getDlmsSessionProperties());

    }

    protected HsmProtocolService getHsmProtocolService() {
        return hsmProtocolService;
    }
}
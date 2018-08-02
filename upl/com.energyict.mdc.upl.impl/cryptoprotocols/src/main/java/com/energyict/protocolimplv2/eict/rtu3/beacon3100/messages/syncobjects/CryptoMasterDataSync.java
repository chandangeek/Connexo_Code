package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.ObjectMapperService;
import com.energyict.mdc.upl.crypto.HsmProtocolService;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100Properties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.CryptoBeaconMessaging;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class CryptoMasterDataSync extends MasterDataSync {

    private final HsmProtocolService hsmProtocolService;

    public CryptoMasterDataSync(CryptoBeaconMessaging beacon3100Messaging, ObjectMapperService objectMapperService, IssueFactory issueFactory, PropertySpecService propertySpecService, DeviceMasterDataExtractor deviceMasterDataExtractor, NlsService nlsService, HsmProtocolService hsmProtocolService) {
        super(beacon3100Messaging, objectMapperService, issueFactory, propertySpecService, deviceMasterDataExtractor, nlsService);
        this.hsmProtocolService = hsmProtocolService;
    }

    protected CryptoMasterDataSerializer getMasterDataSerializer() {
        return new CryptoMasterDataSerializer(getObjectMapperService(), getPropertySpecService(), getDeviceMasterDataExtractor(), getBeacon3100Properties(), getNlsService(), hsmProtocolService);
    }

    private CryptoBeacon3100Properties getBeacon3100Properties() {
        return (CryptoBeacon3100Properties) getProtocol().getDlmsSessionProperties();
    }

}